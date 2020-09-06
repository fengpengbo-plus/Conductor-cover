package com.fengpb.conductor.common.metadata.workflow;

import com.fengpb.conductor.common.metadata.Auditable;
import lombok.Data;

@Data
public class WorkflowDef extends Auditable {

    private String name;

    private String description;
}
