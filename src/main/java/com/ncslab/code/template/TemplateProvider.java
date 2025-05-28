package com.ncslab.code.template;

import java.io.InputStream;

/**
 * Interface for template providers
 */
public interface TemplateProvider {
    
    /**
     * Get a template as an input stream
     * @param templateName The template name or path
     * @return InputStream of the template, or null if not found
     */
    InputStream getTemplate(String templateName);
    
    /**
     * Get a template as a string
     * @param templateName The template name or path
     * @return The template content as a string, or null if not found
     */
    String getTemplateAsString(String templateName);
    
    /**
     * Check if a template exists
     * @param templateName The template name or path
     * @return true if the template exists, false otherwise
     */
    boolean hasTemplate(String templateName);
}
