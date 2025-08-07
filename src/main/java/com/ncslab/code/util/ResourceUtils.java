package com.ncslab.code.util;

import java.io.InputStream;

public class ResourceUtils {
    public static InputStream getResourceAsStream(String resourceName, String subDirectory) {
        InputStream stream = null;
        
        // First, try the exact path as requested
        String fullPath = (subDirectory != null ? subDirectory + "/" : "") + resourceName;
        if (!fullPath.startsWith("com/ncslab/code/")) {
            fullPath = "com/ncslab/code/" + fullPath;
        }
        // Remove leading slash if present since getClassLoader().getResourceAsStream() doesn't need it
        if (fullPath.startsWith("/")) {
            fullPath = fullPath.substring(1);
        }
        stream = ResourceUtils.class.getClassLoader().getResourceAsStream(fullPath);
        // System.out.println("ResourceUtils: Looking for '" + fullPath + "' - " + (stream != null ? "FOUND" : "NOT FOUND"));
        
        // If not found and resourceName contains "../", resolve the relative path
        if (stream == null && resourceName.contains("../")) {
            String resolvedPath = resolveRelativePath(subDirectory, resourceName);
            if (resolvedPath != null) {
                String resolvedFullPath = "com/ncslab/code/" + resolvedPath;
                stream = ResourceUtils.class.getClassLoader().getResourceAsStream(resolvedFullPath);
                // System.out.println("ResourceUtils: Resolved path '" + resolvedFullPath + "' - " + (stream != null ? "FOUND" : "NOT FOUND"));
            }
        }
        
        // If still not found, try in the base 'c' directory for common files
        if (stream == null && subDirectory != null && !subDirectory.equals("c")) {
            String basePath = "com/ncslab/code/c/" + resourceName;
            stream = ResourceUtils.class.getClassLoader().getResourceAsStream(basePath);
            // System.out.println("ResourceUtils: Fallback to base '" + basePath + "' - " + (stream != null ? "FOUND" : "NOT FOUND"));
        }
        
        return stream;
    }
    
    private static String resolveRelativePath(String subDirectory, String resourceName) {
        if (subDirectory == null) subDirectory = "";
        
        String[] subDirParts = subDirectory.split("/");
        String[] resourceParts = resourceName.split("/");
        
        // Start with subdirectory path
        java.util.List<String> pathParts = new java.util.ArrayList<>();
        for (String part : subDirParts) {
            if (!part.isEmpty()) {
                pathParts.add(part);
            }
        }
        
        // Process resource path parts
        for (String part : resourceParts) {
            if (part.equals("..")) {
                // Go up one directory
                if (!pathParts.isEmpty()) {
                    pathParts.remove(pathParts.size() - 1);
                }
            } else if (!part.equals(".") && !part.isEmpty()) {
                pathParts.add(part);
            }
        }
        
        return String.join("/", pathParts);
    }
}
