package com.fengpb.conductor.dao;

import com.fengpb.conductor.common.metadata.tasks.TaskDef;
import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;
import com.fengpb.conductor.core.dao.MetadataDAO;
import com.fengpb.conductor.utils.LocalTaskCache;
import com.fengpb.conductor.utils.LocalWorkflowCache;
import com.fengpb.conductor.utils.LocalWorkflowDefCache;
import com.fengpb.conductor.utils.LocalWorkflowTaskCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LocalMetadataDAO implements MetadataDAO {

    @Autowired
    LocalTaskCache localTaskCache;

    @Autowired
    LocalWorkflowCache localWorkflowCache;

    @Autowired
    LocalWorkflowDefCache localWorkflowDefCache;

    @Autowired
    LocalWorkflowTaskCache localWorkflowTaskCache;

    @Override
    public TaskDef getTaskDef(String name) {
        return null;
    }

    @Override
    public Optional<WorkflowDef> get(String name, int version) {
        List<WorkflowDef> workflowDefs = localWorkflowDefCache.get();
        return Optional.ofNullable(workflowDefs.stream().filter(workflowDef ->
                name.equals(workflowDef.getName()) && version == workflowDef.getVersion()).findFirst().orElse(null));
    }

    @Override
    public void create(WorkflowDef def) {
        localWorkflowDefCache.insertOrUpdate(def);
    }

    @Override
    public void update(WorkflowDef def) {
        localWorkflowDefCache.insertOrUpdate(def);
    }
}
