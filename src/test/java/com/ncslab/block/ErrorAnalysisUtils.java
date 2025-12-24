package com.ncslab.block;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Utility class for analyzing and categorizing errors from block testing.
 * Provides sophisticated error pattern matching, template variable extraction,
 * and actionable recommendations for fixing identified issues.
 */
public class ErrorAnalysisUtils {
    
    // Comprehensive error pattern definitions
    private static final Map<String, List<Pattern>> ERROR_PATTERNS = new HashMap<>();
    
    static {
        // Template-related errors
        ERROR_PATTERNS.put("TEMPLATE_ERROR", Arrays.asList(
            Pattern.compile("TemplateNotFoundException.*template\\s+(['\"][^'\"]+['\"])", Pattern.CASE_INSENSITIVE),
            Pattern.compile("VelocityException.*template.*(['\"][^'\"]+['\"])", Pattern.CASE_INSENSITIVE),
            Pattern.compile("template\\s+(['\"][^'\"]+['\"]).*not found", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Unable to find resource\\s+(['\"][^'\"]+['\"])", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ResourceNotFoundException.*(['\"][^'\"]+['\"])", Pattern.CASE_INSENSITIVE)
        ));
        
        // Template variable errors  
        ERROR_PATTERNS.put("TEMPLATE_VARIABLE_ERROR", Arrays.asList(
            Pattern.compile("\\$([a-zA-Z_]\\w*).*cannot be resolved", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Reference\\s+\\$([a-zA-Z_]\\w*).*not defined", Pattern.CASE_INSENSITIVE),
            Pattern.compile("variable\\s+\\$([a-zA-Z_]\\w*).*not found", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Invalid reference\\s+\\$([a-zA-Z_]\\w*)", Pattern.CASE_INSENSITIVE)
        ));
        
        // DTO-related errors
        ERROR_PATTERNS.put("DTO_ERROR", Arrays.asList(
            Pattern.compile("NullPointerException.*dto", Pattern.CASE_INSENSITIVE),
            Pattern.compile("dto.*is null", Pattern.CASE_INSENSITIVE),
            Pattern.compile("parameter.*null.*dto", Pattern.CASE_INSENSITIVE),
            Pattern.compile("BlockDto.*null", Pattern.CASE_INSENSITIVE),
            Pattern.compile("validation.*failed.*dto", Pattern.CASE_INSENSITIVE),
            Pattern.compile("TypedParameter.*null", Pattern.CASE_INSENSITIVE)
        ));
        
        // Block creation errors
        ERROR_PATTERNS.put("BLOCK_CREATION_ERROR", Arrays.asList(
            Pattern.compile("BlockCreationException.*([a-zA-Z]\\w*Block)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("createBlock.*failed.*([a-zA-Z]\\w*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("unknown.*block.*type.*([a-zA-Z]\\w*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("block.*([a-zA-Z]\\w*).*not supported", Pattern.CASE_INSENSITIVE)
        ));
        
        // Compilation errors
        ERROR_PATTERNS.put("COMPILATION_ERROR", Arrays.asList(
            Pattern.compile("compilation.*failed", Pattern.CASE_INSENSITIVE),
            Pattern.compile("compiler.*error", Pattern.CASE_INSENSITIVE),
            Pattern.compile("make.*error", Pattern.CASE_INSENSITIVE),
            Pattern.compile("build.*failed", Pattern.CASE_INSENSITIVE),
            Pattern.compile("syntax.*error.*line\\s+(\\d+)", Pattern.CASE_INSENSITIVE)
        ));
    }
    
    /**
     * Detailed error analysis result containing categorization and recommendations
     */
    public static class ErrorAnalysis {
        private final String category;
        private final String subCategory;
        private final Set<String> extractedValues;
        private final List<String> recommendations;
        private final int severity; // 1-5, 5 being most severe
        
        public ErrorAnalysis(String category, String subCategory, Set<String> extractedValues, 
                           List<String> recommendations, int severity) {
            this.category = category;
            this.subCategory = subCategory;
            this.extractedValues = extractedValues;
            this.recommendations = recommendations;
            this.severity = severity;
        }
        
        // Getters
        public String getCategory() { return category; }
        public String getSubCategory() { return subCategory; }
        public Set<String> getExtractedValues() { return extractedValues; }
        public List<String> getRecommendations() { return recommendations; }
        public int getSeverity() { return severity; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Category: ").append(category);
            if (subCategory != null) {
                sb.append(" -> ").append(subCategory);
            }
            sb.append(" (Severity: ").append(severity).append(")\n");
            
            if (!extractedValues.isEmpty()) {
                sb.append("Extracted Values: ").append(String.join(", ", extractedValues)).append("\n");
            }
            
            sb.append("Recommendations:\n");
            recommendations.forEach(rec -> sb.append("  - ").append(rec).append("\n"));
            
            return sb.toString();
        }
    }
    
    /**
     * Performs comprehensive analysis of an error message and stack trace
     */
    public static ErrorAnalysis analyzeError(String errorMessage, String stackTrace) {
        String combinedText = (errorMessage != null ? errorMessage : "") + "\n" + 
                             (stackTrace != null ? stackTrace : "");
        
        // Try each error pattern category
        for (Map.Entry<String, List<Pattern>> entry : ERROR_PATTERNS.entrySet()) {
            String category = entry.getKey();
            List<Pattern> patterns = entry.getValue();
            
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(combinedText);
                if (matcher.find()) {
                    Set<String> extractedValues = new HashSet<>();
                    
                    // Extract captured groups
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        String group = matcher.group(i);
                        if (group != null && !group.trim().isEmpty()) {
                            extractedValues.add(group.trim().replaceAll("['\"]", ""));
                        }
                    }
                    
                    return createErrorAnalysis(category, extractedValues, combinedText);
                }
            }
        }
        
        // Fallback analysis for unrecognized errors
        return analyzeUnknownError(combinedText);
    }
    
    /**
     * Creates detailed error analysis with recommendations
     */
    private static ErrorAnalysis createErrorAnalysis(String category, Set<String> extractedValues, String fullText) {
        List<String> recommendations = new ArrayList<>();
        String subCategory = null;
        int severity = 3; // Default severity
        
        switch (category) {
            case "TEMPLATE_ERROR":
                severity = 5; // High severity - blocks code generation
                subCategory = "Missing Template Files";
                recommendations.addAll(Arrays.asList(
                    "Check if template files exist in src/main/resources/templates/ or src/main/resources/com/ncslab/code2/",
                    "Verify template file naming follows the pattern: <language>/<blockCategory>/<BlockName>/",
                    "Ensure template provider is properly configured for the block",
                    "Check ResourceReader configuration for template loading"
                ));
                
                if (!extractedValues.isEmpty()) {
                    recommendations.add("Missing template files: " + String.join(", ", extractedValues));
                }
                break;
                
            case "TEMPLATE_VARIABLE_ERROR":
                severity = 4; // High severity - template rendering fails
                subCategory = "Undefined Template Variables";
                recommendations.addAll(Arrays.asList(
                    "Add missing variables to template context in Block class",
                    "Check TemplateUtils.populateContext() method for automatic variable population",
                    "Verify parameter names match template variable names",
                    "Add null checks before setting template variables"
                ));
                
                if (!extractedValues.isEmpty()) {
                    recommendations.add("Undefined variables: $" + String.join(", $", extractedValues));
                }
                break;
                
            case "DTO_ERROR":
                severity = 4; // High severity - data handling fails
                subCategory = "DTO Null Parameter Issues";
                recommendations.addAll(Arrays.asList(
                    "Implement dual constructor pattern: Block(BlockDto) and Block(JSONObject)",
                    "Add parameter validation in DTO constructor",
                    "Check TypedParameter initialization with default values",
                    "Ensure graceful fallback to JSONObject parsing when DTO fails",
                    "Add @JsonProperty annotations for Jackson serialization"
                ));
                break;
                
            case "BLOCK_CREATION_ERROR":
                severity = 5; // Critical - block cannot be instantiated
                subCategory = "Block Instantiation Failure";
                recommendations.addAll(Arrays.asList(
                    "Register block type in BlockType enum factory",
                    "Implement required constructors: Block(JSONObject, NCSLabModel)",
                    "Check block class is in correct package structure",
                    "Verify block class is not abstract and has public constructor",
                    "Add block to BlockType.createBlock() switch statement"
                ));
                
                if (!extractedValues.isEmpty()) {
                    recommendations.add("Failed block types: " + String.join(", ", extractedValues));
                }
                break;
                
            case "COMPILATION_ERROR":
                severity = 3; // Medium severity - affects deployment
                subCategory = "Generated Code Compilation";
                recommendations.addAll(Arrays.asList(
                    "Check generated C++ code for syntax errors",
                    "Verify include files and dependencies are available",
                    "Check platform-specific compiler flags and makefile configuration",
                    "Ensure template-generated code follows C++ standards",
                    "Test with different compiler versions if needed"
                ));
                break;
        }
        
        // Add general recommendations based on error content analysis
        if (fullText.toLowerCase().contains("nullpointerexception")) {
            recommendations.add("Add null checks for all object references before use");
            if (severity < 4) severity = 4;
        }
        
        if (fullText.toLowerCase().contains("classnotfoundexception")) {
            recommendations.add("Check classpath and ensure all required JAR files are available");
            if (severity < 3) severity = 3;
        }
        
        return new ErrorAnalysis(category, subCategory, extractedValues, recommendations, severity);
    }
    
    /**
     * Fallback analysis for unrecognized error patterns
     */
    private static ErrorAnalysis analyzeUnknownError(String fullText) {
        List<String> recommendations = new ArrayList<>();
        Set<String> extractedValues = new HashSet<>();
        String category = "UNKNOWN_ERROR";
        int severity = 2;
        
        // Basic pattern recognition for unknown errors
        if (fullText.toLowerCase().contains("timeout")) {
            category = "TIMEOUT_ERROR";
            severity = 3;
            recommendations.addAll(Arrays.asList(
                "Increase test timeout duration",
                "Check for infinite loops in block logic",
                "Optimize performance of block calculations",
                "Consider running tests sequentially instead of parallel"
            ));
        } else if (fullText.toLowerCase().contains("outofmemoryerror")) {
            category = "MEMORY_ERROR";
            severity = 4;
            recommendations.addAll(Arrays.asList(
                "Increase JVM heap size with -Xmx parameter",
                "Check for memory leaks in block implementations",
                "Optimize data structures and object creation",
                "Consider using object pooling for frequently created objects"
            ));
        } else {
            recommendations.addAll(Arrays.asList(
                "Review full stack trace for clues",
                "Check recent code changes that might have introduced the issue",
                "Enable debug logging for more detailed error information",
                "Compare with similar working blocks for implementation patterns"
            ));
        }
        
        return new ErrorAnalysis(category, null, extractedValues, recommendations, severity);
    }
    
    /**
     * Extracts template variable names from error messages
     */
    public static Set<String> extractTemplateVariables(String errorText) {
        Set<String> variables = new HashSet<>();
        Pattern variablePattern = Pattern.compile("\\$([a-zA-Z_]\\w*)");
        Matcher matcher = variablePattern.matcher(errorText);
        
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        
        return variables;
    }
    
    /**
     * Extracts file paths from error messages
     */
    public static Set<String> extractFilePaths(String errorText) {
        Set<String> filePaths = new HashSet<>();
        
        // Common file path patterns
        Pattern[] pathPatterns = {
            Pattern.compile("(['\"][^'\"]*\\.(vm|java|c|h|cpp|hpp)['\"])"),
            Pattern.compile("(src/[^\\s]+\\.(vm|java|c|h|cpp|hpp))"),
            Pattern.compile("(templates/[^\\s]+\\.(vm|java|c|h|cpp|hpp))"),
            Pattern.compile("([^\\s]+\\.template)"),
            Pattern.compile("([^\\s]+/[^\\s]+\\.(vm|java))") // General path pattern
        };
        
        for (Pattern pattern : pathPatterns) {
            Matcher matcher = pattern.matcher(errorText);
            while (matcher.find()) {
                String path = matcher.group(1).replaceAll("['\"]", "");
                filePaths.add(path);
            }
        }
        
        return filePaths;
    }
}