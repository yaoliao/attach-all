package com.attach.core.command.impl;

import com.attach.core.command.AbstractCommand;
import com.attach.core.mc.DynamicCompiler;
import com.attach.core.system.Session;
import com.attach.core.util.affect.RowAffect;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * 内存编译
 *
 * @author: TuGai
 * @createTime: 2019-07-24 11:51
 **/
public class MemoryCompilerCommand extends AbstractCommand {

    private List<String> classPath;

    private String dirPath;

    @Override
    public void process(Session session) {

        RowAffect affect = new RowAffect();

        try {
            DynamicCompiler compiler = new DynamicCompiler(ClassLoader.getSystemClassLoader());

            for (String path : classPath) {
                byte[] bytes = Files.readAllBytes(Paths.get(path));
                String source = new String(bytes, Charset.defaultCharset());
                String name = Paths.get(path).getFileName().toString();
                if (name.endsWith(".java")) {
                    name = name.substring(0, name.length() - ".java".length());
                }
                compiler.addSource(name, source);
            }

            Map<String, byte[]> map = compiler.buildByteCodes();

            session.writeStr(getRequestId(), "Memory compiler output:\n");
            map.forEach((k, v) -> {
                session.writeStr(getRequestId(), k + "\n");
                //session.writeStr(getRequestId(), new String(v) + "\n"); // 直接返回会乱码

                String Str = new String(Base64.getEncoder().encode(v), Charset.defaultCharset());
                session.writeStr(getRequestId(), "Base64 编码后内容" + "\n");
                session.writeStr(getRequestId(), Str + "\n");
                affect.rCnt(1);
            });

        } catch (Throwable t) {
            session.writeStr(getRequestId(), "啊呀内存编译出错了呀... : " + t.getMessage());
            session.end();
        } finally {
            session.writeStr(getRequestId(), affect + "\n");
            session.end();
        }
    }

    @Override
    public void parse(Map<String, String> parameter) {
        String s = parameter.get("classPath");
        if (s != null && s.length() > 0) {
            String[] paths = s.split("_");
            classPath = Arrays.asList(paths);
        }

        dirPath = parameter.get("dirPath");
    }
}
