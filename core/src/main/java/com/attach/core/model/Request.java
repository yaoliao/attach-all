package com.attach.core.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: tuGai
 * @createTime: 2019-07-16 01:40
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request implements Serializable {

    private int requestId;

    private String cmdName;

    private Map<String, String> parameter;


    public static void main(String[] args) {
        Request request = new Request();
        request.setRequestId(2333);
        request.setCmdName("jad");

        Map<String, String> map = new HashMap<>();
        map.put("classPattern", "demo.MathGame");
        request.setParameter(map);

        String s = JSON.toJSONString(request);
        System.out.println(s);

        // thread
        // {"cmdName":"thread","parameter":{"threadName":"java.attach-command-execute-daemon-1"},"requestId":2333}

        // trace
        //  {"cmdName":"trace","parameter":{"classPattern":"demo.MathGame","methodPattern":"run"},"requestId":2333}

        // redefine
        //  {"cmdName":"redefine","parameter":{"classPath":"/Users/yaoliao/Documents/study/MathGame.class"},"requestId":2333}

        // redefine reset
        // {"cmdName":"redefine","parameter":{"reset":"true"},"requestId":2333}

        // mc
        // {"cmdName":"mc","parameter":{"classPath":"/Users/yaoliao/Documents/study/MathGame.java"},"requestId":2333}

        // jad
        // {"cmdName":"jad","parameter":{"classPattern":"demo.MathGame"},"requestId":2333}

    }

}
