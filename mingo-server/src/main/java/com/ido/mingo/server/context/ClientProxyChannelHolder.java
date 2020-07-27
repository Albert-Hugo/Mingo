package com.ido.mingo.server.context;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientProxyChannelHolder {
    private final static Map<Integer, Channel> holder = new ConcurrentHashMap<>();


    public static void setMapping(int url, Channel ch) {
        holder.putIfAbsent(url, ch);
    }

    public static Channel getChannel(int url) {
        return holder.get(url);
    }

}
