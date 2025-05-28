package com.ncslab.code.template;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import com.ncslab.code.util.ResourceFileSearcher;

/**
 * Resource template handler for accessing template files
 */
public class ResourceTemplate {
    
    // Cache for template content
    private static final Map<String, byte[]> templateCache = new HashMap<>();
    
    /**
     * Get the content of a template file
     * @param fileName The file name or path
     * @param subDirectory Optional subdirectory to search in (e.g. "c/linux")
     * @return The content of the template file, or null if not found
     */
    public static byte[] getTemplateContent(String fileName, String subDirectory) {
        // Create cache key
        String cacheKey = (subDirectory != null ? subDirectory + "/" : "") + fileName;
        
        // Check cache first
        if (templateCache.containsKey(cacheKey)) {
            return templateCache.get(cacheKey);
        }
        
        // Try to find the resource
        InputStream inputStream = ResourceFileSearcher.findResourceFile(fileName, subDirectory);
        if (inputStream == null) {
            return null;
        }
        
        try {
            // Read the content
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            byte[] content = outputStream.toByteArray();
            
            // Cache the content
            templateCache.put(cacheKey, content);
            
            return content;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    /**
     * Get the content of a template file
     * @param fileName The file name or path
     * @return The content of the template file, or null if not found
     */
    public static byte[] getTemplateContent(String fileName) {
        return getTemplateContent(fileName, null);
    }
    
    /**
     * Get the content of a template file as a string
     * @param fileName The file name or path
     * @param subDirectory Optional subdirectory to search in (e.g. "c/linux")
     * @return The content of the template file as a string, or null if not found
     */
    public static String getTemplateString(String fileName, String subDirectory) {
        byte[] content = getTemplateContent(fileName, subDirectory);
        if (content == null) {
            return null;
        }
        
        return new String(content);
    }
    
    /**
     * Get the content of a template file as a string
     * @param fileName The file name or path
     * @return The content of the template file as a string, or null if not found
     */
    public static String getTemplateString(String fileName) {
        return getTemplateString(fileName, null);
    }
    
    /**
     * Clear the template cache
     */
    public static void clearCache() {
        templateCache.clear();
    }
}
