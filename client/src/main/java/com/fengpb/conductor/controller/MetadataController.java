package com.fengpb.conductor.controller;

import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;
import com.fengpb.conductor.core.dao.MetadataDAO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("metadata")
public class MetadataController {

    @Resource
    MetadataDAO metadataDAO;

    @PostMapping("workflow")
    public void workflow(@RequestBody WorkflowDef workflowDef) {
        metadataDAO.create(workflowDef);
    }
}
