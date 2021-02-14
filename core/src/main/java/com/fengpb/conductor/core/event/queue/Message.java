package com.fengpb.conductor.core.event.queue;

import lombok.Data;

@Data
public class Message {

    private String payload;

    private String id;
}
