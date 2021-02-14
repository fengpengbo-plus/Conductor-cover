package com.fengpb.conductor.dao;

import com.alibaba.fastjson.JSON;
import com.fengpb.conductor.common.metadata.tasks.Task;
import com.fengpb.conductor.common.run.Workflow;
import com.fengpb.conductor.core.dao.ExecutionDAO;
import com.fengpb.conductor.utils.LocalTaskCache;
import com.fengpb.conductor.utils.LocalWorkflowCache;
import com.fengpb.conductor.utils.LocalWorkflowDefCache;
import com.fengpb.conductor.utils.LocalWorkflowTaskCache;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class LocalExecutionDAO implements ExecutionDAO {

    @Autowired
    LocalTaskCache localTaskCache;

    @Autowired
    LocalWorkflowCache localWorkflowCache;

    @Autowired
    LocalWorkflowDefCache localWorkflowDefCache;

    @Autowired
    LocalWorkflowTaskCache localWorkflowTaskCache;

    @Override
    public String createWorkflow(Workflow workflow) {
        return insertOrUpdateWorkflow(workflow);
    }

    @Override
    public String updateWorkflow(Workflow workflow) {
        return insertOrUpdateWorkflow(workflow);
    }

    @Override
    public void removeWorkflow(String workflowId) {
        localWorkflowCache.remove(workflowId);
    }

    @Override
    public Task getTask(String taskId) {
        return localTaskCache.get(taskId);
    }

    @Override
    public List<Task> createTasks(List<Task> tasks) {
        List<Task> created = Lists.newArrayListWithCapacity(tasks.size());
        for (Task task : tasks) {
            task.setScheduledTime(System.currentTimeMillis());
            final String taskKey = taskKey(task);
            String scheduledTask = localWorkflowTaskCache.get(taskKey);
            if (StringUtils.isNotBlank(scheduledTask)) {
                continue;
            }
            localWorkflowTaskCache.insertOrUpdate(taskKey, StringUtils.join(task.getWorkflowInstanceId(),
                    task.getTaskId()));
            localTaskCache.insertOrUpdate(task);
            created.add(task);
        }
        return created;
    }

    @Override
    public void updateTask(Task task) {
        localTaskCache.insertOrUpdate(task);
    }

    @Override
    public Workflow getWorkflow(String workflowId, boolean includeTasks) {
        Workflow workflow = localWorkflowCache.get(workflowId);
        if (includeTasks) {
            List<Task> tasks = getTasksForWorkflow(workflowId);
            workflow.setTasks(tasks);
        }
        return workflow;
    }

    @Override
    public List<Task> getTasksForWorkflow(String workflowId) {
        List<Task> allTask = localTaskCache.getAll();
        return allTask.stream().filter(task -> workflowId.equals(task.getWorkflowInstanceId())).collect(Collectors.toList());
    }

    @Override
    public List<Task> getTasks(List<String> taskIds) {
        return localTaskCache.getAll(taskIds);
    }

    private String insertOrUpdateWorkflow(Workflow workflow) {
        List<Task> tasks = workflow.getTasks();
        workflow.setTasks(Lists.newLinkedList());
        localWorkflowCache.insertOrUpdate(workflow);
        workflow.setTasks(tasks);
        return workflow.getWorkflowId();
    }

    private static String taskKey (Task task) {
        return StringUtils.join(task.getReferenceTaskName(), "_", task.getTaskId());
    }
}
