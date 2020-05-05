package demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.DomainSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @author: TuGai
 * @createTime: 2020-03-16 23:18
 **/
public class Server {

    public static void main(String[] args) throws IOException, InterruptedException {

        String s = Optional.ofNullable(args).filter(e -> e.length > 0).map(e -> e[0])
                .orElse("/tmp/netty_domain_socket_test_1.socket");

        File file = new File(s);
        /*Path path = Paths.get(file.getAbsolutePath());
        if (!Files.exists(path)) {
            Files.createFile(path);
            System.out.println(" create file socket success !!!!! ");
        }*/

        EventLoopGroup boss = new EpollEventLoopGroup();
        EventLoopGroup worker = new EpollEventLoopGroup(8);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(EpollServerDomainSocketChannel.class)
                    .childHandler(new ChannelInitializer<DomainSocketChannel>() {
                        @Override
                        protected void initChannel(DomainSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            ch.pipeline().addLast(new FileSocketServerHandler());
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(new DomainSocketAddress(file.getAbsolutePath())).sync();
            System.out.println(" file socket start success !!!!");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                future.channel().close().addListener((ChannelFutureListener) f -> {
                    if (f.isSuccess()) {
                        System.out.println("channel closed success !!!!");
                    } else {
                        System.err.println("channel closed error  !!!!");
                    }
                });
                boss.shutdownGracefully();
                worker.shutdownGracefully();
            }));


            future.channel().closeFuture().sync();
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }

    }


}
