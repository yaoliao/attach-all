package com.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;

import java.util.Scanner;

/**
 * @author: tuGai
 * @createTime: 2019-07-14 16:38
 **/
public class AttachClient {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 2333;

    public static void main(String[] args) throws Exception {
        // Configure SSL.git
        final SslContext sslCtx;

        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            //p.addLast(new LoggingHandler(LogLevel.INFO));
                            p.addLast(new ObjectEncoder());
                            p.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            p.addLast(new ClientHandler());
                        }
                    });

            // Start the client.
            ChannelFuture f = b.connect(HOST, PORT).sync();

            startConsoleThread(f.channel());

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }
    }

    private static void startConsoleThread(Channel channel) {
        Thread thread = new Thread(() -> {
            while (!Thread.interrupted()) {
                System.out.println("超级变化形态完成，等待: ");
                Scanner sc = new Scanner(System.in);
                String line = sc.nextLine();

                if (line != null && line.length() > 0) {
                    channel.writeAndFlush(line);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 已有命令
     */

    // thread
    // {"cmdName":"thread","parameter":{"threadName":"java.attach-command-execute-daemon-1"},"requestId":2333}

    // trace
    //  {"cmdName":"trace","parameter":{"classPattern":"demo.MathGame","methodPattern":"run"},"requestId":2333}

    // redefine
    //  {"cmdName":"redefine","parameter":{"classPath":"/Users/yaoliao/Documents/study/MathGame.class"},"requestId":2333}
    //  {"cmdName":"redefine","parameter":{"classPath":"/Users/yaoliao/Documents/study/attachdemo/MathGame.class"},"requestId":2333}

    // redefine reset
    // {"cmdName":"redefine","parameter":{"reset":"true"},"requestId":2333}

    // mc
    // {"cmdName":"mc","parameter":{"classPath":"/Users/yaoliao/Documents/study/MathGame.java"},"requestId":2333}

    // jad
    // {"cmdName":"jad","parameter":{"classPattern":"demo.MathGame"},"requestId":2333}

}
