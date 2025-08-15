package com.utils;

import java.util.Properties;
import java.util.logging.Logger;

public class DatabaseConfig {
    private static final Logger logger = Logger.getLogger(DatabaseConfig.class.getName());
    
    private DatabaseConfig() {
        // Utility class
    }
    
    /**
     * Validates that required database environment variables are set
     * @throws IllegalStateException if required variables are missing
     */
    public static void validateDatabaseConfiguration() {
        String password = System.getenv("NCSLAB_DB_PASSWORD");
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalStateException(
                "NCSLab database password not configured. Please set NCSLAB_DB_PASSWORD environment variable."
            );
        }
        
        logger.info("NCSLab database configuration validated successfully");
    }
    
    /**
     * Gets database properties with environment variable substitution
     * @return Properties with database configuration
     */
    public static Properties getDatabaseProperties() {
        validateDatabaseConfiguration();
        
        Properties props = new Properties();
        
        // Database connection properties with secure defaults
        props.setProperty("NCSLAB_DB_URL", 
            System.getenv().getOrDefault("NCSLAB_DB_URL", 
                "jdbc:mysql://localhost:3306/ncslab?useSSL=true&characterEncoding=utf8&serverTimezone=UTC"));
        props.setProperty("NCSLAB_DB_USERNAME", 
            System.getenv().getOrDefault("NCSLAB_DB_USERNAME", "root"));
        props.setProperty("NCSLAB_DB_PASSWORD", 
            System.getenv("NCSLAB_DB_PASSWORD"));
        
        return props;
    }
    
    /**
     * Sanitizes database URL to remove sensitive information for logging
     * @param url Database URL
     * @return Sanitized URL for safe logging
     */
    public static String sanitizeUrlForLogging(String url) {
        if (url == null) return "null";
        
        // Remove password parameter if present
        return url.replaceAll("password=[^&]*", "password=***");
    }
    
    /**
     * Get environment variable names used by NCSLab
     * @return Array of environment variable names
     */
    public static String[] getRequiredEnvironmentVariables() {
        return new String[]{
            "NCSLAB_DB_PASSWORD",  // Required
            "NCSLAB_DB_URL",       // Optional - has default
            "NCSLAB_DB_USERNAME"   // Optional - has default
        };
    }
}