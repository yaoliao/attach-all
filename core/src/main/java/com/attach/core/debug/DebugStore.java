package com.attach.core.debug;

import com.alibaba.fastjson.JSON;
import com.attach.core.system.Session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: TuGai
 * @createTime: 2020-05-03 22:48
 **/
public class DebugStore {

    private static ThreadLocal<Map<String, Object>> localVariables = new ThreadLocal<Map<String, Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };

    private static ThreadLocal<Map<String, Object>> fields = new ThreadLocal<Map<String, Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };

    private static ThreadLocal<Map<String, Object>> staticFields = new ThreadLocal<Map<String, Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };

    private static ThreadLocal<String> stackThreadLocal = new ThreadLocal<String>();

    public static boolean canExecute(int requestId) {
        boolean result = DebugTimesHolder.checkTimes(requestId);
        return result;
    }

    public static void putLocalVariable(String name, Object value) {
        localVariables.get().put(name, value);
    }

    public static void putField(String name, Object value) {
        fields.get().put(name, value);
    }

    public static void putStaticField(String name, Object value) {
        staticFields.get().put(name, value);
    }

    public static void fillStacktrace(Throwable e) {
        String stack = JSON.toJSONString(e);
        stackThreadLocal.set(stack);
    }

    public static void debugEnd(int requestId) {
        Map<String, Object> local = localVariables.get();
        Map<String, Object> filed = fields.get();
        Map<String, Object> staticFiled = staticFields.get();
        String stack = stackThreadLocal.get();
        DebugData debugData = new DebugData(local, filed, staticFiled, stack);
        String msg = JSON.toJSONString(debugData);
        Session session = DebugVisitor.get(requestId);
        if (session == null) {
            // TODO error
            System.err.println("============  can not get session ==========");
            return;
        }
        session.writeStr(requestId, debugData.toString());
        // TODO 这里先不去掉
        // DebugVisitor.clear(requestId);
        reset();
    }


    private static void reset() {
        localVariables.remove();
        fields.remove();
        staticFields.remove();
    }

    static class DebugData implements Serializable {

        private Map<String, Object> localVariables;
        private Map<String, Object> fields;
        private Map<String, Object> staticFields;
        private String stack;

        public DebugData(Map<String, Object> localVariables, Map<String, Object> fields, Map<String, Object> staticFields, String stack) {
            this.localVariables = localVariables;
            this.fields = fields;
            this.staticFields = staticFields;
            this.stack = stack;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "localVariables=" + localVariables +
                    ", fields=" + fields +
                    ", staticFields=" + staticFields +
                    ", stack='" + stack + '\'' +
                    '}';
        }
    }

}
