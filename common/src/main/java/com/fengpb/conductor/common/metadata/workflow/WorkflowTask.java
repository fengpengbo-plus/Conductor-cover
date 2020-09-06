package com.fengpb.conductor.common.metadata.workflow;

import com.fengpb.conductor.common.metadata.tasks.TaskDef;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class WorkflowTask {
    public enum Type {
        SIMPLE, DYNAMIC, FORK_JOIN, FORK_JOIN_DYNAMIC, DECISION, JOIN, SUB_WORKFLOW, EVENT, WAIT, USER_DEFINED;
        private static Set<String> systemTasks = new HashSet<>();
        static {
            systemTasks.add(Type.SIMPLE.name());
            systemTasks.add(Type.DYNAMIC.name());
            systemTasks.add(Type.FORK_JOIN.name());
            systemTasks.add(Type.FORK_JOIN_DYNAMIC.name());
            systemTasks.add(Type.DECISION.name());
            systemTasks.add(Type.JOIN.name());
            systemTasks.add(Type.SUB_WORKFLOW.name());
            systemTasks.add(Type.EVENT.name());
            systemTasks.add(Type.WAIT.name());
            //Do NOT add USER_DEFINED here...
        }
        public static boolean isSystemTask(String name) {
            return systemTasks.contains(name);
        }
    }

    private String name;

    private String taskReferenceName;

    private String description;

    private Map<String, Object> inputParameters = new HashMap<>();

    private TaskDef taskDefinition;

    private boolean asyncComplete = false;
}
