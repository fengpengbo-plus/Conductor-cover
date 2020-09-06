package com.fengpb.conductor.core.execution;

import com.fengpb.conductor.core.execution.tasks.WorkflowSystemTask;

import java.util.HashSet;
import java.util.Set;

public enum SystemTaskType {

    ;
    private static Set<String> builtInTasks = new HashSet<>();

    private WorkflowSystemTask impl;

    SystemTaskType(WorkflowSystemTask impl) {
        this.impl = impl;
    }

    public WorkflowSystemTask impl() {
        return this.impl;
    }

    public static boolean is(String taskType) {
        return WorkflowSystemTask.is(taskType);
    }

    public static boolean isBuiltIn(String taskType) {
        return is(taskType) && builtInTasks.contains(taskType);
    }

}
