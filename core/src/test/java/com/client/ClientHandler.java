package com.client;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author: tuGai
 * @createTime: 2019-07-14 16:39
 **/
public class ClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("连接建立成功。。。。。");
        //ctx.channel().writeAndFlush("啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊");
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
//        System.out.println(s);
        Response response = JSON.parseObject(s, Response.class);
        System.out.println(response.getStr());
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

}
