package com.ido.mingo.client;


import com.ido.mingo.client.connect.ProxyClient;
import com.ido.mingo.common.Config;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientStarter {
    public static void main(String[] args) {
        //todo using cmd tools to handler cmd args
        try {
            new ProxyClient().start(Config.getInstance().getIntValue("mingo.server.port"));
        } catch (InterruptedException e) {
            log.error(e.getMessage(),e);
        }

    }
}
