package com.fengpb.conductor.core.dao;

import com.fengpb.conductor.common.metadata.tasks.TaskDef;
import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;

import java.util.Optional;

public interface MetadataDAO {

    TaskDef getTaskDef(String name);

    Optional<WorkflowDef> get(String name, int version);

    void create(WorkflowDef def);

    void update(WorkflowDef def);
}
