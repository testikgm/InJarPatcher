package io.testikgm.injar.cli;

import io.testikgm.injar.core.JarInspector;
import io.testikgm.injar.core.JarModifier;
import io.testikgm.injar.model.InjectionMode;
import io.testikgm.injar.model.InjectionRule;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class InteractivePatcher {

    private final Scanner scanner = new Scanner(System.in);
    private final JarInspector inspector = new JarInspector();
    private final JarModifier modifier = new JarModifier();

    public void start(File inputJar, File outputJar) {
        try {
            System.out.println("\n=== InJarPatcher: Interactive Mode ===");
            System.out.println("Reading JAR: " + inputJar.getName());

            List<String> classes = inspector.listClasses(inputJar);
            if (classes.isEmpty()) {
                System.out.println("[!] No classes found in the specified JAR archive.");
                return;
            }

            System.out.println("\nAvailable classes (" + classes.size() + "):");
            for (int i = 0; i < classes.size(); i++) {
                System.out.printf("[%d] %s%n", i + 1, classes.get(i));
            }

            int classIdx = promptIndex("Select class number to patch (1-" + classes.size() + "): ", 1, classes.size());
            String selectedClass = classes.get(classIdx - 1);

            List<String> methods = inspector.listMethods(inputJar, selectedClass);
            if (methods.isEmpty()) {
                System.out.println("[!] No modifiable methods found in class " + selectedClass);
                return;
            }

            System.out.println("\nMethods in " + selectedClass + ":");
            for (int i = 0; i < methods.size(); i++) {
                System.out.printf("[%d] %s()%n", i + 1, methods.get(i));
            }

            int methodIdx = promptIndex("Select method number to patch (1-" + methods.size() + "): ", 1, methods.size());
            String selectedMethod = methods.get(methodIdx - 1);

            System.out.println("\nInjection modes:");
            System.out.println("[1] INSERT_BEFORE (Execute code before method)");
            System.out.println("[2] INSERT_AFTER  (Execute code after method)");
            System.out.println("[3] REPLACE_BODY  (Completely replace method body)");

            int modeIdx = promptIndex("Select injection mode (1-3): ", 1, 3);
            InjectionMode mode = switch (modeIdx) {
                case 1 -> InjectionMode.INSERT_BEFORE;
                case 2 -> InjectionMode.INSERT_AFTER;
                default -> InjectionMode.REPLACE_BODY;
            };

            System.out.println("\nEnter Java code block to inject (enclose in { ... }):");
            System.out.println("Example: { System.out.println(\"Injected code!\"); }");
            System.out.print("> ");
            String code = scanner.nextLine().trim();

            if (!code.startsWith("{")) {
                code = "{ " + code + " }";
            }

            InjectionRule rule = new InjectionRule(selectedClass, selectedMethod, code, mode);
            System.out.println("\n[*] Applying patch...");
            modifier.process(inputJar, outputJar, Collections.singletonList(rule));

            System.out.println("[✓] Done! Successfully created patched JAR: " + outputJar.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("[!] Interactive session error: " + e.getMessage());
        }
    }

    private int promptIndex(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Invalid input. Please enter a number between " + min + " and " + max + ".");
        }
    }
}
