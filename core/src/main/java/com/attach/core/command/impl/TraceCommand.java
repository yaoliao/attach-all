package com.attach.core.command.impl;

import com.attach.core.advice.AdviceListener;
import com.attach.core.advice.TraceAdviceListener;
import com.attach.core.system.Session;
import com.attach.core.util.SearchUtils;
import com.attach.core.util.matcher.Matcher;

import java.util.List;
import java.util.Map;

/**
 * @author: TuGai
 * @createTime: 2019-07-21 01:05
 **/
public class TraceCommand extends EnhancerCommand {

    private String classPattern;
    private String methodPattern;
    private String conditionExpress;
    private boolean isRegEx = false;
    private int numberOfLimit = 10;
    private List<String> pathPatterns;
    private boolean skipJDKTrace;

    @Override
    public void parse(Map<String, String> parameter) {
        this.classPattern = parameter.get("classPattern");
        this.methodPattern = parameter.get("methodPattern");
    }

    /**
     * 类名匹配
     *
     * @return 获取类名匹配
     */
    @Override
    protected Matcher getClassNameMatcher() {
        return classNameMatcher = SearchUtils.classNameMatcher(getClassPattern(), isRegEx());
    }

    /**
     * 方法名匹配
     *
     * @return 获取方法名匹配
     */
    @Override
    protected Matcher getMethodNameMatcher() {
        return methodNameMatcher = SearchUtils.classNameMatcher(getMethodPattern(), isRegEx());
    }

    /**
     * 获取监听器
     *
     * @param session
     * @return 返回监听器
     */
    @Override
    protected AdviceListener getAdviceListener(Session session) {
        return new TraceAdviceListener(this, session);
    }

    public String getClassPattern() {
        return classPattern;
    }

    public String getMethodPattern() {
        return methodPattern;
    }

    public boolean isRegEx() {
        return isRegEx;
    }

    public boolean isSkipJDKTrace() {
        return skipJDKTrace;
    }

    public String getConditionExpress() {
        return conditionExpress;
    }

    public int getNumberOfLimit() {
        return numberOfLimit;
    }
}
