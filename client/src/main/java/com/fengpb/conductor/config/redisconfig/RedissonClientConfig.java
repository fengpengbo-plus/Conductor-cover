package com.fengpb.conductor.config.redisconfig;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

//@Configuration
public class RedissonClientConfig {

    @Autowired
    private ServerConfig serverConfig;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        serverConfig.getHostPorts().stream().forEach(hostPorts -> {
            config.useClusterServers().addNodeAddress("redis://" + hostPorts);
        });

        config.setUseScriptCache(true);

        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
