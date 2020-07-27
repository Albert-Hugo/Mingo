package com.ido.mingo.server.context;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResultHolder {
    final static Map<String, Channel> holder = new ConcurrentHashMap<>();

    public static Channel get(String sessionId) {
        return holder.get(sessionId);
    }


    public static void put(String sessionId, Channel result) {
        holder.put(sessionId, result);
    }
}
