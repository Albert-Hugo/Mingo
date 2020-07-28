package com.ido.mingo.server;


import com.ido.mingo.common.Config;
import com.ido.mingo.server.connect.LocalStubServer;
import com.ido.mingo.server.connect.MingoServer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author Ido
 * @Description deploy to public net work
 * @Date 15:39 2020/7/27RequestHandler
 **/
@Slf4j
public class ServerStarter {

    public static void main(String[] args) throws InterruptedException {


        String ports = Config.getInstance().getStringValue("mingo.local.ports");
        String[] ps = ports.split(",");
        ExecutorService executorService = Executors.newFixedThreadPool(ps.length + 1);

        for (String p : ps) {

            executorService.execute(() -> {
                try {
                    //start local server and listening to incoming request
                    new LocalStubServer().start(Integer.parseInt(p));
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                    System.exit(-1);
                }
            });
        }
        executorService.execute(() -> {
            try {
                new MingoServer().start(Config.getInstance().getIntValue("mingo.port"));
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                System.exit(-1);
            }
        });

        executorService.shutdown();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown Hook is running !");
        }));
    }
}
