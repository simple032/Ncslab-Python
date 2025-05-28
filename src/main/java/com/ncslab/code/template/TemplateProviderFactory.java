package com.ncslab.code.template;

/**
 * Factory for creating template providers
 */
public class TemplateProviderFactory {
    
    /**
     * Create a template provider for a specific class
     * @param clazz The class to create a template provider for
     * @return The template provider
     */
    public static TemplateProvider createForClass(Class<?> clazz) {
        String packageName = clazz.getPackage().getName();
        
        // Extract the subdirectory from the package name
        // Example: com.ncslab.code.c.linux.pc -> c/linux/pc
        if (packageName.startsWith("com.ncslab.code.")) {
            String[] parts = packageName.split("\\.");
            
            // Start building from index 3 (after com.ncslab.code)
            StringBuilder subDir = new StringBuilder();
            for (int i = 3; i < parts.length; i++) {
                if (subDir.length() > 0) {
                    subDir.append("/");
                }
                subDir.append(parts[i]);
            }
            
            return new ResourceTemplateProvider(subDir.toString());
        }
        
        // Default provider
        return new ResourceTemplateProvider();
    }
    
    /**
     * Create a template provider for a specific subdirectory
     * @param subDirectory The subdirectory to search in (e.g., "c/linux")
     * @return The template provider
     */
    public static TemplateProvider createForSubdirectory(String subDirectory) {
        return new ResourceTemplateProvider(subDirectory);
    }
    
    /**
     * Create a default template provider with no specific subdirectory
     * @return The template provider
     */
    public static TemplateProvider createDefault() {
        return new ResourceTemplateProvider();
    }
}
