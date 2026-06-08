package com.ncslab.config;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;

public class NCSLabConfigTest {
    @Test
    public void systemPropertyOverridesEnvironmentAndFile() {
        Properties fileProperties = new Properties();
        fileProperties.setProperty("CCodePath", "file-path");

        Map<String, String> environment = new HashMap<>();
        environment.put("NCSLAB_CODE_ROOT", "env-path");

        Properties systemProperties = new Properties();
        systemProperties.setProperty("CCodePath", "system-path");

        NCSLabConfig config = NCSLabConfig.from(fileProperties, environment, systemProperties);

        assertEquals("system-path", config.getProperty("CCodePath"));
    }

    @Test
    public void environmentAliasOverridesFileProperty() {
        Properties fileProperties = new Properties();
        fileProperties.setProperty("filesystem.api.url", "http://from-file");

        Map<String, String> environment = new HashMap<>();
        environment.put("NCSLAB_FILESYSTEM_API_URL", "https://from-env");

        NCSLabConfig config = NCSLabConfig.from(fileProperties, environment, new Properties());

        assertEquals("https://from-env", config.getProperty("filesystem.api.url"));
    }

    @Test
    public void systemAliasOverridesEnvironmentAlias() {
        Properties fileProperties = new Properties();
        fileProperties.setProperty("CCodePath", "file-path");

        Map<String, String> environment = new HashMap<>();
        environment.put("NCSLAB_CODE_ROOT", "env-path");

        Properties systemProperties = new Properties();
        systemProperties.setProperty("NCSLAB_CODE_ROOT", "system-alias-path");

        NCSLabConfig config = NCSLabConfig.from(fileProperties, environment, systemProperties);

        assertEquals("system-alias-path", config.getProperty("CCodePath"));
    }

    @Test
    public void expandsUserDirPlaceholder() {
        Properties fileProperties = new Properties();
        fileProperties.setProperty("CCodePath", "${user.dir}/generated");

        NCSLabConfig config = NCSLabConfig.from(fileProperties, Map.of(), new Properties());

        assertEquals(System.getProperty("user.dir") + "/generated", config.getProperty("CCodePath"));
    }
}
