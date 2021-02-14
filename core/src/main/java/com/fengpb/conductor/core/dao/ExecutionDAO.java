package com.fengpb.conductor.core.dao;

import com.fengpb.conductor.common.metadata.tasks.Task;
import com.fengpb.conductor.common.run.Workflow;

import java.util.List;

public interface ExecutionDAO {

    String createWorkflow(Workflow workflow);

    String updateWorkflow(Workflow workflow);

    void removeWorkflow(String workflowId);

    Task getTask(String taskId);

    List<Task> createTasks(List<Task> tasks);

    void updateTask(Task task);

    Workflow getWorkflow(String workflowId, boolean includeTasks);

    List<Task> getTasksForWorkflow(String workflowId);

    List<Task> getTasks(List<String> taskIds);
}
