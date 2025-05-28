package com.ncslab.code.template;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.ncslab.code.util.ResourceFileSearcher;

/**
 * Implementation of TemplateProvider that uses resources
 */
public class ResourceTemplateProvider implements TemplateProvider {
    
    private final String subDirectory;
    
    /**
     * Create a resource template provider
     * @param subDirectory The subdirectory within resources to search in (e.g., "c/linux")
     */
    public ResourceTemplateProvider(String subDirectory) {
        this.subDirectory = subDirectory;
    }
    
    /**
     * Create a resource template provider with no specific subdirectory
     */
    public ResourceTemplateProvider() {
        this(null);
    }
    
    @Override
    public InputStream getTemplate(String templateName) {
        byte[] content = ResourceTemplate.getTemplateContent(templateName, subDirectory);
        if (content == null) {
            return null;
        }
        
        return new ByteArrayInputStream(content);
    }
    
    @Override
    public String getTemplateAsString(String templateName) {
        return ResourceTemplate.getTemplateString(templateName, subDirectory);
    }
    
    @Override
    public boolean hasTemplate(String templateName) {
        return ResourceTemplate.getTemplateContent(templateName, subDirectory) != null;
    }
}
