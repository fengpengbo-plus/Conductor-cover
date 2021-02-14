package com.fengpb.conductor.core.execution;

import com.alibaba.fastjson.JSON;
import com.fengpb.conductor.common.metadata.tasks.Task;
import static com.fengpb.conductor.common.metadata.tasks.Task.Status.*;
import com.fengpb.conductor.common.metadata.tasks.TaskResult;
import static com.fengpb.conductor.common.metadata.tasks.TaskResult.Status;
import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;
import com.fengpb.conductor.common.run.Workflow;
import com.fengpb.conductor.common.run.Workflow.WorkflowStatus;
import com.fengpb.conductor.core.dao.QueueDAO;
import com.fengpb.conductor.core.execution.tasks.WorkflowSystemTask;
import com.fengpb.conductor.core.execution.utils.QueueUtils;
import com.fengpb.conductor.core.orchestration.ExecutionDAOFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fengpb.conductor.core.execution.DeciderService.DeciderOutcome;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WorkflowExecutor {

    @Autowired
    DeciderService deciderService;

    @Autowired
    ExecutionDAOFacade executionDAOFacade;

    @Resource
    QueueDAO queueDAO;

    public void executeSystemTask(WorkflowSystemTask systemTask, String taskId) {
        try {
            Task task = executionDAOFacade.getTaskById(taskId);
            if (task == null) {
                log.error("TaskId: {} could not be found while executing SystemTask", taskId);
                return;
            }
            log.debug("Task: {} fetched from execution DAO for taskId: {}", task, taskId);

            String workflowId = task.getWorkflowInstanceId();
            Workflow workflow = executionDAOFacade.getWorkflowById(workflowId, true);

            if (task.getStartTime() == 0) {
                task.setStartTime(System.currentTimeMillis());
            }

            switch (task.getStatus()) {
                case SCHEDULED:
                    systemTask.start(workflow, task, this);
                    break;

                case IN_PROGRESS:
                    systemTask.execute(workflow, task, this);
                    break;
                default:
                    break;
            }

            updateTask(new TaskResult(task));
            log.debug("Done Executing {}/{}-{} output={}", task.getTaskType(), task.getTaskId(), task.getStatus(),
                    task.getOutputData().toString());

        } catch (Exception e) {
            log.error("Error executing system task - {}, with id: {}", systemTask, taskId, e);
        }
    }

    public void updateTask(TaskResult taskResult) {
        if (taskResult == null) {
            throw new RuntimeException("Task object is null");
        }

        String workflowId = taskResult.getWorkflowInstanceId();
        Workflow workflowInstance = executionDAOFacade.getWorkflowById(workflowId, true);


        Task task = Optional.ofNullable(executionDAOFacade.getTaskById(taskResult.getTaskId()))
                .orElseThrow(() -> new RuntimeException("No such task found by id: " + taskResult.getTaskId()));

        log.debug("Task: {} belonging to Workflow {} being updated", task, workflowInstance);

        String taskQueueName = QueueUtils.getQueueName(task);

        if (task.getStatus().isTerminal()) {
            // Task was already updated....
            queueDAO.remove(taskQueueName, taskResult.getTaskId());
            log.info("Task: {} has already finished execution with status: {} within workflow: {}. Removed task from queue: {}", task.getTaskId(), task.getStatus(), task.getWorkflowInstanceId(), taskQueueName);
            return;
        }

        if (workflowInstance.getStatus().isTerminal()) {
            // Workflow is in terminal state
            queueDAO.remove(taskQueueName, taskResult.getTaskId());
            log.info("Workflow: {} has already finished execution. Task update for: {} ignored and removed from Queue: {}.", workflowInstance, taskResult.getTaskId(), taskQueueName);
            return;
        }

        // for system tasks, setting to SCHEDULED would mean restarting the task which is undesirable
        // for worker tasks, set status to SCHEDULED and push to the queue
        if (!isSystemTask.test(task) && taskResult.getStatus() == Status.IN_PROGRESS) {
            task.setStatus(SCHEDULED);
        } else {
            task.setStatus(valueOf(taskResult.getStatus().name()));
        }
        task.setReasonForIncompletion(taskResult.getReasonForIncompletion());
        task.setWorkerId(taskResult.getWorkerId());
        task.setOutputData(taskResult.getOutputData());


        if (task.getStatus().isTerminal()) {
            task.setEndTime(System.currentTimeMillis());
        }

        // Update message in Task queue based on Task status
        switch (task.getStatus()) {
            case COMPLETED:
            case CANCELED:
            case FAILED:
            case FAILED_WITH_TERMINAL_ERROR:
            case TIMED_OUT:
                try {
                    queueDAO.remove(taskQueueName, taskResult.getTaskId());
                    log.debug("Task: {} removed from taskQueue: {} since the task status is {}", task, taskQueueName, task.getStatus().name());
                } catch (Exception e) {
                    // Ignore exceptions on queue remove as it wouldn't impact task and workflow execution, and will be cleaned up eventually
                    String errorMsg = String.format("Error removing the message in queue for task: %s for workflow: %s", task.getTaskId(), workflowId);
                    log.warn(errorMsg, e);
                }
                break;
            case IN_PROGRESS:
            case SCHEDULED:
                try {
                    // postpone based on callbackAfterSeconds
                    queueDAO.postpone(taskQueueName, task.getTaskId());
                    log.debug("Task: {} postponed in taskQueue: {} since the task status is {} with callbackAfterSeconds", task, taskQueueName, task.getStatus().name());
                } catch (Exception e) {
                    // Throw exceptions on queue postpone, this would impact task execution
                    String errorMsg = String.format("Error postponing the message in queue for task: %s for workflow: %s", task.getTaskId(), workflowId);
                    log.error(errorMsg, e);
                    throw new RuntimeException(e);
                }
                break;
            default:
                break;
        }

        executionDAOFacade.updateTask(task);
        decide(workflowId, null);
    }

    public Map<String, Object> startWorkflowWithOutput (
            WorkflowDef workflowDefinition,
            Map<String, Object> workflowInput
    ) {
        String workflowId = UUID.randomUUID().toString();
        Workflow workflow = new Workflow();
        workflow.setWorkflowId(workflowId);
        workflow.setWorkflowDefinition(workflowDefinition);
        workflow.setStatus(WorkflowStatus.RUNNING);
        workflow.setCreateTime(System.currentTimeMillis());
        workflow.setUpdatedBy(null);
        workflow.setUpdateTime(null);
        workflow.setInput(workflowInput);
        try {
            Map<String, Object> output = new HashMap<>();
            executionDAOFacade.createWorkflow(workflow);
            decide(workflowId, output);
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public String startWorkflow(
            WorkflowDef workflowDefinition,
            Map<String, Object> workflowInput
            ) {
        String workflowId = UUID.randomUUID().toString();
        Workflow workflow = new Workflow();
        workflow.setWorkflowId(workflowId);
        workflow.setWorkflowDefinition(workflowDefinition);
        workflow.setStatus(WorkflowStatus.RUNNING);
        workflow.setCreateTime(System.currentTimeMillis());
        workflow.setUpdatedBy(null);
        workflow.setUpdateTime(null);
        workflow.setInput(workflowInput);
        try {
            executionDAOFacade.createWorkflow(workflow);
            decide(workflowId, null);
            return workflowId;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private boolean decide(String workflowId, Map<String, Object> output) {
        Workflow workflow = executionDAOFacade.getWorkflowById(workflowId, true);
        try {
            // 1、决策当前工作流，输出决策结果（待编安任务、待更新任务、工作流是否完结）
            DeciderService.DeciderOutcome outcome = deciderService.decide(workflow);
            if (outcome.isComplete) { // 如果角色结果为已完成，更新工作流并返回true
                if (output == null) {
                    completeWorkflow(workflow);
                } else {
                    output.putAll(completeWorkflowWithOutput(workflow));
                }
                return true;
            }

            List<Task> tasksToBeScheduled = outcome.tasksToBeScheduled;
            boolean stateChanged = false;

            // 3、安排待安排任务
            stateChanged = scheduleTask(workflow, tasksToBeScheduled) || stateChanged;

            if (stateChanged) {
                decide(workflowId, output);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private final Predicate<Task> isSystemTask = task -> SystemTaskType.is(task.getTaskType());

    boolean scheduleTask(Workflow workflow, List<Task> tasks) {
        List<Task> createdTasks;

        // 持久化待安排任务
        createdTasks = executionDAOFacade.createTasks(tasks);
        List<Task> systemTasks = createdTasks.stream()
                .filter(isSystemTask)
                .collect(Collectors.toList());

        boolean startedSystemTasks = false;

        // 循环系统任务并直接start同步任务
        for (Task task : systemTasks) {
            WorkflowSystemTask workflowSystemTask = WorkflowSystemTask.get(task.getTaskType());
            if (workflowSystemTask == null) {
                throw new RuntimeException("No system task found by name " + task.getTaskType());
            }
            if (task.getStatus() != null && !task.getStatus().isTerminal() && task.getStartTime() == 0) {
                task.setStartTime(System.currentTimeMillis());
            }
            Workflow workflowInstance = JSON.parseObject(JSON.toJSONString(workflow), Workflow.class);
            if (!workflowSystemTask.isAsync() && workflowSystemTask.execute(workflowInstance, task, this)) {
                try {
                    workflowSystemTask.start(workflow, task, this);
                } catch (Exception e) {
                    String errorMsg = String.format("Unable to start system task: %s, {id: %s, name: %s}", task.getTaskType(), task.getTaskId(), task.getTaskDefName());
                    throw new RuntimeException(errorMsg, e);
                }
                startedSystemTasks = true;
                // 同步任务执行完之后，更新持久化任务
                executionDAOFacade.updateTask(task);
            } else { // TODO 处理异步任务
//                tasksToBeQueued.add(task);
            }
        }
        return startedSystemTasks;
    }

    /**
     * 完成任务
     * @param wf
     */
    private void completeWorkflow (Workflow wf) {
        log.debug("Completing workflow execution for {}", wf.getWorkflowId());
        deciderService.updateWorkflowOutput(wf, null);
        Workflow workflow = executionDAOFacade.getWorkflowById(wf.getWorkflowId(), false);
        workflow.setStatus(WorkflowStatus.COMPLETED);
        workflow.setOutput(wf.getOutput());
        executionDAOFacade.updateWorkflow(workflow);
        log.debug("Completed workflow execution for {}", workflow.getWorkflowId());
//        if (StringUtils.isNotEmpty(workflow.getParentWorkflowId())) {
//            updateParentWorkflowTask(workflow);
//            decide(workflow.getParentWorkflowId());
//        }
    }

    /**
     * 完成任务
     * @param wf
     * @return
     */
    private Map<String, Object> completeWorkflowWithOutput (Workflow wf) {
        completeWorkflow(wf);
        return wf.getOutput();
    }
}
