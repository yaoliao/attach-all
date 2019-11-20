package com.attach.core.command;

import com.attach.core.system.Session;

import java.util.Map;

/**
 * @author: tuGai
 * @createTime: 2019-07-14 22:28
 **/
public interface ICommand {

    void process(Session session);

    void parse(Map<String, String> parameter);

    void setRequestId(int id);

}
