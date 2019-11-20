package com.attach.core.command;

import lombok.Data;

/**
 * @author: tuGai
 * @createTime: 2019-07-17 00:51
 **/
@Data
public abstract class AbstractCommand implements ICommand {

    private int requestId;

    @Override
    public void setRequestId(int id) {
        requestId = id;
    }
}
