package com.attach.core.command.impl;

import com.attach.core.advice.Enhancer;
import com.attach.core.command.AbstractCommand;
import com.attach.core.command.ClassDumpTransformer;
import com.attach.core.command.Decompiler;
import com.attach.core.system.Session;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 反编译
 *
 * @author: TuGai
 * @createTime: 2019-07-24 20:40
 **/
public class JadCommand extends AbstractCommand {

    private static Pattern pattern = Pattern.compile("(?m)^/\\*\\s*\\*/\\s*$" + System.getProperty("line.separator"));

    private String classPattern;
    private String methodName;
    private String code = null;
    private boolean isRegEx = false;

    @Override
    public void process(Session session) {

        // 先判断是否有多个 classPattern
        Set<Class<?>> targetClazzs = new HashSet<>();
        Instrumentation ins = session.getInstrumentation();
        for (Class<?> loadedClass : ins.getAllLoadedClasses()) {
            if (classPattern.equals(loadedClass.getName())) {
                targetClazzs.add(loadedClass);
            }
        }
        if (targetClazzs.size() == 0) {
            session.writeStr(getRequestId(), "未找到啊，是不是类名错了呀");
            return;
        }
        if (targetClazzs.size() > 1) {
            session.writeStr(getRequestId(), "发现多个同名类");
            return;
        }

        // 找到所有的内部类
        String innerClass = targetClazzs.iterator().next().getName() + "$";
        Set<Class<?>> innerSet = new HashSet<>();
        for (Class<?> loadedClass : ins.getAllLoadedClasses()) {
            if (loadedClass.getName().startsWith(innerClass)) {
                innerSet.add(loadedClass);
            }
        }

        try {

            // 找到类和内部类的byte[]
            innerSet.add(targetClazzs.iterator().next());
            Set<String> classNames = innerSet.stream().map(Class::getName).collect(Collectors.toSet());

            ClassDumpTransformer dumpTransformer = new ClassDumpTransformer(classNames);
            Enhancer.enhance(ins, dumpTransformer, innerSet);
            Map<String, Path> pathMap = dumpTransformer.getPathMap();

            // 进行反编译
            String name = targetClazzs.iterator().next().getName().replace(".", File.separator);
            String source = Decompiler.decompile(pathMap.get(name).toString(), methodName);
            source = pattern.matcher(source).replaceAll("");

            session.writeStr(getRequestId(), "编译后的代码: \n");
            session.writeStr(getRequestId(), source);

        } catch (Throwable t) {
            session.writeStr(getRequestId(), "jad失败：" + t.getMessage());
        }

    }

    @Override
    public void parse(Map<String, String> parameter) {

        classPattern = parameter.get("classPattern");
        methodName = parameter.get("methodName");
        code = parameter.get("code");
        isRegEx = Boolean.valueOf(parameter.get("code"));

    }
}
