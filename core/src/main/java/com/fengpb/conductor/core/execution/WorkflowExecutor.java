package com.fengpb.conductor.core.execution;

import com.fengpb.conductor.common.metadata.tasks.Task;
import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;
import com.fengpb.conductor.common.run.Workflow;
import com.fengpb.conductor.common.run.Workflow.WorkflowStatus;
import com.fengpb.conductor.core.execution.tasks.WorkflowSystemTask;
import com.fengpb.conductor.core.orchestration.ExecutionDAOFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fengpb.conductor.core.execution.DeciderService.DeciderOutcome;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class WorkflowExecutor {

    @Autowired
    DeciderService deciderService;

    @Autowired
    ExecutionDAOFacade executionDAOFacade;

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
            decide(workflowId);
            return workflowId;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private boolean decide(String workflowId) {
        Workflow workflow = executionDAOFacade.getWorkflowById(workflowId, true);
        try {
            DeciderService.DeciderOutcome outcome = deciderService.decide(workflow);
            if (outcome.isComplete) {
                completeWorkflow(workflow);
                return true;
            }

            List<Task> tasksToBeScheduled = outcome.tasksToBeScheduled;
            List<Task> tasksToBeUpdated = outcome.tasksToBeUpdated;
            boolean stateChanged = false;

            stateChanged = scheduleTask(workflow, tasksToBeScheduled) || stateChanged;

            if (stateChanged) {
                decide(workflowId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private final Predicate<Task> isSystemTask = task -> SystemTaskType.is(task.getTaskType());

    boolean scheduleTask(Workflow workflow, List<Task> tasks) {
        List<Task> createdTasks;

        createdTasks = executionDAOFacade.createTasks(tasks);
        List<Task> systemTasks = createdTasks.stream()
                .filter(isSystemTask)
                .collect(Collectors.toList());

        boolean startedSystemTasks = false;

        for (Task task : systemTasks) {
            WorkflowSystemTask workflowSystemTask = WorkflowSystemTask.get(task.getTaskType());
            if (workflowSystemTask == null) {
                throw new RuntimeException("No system task found by name " + task.getTaskType());
            }
            if (task.getStatus() != null && !task.getStatus().isTerminal() && task.getStartTime() == 0) {
                task.setStartTime(System.currentTimeMillis());
            }
            if (!workflowSystemTask.isAsync()) {
                try {
                    workflowSystemTask.start(workflow, task, this);
                } catch (Exception e) {
                    String errorMsg = String.format("Unable to start system task: %s, {id: %s, name: %s}", task.getTaskType(), task.getTaskId(), task.getTaskDefName());
                    throw new RuntimeException(errorMsg, e);
                }
                startedSystemTasks = true;
                executionDAOFacade.updateTask(task);
            } else {
//                tasksToBeQueued.add(task);
            }
        }
        return startedSystemTasks;
    }

    private void completeWorkflow(Workflow workflow) {

    }
}
