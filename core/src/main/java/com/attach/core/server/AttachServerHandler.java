package com.attach.core.server;

import com.attach.core.AttachCore;
import com.attach.core.system.Session;
import com.attach.core.system.SessionHolder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

/**
 * @author: tuGai
 * @createTime: 2019-07-14 00:42
 **/
@ChannelHandler.Sharable
public class AttachServerHandler extends SimpleChannelInboundHandler<String> {

    private static final AttributeKey<Session> ATTRIBUTE_KEY = AttributeKey.valueOf("session");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接建立了。。。。。");
        Session session = SessionHolder.creatSession(ctx.channel(), 2333);
        session.setInstrumentation(AttachServer.instrumentation);

        ctx.channel().attr(ATTRIBUTE_KEY).setIfAbsent(session);
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        System.out.println("收到信号  -----> " + s);

        Session session = ctx.channel().attr(ATTRIBUTE_KEY).get();
        AttachCore.doCommand(s, session);

        //ctx.channel().writeAndFlush(s);
    }
}
