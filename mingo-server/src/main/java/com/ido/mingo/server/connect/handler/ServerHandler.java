package com.ido.mingo.server.connect.handler;

import com.ido.mingo.server.context.ClientProxyChannelHolder;
import com.ido.mingo.server.context.ResultHolder;
import com.ido.mingo.common.proto.DataInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.UUID;

@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private final String route = "/home";
    private static Random random = new Random();

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("移除断开的链接 "+ctx.channel().id().asLongText());
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {


        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx.channel().eventLoop().execute(()->{
            ByteBuf data = (ByteBuf) msg;


            byte[] d = new byte[data.readableBytes()];
            data.getBytes(0, d);
            String body = new String(d);
            String uuid = UUID.randomUUID().toString();
            //todo the key is need to set
            String key = "test";
            DataInfo.Msg bu = DataInfo.Msg.newBuilder().setData(body).setID(uuid).setKey(key).build();
            //获取目标客户的channel
            Channel remoteClientProxy = ClientProxyChannelHolder.getChannel(route);
            if (remoteClientProxy == null) {
                return;

            }
            ChannelFuture cf = remoteClientProxy.writeAndFlush(bu);

            if(ResultHolder.get(uuid)==null){
                log.info("设置target channel" + uuid);
                ResultHolder.put(uuid,ctx.pipeline().channel());
            }

            cf.addListener(future -> {
                if (future.isSuccess()) {
                    ReferenceCountUtil.release(msg);
                }
            });
        });




    }
}
