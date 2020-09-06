package com.fengpb.conductor.common.run;

import com.fengpb.conductor.common.metadata.Auditable;
import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;
import lombok.Data;

import java.util.HashMap;
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

    private Map<String, Object> input = new HashMap<>();

    private Map<String, Object> output = new HashMap<>();;

    private WorkflowDef workflowDefinition;
}
