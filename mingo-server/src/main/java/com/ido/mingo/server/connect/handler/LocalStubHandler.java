package com.ido.mingo.server.connect.handler;

import com.ido.mingo.common.proto.DataInfo;
import com.ido.mingo.server.context.ClientProxyChannelHolder;
import com.ido.mingo.server.context.ResultHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.UUID;

@Slf4j
public class LocalStubHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("移除断开的链接 " + ctx.channel().id().asLongText());
        ctx.close();
    }


    /**
     * @Author Ido
     * @Description //这里的数据是 被代理的服务请求第一次过来的数据 ， 原始的 TCP data
     * @Date 15:50 2020/7/27
     **/
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        int port = sa.getPort();

        ctx.channel().eventLoop().execute(() -> {
            ByteBuf data = (ByteBuf) msg;


            byte[] d = new byte[data.readableBytes()];
            data.getBytes(0, d);
            String body = new String(d);
            String uuid = UUID.randomUUID().toString();
            DataInfo.Msg bu = DataInfo.Msg.newBuilder().setData(body).setType(DataInfo.Msg.Type.DATA).setID(uuid).build();
            //根据请求的端口，获取Mingo client 的channel
            Channel remoteClientProxy = ClientProxyChannelHolder.getChannel(port);
            if (remoteClientProxy == null) {
                ctx.close();
                return;

            }
            ChannelFuture cf = remoteClientProxy.writeAndFlush(bu);

            if (ResultHolder.get(uuid) == null) {
                log.info("设置target channel" + uuid);
                ResultHolder.put(uuid, ctx.pipeline().channel());
            }

            cf.addListener(future -> {
                if (future.isSuccess()) {
                    ReferenceCountUtil.release(msg);
                }
            });
        });




    }
}
