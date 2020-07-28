package com.ido.mingo.client.connect;

import com.ido.mingo.client.connect.handler.RequestHandler;
import com.ido.mingo.common.Config;
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

/**
 * 部署到客户本地的proxy client
 */
@Slf4j
public class ProxyClient {


    public void start() throws InterruptedException {
        int port = Config.getInstance().getIntValue("mingo.server.port");
        String host = Config.getInstance().getStringValue("mingo.server.host");
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
                                    .addLast(new ProtobufDecoder(DataInfo.Msg.getDefaultInstance()))
                                    .addLast(new ProtobufEncoder())
                                    .addLast(new RequestHandler())

                            ;
                        }
                    });
            ChannelFuture f = bootstrap.connect(host, port);

            f
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (!future.isSuccess()) {
                                future.cause().printStackTrace();
                            }

                            log.info("connect to mingo server {}:{}", host, port);


                        }
                    });


            f.channel().closeFuture().sync();


        } finally {
            master.shutdownGracefully();
        }


    }
}
