package com.fengpb.conductor.core.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fengpb.conductor.common.run.Workflow;
import com.fengpb.conductor.core.dao.ExecutionDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExecutionDAOFacade {

    ExecutionDAO executionDAO;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Workflow getWorkflowById(String workflowId, boolean includeTasks) {
        Workflow workflow = executionDAO.getWorkflow(workflowId, includeTasks);
        return workflow;
    }

    public String createWorkflow(Workflow workflow) {
        workflow.setCreateTime(System.currentTimeMillis());
        executionDAO.createWorkflow(workflow);
        return workflow.getWorkflowId();
    }

    public String updateWorkflow(Workflow workflow) {
        workflow.setUpdateTime(System.currentTimeMillis());
        executionDAO.updateWorkflow(workflow);
        return workflow.getWorkflowId();
    }

    public void removeWorkflow(String workflowId, boolean archiveWorkflow) {
        try {
            executionDAO.removeWorkflow(workflowId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
