package com.fengpb.conductor.config.threadconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存线程池
 */
//@Configuration
public class LocalCacheLoadThreadPool {

    @Value("${ThreadPool.LocalCacheLoadThreadPool.corePoolSize}")
    private int corePoolSize;

    @Value("${ThreadPool.LocalCacheLoadThreadPool.maxPoolSize}")
    private int maxPoolSize;

    @Value("${ThreadPool.LocalCacheLoadThreadPool.keepAliveSeconds}")
    private int keepAliveSeconds;

    @Value("${ThreadPool.LocalCacheLoadThreadPool.queueCapacity}")
    private int queueCapacity;

    @Value("${ThreadPool.LocalCacheLoadThreadPool.poolName}")
    private String poolName;

    @Bean
    public ExecutorService localCacheExecutorService() {
        return new EventThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity), poolName);
    }
}
