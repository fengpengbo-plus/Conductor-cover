package com.fengpb.conductor.dao;

import com.fengpb.conductor.common.metadata.tasks.Task;
import com.fengpb.conductor.common.run.Workflow;
import com.fengpb.conductor.core.dao.ExecutionDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

//@Repository
public class MySQLExecutionDAO implements ExecutionDAO {
    @Override
    public String createWorkflow(Workflow workflow) {
        return null;
    }

    @Override
    public String updateWorkflow(Workflow workflow) {
        return null;
    }

    @Override
    public void removeWorkflow(String workflowId) {

    }

    @Override
    public Task getTask(String taskId) {
        return null;
    }

    @Override
    public List<Task> createTasks(List<Task> tasks) {
        return null;
    }

    @Override
    public void updateTask(Task task) {

    }

    @Override
    public Workflow getWorkflow(String workflowId, boolean includeTasks) {
        return null;
    }

    @Override
    public List<Task> getTasksForWorkflow(String workflowId) {
        return null;
    }

    @Override
    public List<Task> getTasks(List<String> taskIds) {
        return null;
    }
}
