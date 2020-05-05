package com.attach.core.command.impl.breakpoint;

import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: TuGai
 * @createTime: 2020-05-03 16:02
 **/
public class ClassMetadata {

    private final List<ClassField> fields;
    private final List<ClassField> staticFields;
    private final Map<String, List<LocalVariable>> variables;

    public ClassMetadata() {
        this.fields = new ArrayList<>();
        this.staticFields = new ArrayList<>();
        this.variables = new HashMap<>();
    }

    public void addField(final ClassField field) {
        if (isStaticField(field)) {
            staticFields.add(field);
        } else {
            fields.add(field);
        }
    }

    public void addVariable(final String methodId, final LocalVariable variable) {
        variables.computeIfAbsent(methodId, k -> new ArrayList<>());
        variables.get(methodId).add(variable);
    }

    private boolean isStaticField(final ClassField field) {
        return (field.getAccess() & Opcodes.ACC_STATIC) != 0;
    }

    public List<ClassField> getFields() {
        return fields;
    }

    public List<ClassField> getStaticFields() {
        return staticFields;
    }

    public Map<String, List<LocalVariable>> getVariables() {
        return variables;
    }


}
