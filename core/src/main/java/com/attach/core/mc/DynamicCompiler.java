package com.attach.core.mc;

import javax.tools.*;
import java.io.Writer;
import java.util.*;

public class DynamicCompiler {
    private final JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
    private final StandardJavaFileManager standardFileManager;
    private final List<String> options = new ArrayList<String>();
    private final DynamicClassLoader dynamicClassLoader;

    private final Collection<JavaFileObject> compilationUnits = new ArrayList<JavaFileObject>();
    private final List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<Diagnostic<? extends JavaFileObject>>();
    private final List<Diagnostic<? extends JavaFileObject>> warnings = new ArrayList<Diagnostic<? extends JavaFileObject>>();

    private Writer writer;

    public DynamicCompiler(ClassLoader classLoader) {
        this(classLoader, null);
    }

    public DynamicCompiler(ClassLoader classLoader, Writer writer) {
        standardFileManager = javaCompiler.getStandardFileManager(null, null, null);

        options.add("-Xlint:unchecked");
        dynamicClassLoader = new DynamicClassLoader(classLoader);
        this.writer = writer;
    }

    public void addSource(String className, String source) {
        addSource(new StringSource(className, source));
    }

    public void addSource(JavaFileObject javaFileObject) {
        compilationUnits.add(javaFileObject);
    }

    public Map<String, byte[]> buildByteCodes() {

        errors.clear();
        warnings.clear();

        JavaFileManager fileManager = new DynamicJavaFileManager(standardFileManager, dynamicClassLoader);

        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();
        JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fileManager, collector, options, null,
                compilationUnits);

        try {

            if (!compilationUnits.isEmpty()) {
                boolean result = task.call();

                if (!result || collector.getDiagnostics().size() > 0) {

                    for (Diagnostic<? extends JavaFileObject> diagnostic : collector.getDiagnostics()) {
                        switch (diagnostic.getKind()) {
                            case NOTE:
                            case MANDATORY_WARNING:
                            case WARNING:
                                warnings.add(diagnostic);
                                break;
                            case OTHER:
                            case ERROR:
                            default:
                                errors.add(diagnostic);
                                break;
                        }

                    }

                    if (!errors.isEmpty()) {
                        throw new DynamicCompilerException("Compilation Error", errors);
                    }
                }
            }

            return dynamicClassLoader.getByteCodes();
        } catch (ClassFormatError e) {
            throw new DynamicCompilerException(e);
        } finally {
            compilationUnits.clear();

        }

    }

    private List<String> diagnosticToString(List<Diagnostic<? extends JavaFileObject>> diagnostics) {

        List<String> diagnosticMessages = new ArrayList<String>();

        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
            diagnosticMessages.add(
                    "line: " + diagnostic.getLineNumber() + ", message: " + diagnostic.getMessage(Locale.US));
        }

        return diagnosticMessages;

    }

    public List<String> getErrors() {
        return diagnosticToString(errors);
    }

    public List<String> getWarnings() {
        return diagnosticToString(warnings);
    }

    public ClassLoader getClassLoader() {
        return dynamicClassLoader;
    }
}
