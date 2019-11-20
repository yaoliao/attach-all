package com.attach.core.system;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: tuGai
 * @createTime: 2019-07-16 00:44
 **/
public class SessionHolder {

    private static final AtomicInteger SESSION_ID = new AtomicInteger(0);

    private static final Map<Integer, Session> sessionMap = new ConcurrentHashMap<>();


    public static Session creatSession(Channel channel, Integer pid) {
        int sessionId = SESSION_ID.incrementAndGet();
        // TODO MD 坑比build模式
        Session session = Session.builder().channel(channel).javaPid(pid).sessionId(sessionId).build();
        session.setTimes(new AtomicInteger(0));
        Session old = sessionMap.putIfAbsent(sessionId, session);
        if (old != null) return old;
        return session;
    }


    public static Session get(int sessionId) {
        return sessionMap.get(sessionId);
    }


}
