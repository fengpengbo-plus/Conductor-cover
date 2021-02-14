package com.fengpb.conductor.common.metadata.tasks;

import com.fengpb.conductor.common.metadata.workflow.WorkflowTask;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Task {

    public enum Status {

        IN_PROGRESS(false, true, true),
        CANCELED(true, false, false),
        FAILED(true, false, true),
        FAILED_WITH_TERMINAL_ERROR(true, false, false), //No Retires even if retries are configured, the task and the related workflow should be terminated
        COMPLETED(true, true, true),
        COMPLETED_WITH_ERRORS(true, true, true),
        SCHEDULED(false, true, true),
        TIMED_OUT(true, false, true),
        SKIPPED(true, true, false);

        private boolean terminal;

        private boolean successful;

        private boolean retriable;

        Status(boolean terminal, boolean successful, boolean retriable) {
            this.terminal = terminal;
            this.successful = successful;
            this.retriable = retriable;
        }

        public boolean isTerminal() {
            return terminal;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public boolean isRetriable() {
            return retriable;
        }
    }

    private Status status;

    private Map<String, Object> inputData = new HashMap<>();

    private Map<String, Object> outputData = new HashMap<>();

    private String referenceTaskName;

    private String taskDefName;

    private WorkflowTask workflowTask;

    private String workflowInstanceId;

    private String workerId;

    private String taskType;

    private long startTime;

    private long scheduledTime;

    private long endTime;

    private long updateTime;

    private String taskId;

    private boolean executed;

    private int seq;

    private String reasonForIncompletion;
}
