package com.ido.mingo.client.connect.handler;

import com.ido.mingo.common.Config;
import com.ido.mingo.common.proto.DataInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * 数据透传
 */
@Slf4j
public class RequestHandler extends ChannelInboundHandlerAdapter {
    private static final ExecutorService taskWorker = new ThreadPoolExecutor(1000, 1000, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.AbortPolicy());
    private static final ScheduledExecutorService HEAT_BEAT_EXECUTORS = new ScheduledThreadPoolExecutor(1);

    /**
     * @Author Ido
     * @Description //连接Mingo server
     * @Date 22:08 2020/7/27
     **/
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //发送 authentication 信息
        DataInfo.Msg dataInfo = DataInfo.Msg.newBuilder()
                .setKey(Config.getInstance().getStringValue("mingo.key"))
                .setPort(Config.getInstance().getIntValue("mingo.remote.mapping.port"))
                .setType(DataInfo.Msg.Type.AUTH).build();
        ctx.writeAndFlush(dataInfo);

        sendHeartbeat(ctx);

    }

    private void sendHeartbeat(ChannelHandlerContext ctx) {
        HEAT_BEAT_EXECUTORS.scheduleAtFixedRate(() -> {
            DataInfo.Msg heartbeat = DataInfo.Msg.newBuilder()
                    .setKey(Config.getInstance().getStringValue("mingo.key"))
                    .setType(DataInfo.Msg.Type.HEART_BEAT).build();
            ctx.writeAndFlush(heartbeat);
        }, 5, 10, TimeUnit.SECONDS);
    }


    /**
     * @Author Ido
     * @Description //回写数据到目标客户端
     * @Date 22:12 2020/7/27
     **/
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        DataInfo.Msg data = (DataInfo.Msg) msg;
        if (data.getType().equals(DataInfo.Msg.Type.DATA)) {
            transferData(ctx, data);
        } else {
            log.warn("not support message type {}", data.getType());
        }


    }

    private void transferData(ChannelHandlerContext ctx, DataInfo.Msg data) {
        taskWorker.execute(() -> {
            try {

                long start = System.currentTimeMillis();


                Socket socket = null;

                socket = new Socket(Config.getInstance().getStringValue("mingo.target.host"), Config.getInstance().getIntValue("mingo.target.port"));
                //获取输出流，向服务器端发送信息
                OutputStream outputStream = socket.getOutputStream();//字节输出流
                PrintWriter pw = new PrintWriter(outputStream); //将输出流包装为打印流
                pw.write(data.getData());
                pw.flush();
                socket.shutdownOutput();

                //获取输入流，读取服务器端的响应
                InputStream inputStream = socket.getInputStream();
                InputStream br = new BufferedInputStream(inputStream);
                byte[] bs = new byte[1024];
                StringBuilder sb = new StringBuilder();
                while (-1 != br.read(bs)) {
                    sb.append(new String(bs));

                }
                socket.shutdownInput();

                //关闭资源
                br.close();
                inputStream.close();
                pw.close();
                outputStream.close();
                socket.close();
                DataInfo.Msg testBuf = DataInfo.Msg.newBuilder().setID(data.getID()).setType(DataInfo.Msg.Type.DATA).setData(sb.toString()).build();
                ctx.writeAndFlush(testBuf);
                long end = System.currentTimeMillis();
                if (log.isErrorEnabled()) {
                    log.debug("using time " + (end - start));
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        });
    }
}
