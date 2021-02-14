package com.fengpb.conductor.core.dao;

import java.util.List;

public interface QueueDAO {

    List<String> pop(String queueName, int count, int timeout);

    void remove(String queueName, String messageId);

    void push(String queueName, String id);

    default boolean postpone(String queueName, String messageId) {
        remove(queueName, messageId);
        push(queueName, messageId);
        return true;
    }

    void popMessages(String queueName, int count, int timeout);
}
