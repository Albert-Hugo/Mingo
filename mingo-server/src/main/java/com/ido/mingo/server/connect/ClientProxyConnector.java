package com.ido.mingo.server.connect;

import com.ido.mingo.server.connect.handler.ClientHandler;
import com.ido.mingo.server.context.ClientProxyChannelHolder;
import com.ido.mingo.common.proto.DataInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * client 用于连接远程proxy客户端
 * @author Carl
 * @date 2019/12/23
 */
@Slf4j
public class ClientProxyConnector {


    public void connect(int host) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup master = new NioEventLoopGroup();
        try {

            bootstrap.group(master)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline
                                    .addLast(new ProtobufVarint32FrameDecoder())
                                    .addLast(new ProtobufVarint32LengthFieldPrepender())
                                    .addLast(new ProtobufEncoder())
                                    .addLast(new ProtobufDecoder(DataInfo.Msg.getDefaultInstance()))
                                    .addLast(new ClientHandler());
                            ClientProxyChannelHolder.setMapping("/home", pipeline.channel());
                        }
                    });
            ChannelFuture f = bootstrap.connect("127.0.0.1", host);

            f
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (!future.isSuccess()) {
                                future.cause().printStackTrace();
                            }


                        }
                    });


            f.channel().closeFuture().sync();

        } finally {
            log.info("client shutdown");
            master.shutdownGracefully();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                        new ClientProxyConnector().connect(host);
                    } catch (InterruptedException e) {
                        log.info(e.getMessage(),e);
                    }
                }
            }).start();

        }
    }

}
