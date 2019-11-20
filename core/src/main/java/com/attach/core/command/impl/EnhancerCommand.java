package com.attach.core.command.impl;

import com.attach.core.advice.AbstractTraceAdviceListener;
import com.attach.core.advice.AdviceListener;
import com.attach.core.advice.Enhancer;
import com.attach.core.advice.InvokeTraceable;
import com.attach.core.command.AbstractCommand;
import com.attach.core.system.Session;
import com.attach.core.util.affect.EnhancerAffect;
import com.attach.core.util.matcher.Matcher;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * @author: tuGai
 * @createTime: 2019-07-18 00:01
 **/
public abstract class EnhancerCommand extends AbstractCommand {

    protected Matcher classNameMatcher;
    protected Matcher methodNameMatcher;

    /**
     * 类名匹配
     *
     * @return 获取类名匹配
     */
    protected abstract Matcher getClassNameMatcher();

    /**
     * 方法名匹配
     *
     * @return 获取方法名匹配
     */
    protected abstract Matcher getMethodNameMatcher();

    /**
     * 获取监听器
     *
     * @return 返回监听器
     */
    protected abstract AdviceListener getAdviceListener(Session session);

    @Override
    public void process(Session session) {

        // 执行增强
        enhance(session);
    }


    protected void enhance(Session session) {
        if (!session.tryLock()) {
            session.writeStr(getRequestId(), "someone else is enhancing classes, pls. wait.\n");
            return;
        }

        int lock = session.getLock();
        try {
            Instrumentation inst = session.getInstrumentation();
            AdviceListener listener = getAdviceListener(session);

            if (listener == null) return;

            boolean skipJDKTrace = false;
            if (listener instanceof AbstractTraceAdviceListener) {
                skipJDKTrace = ((AbstractTraceAdviceListener) listener).getCommand().isSkipJDKTrace();
                ((AbstractTraceAdviceListener) listener).setRequestId(getRequestId());
            }

            EnhancerAffect effect = Enhancer.enhance(inst, lock, listener instanceof InvokeTraceable,
                    skipJDKTrace, getClassNameMatcher(), getMethodNameMatcher());

            if (effect.cCnt() == 0 || effect.mCnt() == 0) {
                // no class effected
                // might be method code too large
                session.writeStr(getRequestId(), "No class or method is affected, try:\n"
                        + "1. sm CLASS_NAME METHOD_NAME to make sure the method you are tracing actually exists (it might be in your parent class).\n"
                        + "2. reset CLASS_NAME and try again, your method body might be too large.\n"
                        + "3. check arthas log: \n"
                        + "4. visit https://github.com/alibaba/arthas/issues/47 for more details.\n");
//                process.end();
                return;
            }

            // 这里做个补偿,如果在enhance期间,unLock被调用了,则补偿性放弃
            if (session.getLock() == lock) {
                // 注册通知监听器
                session.register(lock, listener);
//                if (process.isForeground()) {
//                    process.echoTips(Constants.Q_OR_CTRL_C_ABORT_MSG + "\n");
//                }
            }

        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        } finally {
            if (session.getLock() == lock) {
                // enhance结束后解锁
                session.unLock();
            }
        }


    }
}
