package com.ido.mingo.server.connect.handler;

import com.ido.mingo.server.context.ResultHolder;
import com.ido.mingo.common.proto.DataInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<DataInfo.Msg> {
    private static AtomicInteger count = new AtomicInteger(0);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.debug("connect to remote proxy client");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DataInfo.Msg msg) throws Exception {
        // 将这里的结果返回到 proxy server里面
        channelHandlerContext.channel().eventLoop().execute(()->{

            ByteBuf r = Unpooled.copiedBuffer(msg.getData().getBytes());
            Channel channel = ResultHolder.get(msg.getID());
            channel.writeAndFlush(r);

            ReferenceCountUtil.release(msg);
            channel.close();
        });

    }
}
