package com.attach.core.command.impl;

import com.alibaba.fastjson.JSON;
import com.attach.core.command.AbstractCommand;
import com.attach.core.model.Message;
import com.attach.core.model.Response;
import com.attach.core.system.Session;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;

/**
 * @author: tuGai
 * @createTime: 2019-07-16 01:06
 **/
public class ThreadCommand extends AbstractCommand {

    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();


    @Override
    public void process(Session session) {
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        for (ThreadInfo threadInfo : threadInfos) {
            Response response = Response.builder().requestId(getRequestId()).str(threadInfo.toString()).build();
            session.writeMessage(Message.builder().session(session).response(response).build());
        }

    }

    @Override
    public void parse(Map<String, String> parameter) {
        System.out.println("收到 参数：" + JSON.toJSONString(parameter));
    }
}
