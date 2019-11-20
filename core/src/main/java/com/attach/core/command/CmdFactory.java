package com.attach.core.command;

import com.alibaba.fastjson.JSON;
import com.attach.core.command.impl.*;
import com.attach.core.model.Request;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: tuGai
 * @createTime: 2019-07-16 01:00
 * <p>
 * 用于初始化构建命令
 **/
public class CmdFactory {

    private static volatile CmdFactory instance = null;

    private static final Map<String, Class<? extends ICommand>> cmdMap = new HashMap<>();

    private CmdFactory() {
        init();
    }

    private void init() {
        // TODO 添加命令
        cmdMap.put("thread", ThreadCommand.class);
        cmdMap.put("trace", TraceCommand.class);
        cmdMap.put("redefine", RedefineCommand.class);
        cmdMap.put("mc", MemoryCompilerCommand.class);
        cmdMap.put("jad", JadCommand.class);
    }

    public static CmdFactory getInstance() {
        if (instance == null) {
            synchronized (CmdFactory.class) {
                if (instance == null) {
                    instance = new CmdFactory();
                }
            }
        }
        return instance;
    }

    public ICommand getCmd(String s) {
        Request request = JSON.parseObject(s, Request.class);
        String cmdName = request.getCmdName();
        Map<String, String> parameter = request.getParameter();
        Class<? extends ICommand> commandClass = cmdMap.get(cmdName);
        if (commandClass == null) throw new RuntimeException("命令有误：" + cmdName);
        ICommand command;
        try {
            command = commandClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("实例化命令失败：" + cmdName);
        }
        command.parse(parameter);
        command.setRequestId(request.getRequestId());
        return command;
    }
}
