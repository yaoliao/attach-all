package demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.DomainSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author: TuGai
 * @createTime: 2020-03-17 09:52
 **/
public class Client {

    public static void main(String[] args) {

        String s = Optional.ofNullable(args).filter(e -> e.length > 0).map(e -> e[0])
                .orElse("/tmp/netty_domain_socket_test_1.socket");

        File file = new File(s);

        EpollEventLoopGroup work = new EpollEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        try {
            bootstrap.group(work)
                    .channel(EpollDomainSocketChannel.class)
                    .handler(new ChannelInitializer<DomainSocketChannel>() {
                        @Override
                        protected void initChannel(DomainSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            ch.pipeline().addLast(new FileSocketClientHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect(new DomainSocketAddress(file.getAbsolutePath())).sync();

            System.out.println(" file socket client start success !!!!");

            send(future.channel());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                future.channel().close().addListener((ChannelFutureListener) f -> {
                    if (f.isSuccess()) {
                        System.out.println("channel closed success !!!!");
                    } else {
                        System.err.println("channel closed error  !!!!");
                    }
                });
                work.shutdownGracefully();
            }));

            future.channel().closeFuture().addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    System.out.println("closeFuture channel closed success !!!!");
                } else {
                    System.err.println("closeFuture channel closed error  !!!!");
                }
            });
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            work.shutdownGracefully();
        }

    }

    private static void send(Channel channel) {

        new Thread(() -> {
            System.out.println("start to send ......");

            int i = 2333;
            while (true) {
                /*Scanner s = new Scanner(System.in);
                String line = s.nextLine();
                System.out.println("send " + line);
                channel.writeAndFlush(line);*/

                System.out.println("send :" + i);
                channel.writeAndFlush(String.valueOf(i));
                i++;

                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

}
