package com.ncslab.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Central runtime configuration for NCSLab.
 *
 * Lookup order:
 * 1. JVM system property with the original key, for example -DCCodePath=...
 * 2. Supported environment variable aliases, for example NCSLAB_CODE_ROOT
 * 3. classpath config.properties
 * 4. caller supplied default
 */
public final class NCSLabConfig {
    private static final Logger LOGGER = Logger.getLogger(NCSLabConfig.class.getName());
    private static final String CONFIG_RESOURCE = "config.properties";

    private static final Map<String, List<String>> ENV_ALIASES = createEnvAliases();
    private static volatile NCSLabConfig current;

    private final Properties fileProperties;
    private final Map<String, String> environment;
    private final Properties systemProperties;

    private NCSLabConfig(Properties fileProperties, Map<String, String> environment, Properties systemProperties) {
        this.fileProperties = new Properties();
        this.fileProperties.putAll(fileProperties);
        this.environment = Collections.unmodifiableMap(new HashMap<>(environment));
        this.systemProperties = new Properties();
        this.systemProperties.putAll(systemProperties);
    }

    public static NCSLabConfig get() {
        NCSLabConfig local = current;
        if (local == null) {
            synchronized (NCSLabConfig.class) {
                local = current;
                if (local == null) {
                    local = load();
                    current = local;
                }
            }
        }
        return local;
    }

    public static NCSLabConfig load() {
        return new NCSLabConfig(loadClasspathProperties(), System.getenv(), System.getProperties());
    }

    public static NCSLabConfig from(Properties fileProperties, Map<String, String> environment,
            Properties systemProperties) {
        return new NCSLabConfig(fileProperties, environment, systemProperties);
    }

    public static void reload() {
        current = load();
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public String getProperty(String key, String defaultValue) {
        Objects.requireNonNull(key, "key");

        String systemValue = trimToNull(systemProperties.getProperty(key));
        if (systemValue != null) {
            return expandPlaceholders(systemValue);
        }

        for (String envName : envNamesFor(key)) {
            String systemAliasValue = trimToNull(systemProperties.getProperty(envName));
            if (systemAliasValue != null) {
                return expandPlaceholders(systemAliasValue);
            }

            String envValue = trimToNull(environment.get(envName));
            if (envValue != null) {
                return expandPlaceholders(envValue);
            }
        }

        String fileValue = trimToNull(fileProperties.getProperty(key));
        if (fileValue != null) {
            return expandPlaceholders(fileValue);
        }

        return defaultValue;
    }

    public String requireProperty(String key) {
        String value = getProperty(key);
        if (trimToNull(value) == null) {
            throw new IllegalStateException("Missing required NCSLab configuration: " + key);
        }
        return value;
    }

    public Map<String, String> safeSummary() {
        Map<String, String> summary = new LinkedHashMap<>();
        for (String key : List.of(
                "mode",
                "CCodePath",
                "CCodePathWin",
                "MCodePath",
                "PLCCodePath",
                "MfcalcCodePath",
                "MakeTool",
                "filesystem.api.url",
                "mfcalc.server.host",
                "mfcalc.server.port",
                "NCSLAB_DB_URL",
                "NCSLAB_DB_USERNAME",
                "NCSLAB_DB_PASSWORD")) {
            String value = getProperty(key);
            if (value != null) {
                summary.put(key, maskIfSensitive(key, value));
            }
        }
        return summary;
    }

    public void validateRequiredProperties(List<String> keys) {
        List<String> missing = new ArrayList<>();
        for (String key : keys) {
            if (trimToNull(getProperty(key)) == null) {
                missing.add(key);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required NCSLab configuration: " + missing);
        }
    }

    private static Properties loadClasspathProperties() {
        Properties properties = new Properties();
        try (InputStream input = NCSLabConfig.class.getClassLoader().getResourceAsStream(CONFIG_RESOURCE)) {
            if (input == null) {
                LOGGER.warning("Unable to find " + CONFIG_RESOURCE + " on the classpath");
                return properties;
            }
            try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load " + CONFIG_RESOURCE, ex);
        }
        return properties;
    }

    private static List<String> envNamesFor(String key) {
        List<String> names = new ArrayList<>();
        List<String> aliases = ENV_ALIASES.get(key);
        if (aliases != null) {
            names.addAll(aliases);
        }
        String canonical = toEnvName(key);
        if (!names.contains(canonical)) {
            names.add(canonical);
        }
        return names;
    }

    private static String toEnvName(String key) {
        StringBuilder builder = new StringBuilder("NCSLAB_");
        char previous = 0;
        for (int i = 0; i < key.length(); i++) {
            char ch = key.charAt(i);
            if (Character.isUpperCase(ch) && i > 0 && Character.isLowerCase(previous)) {
                builder.append('_');
            }
            if (Character.isLetterOrDigit(ch)) {
                builder.append(Character.toUpperCase(ch));
            } else {
                builder.append('_');
            }
            previous = ch;
        }
        return builder.toString().replaceAll("_+", "_");
    }

    private static Map<String, List<String>> createEnvAliases() {
        Map<String, List<String>> aliases = new HashMap<>();
        aliases.put("CCodePath", List.of("NCSLAB_CODE_ROOT", "NCSLAB_C_CODE_PATH"));
        aliases.put("CCodePathWin", List.of("NCSLAB_CODE_ROOT_WIN", "NCSLAB_CODE_ROOT", "NCSLAB_C_CODE_PATH_WIN"));
        aliases.put("MCodePath", List.of("NCSLAB_M_CODE_ROOT"));
        aliases.put("PLCCodePath", List.of("NCSLAB_PLC_CODE_ROOT"));
        aliases.put("MfcalcCodePath", List.of("NCSLAB_MFCALC_CODE_ROOT"));
        aliases.put("MakeTool", List.of("NCSLAB_MAKE_TOOL"));
        aliases.put("mode", List.of("NCSLAB_MODE"));
        aliases.put("debug", List.of("NCSLAB_DEBUG"));
        aliases.put("filesystem.api.url", List.of("NCSLAB_FILESYSTEM_API_URL"));
        aliases.put("mfcalc.server.host", List.of("NCSLAB_MFCALC_SERVER_HOST"));
        aliases.put("mfcalc.server.port", List.of("NCSLAB_MFCALC_SERVER_PORT"));
        aliases.put("mfcalc.max.reconnection.attempts", List.of("NCSLAB_MFCALC_MAX_RECONNECTION_ATTEMPTS"));
        aliases.put("mfcalc.reconnection.delay.ms", List.of("NCSLAB_MFCALC_RECONNECTION_DELAY_MS"));
        aliases.put("NCSLAB_DB_URL", List.of("NCSLAB_DB_URL"));
        aliases.put("NCSLAB_DB_USERNAME", List.of("NCSLAB_DB_USERNAME"));
        aliases.put("NCSLAB_DB_PASSWORD", List.of("NCSLAB_DB_PASSWORD"));
        return aliases;
    }

    private static String expandPlaceholders(String value) {
        return value
                .replace("${user.home}", System.getProperty("user.home", ""))
                .replace("${user.dir}", System.getProperty("user.dir", ""));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String maskIfSensitive(String key, String value) {
        String normalized = key.toLowerCase();
        if (normalized.contains("password") || normalized.contains("secret") || normalized.contains("token")) {
            return "***";
        }
        return value;
    }
}
