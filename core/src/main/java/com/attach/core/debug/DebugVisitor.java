package com.attach.core.debug;

import com.attach.core.command.impl.breakpoint.ClassField;
import com.attach.core.command.impl.breakpoint.ClassMetadata;
import com.attach.core.command.impl.breakpoint.LocalVariable;
import com.attach.core.system.Session;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: TuGai
 * @createTime: 2020-05-03 21:25
 **/
public class DebugVisitor extends ClassVisitor implements Opcodes {

    private final static Map<Integer/*requestId*/, Session> SESSIONS
            = new ConcurrentHashMap<Integer, Session>();

    private static final String SPY_NAME = "com/attach/agent/Spy";

    private static final String DEFAULT_DEBUG_ADDKV_DESC = "(Ljava/lang/String;Ljava/lang/Object;)V";
    private static final String THROW_DESC = "(Ljava/lang/Throwable;)V";
    private static final String INTEGER_DESC = "(Ljava/lang/Integer;)V";

    private final ClassMetadata classMetadata;
    private int line;
    private String condition;

    private String className;
    private int requestId;

    public DebugVisitor(ClassVisitor classVisitor, ClassMetadata classMetadata, int line, String condition, int requestId) {
        super(Opcodes.ASM7, classVisitor);
        this.classMetadata = classMetadata;
        this.line = line;
        this.condition = condition;
        this.requestId = requestId;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        return new DebugMethodVisitor(api, methodVisitor, access, name, descriptor,
                classMetadata, line, className, requestId);
    }

    public static void reg(Integer request, Session session) {
        SESSIONS.putIfAbsent(request, session);
    }

    public static Session get(Integer request) {
        return SESSIONS.get(request);
    }

    public static void clear(Integer request) {
        SESSIONS.remove(request);
    }

    private class DebugMethodVisitor extends AdviceAdapter {

        private final ClassMetadata classMetadata;
        private int debugLine;
        private String methodId;
        private String className;
        private Integer requestId;

        private boolean hasChange;

        protected DebugMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor,
                                     ClassMetadata classMetadata, int line, String className, Integer requestId) {
            super(api, methodVisitor, access, name, descriptor);
            this.classMetadata = classMetadata;
            this.debugLine = line;
            this.methodId = name + descriptor;
            this.className = className;
            this.requestId = requestId;
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            super.visitLineNumber(line, start);

            if (line == debugLine) {
                hasChange = true;

                // 1、保存 变量
                captureSnapshot(line);

                // 2、输出
                printEnd();
            }
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            if (hasChange) {
                super.visitMaxs(Math.max(maxStack, 3), maxLocals);
            } else {
                super.visitMaxs(maxStack, maxLocals);
                hasChange = false;
            }
        }

        private void captureSnapshot(int line) {
            addLocals(line);
            addStaticFields();
            addFields();
            addStack();
        }


        private void addFields() {
            List<ClassField> fields = classMetadata.getFields();
            for (ClassField field : fields) {
                String name = field.getName();
                String desc = field.getDesc();
                mv.visitLdcInsn(name);
                mv.visitVarInsn(ALOAD, 0); // 拿到 this 的引用
                mv.visitFieldInsn(GETFIELD, className, name, desc);
                boxingIfShould(desc);
                mv.visitMethodInsn(INVOKESTATIC, SPY_NAME,
                        "putField", DEFAULT_DEBUG_ADDKV_DESC, false);
            }
        }

        private void addStaticFields() {
            List<ClassField> staticFields = classMetadata.getStaticFields();
            for (ClassField staticField : staticFields) {
                String name = staticField.getName();
                String desc = staticField.getDesc();
                mv.visitLdcInsn(name);
//                mv.visitLdcInsn(desc);
                mv.visitFieldInsn(GETSTATIC, className, name, desc);
                boxingIfShould(desc);
                mv.visitMethodInsn(INVOKESTATIC, SPY_NAME,
                        "putStaticField", DEFAULT_DEBUG_ADDKV_DESC, false);
            }

        }

        private void addLocals(int line) {
            List<LocalVariable> localVariables = classMetadata.getVariables().get(methodId);
            for (LocalVariable localVariable : localVariables) {
                if (line >= localVariable.getStart() && line < localVariable.getEnd()) {
                    mv.visitLdcInsn(localVariable.getName());
                    mv.visitVarInsn(Type.getType(localVariable.getDesc()).getOpcode(ILOAD), localVariable.getIndex());
                    boxingIfShould(localVariable.getDesc());
                    mv.visitMethodInsn(INVOKESTATIC, SPY_NAME,
                            "putLocalVariable", DEFAULT_DEBUG_ADDKV_DESC, false);
                }
            }
        }

        private void addStack() {
            mv.visitTypeInsn(NEW, "java/lang/Throwable");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Throwable", "<init>", "()V", false);
            mv.visitMethodInsn(INVOKESTATIC, SPY_NAME,
                    "fillStacktrace", THROW_DESC, false);
        }

        private void printEnd() {
            push(requestId);
            boxingIfShould(Type.getType(int.class).getDescriptor());
            mv.visitMethodInsn(INVOKESTATIC, SPY_NAME,
                    "debugEnd", INTEGER_DESC, false);
        }


        private void boxingIfShould(final String desc) {
            switch (Type.getType(desc).getSort()) {
                case Type.BOOLEAN:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                    break;
                case Type.BYTE:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
                    break;
                case Type.CHAR:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
                    break;
                case Type.SHORT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
                    break;
                case Type.INT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                    break;
                case Type.FLOAT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                    break;
                case Type.LONG:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                    break;
                case Type.DOUBLE:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                    break;
            }
        }

    }
}
