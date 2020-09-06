package com.fengpb.conductor.core.execution.tasks;

import com.fengpb.conductor.common.metadata.tasks.Task;
import com.fengpb.conductor.common.metadata.workflow.WorkflowTask;
import com.fengpb.conductor.common.run.Workflow;
import com.fengpb.conductor.core.execution.WorkflowExecutor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WorkflowSystemTask {
    private static Map<String, WorkflowSystemTask> registry = new HashMap<>();

    private String name;

    public WorkflowSystemTask(String name) {
        this.name = name;
        registry.put(name, this);
    }

    /**
     * Start the task execution
     * @param workflow Workflow for which the task is being started
     * @param task Instance of the Task
     * @param executor Workflow Executor
     */
    public void start(Workflow workflow, Task task, WorkflowExecutor executor) {
        //Do nothing unless overridden by the task implementation
    }

    /**
     *
     * @param workflow Workflow for which the task is being started
     * @param task Instance of the Task
     * @param executor Workflow Executor
     * @return true, if the execution has changed the task status.  return false otherwise.
     */
    public boolean execute(Workflow workflow, Task task, WorkflowExecutor executor) {
        return false;
    }

    /**
     * Cancel task execution
     * @param workflow Workflow for which the task is being started
     * @param task Instance of the Task
     * @param executor Workflow Executor
     */
    public void cancel(Workflow workflow, Task task, WorkflowExecutor executor) {
    }

    /**
     *
     * @return True if the task is supposed to be started asynchronously using internal queues.
     */
    public boolean isAsync() {
        return false;
    }

    /**
     *
     * @return True to keep task in 'IN_PROGRESS' state, and 'COMPLETE' later by an external message.
     */
    public boolean isAsyncComplete(Task task) {
        if (task.getInputData().containsKey("asyncComplete")) {
            return Optional.ofNullable(task.getInputData().get("asyncComplete"))
                    .map(result -> (Boolean) result)
                    .orElse(false);
        } else {
            return Optional.ofNullable(task.getWorkflowTask())
                    .map(WorkflowTask::isAsyncComplete)
                    .orElse(false);
        }
    }

    /**
     *
     * @return Time in seconds after which the task should be retried if rate limited or remains in in_progress after start method execution.
     */
    public int getRetryTimeInSecond() {
        return 30;
    }
    /**
     *
     * @return name of the system task
     */
    public String getName(){
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static boolean is(String type){
        return registry.containsKey(type);
    }

    public static WorkflowSystemTask get(String type) {
        return registry.get(type);
    }

    public static Collection<WorkflowSystemTask> all() {
        return registry.values();
    }

}
