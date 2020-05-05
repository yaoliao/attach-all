package com.attach.core.command.impl.breakpoint;

import com.attach.core.advice.Enhancer;
import com.attach.core.command.AbstractCommand;
import com.attach.core.command.LineNumberTransformer;
import com.attach.core.debug.DebugVisitor;
import com.attach.core.system.Session;
import com.attach.core.util.SearchUtils;
import com.attach.core.util.matcher.Matcher;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author: TuGai
 * @createTime: 2020-05-03 15:30
 **/
public class BreakPointCommand extends AbstractCommand {

    private String className;

    private int line;

    private String condition;

    @Override
    public void process(Session session) {

        Instrumentation inst = session.getInstrumentation();
        Matcher<String> classMatcher = SearchUtils.classNameMatcher(className, false);
        Set<Class<?>> classes = SearchUtils.searchClass(inst, classMatcher);
        Class<?> clazz = classes.iterator().next();

        // 保存 session
        DebugVisitor.reg(getRequestId(), session);

        try {
            LineNumberTransformer transformer = new LineNumberTransformer(className, line, condition, getRequestId());
            Enhancer.enhance(inst, transformer, Collections.singleton(clazz));
            session.writeStr(getRequestId(), "BreakPointCommand 成功，开始 debug");
        } catch (Throwable t) {
            session.writeStr(getRequestId(), "BreakPointCommand失败：" + t.getMessage());
        }

    }

    @Override
    public void parse(Map<String, String> parameter) {
        this.className = parameter.get("className");
        this.line = Integer.valueOf(parameter.get("line"));
        this.condition = parameter.get("condition");
    }
}
