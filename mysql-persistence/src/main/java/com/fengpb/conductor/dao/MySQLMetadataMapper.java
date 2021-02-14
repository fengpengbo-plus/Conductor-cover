package com.fengpb.conductor.dao;

import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MySQLMetadataMapper {

    @Insert("insert into meta_workflow_def (name, version, json_data) values (#{def.name}, #{def.version}, #{jsonData})")
    Integer insertWorkflowDef (WorkflowDef def, String jsonData);

    @Select("select json_data from meta_workflow_def")
    List<WorkflowDef> getAllWorkflowDef();
}
