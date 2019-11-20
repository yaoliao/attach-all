package com.attach.core;

import com.attach.common.ProcessUtils;
import com.attach.core.command.CmdExecutor;
import com.attach.core.command.CmdFactory;
import com.attach.core.command.ICommand;
import com.attach.core.system.Session;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author: tuGai
 * @createTime: 2019-07-08 14:28
 **/
public class AttachCore {

    private static final ExecutorService commandExecutors = new ThreadPoolExecutor(2, 4,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(100),
            new ThreadFactory() {
                AtomicInteger ai = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    final Thread t = new Thread(r, "java.attach-command-execute-daemon-" + ai.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                }
            });

    public static void doCommand(String requestStr, Session session) {
        commandExecutors.submit(() -> {
            ICommand cmd = CmdFactory.getInstance().getCmd(requestStr);
            CmdExecutor.exec(cmd, session);
        });
    }

    public static void main(String[] args) {

//        String agentPath = "/Users/yaoliao/IdeaProjects/java.attach-all/agent/target/java.attach-agent-jar-with-dependencies.jar";
        String agentPath = "/Users/yaoliao/IdeaProjects/attach-all/agent/target/java.attach-agent-jar-with-dependencies.jar";

//        String corePath = "/Users/yaoliao/IdeaProjects/java.attach-all/core/target/java.attach-core-jar-with-dependencies.jar";
        String corePath = "/Users/yaoliao/IdeaProjects/attach-all/core/target/java.attach-core-jar-with-dependencies.jar";

        Map<Integer, String> jpsMap = ProcessUtils.listProcessByJps(false);

        String pid = null;
        for (Map.Entry<Integer, String> entry : jpsMap.entrySet()) {
            String value = entry.getValue();
            if ("arthas-demo.jar".equals(value.substring(value.indexOf(" ")).trim())) {
                pid = String.valueOf(entry.getKey());
            }
        }

        if (pid == null) {
            System.out.println("没找到啊。。。。。。");
            return;
        }

        VirtualMachine attach = null;
        try {
            attach = VirtualMachine.attach(pid);
            attach.loadAgent(agentPath, pid + ";2333;" + corePath);

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (attach != null) {
                    attach.detach();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
