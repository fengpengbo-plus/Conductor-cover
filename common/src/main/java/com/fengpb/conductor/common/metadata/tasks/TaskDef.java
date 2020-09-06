package com.fengpb.conductor.common.metadata.tasks;

import com.fengpb.conductor.common.metadata.Auditable;
import lombok.Data;

@Data
public class TaskDef extends Auditable {

    private String name;

    private String description;

}
