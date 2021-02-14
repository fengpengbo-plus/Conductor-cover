package com.fengpb.conductor.config.redisconfig;

import java.util.List;

//@Configuration
//@ConfigurationProperties(prefix = "redis")
public class ServerConfig {
    private String masterId;
    private String password;
    private List<String> hostPorts;

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getHostPorts() {
        return hostPorts;
    }

    public void setHostPorts(List<String> hostPorts) {
        this.hostPorts = hostPorts;
    }
}
