package com.fengpb.conductor.common.metadata;

import lombok.Data;

@Data
public abstract class Auditable {

    private String ownerApp;

    private Long createTime;

    private Long updateTime;

    private String createdBy;

    private String updatedBy;
}
