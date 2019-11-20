package com.attach.core.advice;


import com.attach.core.command.impl.TraceCommand;
import com.attach.core.system.Session;
import com.attach.core.util.ThreadLocalWatch;

/**
 * @author ralf0131 2017-01-06 16:02.
 */
public class AbstractTraceAdviceListener extends ReflectAdviceListenerAdapter {

    protected final ThreadLocalWatch threadLocalWatch = new ThreadLocalWatch();
    protected TraceCommand command;
    protected Session session;
    private int requestId;

    protected final ThreadLocal<TraceEntity> threadBoundEntity = new ThreadLocal<TraceEntity>() {

        @Override
        protected TraceEntity initialValue() {
            return new TraceEntity();
        }
    };

    /**
     * Constructor
     */
    public AbstractTraceAdviceListener(TraceCommand command, Session session) {
        this.command = command;
        this.session = session;
    }

    @Override
    public void destroy() {
        threadBoundEntity.remove();
    }

    @Override
    public void before(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args)
            throws Throwable {
        threadBoundEntity.get().view.begin(clazz.getName() + ":" + method.getName() + "()");
        threadBoundEntity.get().deep++;
        // 开始计算本次方法调用耗时
        threadLocalWatch.start();
    }

    @Override
    public void afterReturning(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args,
                               Object returnObject) throws Throwable {
        threadBoundEntity.get().view.end();
        final Advice advice = Advice.newForAfterRetuning(loader, clazz, method, target, args, returnObject);
        finishing(advice);
    }

    @Override
    public void afterThrowing(ClassLoader loader, Class<?> clazz, ArthasMethod method, Object target, Object[] args,
                              Throwable throwable) throws Throwable {
        int lineNumber = throwable.getStackTrace()[0].getLineNumber();
        threadBoundEntity.get().view.begin("throw:" + throwable.getClass().getName() + "()" + " #" + lineNumber).end().end();
        final Advice advice = Advice.newForAfterThrowing(loader, clazz, method, target, args, throwable);
        finishing(advice);
    }

    public TraceCommand getCommand() {
        return command;
    }


    // TODO
    private void finishing(Advice advice) {
        // 本次调用的耗时
        double cost = threadLocalWatch.costInMillis();
        if (--threadBoundEntity.get().deep == 0) {
            try {
                if (isConditionMet(command.getConditionExpress(), advice, cost)) {
                    // 满足输出条件
                    if (isLimitExceeded(command.getNumberOfLimit(), session.times().get())) {
                        // TODO: concurrency issue to abort session
                        abortProcess(session, command.getNumberOfLimit(), requestId);
                    } else {
                        session.times().incrementAndGet();
                        // TODO: concurrency issues for session.write
                        session.writeStr(requestId, threadBoundEntity.get().view.draw() + "\n");
                    }
                }
            } catch (Throwable e) {
                //LogUtil.getArthasLogger().warn("trace failed.", e);
                session.writeStr(requestId, "trace failed, condition is: " + command.getConditionExpress() + ", " + e.getMessage()
                        + ", visit  for more details.\n");
//                session.end();
            } finally {
                threadBoundEntity.remove();
            }
        }
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getRequestId() {
        return requestId;
    }
}
