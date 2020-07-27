package com.ido.mingo.server;


import com.ido.mingo.common.Config;
import com.ido.mingo.server.connect.ClientProxyConnector;
import com.ido.mingo.server.connect.ProxyServer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerStarter {
    private final static CountDownLatch countDownLatch  = new CountDownLatch(2);

    public static void main(String[] args) throws InterruptedException {


        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(()->{
            try {
                new ProxyServer().start(Config.getInstance().getIntValue("mingo.server.port"));//start http server and listening
                countDownLatch.countDown();;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        executorService.execute(()->{
            try {
                new ClientProxyConnector().connect(Config.getInstance().getIntValue("mingo.client.port"));
                countDownLatch.countDown();;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        countDownLatch.await();
        executorService.shutdown();

    }
}
