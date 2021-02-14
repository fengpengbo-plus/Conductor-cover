package com.fengpb.conductor.config.redisconfig;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

//@Configuration
public class LettuceClientConfig {
    @Autowired
    private ServerConfig serverConfig;

    /**
     * lettuce客户端配置
     * @return
     */
    public RedisClusterClient lettuceClient() {
        List<RedisURI> redisURIList = new ArrayList<>();
        serverConfig.getHostPorts().stream().forEach(hostPorts -> {
            String[] hostPortArr = hostPorts.split(":");
            RedisURI redisURI = RedisURI.create(hostPortArr[0], Integer.parseInt(hostPortArr[1]));
            redisURI.setPassword(serverConfig.getPassword());
            redisURIList.add(redisURI);
        });
        return RedisClusterClient.create(redisURIList);
    }

    /**
     * lettuce同步连接
     * @return
     */
    @Bean
    public RedisAdvancedClusterCommands<String, String> lettuceCommands() {
        return this.lettuceClient().connect().sync();
    }

    /**
     * lettuce异步连接
     * @return
     */
    @Bean
    public RedisAdvancedClusterAsyncCommands<String, String> lettuceAsyncCommands() {
        return this.lettuceClient().connect().async();
    }
}
