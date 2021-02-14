package com.fengpb.conductor.core.execution;

import com.alibaba.fastjson.JSON;
import com.fengpb.conductor.common.metadata.tasks.Task;
import com.fengpb.conductor.common.metadata.tasks.Task.Status;
import static com.fengpb.conductor.common.metadata.tasks.Task.Status.*;

import com.fengpb.conductor.common.metadata.workflow.TaskType;
import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;
import com.fengpb.conductor.common.metadata.workflow.WorkflowTask;
import com.fengpb.conductor.common.run.Workflow;
import static com.fengpb.conductor.common.run.Workflow.WorkflowStatus.*;

import com.fengpb.conductor.core.execution.mapper.TaskMapper;
import com.fengpb.conductor.core.execution.mapper.TaskMapperContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DeciderService {

    @Autowired
    private ParametersUtils parametersUtils;

    @Autowired
    private Map<String, TaskMapper> taskMappers;

    private final Predicate<Task> isNonPendingTask = task -> !task.getStatus().equals(SKIPPED) && !task.isExecuted();

    public DeciderOutcome decide(Workflow workflow) {
        final List<Task> tasks = workflow.getTasks();
        // 获取工作流中未被处理的任务
        List<Task> unprocessedTasks = tasks.stream()
                .filter(isNonPendingTask)
                .collect(Collectors.toList());

        List<Task> tasksToBeScheduled = new LinkedList<>();
        if (unprocessedTasks.isEmpty()) { // 为空则代表是新工作流，调用startWorkflow方法启动工作流
            tasksToBeScheduled = startWorkflow(workflow);
        }
        // 决策并填充待安排任务
        return decide(workflow, tasksToBeScheduled);
    }

    public List<Task> startWorkflow(Workflow workflow) {
        final WorkflowDef workflowDef = workflow.getWorkflowDefinition();
        log.debug("开始工作流：{}", workflow);
        List<Task> tasks = workflow.getTasks();
        if (tasks.isEmpty()) {
            if (workflowDef.getTasks().isEmpty()) {
                throw new RuntimeException("没有找到可执行任务");
            }
            WorkflowTask taskToBeScheduled = workflowDef.getTasks().get(0);
            return getTasksToBeScheduled(workflow, taskToBeScheduled, 0);
        }
        Task returnFromTask = tasks.stream()
                .findFirst()
                .map(task -> {
                    task.setStatus(SCHEDULED);
                    return task;
                })
                .orElseThrow(() ->{
                    String reason = String.format("工作流%s未能找到开始任务", workflow.getWorkflowId());
                    return new RuntimeException(reason);
                });

        return Collections.singletonList(returnFromTask);
    }

    private DeciderOutcome decide(final Workflow workflow, List<Task> preScheduledTasks) {
        DeciderOutcome outcome = new DeciderOutcome();

        if (workflow.getStatus().equals(PAUSED)) {
            log.debug("工作流:{}已暂停",workflow.getWorkflowId());
            return outcome;
        }

        List<Task> pendingTasks = workflow.getTasks().stream()
                .filter(isNonPendingTask)
                .collect(Collectors.toList());

        // 已执行的任务名集合
        Set<String> executedTasksRefNames = workflow.getTasks().stream()
                .filter(Task::isExecuted)
                .map(Task::getReferenceTaskName)
                .collect(Collectors.toSet());

        // 待安排的任务
        Map<String, Task> taskToBeScheduled = new LinkedHashMap<>();
        for (Task pendingTask : pendingTasks) {
            if (!pendingTask.isExecuted() && pendingTask.getStatus().isTerminal()) {
                pendingTask.setExecuted(true);
                List<Task> nextTasks = getNextTask(workflow, pendingTask);
                nextTasks.forEach(nextTask -> taskToBeScheduled.putIfAbsent(nextTask.getReferenceTaskName(), nextTask));
                log.debug("为工作流{}安排任务，工作流{}的下一个任务{}",pendingTask.getTaskDefName(),
                        nextTasks.stream()
                        .map(Task::getTaskDefName)
                        .collect(Collectors.toList())
                        , workflow.getWorkflowId());
            }
        }
        // 之前安排的任务（主要是start工作流时起效）
        preScheduledTasks.forEach(preScheduledTask -> {
            taskToBeScheduled.put(preScheduledTask.getReferenceTaskName(), preScheduledTask);
        });
        List<Task> unScheduledTasks = taskToBeScheduled.values().stream()
                .filter(task -> !executedTasksRefNames.contains(task.getReferenceTaskName()))
                .collect(Collectors.toList());
        if (!unScheduledTasks.isEmpty()) {
            log.debug("为工作流：{} 安排任务：{}", workflow.getWorkflowId(),
                    unScheduledTasks.stream().map(Task::getTaskDefName).collect(Collectors.toList()));
            outcome.tasksToBeScheduled.addAll(unScheduledTasks);
        }
        if (outcome.tasksToBeScheduled.isEmpty() && checkForWorkflowCompletion(workflow)) {
            outcome.isComplete = true;
        }
        return outcome;
    }


    List<Task> getNextTask(Workflow workflow, Task task) {
        final WorkflowDef workflowDef = workflow.getWorkflowDefinition();

        // Get the following task after the last completed task
        if (SystemTaskType.is(task.getTaskType()) && SystemTaskType.DECISION.name().equals(task.getTaskType())) {
            if (task.getInputData().get("hasChildren") != null) {
                return Collections.emptyList();
            }
        }

//        String taskReferenceName = task.isLoopOverTask() ? TaskUtils.removeIterationFromTaskRefName(task.getReferenceTaskName()) : task.getReferenceTaskName();
        String taskReferenceName = task.getReferenceTaskName();
        WorkflowTask taskToSchedule = workflowDef.getNextTask(taskReferenceName);
        if (taskToSchedule != null) {
            return getTasksToBeScheduled(workflow, taskToSchedule, 0);
        }

        return Collections.emptyList();
    }

    private String getNextTasksToBeScheduled(Workflow workflow, Task task) {
        final WorkflowDef def = workflow.getWorkflowDefinition();
        String taskReferenceName = task.getReferenceTaskName();
        WorkflowTask taskToBeSchedule = def.getNextTask(taskReferenceName);
        return taskToBeSchedule == null ? null : taskToBeSchedule.getTaskReferenceName();
    }

    void updateWorkflowOutput(final Workflow workflow, @Nullable Task task) {
        List<Task> allTasks = workflow.getTasks();
        if (allTasks.isEmpty()) {
            return;
        }

        Task last = Optional.ofNullable(task).orElse(allTasks.get(allTasks.size() - 1));

        WorkflowDef workflowDef = workflow.getWorkflowDefinition();
        Map<String, Object> output;
        if (workflowDef.getOutputParameters() != null && !workflowDef.getOutputParameters().isEmpty()) {
            Workflow workflowInstance = JSON.parseObject(JSON.toJSONString(workflow), Workflow.class);
            output = parametersUtils.getTaskInput(workflowDef.getOutputParameters(), workflowInstance, null);
        } else {
            output = last.getOutputData();
        }

        workflow.setOutput(output);
    }

    public List<Task> getTasksToBeScheduled(Workflow workflow, WorkflowTask taskToSchedule, int retryCount) {
        return getTasksToBeScheduled(workflow, taskToSchedule, retryCount, null);
    }

    /**
     * 获取待安排任务并填充参数
     * @param workflow
     * @param taskToSchedule
     * @return
     */
    public List<Task> getTasksToBeScheduled(Workflow workflow, WorkflowTask taskToSchedule,
                                            int retryCount, String retriedTaskId) {
        Map<String, Object> input = parametersUtils.getTaskInput(taskToSchedule.getInputParameters(),workflow, null);

        List<String> tasksInWorkflow = workflow.getTasks().stream()
                .filter(runningTask -> runningTask.getStatus().equals(Status.IN_PROGRESS) || runningTask.getStatus().isTerminal())
                .map(Task::getReferenceTaskName)
                .collect(Collectors.toList());

        TaskType taskType = taskToSchedule.getType();

        String taskId = UUID.randomUUID().toString();
        TaskMapperContext taskMapperContext = TaskMapperContext.newBuilder()
                .withWorkflowDefinition(workflow.getWorkflowDefinition())
                .withWorkflowInstance(workflow)
                .withTaskDefinition(taskToSchedule.getTaskDefinition())
                .withTaskToSchedule(taskToSchedule)
                .withTaskInput(input)
                .withRetryCount(retryCount)
                .withRetryTaskId(retriedTaskId)
                .withTaskId(taskId)
                .withDeciderService(this)
                .build();

        // for static forks, each branch of the fork creates a join task upon completion
        // for dynamic forks, a join task is created with the fork and also with each branch of the fork
        // a new task must only be scheduled if a task with the same reference name is not already in this workflow instance
        List<Task> tasks = taskMappers.get(taskType.name()).getMappedTasks(taskMapperContext).stream()
                .filter(task -> !tasksInWorkflow.contains(task.getReferenceTaskName()))
                .collect(Collectors.toList());
//        tasks.forEach(this::externalizeTaskData);
        return tasks;
    }


    private boolean checkForWorkflowCompletion(final Workflow workflow) throws RuntimeException {
        List<Task> allTasks = workflow.getTasks();
        if (allTasks.isEmpty()) {
            return false;
        }

        Map<String, Status> taskStatusMap = new HashMap<>();
        workflow.getTasks().forEach(task -> taskStatusMap.put(task.getReferenceTaskName(), task.getStatus()));

        List<WorkflowTask> workflowTasks = workflow.getWorkflowDefinition().getTasks();
        boolean allCompletedSuccessfully = workflowTasks.stream()
                .parallel()
                .allMatch(wftask -> {
                    Status status = taskStatusMap.get(wftask.getTaskReferenceName());
                    return status != null && status.isSuccessful() && status.isTerminal();
                });

        boolean noPendingTasks = taskStatusMap.values()
                .stream()
                .allMatch(Status::isTerminal);

        boolean noPendingSchedule = workflow.getTasks().stream()
                .parallel()
                .noneMatch(wftask -> {
                    String next = getNextTasksToBeScheduled(workflow, wftask);
                    return next != null && !taskStatusMap.containsKey(next);
                });

        return allCompletedSuccessfully && noPendingTasks && noPendingSchedule;
    }

    public static class DeciderOutcome {

        List<Task> tasksToBeScheduled = new LinkedList<>();

        List<Task> tasksToBeUpdated = new LinkedList<>();

        boolean isComplete;

        private DeciderOutcome() {
        }

    }
}
