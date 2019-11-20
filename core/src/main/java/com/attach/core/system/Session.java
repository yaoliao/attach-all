package com.attach.core.system;

import com.alibaba.fastjson.JSON;
import com.attach.core.advice.AdviceListener;
import com.attach.core.advice.AdviceWeaver;
import com.attach.core.model.Message;
import com.attach.core.model.Response;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: tuGai
 * @createTime: 2019-07-14 22:38
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    private int javaPid;

    private int sessionId;

    private Channel channel;

    private Instrumentation instrumentation;

    private AtomicInteger times;


    private static final ExecutorService responseExecutors = new ThreadPoolExecutor(2, 4,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(100),
            new ThreadFactory() {
                AtomicInteger ai = new AtomicInteger(0);


                public Thread newThread(Runnable r) {
                    final Thread t = new Thread(r, "java.attach-command-execute-daemon-" + ai.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                }
            });


    public void writeMessage(Message message) {
        // TODO 到时候换线程池 先直接发送
        String msgStr = JSON.toJSONString(message.getResponse());
        message.getSession().channel.writeAndFlush(msgStr);
    }

    public void writeStr(int requestId, String msg) {
        Response response = Response.builder().requestId(requestId).str(msg).build();
        String msgStr = JSON.toJSONString(response);
        this.getChannel().writeAndFlush(msgStr);
    }

    // =========================

    private final static AtomicInteger lockSequence = new AtomicInteger();
    private final static int LOCK_TX_EMPTY = -1;
    private final AtomicInteger lock = new AtomicInteger(LOCK_TX_EMPTY);

    private Map<String, Object> data = new HashMap<String, Object>();


    public Session put(String key, Object obj) {
        if (obj == null) {
            data.remove(key);
        } else {
            data.put(key, obj);
        }
        return this;
    }


    public <T> T get(String key) {
        return (T) data.get(key);
    }


    public <T> T remove(String key) {
        return (T) data.remove(key);
    }


    public boolean tryLock() {
        return lock.compareAndSet(LOCK_TX_EMPTY, lockSequence.getAndIncrement());
    }


    public void unLock() {
        int currentLockTx = lock.get();
        if (!lock.compareAndSet(currentLockTx, LOCK_TX_EMPTY)) {
            throw new IllegalStateException();
        }
    }


    public boolean isLocked() {
        return lock.get() != LOCK_TX_EMPTY;
    }


    public int getLock() {
        return lock.get();
    }

    public void register(int lock, AdviceListener listener) {
        AdviceWeaver.reg(lock, listener);
    }

    public AtomicInteger times() {
        return times;
    }

    public void end() {
        AdviceWeaver.unReg(lock.get());
    }
}
