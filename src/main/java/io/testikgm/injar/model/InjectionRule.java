package io.testikgm.injar.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class InjectionRule {

    @JsonProperty("targetClass")
    private String targetClass;

    @JsonProperty("targetMethod")
    private String targetMethod;

    @JsonProperty("methodDescriptor")
    private String methodDescriptor;

    @JsonProperty("mode")
    private InjectionMode mode = InjectionMode.REPLACE_BODY;

    @JsonProperty("code")
    private String code;

    @JsonProperty("extraImports")
    private List<String> extraImports;

    public InjectionRule() {
    }

    public InjectionRule(String targetClass, String targetMethod, String code, InjectionMode mode) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.code = code;
        this.mode = mode != null ? mode : InjectionMode.REPLACE_BODY;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getMethodDescriptor() {
        return methodDescriptor;
    }

    public void setMethodDescriptor(String methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }

    public InjectionMode getMode() {
        return mode;
    }

    public void setMode(InjectionMode mode) {
        this.mode = mode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<String> getExtraImports() {
        return extraImports;
    }

    public void setExtraImports(List<String> extraImports) {
        this.extraImports = extraImports;
    }
}
