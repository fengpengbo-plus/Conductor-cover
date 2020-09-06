package com.fengpb.conductor.core.execution;

import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;
import com.fengpb.conductor.common.run.Workflow;
import com.fengpb.conductor.common.run.Workflow.WorkflowStatus;
import com.fengpb.conductor.core.orchestration.ExecutionDAOFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class WorkflowExecutor {

    @Autowired
    DeciderService deciderService;

    @Autowired
    ExecutionDAOFacade executionDAOFacade;

    public String startWorkflow(
            WorkflowDef workflowDefinition,
            Map<String, Object> workflowInput
            ) {
        String workflowId = UUID.randomUUID().toString();
        Workflow workflow = new Workflow();
        workflow.setWorkflowId(workflowId);
        workflow.setWorkflowDefinition(workflowDefinition);
        workflow.setStatus(WorkflowStatus.RUNNING);
        workflow.setCreateTime(System.currentTimeMillis());
        workflow.setUpdatedBy(null);
        workflow.setUpdateTime(null);
        workflow.setInput(workflowInput);
        try {
            executionDAOFacade.createWorkflow(workflow);
            decide(workflowId);
            return workflowId;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean decide(String workflowId) {
        Workflow workflow = executionDAOFacade.getWorkflowById(workflowId, true);
        try {
            DeciderService.DeciderOutcome outcome = deciderService.decide(workflow);
            if (outcome.isComplete) {
                completeWorkflow(workflow);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void completeWorkflow(Workflow workflow) {

    }
}
