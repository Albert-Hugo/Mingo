package com.ido.mingo.server.connect;

import com.ido.mingo.common.proto.DataInfo;
import com.ido.mingo.server.connect.handler.MingoHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

/**
 * mingo server  用于被 mingo client 连接
 *
 * @author Carl
 * @date 2019/12/23
 */
@Slf4j
public class MingoServer {


    public void start(int port) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup master = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {

            bootstrap.group(master, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline
                                    .addLast(new ProtobufVarint32FrameDecoder())
                                    .addLast(new ProtobufVarint32LengthFieldPrepender())
                                    .addLast(new ProtobufEncoder())
                                    .addLast(new ProtobufDecoder(DataInfo.Msg.getDefaultInstance()))
                                    .addLast(new MingoHandler());
                        }
                    });
            ChannelFuture f = bootstrap.bind(port);

            f
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (!future.isSuccess()) {
                                future.cause().printStackTrace();
                                log.error(future.cause().getMessage(), future.cause());
                                return;
                            }

                            log.info("mingo server started at " + port);


                        }
                    });


            f.channel().closeFuture().sync();

        } finally {
            log.info("client shutdown");
            master.shutdownGracefully();
            worker.shutdownGracefully();


        }
    }

}
