package io.testikgm.injar.core;

import io.testikgm.injar.model.InjectionRule;
import javassist.ClassPool;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

public class JarModifier {

    public void process(File inputJar, File outputJar, List<InjectionRule> rules) throws Exception {
        if (!inputJar.exists()) {
            throw new IllegalArgumentException("Input JAR does not exist: " + inputJar.getAbsolutePath());
        }

        Map<String, List<InjectionRule>> rulesByClass = rules.stream()
                .collect(Collectors.groupingBy(InjectionRule::getTargetClass));

        ClassPool classPool = new ClassPool(true);
        classPool.appendClassPath(inputJar.getAbsolutePath());
        BytecodeTransformer transformer = new BytecodeTransformer(classPool);

        File tempOutput = File.createTempFile("injar_patch_", ".jar");

        try (JarFile jarFile = new JarFile(inputJar);
             JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(tempOutput))) {

            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                JarEntry newEntry = new JarEntry(entryName);
                newEntry.setTime(entry.getTime());
                jarOut.putNextEntry(newEntry);

                byte[] entryData = jarFile.getInputStream(entry).readAllBytes();

                if (entryName.endsWith(".class")) {
                    String className = entryName
                            .substring(0, entryName.length() - 6)
                            .replace('/', '.');

                    List<InjectionRule> classRules = rulesByClass.get(className);
                    if (classRules != null && !classRules.isEmpty()) {
                        entryData = transformer.transformClass(className, entryData, classRules);
                        System.out.println("[+] Patched: " + className + " (" + classRules.size() + " rule(s))");
                    }
                }

                jarOut.write(entryData);
                jarOut.closeEntry();
            }
        }

        if (outputJar.getParentFile() != null) {
            outputJar.getParentFile().mkdirs();
        }
        Files.move(tempOutput.toPath(), outputJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
