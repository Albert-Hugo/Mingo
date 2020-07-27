package com.ido.mingo.server.connect.handler;

import com.ido.mingo.common.proto.DataInfo;
import com.ido.mingo.server.context.ClientProxyChannelHolder;
import com.ido.mingo.server.context.MingoClientConfig;
import com.ido.mingo.server.context.ResultHolder;
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

    }

    /**
     * @Author Ido
     * @Description //mingo client 连接后，需要绑定指定的channel，用于后面的转发
     * @Date 15:52 2020/7/27
     **/
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataInfo.Msg msg) throws Exception {

        log.debug("client connect");
        if (msg.getType().equals(DataInfo.Msg.Type.AUTH)) {
            handlerAuth(ctx, msg);
        } else if (msg.getType().equals(DataInfo.Msg.Type.DATA)) {
            // 将这里的结果返回到 proxy server里面
            ctx.channel().eventLoop().execute(() -> {

                ByteBuf r = Unpooled.copiedBuffer(msg.getData().getBytes());
                Channel channel = ResultHolder.get(msg.getID());
                channel.writeAndFlush(r);

                ReferenceCountUtil.release(msg);
                channel.close();
            });
        }


    }

    private void handlerAuth(ChannelHandlerContext ctx, DataInfo.Msg msg) {
        //todo 连接信息, 根据KEY 获取对应连接client 的 端口请求配置
        log.info("client {}  connect", msg.getKey());
        String key = msg.getKey();
        int port = MingoClientConfig.getClientPort(key);
        ClientProxyChannelHolder.setMapping(port, ctx.channel());


    }
}
