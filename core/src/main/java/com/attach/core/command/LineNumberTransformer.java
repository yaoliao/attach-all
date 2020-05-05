package com.attach.core.command;

import com.attach.core.command.impl.breakpoint.ClassField;
import com.attach.core.command.impl.breakpoint.ClassMetadata;
import com.attach.core.command.impl.breakpoint.LocalVariable;
import com.attach.core.debug.DebugVisitor;
import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: TuGai
 * @createTime: 2020-05-03 15:56
 **/
public class LineNumberTransformer implements ClassFileTransformer {

    private String clazzName;

    private int line;

    private String condition;

    private int requestId;

    public LineNumberTransformer(String clazzName, int line, String condition, int requestId) {
        this.clazzName = clazzName;
        this.line = line;
        this.condition = condition;
        this.requestId = requestId;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        String classStr = className.replaceAll("/", ".");
        if (!classStr.equals(clazzName)) return classfileBuffer;

        // 获取行号、变量等信息
        ClassMetadata classMetadata = new ClassMetadata();
        ClassReader classReader = new ClassReader(classfileBuffer);
        MetadataCollector metadataCollector = new MetadataCollector(classMetadata);
        TraceClassVisitor traceClassVisitor1 = new TraceClassVisitor(metadataCollector, new PrintWriter(System.out));
        classReader.accept(traceClassVisitor1, ClassReader.SKIP_FRAMES);
        byte[] bytes = null;

        try {
            //增强对应字节码
            classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            TraceClassVisitor traceClassVisitor = new TraceClassVisitor(classWriter, new PrintWriter(System.out));
            CheckClassAdapter checkClassAdapter = new CheckClassAdapter(traceClassVisitor, true);
            DebugVisitor debugVisitor = new DebugVisitor(checkClassAdapter, classMetadata, line, condition, requestId);
            classReader.accept(debugVisitor, ClassReader.EXPAND_FRAMES);
            bytes = classWriter.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return bytes;
    }


    class MetadataCollector extends ClassVisitor {

        private static final int ASM_VERSION = Opcodes.ASM7;
        private final ClassMetadata classMetadata;

        public MetadataCollector(ClassMetadata classMetadata) {
            super(ASM_VERSION);
            this.classMetadata = classMetadata;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            ClassField classField = new ClassField(access, name, descriptor);
            classMetadata.addField(classField);

            return super.visitField(access, name, descriptor, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String methodName, String methodDes, String signature, String[] exceptions) {

            MethodVisitor methodVisitor = super.visitMethod(access, methodName, methodDes, signature, exceptions);

            return new MethodVisitor(ASM_VERSION, methodVisitor) {

                Map<Label, Integer> lineMap = new HashMap<>();

                @Override
                public void visitLineNumber(int line, Label start) {
                    super.visitLineNumber(line, start);
                    lineMap.put(start, line);
                }

                @Override
                public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                    super.visitLocalVariable(name, descriptor, signature, start, end, index);
                    LocalVariable localVariable = new LocalVariable(name, descriptor, getLine(start), getLine(end), index);
                    classMetadata.addVariable(methodName + methodDes, localVariable);
                }

                private int getLine(Label label) {
                    return lineMap.get(label) == null ? Integer.MAX_VALUE : lineMap.get(label);
                }
            };
        }
    }
}
