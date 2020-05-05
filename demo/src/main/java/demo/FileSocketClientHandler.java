package demo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author: TuGai
 * @createTime: 2020-03-17 09:56
 **/
public class FileSocketClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("client 收到返回的信息 ：" + msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("...............   error .........");
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
