package com.ido.mingo.server.context;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ClientProxyChannelHolder {
    private final static Map<Integer, Channel> holder = new ConcurrentHashMap<>();
    private static Map<Integer, ChannelLifeState> states = new HashMap<>();
    private final static int TIME_OUT_THRESHOLD = 60 * 1000;
    private final static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    static {
        scheduledExecutorService.scheduleAtFixedRate(ClientProxyChannelHolder::clean, 60, 10, TimeUnit.SECONDS);
    }

    static class ChannelLifeState {
        long refreshTime;

        public ChannelLifeState(long refreshTime) {
            this.refreshTime = refreshTime;
        }
    }

    public static void refresh(int port) {
        long refreshTime = System.currentTimeMillis();
        ChannelLifeState s = states.get(port);
        if (s == null) {
            return;
        }

        s.refreshTime = refreshTime;

    }


    public static void setMapping(int port, Channel ch) {
        holder.putIfAbsent(port, ch);
        states.put(port, new ChannelLifeState(System.currentTimeMillis()));
    }


    /**
     * @Author Ido
     * @Description 清理断开过期的channel，基于heart beat 判断是否一段时间内有更新
     * @Date 22:05 2020/7/28
     **/
    public static void clean() {
        long currentTimeMillis = System.currentTimeMillis();

        for (Map.Entry<Integer, ChannelLifeState> entry : states.entrySet()) {
            ChannelLifeState s = entry.getValue();

            if (currentTimeMillis - s.refreshTime > TIME_OUT_THRESHOLD) {
                holder.remove(entry.getKey());
                states.remove(entry.getKey());
            }

        }

    }

    public static Channel getChannel(int port) {
        return holder.get(port);
    }

}
