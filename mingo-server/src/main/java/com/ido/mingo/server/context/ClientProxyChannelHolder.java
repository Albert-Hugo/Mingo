package com.ido.mingo.server.context;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientProxyChannelHolder {
    private final static Map<String, Channel> holder = new ConcurrentHashMap<>();



    public static void setMapping(String url, Channel ch) {
        holder.put(url, ch);
    }

    public static Channel getChannel(String url) {
        return holder.get(url);
    }

}
