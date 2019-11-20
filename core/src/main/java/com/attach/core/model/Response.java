package com.attach.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: tuGai
 * @createTime: 2019-07-16 00:23
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response implements Serializable {

    private int requestId;

    private String str;

}
