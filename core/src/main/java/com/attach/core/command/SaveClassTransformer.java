package com.attach.core.command;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: TuGai
 * @createTime: 2019-07-24 01:30
 **/
public class SaveClassTransformer implements ClassFileTransformer {

    private static final Map<String, ClassDefinition> DEFAULT_CLASS = new ConcurrentHashMap<>();

    private Set<Class<?>> clazzs;

    public SaveClassTransformer(Set<Class<?>> clazzs) {
        this.clazzs = clazzs;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        if (clazzs.contains(classBeingRedefined) && !DEFAULT_CLASS.keySet().contains(className)) {
            DEFAULT_CLASS.putIfAbsent(className, new ClassDefinition(classBeingRedefined, classfileBuffer));
        }
        return null;
    }

    public static Map<String, ClassDefinition> getDefaultClass() {
        return DEFAULT_CLASS;
    }
}
