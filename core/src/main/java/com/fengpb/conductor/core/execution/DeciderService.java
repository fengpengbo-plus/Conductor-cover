package com.fengpb.conductor.core.execution;

import com.fengpb.conductor.common.metadata.tasks.Task;
import com.fengpb.conductor.common.run.Workflow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class DeciderService {

    @Autowired
    private ParametersUtils parametersUtils;

    public DeciderOutcome decide(Workflow workflow) {
        return null;
    }

    public static class DeciderOutcome {

        List<Task> tasksToBeScheduled = new LinkedList<>();

        List<Task> tasksToBeUpdated = new LinkedList<>();

        boolean isComplete;

        private DeciderOutcome() {
        }

    }
}
