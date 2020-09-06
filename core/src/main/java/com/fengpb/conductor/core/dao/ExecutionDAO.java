package com.fengpb.conductor.core.dao;

import com.fengpb.conductor.common.run.Workflow;

public interface ExecutionDAO {

    Workflow getWorkflow(String workflowId, boolean includeTasks);

    void createWorkflow(Workflow workflow);

    void updateWorkflow(Workflow workflow);

    void removeWorkflow(String workflowId);
}
