package com.ido.mingo.server.connect;

import com.ido.mingo.server.connect.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Ido
 * @Description //TODO 最好支持在线动态切换端口
 * @Date 16:44 2020/7/27
 **/
@Slf4j
public class LocalStubServer {

    public void start(int port) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup master = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {

            bootstrap.group(master, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline
                                    .addLast(new ServerHandler());

                        }
                    });
            ChannelFuture f = bootstrap.bind("127.0.0.1", port);

            f
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (!future.isSuccess()) {
                                future.cause().printStackTrace();
                            }

                            log.info("stub server started and listen at " + port);


                        }
                    });


            f.channel().closeFuture().sync();


        } finally {
            master.shutdownGracefully();
            worker.shutdownGracefully();

        }
    }
}
