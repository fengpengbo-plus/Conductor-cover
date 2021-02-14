package com.fengpb.conductor.dao;

import com.fengpb.conductor.core.dao.QueueDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

//@Repository
public class MySQLQueueDAO implements QueueDAO {
    @Override
    public List<String> pop(String queueName, int count, int timeout) {
        return null;
    }

    @Override
    public void remove(String queueName, String messageId) {

    }

    @Override
    public void push(String queueName, String id) {

    }

    @Override
    public void popMessages(String queueName, int count, int timeout) {

    }
}
