package io.testikgm.injar.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.testikgm.injar.model.PatchConfig;

import java.io.File;
import java.io.IOException;

public class ConfigLoader {

    private final ObjectMapper mapper = new ObjectMapper();

    public PatchConfig loadConfig(File configFile) throws IOException {
        if (!configFile.exists()) {
            throw new IllegalArgumentException("Config file not found: " + configFile.getAbsolutePath());
        }
        return mapper.readValue(configFile, PatchConfig.class);
    }
}
