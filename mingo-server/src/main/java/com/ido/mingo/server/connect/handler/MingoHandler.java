package com.ido.mingo.server.connect.handler;

import com.ido.mingo.common.Config;
import com.ido.mingo.common.proto.DataInfo;
import com.ido.mingo.server.context.ClientProxyChannelHolder;
import com.ido.mingo.server.context.ResultHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MingoHandler extends SimpleChannelInboundHandler<DataInfo.Msg> {


    /**
     * @Author Ido
     * @Description //mingo client 连接后，需要绑定指定的channel，用于后面的转发
     * @Date 15:52 2020/7/27
     **/
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataInfo.Msg msg) throws Exception {


        if (msg.getType().equals(DataInfo.Msg.Type.AUTH)) {
            handlerAuth(ctx, msg);
        } else if (msg.getType().equals(DataInfo.Msg.Type.DATA)) {
            handlerData(ctx, msg);
        } else if (msg.getType().equals(DataInfo.Msg.Type.HEART_BEAT)) {
            log.info("heart beat from {}", msg.getKey());
            // 指定时间内，没有heart beat 则下线，解除绑定的channel
            int port = msg.getPort();
            ClientProxyChannelHolder.refresh(port);
        }


    }

    private void handlerData(ChannelHandlerContext ctx, DataInfo.Msg msg) {
        // 将这里的结果返回到 proxy server里面
        ctx.channel().eventLoop().execute(() -> {

            ByteBuf r = Unpooled.copiedBuffer(msg.getData().getBytes());
            Channel channel = ResultHolder.get(msg.getID());
            channel.writeAndFlush(r);

            ReferenceCountUtil.release(msg);
            channel.close();
            ResultHolder.remove(msg.getID());
        });
    }

    private void handlerAuth(ChannelHandlerContext ctx, DataInfo.Msg msg) {
        //连接信息, 根据KEY 获取对应连接client 的 端口请求配置
        String key = msg.getKey();
        if (clientNotExist(key)) {
            ctx.close();
        }

        log.info("client {}  connect, mapping port {}", msg.getKey(), msg.getPort());
        int port = msg.getPort();
        ClientProxyChannelHolder.setMapping(port, ctx.channel());

    }

    private boolean clientNotExist(String key) {
        if (StringUtil.isNullOrEmpty(key)) {
            return true;
        }
        String keys = Config.getInstance().getStringValue("mingo.client.keys");
        for (String s : keys.split(",")) {
            if (s.trim().equals(key)) {
                return false;
            }
        }
        return true;
    }
}
