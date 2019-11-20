package com.attach.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: tuGai
 * @createTime: 2019-07-14 00:18
 **/
public class AttachServer {

    private AtomicBoolean isBindRef = new AtomicBoolean(false);

    private static final int PORT = 2333;

    private static AttachServer attachServer = null;

    public static int pid;
    public static Instrumentation instrumentation;

    private AttachServer(int javaPid, Instrumentation ins) {
        pid = javaPid;
        instrumentation = ins;
    }

    public static void main(String[] args) {
        new AttachServer(0, null).bind();
    }

    /**
     * 单例
     *
     * @param instrumentation JVM增强
     * @return ArthasServer单例
     */
    public synchronized static AttachServer getInstance(int javaPid, Instrumentation instrumentation) {
        if (attachServer == null) {
            attachServer = new AttachServer(javaPid, instrumentation);
        }
        return attachServer;
    }

    public void bind() {

        if (!isBindRef.compareAndSet(false, true)) {
            throw new IllegalStateException("already bind");
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        AttachServerHandler handler = new AttachServerHandler();

        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    //.handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();

                            p.addLast(new ObjectEncoder());
                            p.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            p.addLast(handler);
                        }
                    });

            ChannelFuture future = server.bind(PORT).sync();

            future.channel().closeFuture().addListener(f -> {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断服务端是否已经启动
     *
     * @return true:服务端已经启动;false:服务端关闭
     */
    public boolean isBind() {
        return isBindRef.get();
    }

}
