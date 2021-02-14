package com.fengpb.conductor.common.metadata.workflow;

import com.fengpb.conductor.common.metadata.Auditable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;

@Data
public class WorkflowDef extends Auditable {

    private String name;

    private String description;

    private int version = 1;

    @NotNull
    @NotEmpty(message = "工作流任务集合不能为空")
    private List<@Valid WorkflowTask> tasks = new LinkedList<>();

    private List<String> inputParameters = new LinkedList<>();

    private Map<String, Object> outputParameters = new HashMap<>();

    public WorkflowTask getNextTask(String taskReferenceName){
        Iterator<WorkflowTask> it = tasks.iterator();
        while(it.hasNext()){
            WorkflowTask task = it.next();
            WorkflowTask nextTask = task.next(taskReferenceName, null);
            if(nextTask != null){
                return nextTask;
            } else if (TaskType.DO_WHILE.name().equals(task.getType()) && !task.getTaskReferenceName().equals(taskReferenceName) && task.has(taskReferenceName)) {
                // If the task is child of Loop Task and at last position, return null.
                return null;
            }

            if(task.getTaskReferenceName().equals(taskReferenceName) || task.has(taskReferenceName)){
                break;
            }
        }
        if(it.hasNext()){
            return it.next();
        }
        return null;
    }
}
