package com.fengpb.conductor.controller;

import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;
import com.fengpb.conductor.common.run.Workflow;
import com.fengpb.conductor.core.dao.ExecutionDAO;
import com.fengpb.conductor.core.dao.MetadataDAO;
import com.fengpb.conductor.core.execution.WorkflowExecutor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("workflow")
public class WorkflowController {

    @Resource
    WorkflowExecutor executor;

    @Resource
    ExecutionDAO executionDAO;

    @Resource
    MetadataDAO metadataDAO;

    @GetMapping("workflow/{id}")
    public Workflow getWorkflowById(@PathVariable String id) {
        return executionDAO.getWorkflow(id, true);
    }

    @PostMapping("workflow/{name}")
    public Map<String, Object> process(@PathVariable String name, @RequestBody Map<String, Object> params) {
        Optional<WorkflowDef> workflowDef = metadataDAO.get(name, 1);
        return executor.startWorkflowWithOutput(workflowDef.get(), params);
    }
}
