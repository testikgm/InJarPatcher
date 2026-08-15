package io.testikgm.injar.core;

import io.testikgm.injar.model.InjectionMode;
import io.testikgm.injar.model.InjectionRule;
import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class BytecodeTransformer {

    private final ClassPool classPool;

    public BytecodeTransformer() {
        this.classPool = new ClassPool(true);
    }

    public BytecodeTransformer(ClassPool basePool) {
        this.classPool = basePool;
    }

    public byte[] transformClass(String className, byte[] originalBytecode, List<InjectionRule> rules) throws Exception {
        classPool.insertClassPath(new ByteArrayClassPath(className, originalBytecode));

        try (InputStream in = new ByteArrayInputStream(originalBytecode)) {
            CtClass ctClass = classPool.makeClass(in, false);
            ctClass.defrost();

            for (InjectionRule rule : rules) {
                applyRule(ctClass, rule);
            }

            return ctClass.toBytecode();
        }
    }

    private void applyRule(CtClass ctClass, InjectionRule rule) throws Exception {
        if (rule.getExtraImports() != null) {
            for (String importPkg : rule.getExtraImports()) {
                classPool.importPackage(importPkg);
            }
        }

        CtMethod targetMethod = findMethod(ctClass, rule.getTargetMethod(), rule.getMethodDescriptor());
        if (targetMethod == null) {
            throw new IllegalArgumentException(
                    "Method '" + rule.getTargetMethod() + "' not found in class " + ctClass.getName()
            );
        }

        InjectionMode mode = rule.getMode() != null ? rule.getMode() : InjectionMode.REPLACE_BODY;

        switch (mode) {
            case REPLACE_BODY -> targetMethod.setBody(rule.getCode());
            case INSERT_BEFORE -> targetMethod.insertBefore(rule.getCode());
            case INSERT_AFTER -> targetMethod.insertAfter(rule.getCode());
        }
    }

    private CtMethod findMethod(CtClass ctClass, String methodName, String descriptor) {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (descriptor == null || descriptor.isBlank() || method.getSignature().equals(descriptor)) {
                return method;
            }
        }
        return null;
    }
}
