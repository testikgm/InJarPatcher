package io.testikgm.injar;

import io.testikgm.injar.cli.InteractivePatcher;
import io.testikgm.injar.config.ConfigLoader;
import io.testikgm.injar.core.JarInspector;
import io.testikgm.injar.core.JarModifier;
import io.testikgm.injar.model.InjectionMode;
import io.testikgm.injar.model.InjectionRule;
import io.testikgm.injar.model.PatchConfig;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
    name = "injar-patcher",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "Консольная утилита для изменения байткода и методов в JAR файлах"
)
public class InJarApp implements Callable<Integer> {

    @Option(names = {"-i", "--input"}, required = true, description = "Path to source input JAR file")
    private File inputJar;

    @Option(names = {"-o", "--output"}, description = "Path to result output JAR file")
    private File outputJar;

    @Option(names = {"--config"}, description = "Path to JSON config with injection rules")
    private File configFile;

    @Option(names = {"--interactive"}, description = "Run in interactive wizard mode to select class and method")
    private boolean interactive;

    @Option(names = {"--list"}, description = "List all available classes inside the JAR")
    private boolean listOnly;

    @Option(names = {"-c", "--class"}, description = "Target class name (e.g. com.example.MyPlugin)")
    private String targetClass;

    @Option(names = {"-m", "--method"}, description = "Target method name (e.g. onEnable)")
    private String targetMethod;

    @Option(names = {"--desc"}, description = "Optional method descriptor / signature")
    private String methodDescriptor;

    @Option(names = {"--mode"}, description = "Mode: REPLACE_BODY, INSERT_BEFORE, INSERT_AFTER (default: REPLACE_BODY)")
    private InjectionMode mode = InjectionMode.REPLACE_BODY;

    @Option(names = {"-b", "--body"}, description = "Java source code block to inject (e.g. '{ System.out.println(\"Hi\"); }')")
    private String codeBody;

    @Override
    public Integer call() {
        try {
            if (inputJar == null || !inputJar.exists()) {
                System.err.println("[!] Error: Input JAR file does not exist: " + inputJar);
                return 1;
            }

            if (listOnly) {
                JarInspector inspector = new JarInspector();
                List<String> classes = inspector.listClasses(inputJar);
                System.out.println("Classes in " + inputJar.getName() + " (" + classes.size() + "):");
                for (String cls : classes) {
                    System.out.println("  - " + cls);
                }
                return 0;
            }

            if (interactive) {
                if (outputJar == null) {
                    String baseName = inputJar.getName();
                    String outName = baseName.endsWith(".jar")
                            ? baseName.substring(0, baseName.length() - 4) + "_patched.jar"
                            : baseName + "_patched.jar";
                    outputJar = new File(inputJar.getParentFile(), outName);
                }
                new InteractivePatcher().start(inputJar, outputJar);
                return 0;
            }

            if (outputJar == null) {
                System.err.println("[!] Error: Output JAR path (-o, --output) is required when not in --list mode.");
                return 1;
            }

            List<InjectionRule> rules = new ArrayList<>();

            if (configFile != null) {
                ConfigLoader configLoader = new ConfigLoader();
                PatchConfig config = configLoader.loadConfig(configFile);
                if (config.getRules() != null) {
                    rules.addAll(config.getRules());
                }
            }

            if (targetClass != null && targetMethod != null && codeBody != null) {
                InjectionRule directRule = new InjectionRule(targetClass, targetMethod, codeBody, mode);
                directRule.setMethodDescriptor(methodDescriptor);
                rules.add(directRule);
            }

            if (rules.isEmpty()) {
                System.err.println("[!] Error: No injection rules specified. Provide --config, --interactive or -c/-m/-b arguments.");
                return 1;
            }

            System.out.println("[*] Processing JAR: " + inputJar.getName() + " -> " + outputJar.getName());
            System.out.println("[*] Total rules to apply: " + rules.size());

            JarModifier modifier = new JarModifier();
            modifier.process(inputJar, outputJar, rules);

            System.out.println("[✓] Successfully completed! Patched JAR saved to: " + outputJar.getAbsolutePath());
            return 0;

        } catch (Exception e) {
            System.err.println("[!] Execution failed: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new InJarApp()).execute(args);
        System.exit(exitCode);
    }
}
