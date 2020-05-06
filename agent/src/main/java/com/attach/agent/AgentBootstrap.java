package com.attach.agent;


import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.jar.JarFile;

/**
 * @author: tuGai
 * @createTime: 2019-07-08 13:58
 **/
public class AgentBootstrap {

    private static final String ATTACH_BOOTSTRAP = "com.attach.core.server.AttachServer";
    private static final String BIND = "bind";

    private static final String ADVICEWEAVER = "com.attach.core.advice.AdviceWeaver";
    private static final String ON_BEFORE = "methodOnBegin";
    private static final String ON_RETURN = "methodOnReturnEnd";
    private static final String ON_THROWS = "methodOnThrowingEnd";
    private static final String BEFORE_INVOKE = "methodOnInvokeBeforeTracing";
    private static final String AFTER_INVOKE = "methodOnInvokeAfterTracing";
    private static final String THROW_INVOKE = "methodOnInvokeThrowTracing";
    private static final String RESET = "resetArthasClassLoader";
    private static final String ARTHAS_SPY_JAR = "arthas-spy.jar";
    private static final String ARTHAS_CONFIGURE = "com.taobao.arthas.core.config.Configure";
    private static final String ARTHAS_BOOTSTRAP = "com.taobao.arthas.core.server.ArthasBootstrap";
    private static final String TO_CONFIGURE = "toConfigure";
    private static final String GET_JAVA_PID = "getJavaPid";
    private static final String GET_INSTANCE = "getInstance";
    private static final String IS_BIND = "isBind";


    // ============= debug 想相关 ===========
    private static final String DEBUGSTORE = "com.attach.core.debug.DebugStore";
    private static final String PUT_LOCAL_VARIABLE = "putLocalVariable";
    private static final String put_Field = "putField";
    private static final String put_Static_Field = "putStaticField";
    private static final String fill_Stack_trace = "fillStacktrace";
    private static final String DEBUG_END = "debugEnd";
    private static final String CAN_EXECUTR = "canExecute";


    // 全局持有classloader用于隔离 Arthas 实现
    private static volatile ClassLoader attachClassLoader;

    public static void premain(String args, Instrumentation inst) {
        main(args, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        main(args, inst);
    }

    private static void main(String args, Instrumentation inst) {
        System.out.println("java.attach agent success...........");
        System.out.println("args: " + args);


        try {
            String[] split = args.split(";");
            String pid = split[0];
            String port = split[1];
            String corePath = split[2];

            // TODO 使用自定义classLoader加载 java.attach-core 的类
            inst.appendToBootstrapClassLoaderSearch(
                    new JarFile(AgentBootstrap.class.getProtectionDomain().getCodeSource().getLocation().getFile()));

            final ClassLoader loader = loadOrDefineClassLoader(corePath);

            initSpy(loader);

            // TODO 利用反射启动一个socket，用来接受命令
            System.out.println("开始启动 bootstrap .....");
            Class<?> bootstrap = loader.loadClass(ATTACH_BOOTSTRAP);

            Object attachServer = bootstrap.getDeclaredMethod(GET_INSTANCE, int.class, Instrumentation.class).invoke(null, Integer.valueOf(pid), inst);
            boolean isBind = (Boolean) bootstrap.getMethod(IS_BIND).invoke(attachServer);
            if (!isBind) {
                bootstrap.getDeclaredMethod(BIND).invoke(attachServer);
                System.out.println("启动 bootstrap success.....");
            }

        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("失败了呀。。。。。。。" + t.getMessage());
        }

//        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
//        long[] ids = threadMXBean.getAllThreadIds();
//        ThreadInfo[] threadInfo = threadMXBean.getThreadInfo(ids);
//        Arrays.stream(threadInfo).forEach(System.out::println);
    }

    private static ClassLoader loadOrDefineClassLoader(String corePath) throws Throwable {
        if (attachClassLoader == null) {
            attachClassLoader = new AttachClassLoader(new URL[]{new File(corePath).toURI().toURL()});
        }
        return attachClassLoader;
    }

    private static void initSpy(ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException {
        Class<?> adviceWeaverClass = classLoader.loadClass(ADVICEWEAVER);
        Method onBefore = adviceWeaverClass.getMethod(ON_BEFORE, int.class, ClassLoader.class, String.class,
                String.class, String.class, Object.class, Object[].class);
        Method onReturn = adviceWeaverClass.getMethod(ON_RETURN, Object.class);
        Method onThrows = adviceWeaverClass.getMethod(ON_THROWS, Throwable.class);
        Method beforeInvoke = adviceWeaverClass.getMethod(BEFORE_INVOKE, int.class, String.class, String.class, String.class, int.class);
        Method afterInvoke = adviceWeaverClass.getMethod(AFTER_INVOKE, int.class, String.class, String.class, String.class, int.class);
        Method throwInvoke = adviceWeaverClass.getMethod(THROW_INVOKE, int.class, String.class, String.class, String.class, int.class);
        Method reset = AgentBootstrap.class.getMethod(RESET);
        Spy.initForAgentLauncher(classLoader, onBefore, onReturn, onThrows, beforeInvoke, afterInvoke, throwInvoke, reset);

        // 初始化 debug 相关钩子
        Class<?> debugStoreClass = classLoader.loadClass(DEBUGSTORE);
        Method putLocalVariable = debugStoreClass.getMethod(PUT_LOCAL_VARIABLE, String.class, Object.class);
        Method putField = debugStoreClass.getMethod(put_Field, String.class, Object.class);
        Method putStaticField = debugStoreClass.getMethod(put_Static_Field, String.class, Object.class);
        Method fillStacktrace = debugStoreClass.getMethod(fill_Stack_trace, Throwable.class);
        Method debugEnd = debugStoreClass.getMethod(DEBUG_END, int.class);
        Method canExecute = debugStoreClass.getMethod(CAN_EXECUTR, int.class);
        Spy.initDebug(putLocalVariable, putField, putStaticField, fillStacktrace, debugEnd, canExecute);
    }


    /**
     * 让下次再次启动时有机会重新加载
     */
    public synchronized static void resetArthasClassLoader() {
        attachClassLoader = null;
    }

}
