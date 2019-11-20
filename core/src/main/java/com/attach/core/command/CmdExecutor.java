package com.attach.core.command;

import com.attach.core.system.Session;

/**
 * @author: tuGai
 * @createTime: 2019-07-16 02:07
 **/
public class CmdExecutor {

    public static void exec(ICommand cmd, Session session) {

        //执行具体的操作
        cmd.process(session);

    }

}
