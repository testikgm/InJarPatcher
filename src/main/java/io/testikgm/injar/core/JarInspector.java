package io.testikgm.injar.core;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarInspector {

    public List<String> listClasses(File jarFile) throws Exception {
        List<String> classes = new ArrayList<>();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class") && !name.contains("$")) {
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    classes.add(className);
                }
            }
        }
        classes.sort(String::compareTo);
        return classes;
    }

    public List<String> listMethods(File jarFile, String className) throws Exception {
        List<String> methodNames = new ArrayList<>();
        ClassPool pool = new ClassPool(true);
        pool.appendClassPath(jarFile.getAbsolutePath());

        CtClass ctClass = pool.get(className);
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            int modifiers = method.getModifiers();
            if (Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers)) {
                continue;
            }
            methodNames.add(method.getName());
        }
        methodNames.sort(String::compareTo);
        return methodNames;
    }
}
