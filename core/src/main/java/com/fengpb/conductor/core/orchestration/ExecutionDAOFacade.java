package com.fengpb.conductor.core.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fengpb.conductor.common.metadata.tasks.Task;
import com.fengpb.conductor.common.run.Workflow;
import com.fengpb.conductor.core.dao.ExecutionDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ExecutionDAOFacade {

    @Autowired
    ExecutionDAO executionDAO;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Workflow getWorkflowById(String workflowId, boolean includeTasks) {
        Workflow workflow = executionDAO.getWorkflow(workflowId, includeTasks);
        return workflow;
    }

    public String createWorkflow(Workflow workflow) {
        workflow.setCreateTime(System.currentTimeMillis());
        executionDAO.createWorkflow(workflow);
        return workflow.getWorkflowId();
    }

    public String updateWorkflow(Workflow workflow) {
        workflow.setUpdateTime(System.currentTimeMillis());
        executionDAO.updateWorkflow(workflow);
        return workflow.getWorkflowId();
    }

    public void updateTask(Task task) {
        try {
            if (task.getStatus() != null) {
                if (!task.getStatus().isTerminal() || (task.getStatus().isTerminal() && task.getUpdateTime() == 0)) {
                    task.setUpdateTime(System.currentTimeMillis());
                }
                if (task.getStatus().isTerminal() && task.getEndTime() == 0) {
                    task.setEndTime(System.currentTimeMillis());
                }
            }
            executionDAO.updateTask(task);
        } catch (Exception e) {
            String errorMsg = String.format("Error updating task: %s in workflow: %s", task.getTaskId(), task.getWorkflowInstanceId());
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    public void updateTasks(List<Task> tasks) {
        tasks.forEach(this::updateTask);
    }

    public List<Task> createTasks(List<Task> task) {
        return executionDAO.createTasks(task);
    }

    public Task getTaskById(String taskId) {
        return executionDAO.getTask(taskId);
    }

    public void removeWorkflow(String workflowId, boolean archiveWorkflow) {
        try {
            executionDAO.removeWorkflow(workflowId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
