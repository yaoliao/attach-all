package com.attach.common;

import java.lang.management.ManagementFactory;

/**
 * @author: tuGai
 * @createTime: 2019-07-08 16:18
 **/
public class PidUtils {

    private static String PID = "-1";

    static {
        // https://stackoverflow.com/a/7690178
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        int index = jvmName.indexOf('@');

        if (index > 0) {
            try {
                PID = Long.toString(Long.parseLong(jvmName.substring(0, index)));
            } catch (Throwable e) {
                // ignore
            }
        }
    }

    public static String currentPid() {
        return PID;
    }

}
