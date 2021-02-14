package com.fengpb.conductor.utils;

import com.fengpb.conductor.common.run.Workflow;
import com.fengpb.conductor.constant.CacheKeyEnum;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

@Component
public class LocalWorkflowCache {

    private Cache<String, Workflow> localCache = CacheBuilder.newBuilder()
            .maximumSize(CacheKeyEnum.local_workflow_cache.maximumSize())
            .expireAfterAccess(CacheKeyEnum.local_workflow_cache.expireTime()
                    , CacheKeyEnum.local_workflow_cache.timeUnit())
            .build();

    public void insertOrUpdate(Workflow workflow) {
        add(workflow);
    }

    public Workflow get(String key) {
        return localCache.getIfPresent(key);
    }

    public void remove(String key) {
        localCache.invalidate(key);
    }

    public void add(Workflow workflow) {
        localCache.put(workflow.getWorkflowId(), workflow);
    }
}
