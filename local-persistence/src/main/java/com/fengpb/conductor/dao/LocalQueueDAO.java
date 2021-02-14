package com.fengpb.conductor.dao;

import com.fengpb.conductor.core.dao.QueueDAO;
import com.fengpb.conductor.utils.LocalTaskCache;
import com.fengpb.conductor.utils.LocalWorkflowCache;
import com.fengpb.conductor.utils.LocalWorkflowDefCache;
import com.fengpb.conductor.utils.LocalWorkflowTaskCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LocalQueueDAO implements QueueDAO {

    @Autowired
    LocalTaskCache localTaskCache;

    @Autowired
    LocalWorkflowCache localWorkflowCache;

    @Autowired
    LocalWorkflowDefCache localWorkflowDefCache;

    @Autowired
    LocalWorkflowTaskCache localWorkflowTaskCache;

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
