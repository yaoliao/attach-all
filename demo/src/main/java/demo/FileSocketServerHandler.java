package demo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author: TuGai
 * @createTime: 2020-03-16 23:20
 **/
public class FileSocketServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("收到请求：  " + msg);
        ctx.channel().writeAndFlush(msg);
    }
}
