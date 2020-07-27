package com.ido.mingo.server;


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
                new ProxyServer().start(20001);//start http server and listening
                countDownLatch.countDown();;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        executorService.execute(()->{
            try {
                new ClientProxyConnector().connect(20002);
                countDownLatch.countDown();;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        countDownLatch.await();
        executorService.shutdown();

    }
}
