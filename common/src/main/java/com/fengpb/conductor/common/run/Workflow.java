package com.fengpb.conductor.common.run;

import com.fengpb.conductor.common.metadata.Auditable;
import com.fengpb.conductor.common.metadata.tasks.Task;
import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class Workflow extends Auditable {
    public enum  WorkflowStatus {
        RUNNING(false, false),
        COMPLETED(true, true),
        FAILED(true, false),
        TIMED_OUT(true, false),
        TERMINATED(true, false),
        PAUSED(false, true);

        private boolean terminal;

        private boolean successful;

        WorkflowStatus(boolean terminal, boolean successful){
            this.terminal = terminal;
            this.successful = successful;
        }

        public boolean isTerminal(){
            return terminal;
        }

        public boolean isSuccessful(){
            return successful;
        }
    }

    private WorkflowStatus status = WorkflowStatus.RUNNING;

    private String workflowId;

    private String workflowName;

    private List<Task> tasks;

    private Map<String, Object> input = new HashMap<>();

    private Map<String, Object> output = new HashMap<>();;

    private WorkflowDef workflowDefinition;

    public Task getTaskByRefName(String refName) {
        if (refName == null) {
            throw new RuntimeException("refName passed is null.  Check the workflow execution.  For dynamic tasks, make sure referenceTaskName is set to a not null value");
        }
        LinkedList<Task> found = new LinkedList<>();
        for (Task t : tasks) {
            if (t.getReferenceTaskName() == null) {
                throw new RuntimeException("Task " + t.getTaskDefName() + ", seq=" + t.getSeq() + " does not have reference name specified.");
            }
            if (t.getReferenceTaskName().equals(refName)) {
                found.add(t);
            }
        }
        if (found.isEmpty()) {
            return null;
        }
        return found.getLast();
    }
}
