package com.attach.agent;

import java.lang.reflect.Method;

/**
 * 间谍类<br/>
 * 藏匿在各个ClassLoader中
 * Created by vlinux on 15/8/23.
 */
public class Spy {


    // -- 各种Advice的钩子引用 --
    public static volatile Method ON_BEFORE_METHOD;
    public static volatile Method ON_RETURN_METHOD;
    public static volatile Method ON_THROWS_METHOD;
    public static volatile Method BEFORE_INVOKING_METHOD;
    public static volatile Method AFTER_INVOKING_METHOD;
    public static volatile Method THROW_INVOKING_METHOD;

    // debug 相关的钩子
    public static volatile Method PUT_LOCAL_VARIABLE_METHOD;
    private static volatile Method PUT_FIELD_METHOD;
    private static volatile Method PUT_STATIC_FIELD_METHOD;
    private static volatile Method FILL_STACK_TRACE_METHOD;
    private static volatile Method DEBUG_END;
    private static volatile Method CAN_EXECUTE;


    /**
     * arthas's classloader 引用
     */
    public static volatile ClassLoader CLASSLOADER;

    /**
     * 代理重设方法
     */
    public static volatile Method AGENT_RESET_METHOD;

    /**
     * 用于普通的间谍初始化
     */
    public static void init(
            ClassLoader classLoader,
            Method onBeforeMethod,
            Method onReturnMethod,
            Method onThrowsMethod,
            Method beforeInvokingMethod,
            Method afterInvokingMethod,
            Method throwInvokingMethod) {
        CLASSLOADER = classLoader;
        ON_BEFORE_METHOD = onBeforeMethod;
        ON_RETURN_METHOD = onReturnMethod;
        ON_THROWS_METHOD = onThrowsMethod;
        BEFORE_INVOKING_METHOD = beforeInvokingMethod;
        AFTER_INVOKING_METHOD = afterInvokingMethod;
        THROW_INVOKING_METHOD = throwInvokingMethod;
    }

    /**
     * 用于启动线程初始化
     */
    public static void initForAgentLauncher(
            ClassLoader classLoader,
            Method onBeforeMethod,
            Method onReturnMethod,
            Method onThrowsMethod,
            Method beforeInvokingMethod,
            Method afterInvokingMethod,
            Method throwInvokingMethod,
            Method agentResetMethod) {
        CLASSLOADER = classLoader;
        ON_BEFORE_METHOD = onBeforeMethod;
        ON_RETURN_METHOD = onReturnMethod;
        ON_THROWS_METHOD = onThrowsMethod;
        BEFORE_INVOKING_METHOD = beforeInvokingMethod;
        AFTER_INVOKING_METHOD = afterInvokingMethod;
        THROW_INVOKING_METHOD = throwInvokingMethod;
        AGENT_RESET_METHOD = agentResetMethod;
    }

    /**
     * Clean up the reference to com.taobao.arthas.agent.AgentLauncher$1
     * to avoid classloader leak.
     */
    public static void destroy() {
        CLASSLOADER = null;
        ON_BEFORE_METHOD = null;
        ON_RETURN_METHOD = null;
        ON_THROWS_METHOD = null;
        BEFORE_INVOKING_METHOD = null;
        AFTER_INVOKING_METHOD = null;
        THROW_INVOKING_METHOD = null;
        // clear the reference to ArthasClassLoader in AgentLauncher
        if (AGENT_RESET_METHOD != null) {
            try {
                AGENT_RESET_METHOD.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AGENT_RESET_METHOD = null;
    }

    // ================ debug ============


    public static void initDebug(Method putLocalVariable,
                                 Method putField,
                                 Method putStaticField,
                                 Method fillStackTeace,
                                 Method debugEnd,
                                 Method canExecute) {
        PUT_LOCAL_VARIABLE_METHOD = putLocalVariable;
        PUT_FIELD_METHOD = putField;
        PUT_STATIC_FIELD_METHOD = putStaticField;
        FILL_STACK_TRACE_METHOD = fillStackTeace;
        DEBUG_END = debugEnd;
        CAN_EXECUTE = canExecute;
    }

    public static boolean canExecute(Integer requestId) {
        final boolean defaultValue = true;
        try {
            return (boolean) doInvokeMethod(Spy.CAN_EXECUTE, defaultValue, new Object[]{requestId});
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            return false;
        }
    }

    public static void putLocalVariable(String key, Object value) {
        final Void defaultValue = null;
        try {
            doInvokeMethod(Spy.PUT_LOCAL_VARIABLE_METHOD, defaultValue, new Object[]{key, value});
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    public static void putField(String key, Object value) {
        final Void defaultValue = null;
        try {
            doInvokeMethod(Spy.PUT_FIELD_METHOD, defaultValue, new Object[]{key, value});
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    public static void putStaticField(String key, Object value) {
        final Void defaultValue = null;
        try {
            doInvokeMethod(Spy.PUT_STATIC_FIELD_METHOD, defaultValue, new Object[]{key, value});
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    public static void fillStacktrace(Throwable e) {
        final Void defaultValue = null;
        try {
            doInvokeMethod(Spy.FILL_STACK_TRACE_METHOD, defaultValue, new Object[]{e});
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    public static void debugEnd(Integer requestId) {
        final Void defaultValue = null;
        try {
            doInvokeMethod(Spy.DEBUG_END, defaultValue, new Object[]{requestId});
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    private static Object doInvokeMethod(Method method, Object defaultValue, Object[] args) throws Throwable {
        if (method == null) {
            return defaultValue;
        }

        return method.invoke(null, args);
    }

}
