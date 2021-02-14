package com.fengpb.conductor.dao;

import com.alibaba.fastjson.JSON;
import com.fengpb.conductor.common.metadata.tasks.TaskDef;
import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;
import com.fengpb.conductor.core.dao.MetadataDAO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Optional;

//@Repository
public class MySQLMetadataDAO implements MetadataDAO {

    @Resource
    MySQLMetadataMapper mapper;

    @Override
    public TaskDef getTaskDef(String name) {
        return null;
    }

    @Override
    public Optional<WorkflowDef> get(String name, int version) {
        return Optional.empty();
    }

    @Override
    public void create(WorkflowDef def) {
        mapper.insertWorkflowDef(def, JSON.toJSONString(def));
    }

    @Override
    public void update(WorkflowDef def) {

    }
}
