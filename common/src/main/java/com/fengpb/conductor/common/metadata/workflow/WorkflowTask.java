package com.fengpb.conductor.common.metadata.workflow;

import com.fengpb.conductor.common.metadata.Auditable;
import com.fengpb.conductor.common.metadata.tasks.TaskDef;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.*;

@Data
public class WorkflowTask extends Auditable {
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

    @NotEmpty(message = "工作流任务名不可为空")
    private String name;

    @NotEmpty(message = "工作流任务引用名不可为空")
    private String taskReferenceName;

    private String description;

    private TaskType type;

    private Map<String, Object> inputParameters = new HashMap<>();

    private TaskDef taskDefinition;

    private boolean asyncComplete = false;

    private boolean optional = false;

    private List<@Valid List<@Valid WorkflowTask>> forkTasks = new LinkedList<>();

    private List<String> joinOn = new LinkedList<>();

    private Map<String, @Valid List<@Valid WorkflowTask>> decisionCases = new LinkedHashMap<>();

    private List<@Valid WorkflowTask> defaultCase = new LinkedList<>();

    private String caseExpression;

    private String caseValueParam;

    private Collection<List<WorkflowTask>> children() {
        Collection<List<WorkflowTask>> workflowTaskLists = new LinkedList<>();
        TaskType taskType = TaskType.USER_DEFINED;

        switch (taskType) {
            case DECISION:
                workflowTaskLists.addAll(decisionCases.values());
                workflowTaskLists.add(defaultCase);
                break;
            case FORK_JOIN:
                workflowTaskLists.addAll(forkTasks);
                break;
            case DO_WHILE:
//                workflowTaskLists.add(loopOver);
                break;
            default:
                break;
        }
        return workflowTaskLists;

    }

    public WorkflowTask next(String taskReferenceName, WorkflowTask parent) {
        TaskType taskType = TaskType.USER_DEFINED;

        switch (taskType) {
            case DO_WHILE:
            case DECISION:
                for (List<WorkflowTask> workflowTasks : children()) {
                    Iterator<WorkflowTask> iterator = workflowTasks.iterator();
                    while (iterator.hasNext()) {
                        WorkflowTask task = iterator.next();
                        if (task.getTaskReferenceName().equals(taskReferenceName)) {
                            break;
                        }
                        WorkflowTask nextTask = task.next(taskReferenceName, this);
                        if (nextTask != null) {
                            return nextTask;
                        }
                        if (task.has(taskReferenceName)) {
                            break;
                        }
                    }
                    if (iterator.hasNext()) {
                        return iterator.next();
                    }
                }
                if (taskType == TaskType.DO_WHILE && this.has(taskReferenceName)) {
                    // come here means this is DO_WHILE task and `taskReferenceName` is the last task in
                    // this DO_WHILE task, because DO_WHILE task need to be executed to decide whether to
                    // schedule next iteration, so we just return the DO_WHILE task, and then ignore
                    // generating this task again in deciderService.getNextTask()
                    return this;
                }
                break;
            case FORK_JOIN:
                boolean found = false;
                for (List<WorkflowTask> workflowTasks : children()) {
                    Iterator<WorkflowTask> iterator = workflowTasks.iterator();
                    while (iterator.hasNext()) {
                        WorkflowTask task = iterator.next();
                        if (task.getTaskReferenceName().equals(taskReferenceName)) {
                            found = true;
                            break;
                        }
                        WorkflowTask nextTask = task.next(taskReferenceName, this);
                        if (nextTask != null) {
                            return nextTask;
                        }
                        if (task.has(taskReferenceName)) {
                            break;
                        }
                    }
                    if (iterator.hasNext()) {
                        return iterator.next();
                    }
                    if (found && parent != null) {
                        return parent.next(this.taskReferenceName, parent);        //we need to return join task... -- get my sibling from my parent..
                    }
                }
                break;
            case DYNAMIC:
            case SIMPLE:
                return null;
            default:
                break;
        }
        return null;
    }

    public boolean has(String taskReferenceName) {
        if (this.getTaskReferenceName().equals(taskReferenceName)) {
            return true;
        }

        TaskType taskType = TaskType.USER_DEFINED;

        switch (taskType) {
            case DECISION:
            case DO_WHILE:
            case FORK_JOIN:
                for (List<WorkflowTask> childx : children()) {
                    for (WorkflowTask child : childx) {
                        if (child.has(taskReferenceName)) {
                            return true;
                        }
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }

}
