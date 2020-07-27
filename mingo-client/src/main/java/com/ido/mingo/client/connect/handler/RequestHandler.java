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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 数据透传
 */
@Slf4j
public class RequestHandler extends ChannelInboundHandlerAdapter {
    private static final ExecutorService taskWorker = new ThreadPoolExecutor(1000, 1000, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.AbortPolicy());

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        DataInfo.Msg dataInfo = DataInfo.Msg.newBuilder().setKey(Config.getInstance().getStringValue("mingo.key")).setType(DataInfo.Msg.Type.AUTH).build();
        ctx.writeAndFlush(dataInfo);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        DataInfo.Msg data = (DataInfo.Msg) msg;
        if (data.getType().equals(DataInfo.Msg.Type.DATA)) {
            taskWorker.execute(() -> {
                try {

                    long start = System.currentTimeMillis();


                    Socket socket = null;

                    socket = new Socket(Config.getInstance().getStringValue("target.host"), Config.getInstance().getIntValue("target.port"));
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

        log.info("not support message type" + data.getType());

    }
}
