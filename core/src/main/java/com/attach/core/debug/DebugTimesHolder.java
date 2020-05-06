package com.attach.core.debug;

import com.attach.core.system.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: TuGai
 * @createTime: 2020-05-06 23:55
 **/
public class DebugTimesHolder {

    private final static Map<Integer/*requestId*/, Integer> VALID_TIMES
            = new ConcurrentHashMap<Integer, Integer>();

    public static void reg(Integer request, Integer session) {
        VALID_TIMES.put(request, session);
    }

    public static Integer get(Integer request) {
        return VALID_TIMES.get(request);
    }

    public static void clear(Integer request) {
        VALID_TIMES.remove(request);
    }

    public static boolean checkTimes(Integer request) {
        Integer validTimes = VALID_TIMES.get(request);
        if (validTimes == null) {
            end(request);
            return false;
        }
        Session session = DebugVisitor.get(request);
        if (session == null) {
            end(request);
            return false;
        }
        int i = session.getTimes().getAndIncrement();
        if (i >= validTimes) {
            end(request);
            session.writeStr(request, "debug 结束了，下次再来");
            return false;
        }
        return true;
    }

    private static void end(Integer request) {
        VALID_TIMES.remove(request);
        DebugVisitor.clear(request);
    }
}
