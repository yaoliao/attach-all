package com.attach.core.command;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author: TuGai
 * @createTime: 2019-07-25 00:47
 **/
public class ClassDumpTransformer implements ClassFileTransformer {

    private Set<String> classSet;

    private Map<String, byte[]> map;

    private Map<String, Path> pathMap;

    private String dirPath = System.getProperty("user.home") + File.separator + "logs" + File.separator + "attach";

    public ClassDumpTransformer(Set<String> classSet) {
        this.classSet = classSet;
        this.map = new HashMap<>();
        this.pathMap = new HashMap<>();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        String classStr = className.replaceAll("/", ".");
        if (classSet != null && classSet.contains(classStr)) {
            map.putIfAbsent(className, classfileBuffer);
        }
        if (map != null && !map.isEmpty()) {
            try {
                Path path = Paths.get(dirPath);
                createDir(path);
                for (Map.Entry<String, byte[]> entry : map.entrySet()) {
                    String k = entry.getKey();
                    byte[] v = entry.getValue();
                    String fileName = dirPath + File.separator + k + ".class";
                    createDir(Paths.get(fileName.substring(0, fileName.lastIndexOf(File.separator))));
                    Path classPath = Paths.get(fileName);
                    Files.deleteIfExists(classPath);
                    Files.write(classPath, v);
                    pathMap.putIfAbsent(k, classPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Map<String, byte[]> getMap() {
        return map;
    }

    public Map<String, Path> getPathMap() {
        return pathMap;
    }

    private void createDir(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
    }
}
