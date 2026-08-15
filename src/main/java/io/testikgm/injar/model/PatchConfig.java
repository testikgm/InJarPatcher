package io.testikgm.injar.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class PatchConfig {

    @JsonProperty("rules")
    private List<InjectionRule> rules = new ArrayList<>();

    public PatchConfig() {
    }

    public PatchConfig(List<InjectionRule> rules) {
        this.rules = rules;
    }

    public List<InjectionRule> getRules() {
        return rules;
    }

    public void setRules(List<InjectionRule> rules) {
        this.rules = rules;
    }
}
