package com.ido.mingo.client.config;

import com.sun.security.ntlm.Server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManger {
    private static final Map<String,ServerInfo> SERVER_INFO_MAP = new ConcurrentHashMap<>();
    static {

        SERVER_INFO_MAP.put("test",new ServerInfo("127.0.0.1",8081));
    }
    public static ServerInfo getTargetServerInfo(String id){
        return SERVER_INFO_MAP.get(id);
    }
}
