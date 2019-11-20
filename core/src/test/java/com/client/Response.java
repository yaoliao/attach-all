package com.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: TuGai
 * @createTime: 2019-07-22 01:48
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response implements Serializable {

    private int requestId;

    private String str;


}
