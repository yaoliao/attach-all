package com.attach.core.command.impl;

import com.attach.core.advice.Enhancer;
import com.attach.core.command.AbstractCommand;
import com.attach.core.command.SaveClassTransformer;
import com.attach.core.system.Session;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.*;

/**
 * @author: TuGai
 * @createTime: 2019-07-23 23:14
 **/
public class RedefineCommand extends AbstractCommand {


    /**
     * 类文件的地址
     */
    private List<String> classPath;

    /**
     * 是否重置类
     */
    private boolean reset = false;

    @Override
    public void process(Session session) {
        Instrumentation insn = session.getInstrumentation();

        if (reset) {
            try {
                insn.redefineClasses(SaveClassTransformer.getDefaultClass().values().toArray(new ClassDefinition[0]));
                session.writeStr(getRequestId(), "redefine reset success, size: " + SaveClassTransformer.getDefaultClass().values().size());
                return;
            } catch (Throwable t) {
                session.writeStr(getRequestId(), "redefine reset 失败, " + t.getMessage());
            }
        }

        for (String s : classPath) {
            File file = new File(s);

            if (!file.exists()) {
                session.writeStr(getRequestId(), "类路径有误, not exist");
                return;
            }

            if (!file.isFile()) {
                session.writeStr(getRequestId(), "类路径有误, not a file");
                return;
            }
        }

        Map<String, byte[]> map = new HashMap<>();

        for (String path : classPath) {
            try (RandomAccessFile f = new RandomAccessFile(path, "r")) {
                byte[] bytes = new byte[(int) f.length()];
                f.readFully(bytes);
                String className = new ClassReader(bytes).getClassName().replace("/", ".");
                map.put(className, bytes);
            } catch (Throwable t) {
                session.writeStr(getRequestId(), "Redefine 发生异常：" + t.getMessage());
                break;
            }
        }

        if (map.keySet().size() != classPath.size()) {
            session.writeStr(getRequestId(), "含有重复的类！！");
            return;
        }

        List<ClassDefinition> list = new ArrayList<>();
        Set<Class<?>> clazzSet = new HashSet<>();
        for (Class<?> loadedClass : insn.getAllLoadedClasses()) {
            if (map.keySet().contains(loadedClass.getName())) {
                clazzSet.add(loadedClass);
                ClassDefinition definition = new ClassDefinition(loadedClass, map.get(loadedClass.getName()));
                list.add(definition);
            }
        }

        // 保存原始类
        try {
            SaveClassTransformer transformer = new SaveClassTransformer(clazzSet);
            Enhancer.enhance(insn, transformer, clazzSet);
        } catch (Throwable t) {
            session.writeStr(getRequestId(), "redefine 保存原始类信息 发生异常：" + t.getMessage());
        }

        try {
            insn.redefineClasses(list.toArray(new ClassDefinition[0]));
            session.writeStr(getRequestId(), "redefine success, size: " + list.size() + "\n");
        } catch (Throwable t) {
            session.writeStr(getRequestId(), "Redefine 发生异常：" + t.getMessage());
        }

        session.end();
    }

    @Override
    public void parse(Map<String, String> parameter) {
        String s = parameter.get("classPath");
        if (s != null && s.length() > 0) {
            String[] paths = s.split("_");
            classPath = Arrays.asList(paths);
            System.out.println("需要转换的class path: " + Arrays.toString(paths));
        }

        String resetStr = parameter.get("reset");
        reset = Boolean.valueOf(resetStr);

    }

    /**
     * 获取原来类的信息（用于reset）
     *
     * @param clazz
     * @return
     * @throws Throwable
     */
    private byte[] getClassByte(Class<?> clazz) throws Throwable {

        /**
         * 如果在jar包中的类文件，用 clazz.getClassLoader().getResource("") 会返回null
         * 需要使用  clazz.getResourceAsStream() 或 clazz.getResource()
         */
        try (InputStream is = clazz.getResourceAsStream(clazz.getSimpleName() + ".class")) {
            byte[] bytes = new byte[is.available()];
            int count = is.read(bytes);
            return bytes;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
