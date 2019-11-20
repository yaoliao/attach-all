package com.attach.core.model;

import com.attach.core.system.Session;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: tuGai
 * @createTime: 2019-07-17 00:24
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private Response response;

    private Session session;

}
