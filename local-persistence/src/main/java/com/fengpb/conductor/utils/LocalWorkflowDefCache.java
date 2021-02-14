package com.fengpb.conductor.utils;

import com.alibaba.fastjson.JSON;
import com.fengpb.conductor.common.metadata.workflow.WorkflowDef;
import com.fengpb.conductor.constant.CacheKeyEnum;
import com.fengpb.conductor.dao.MySQLMetadataMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Component
public class LocalWorkflowDefCache {

    @Resource
    MySQLMetadataMapper mapper;

    private LoadingCache<String, List<WorkflowDef>> localCache = CacheBuilder.newBuilder()
            .maximumSize(CacheKeyEnum.local_workflow_def_cache.maximumSize())
            .refreshAfterWrite(CacheKeyEnum.local_workflow_def_cache.expireTime()
                    , CacheKeyEnum.local_workflow_def_cache.timeUnit())
            .build(
                    new CacheLoader<String, List<WorkflowDef>>() {
                        @Override
                        public List<WorkflowDef> load(String s) throws Exception {
                            return mapper.getAllWorkflowDef();
                        }
                    }
            );

    @PostConstruct
    public void initCache() {
        localCache.getUnchecked(CacheKeyEnum.local_workflow_def_cache.key());
    }

    public void insertOrUpdate(WorkflowDef def) {
        mapper.insertWorkflowDef(def, JSON.toJSONString(def));
        this.initCache();
    }

    public List<WorkflowDef> get() {
        return localCache.getUnchecked(CacheKeyEnum.local_workflow_def_cache.key());
    }

    public void remove(String key) {
        localCache.invalidate(key);
    }
}
