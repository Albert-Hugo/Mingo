package com.ido.mingo.client;


import com.ido.mingo.client.connect.ProxyClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientStarter {
    public static void main(String[] args) {
        try {
            new ProxyClient().start();
        } catch (InterruptedException e) {
            log.error(e.getMessage(),e);
        }

    }
}
