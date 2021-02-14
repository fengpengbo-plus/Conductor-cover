package com.fengpb.conductor.utils;

import com.fengpb.conductor.constant.CacheKeyEnum;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

@Component
public class LocalWorkflowTaskCache {

    private Cache<String, String> localCache = CacheBuilder.newBuilder()
            .maximumSize(CacheKeyEnum.local_workflow_task_cache.maximumSize())
            .expireAfterAccess(CacheKeyEnum.local_workflow_task_cache.expireTime()
                    , CacheKeyEnum.local_workflow_task_cache.timeUnit())
            .build();

    public void insertOrUpdate(String key, String value) {
        localCache.put(key, value);
    }

    public String get(String key) {
        return localCache.getIfPresent(key);
    }

    public void remove(String key) {
        localCache.invalidate(key);
    }

}
