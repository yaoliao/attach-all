package com.attach.core.util.matcher;


import java.util.Objects;

/**
 * 字符串全匹配
 * @author ralf0131 2017-01-06 13:18.
 */
public class EqualsMatcher<T> implements Matcher<T> {

    private final T pattern;

    public EqualsMatcher(T pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean matching(T target) {
        return Objects.equals(target, pattern);
    }
}
