package com.fengpb.conductor.utils;

import com.fengpb.conductor.common.metadata.tasks.Task;
import com.fengpb.conductor.constant.CacheKeyEnum;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocalTaskCache {

    private Cache<String, Task> localCache = CacheBuilder.newBuilder()
            .maximumSize(CacheKeyEnum.local_task_cache.maximumSize())
            .expireAfterAccess(CacheKeyEnum.local_task_cache.expireTime(), CacheKeyEnum.local_task_cache.timeUnit())
            .build();

    public void insertOrUpdate(Task task) {
        add(task);
    }

    public Task get(String key) {
        return localCache.getIfPresent(key);
    }

    public void remove(String key) {
        localCache.invalidate(key);
    }

    public void add(Task task) {
        localCache.put(task.getTaskId(), task);
    }

    public List<Task> getAll(List<String> taskIds) {
        return localCache.getAllPresent(taskIds).values().stream().collect(Collectors.toList());
    }

    public List<Task> getAll() {
        return localCache.asMap().values().stream().collect(Collectors.toList());
    }
}
