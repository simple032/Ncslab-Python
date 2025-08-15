package com.ncslab.util;

import com.ncslab.dto.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Comprehensive system-wide DTO validation utility
 * Provides centralized validation for all DTOs in the NCSLab system
 */
@Slf4j
public class DtoValidationUtils {
    
    /**
     * Validation result container
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        private final Map<String, Object> metadata;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors != null ? errors : new ArrayList<>();
            this.warnings = warnings != null ? warnings : new ArrayList<>();
            this.metadata = new HashMap<>();
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public Map<String, Object> getMetadata() { return metadata; }
        
        public String getErrorSummary() {
            if (errors.isEmpty()) return null;
            return String.join("; ", errors);
        }
        
        public String getWarningSummary() {
            if (warnings.isEmpty()) return null;
            return String.join("; ", warnings);
        }
        
        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
        }
    }
    
    /**
     * Validate ModelJson DTO with comprehensive checks
     * @param modelDto ModelJson to validate
     * @return ValidationResult
     */
    public static ValidationResult validateModel(ModelJson modelDto) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (modelDto == null) {
            errors.add("ModelJson is null");
            return new ValidationResult(false, errors, warnings);
        }
        
        // Basic field validation
        if (modelDto.getModelName() == null || modelDto.getModelName().trim().isEmpty()) {
            errors.add("Model name is required");
        }
        
        if (modelDto.getUserId() <= 0) {
            errors.add("User ID must be positive");
        }
        
        if (modelDto.getModelId() <= 0) {
            errors.add("Model ID must be positive");
        }
        
        // Block validation
        if (modelDto.getBlocks() != null) {
            Set<String> blockNames = new HashSet<>();
            for (int i = 0; i < modelDto.getBlocks().size(); i++) {
                BlockJson block = modelDto.getBlocks().get(i);
                ValidationResult blockResult = validateBlock(block);
                if (!blockResult.isValid()) {
                    errors.add("Block " + i + ": " + blockResult.getErrorSummary());
                }
                if (!blockResult.getWarnings().isEmpty()) {
                    warnings.add("Block " + i + ": " + blockResult.getWarningSummary());
                }
                
                // Check for duplicate block names
                if (block != null && block.getBlockName() != null) {
                    if (blockNames.contains(block.getBlockName())) {
                        warnings.add("Duplicate block name: " + block.getBlockName());
                    } else {
                        blockNames.add(block.getBlockName());
                    }
                }
            }
        }
        
        // Line validation
        if (modelDto.getLines() != null) {
            for (int i = 0; i < modelDto.getLines().size(); i++) {
                LineJson line = modelDto.getLines().get(i);
                ValidationResult lineResult = validateLine(line);
                if (!lineResult.isValid()) {
                    errors.add("Line " + i + ": " + lineResult.getErrorSummary());
                }
                if (!lineResult.getWarnings().isEmpty()) {
                    warnings.add("Line " + i + ": " + lineResult.getWarningSummary());
                }
            }
        }
        
        // Config validation
        if (modelDto.getConfig() != null) {
            ValidationResult configResult = validateConfig(modelDto.getConfig());
            if (!configResult.isValid()) {
                errors.add("Config: " + configResult.getErrorSummary());
            }
            if (!configResult.getWarnings().isEmpty()) {
                warnings.add("Config: " + configResult.getWarningSummary());
            }
        }
        
        ValidationResult result = new ValidationResult(errors.isEmpty(), errors, warnings);
        result.addMetadata("blockCount", modelDto.getBlocks() != null ? modelDto.getBlocks().size() : 0);
        result.addMetadata("lineCount", modelDto.getLines() != null ? modelDto.getLines().size() : 0);
        
        return result;
    }
    
    /**
     * Validate BlockJson DTO
     * @param blockDto BlockJson to validate
     * @return ValidationResult
     */
    public static ValidationResult validateBlock(BlockJson blockDto) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (blockDto == null) {
            errors.add("BlockJson is null");
            return new ValidationResult(false, errors, warnings);
        }
        
        if (blockDto.getBlockName() == null || blockDto.getBlockName().trim().isEmpty()) {
            errors.add("Block name is required");
        }
        
        if (blockDto.getBlockType() == null || blockDto.getBlockType().trim().isEmpty()) {
            errors.add("Block type is required");
        }
        
        if (blockDto.getSrcBlock() == null || blockDto.getSrcBlock().trim().isEmpty()) {
            errors.add("Source block path is required");
        }
        
        // Circuit block specific validation
        if (blockDto instanceof CircuitBlockJson) {
            CircuitBlockJson circuitBlock = (CircuitBlockJson) blockDto;
            ValidationResult circuitResult = validateCircuitBlock(circuitBlock);
            if (!circuitResult.isValid()) {
                errors.addAll(circuitResult.getErrors());
            }
            warnings.addAll(circuitResult.getWarnings());
        }
        
        // Parameter validation
        if (blockDto.getParamValues() != null && blockDto.getParamValues().isEmpty()) {
            warnings.add("Block has no parameters");
        }
        
        ValidationResult result = new ValidationResult(errors.isEmpty(), errors, warnings);
        result.addMetadata("blockType", blockDto.getBlockType());
        result.addMetadata("paramCount", blockDto.getParamValues() != null ? blockDto.getParamValues().size() : 0);
        
        return result;
    }
    
    /**
     * Validate CircuitBlockJson DTO
     * @param circuitBlockDto CircuitBlockJson to validate
     * @return ValidationResult
     */
    public static ValidationResult validateCircuitBlock(CircuitBlockJson circuitBlockDto) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (circuitBlockDto == null) {
            errors.add("CircuitBlockJson is null");
            return new ValidationResult(false, errors, warnings);
        }
        
        // Use the built-in validation
        if (!circuitBlockDto.isValidCircuitBlock()) {
            String error = circuitBlockDto.getCircuitValidationError();
            if (error != null) {
                errors.add(error);
            }
        }
        
        // Additional validation for circuit elements
        if ("element".equals(circuitBlockDto.getCircuitType())) {
            Double elementValue = circuitBlockDto.getElementValue();
            if (elementValue != null && elementValue <= 0) {
                warnings.add("Circuit element value should be positive: " + elementValue);
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Validate LineJson DTO
     * @param lineDto LineJson to validate
     * @return ValidationResult
     */
    public static ValidationResult validateLine(LineJson lineDto) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (lineDto == null) {
            errors.add("LineJson is null");
            return new ValidationResult(false, errors, warnings);
        }
        
        if (lineDto.getFromBlockName() == null || lineDto.getFromBlockName().trim().isEmpty()) {
            errors.add("From block name is required");
        }
        
        if (lineDto.getToBlockName() == null || lineDto.getToBlockName().trim().isEmpty()) {
            errors.add("To block name is required");
        }
        
        // Check for self-connection
        if (lineDto.getFromBlockName() != null && lineDto.getFromBlockName().equals(lineDto.getToBlockName())) {
            warnings.add("Line connects block to itself: " + lineDto.getFromBlockName());
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Validate ConfigJson DTO
     * @param configDto ConfigJson to validate
     * @return ValidationResult
     */
    public static ValidationResult validateConfig(ConfigJson configDto) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (configDto == null) {
            errors.add("ConfigJson is null");
            return new ValidationResult(false, errors, warnings);
        }
        
        // Validate solver
        if (configDto.getSolver() != null) {
            Set<String> validSolvers = Set.of("ode1", "ode2", "ode3", "ode4", "ode5", "ode23", "ode45");
            if (!validSolvers.contains(configDto.getSolver())) {
                warnings.add("Unknown solver: " + configDto.getSolver());
            }
        }
        
        // Validate time parameters
        if (configDto.getStartTime() != null && configDto.getStopTime() != null) {
            try {
                double startTime = Double.parseDouble(configDto.getStartTime());
                double stopTime = Double.parseDouble(configDto.getStopTime());
                if (startTime >= stopTime) {
                    errors.add("Start time must be less than stop time");
                }
            } catch (NumberFormatException e) {
                errors.add("Invalid time format in config");
            }
        }
        
        // Validate step size
        if (configDto.getFixedStep() != null) {
            try {
                double fixedStep = ((Number) configDto.getFixedStep()).doubleValue();
                if (fixedStep <= 0) {
                    errors.add("Fixed step must be positive");
                }
            } catch (ClassCastException e) {
                errors.add("Fixed step must be a number");
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Validate ServerRequestJson DTO
     * @param requestDto ServerRequestJson to validate
     * @return ValidationResult
     */
    public static ValidationResult validateServerRequest(ServerRequestJson requestDto) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (requestDto == null) {
            errors.add("ServerRequestJson is null");
            return new ValidationResult(false, errors, warnings);
        }
        
        // Use the built-in validation
        if (!requestDto.isValid()) {
            String error = requestDto.getValidationError();
            if (error != null) {
                errors.add(error);
            }
        }
        
        // Additional validation
        if (requestDto.getTimeout() != null && requestDto.getTimeout() <= 0) {
            warnings.add("Timeout should be positive: " + requestDto.getTimeout());
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Validate ServerResponseJson DTO
     * @param responseDto ServerResponseJson to validate
     * @return ValidationResult
     */
    public static ValidationResult validateServerResponse(ServerResponseJson responseDto) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (responseDto == null) {
            errors.add("ServerResponseJson is null");
            return new ValidationResult(false, errors, warnings);
        }
        
        if (responseDto.getStatus() == null || responseDto.getStatus().trim().isEmpty()) {
            errors.add("Response status is required");
        }
        
        // Validate status values
        if (responseDto.getStatus() != null) {
            Set<String> validStatuses = Set.of("success", "error", "timeout");
            if (!validStatuses.contains(responseDto.getStatus())) {
                warnings.add("Unknown status: " + responseDto.getStatus());
            }
        }
        
        // Error responses should have error message
        if ("error".equals(responseDto.getStatus()) && 
            (responseDto.getError() == null || responseDto.getError().trim().isEmpty())) {
            warnings.add("Error response should have error message");
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Validate WebSocketMessageJson DTO
     * @param messageDto WebSocketMessageJson to validate
     * @return ValidationResult
     */
    public static ValidationResult validateWebSocketMessage(WebSocketMessageJson messageDto) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (messageDto == null) {
            errors.add("WebSocketMessageJson is null");
            return new ValidationResult(false, errors, warnings);
        }
        
        // Either msg or com should be present
        if ((messageDto.getMsg() == null || messageDto.getMsg().trim().isEmpty()) &&
            (messageDto.getCom() == null || messageDto.getCom().trim().isEmpty())) {
            errors.add("Either message type (msg) or command (com) is required");
        }
        
        // Validate progress
        if (messageDto.getProgress() != null && 
            (messageDto.getProgress() < 0 || messageDto.getProgress() > 100)) {
            warnings.add("Progress should be between 0 and 100: " + messageDto.getProgress());
        }
        
        // Validate simulation time
        if (messageDto.getTime() != null && messageDto.getTimeLength() != null &&
            messageDto.getTime() > messageDto.getTimeLength()) {
            warnings.add("Current time exceeds total time");
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Perform comprehensive validation of all DTOs in a model
     * @param modelDto ModelJson to validate
     * @return ValidationResult with comprehensive analysis
     */
    public static ValidationResult validateModelComprehensive(ModelJson modelDto) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Validate the model itself
        ValidationResult modelResult = validateModel(modelDto);
        errors.addAll(modelResult.getErrors());
        warnings.addAll(modelResult.getWarnings());
        
        // Cross-validation: ensure line references valid blocks
        if (modelDto.getBlocks() != null && modelDto.getLines() != null) {
            Set<String> blockNames = new HashSet<>();
            for (BlockJson block : modelDto.getBlocks()) {
                if (block != null && block.getBlockName() != null) {
                    blockNames.add(block.getBlockName());
                }
            }
            
            for (LineJson line : modelDto.getLines()) {
                if (line != null) {
                    if (line.getFromBlockName() != null && !blockNames.contains(line.getFromBlockName())) {
                        errors.add("Line references non-existent from block: " + line.getFromBlockName());
                    }
                    if (line.getToBlockName() != null && !blockNames.contains(line.getToBlockName())) {
                        errors.add("Line references non-existent to block: " + line.getToBlockName());
                    }
                }
            }
        }
        
        ValidationResult result = new ValidationResult(errors.isEmpty(), errors, warnings);
        result.addMetadata("validationType", "comprehensive");
        result.addMetadata("totalChecks", errors.size() + warnings.size());
        
        return result;
    }
    
    /**
     * Get system-wide validation statistics
     * @return Map containing validation statistics
     */
    public static Map<String, Object> getValidationMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("supportedDTOs", List.of(
            "ModelJson", "BlockJson", "CircuitBlockJson", "LineJson", "ConfigJson",
            "ServerRequestJson", "ServerResponseJson", "WebSocketMessageJson"
        ));
        
        metrics.put("validationFeatures", List.of(
            "Field validation", "Cross-reference validation", "Circuit-specific validation",
            "Parameter validation", "Warning detection", "Metadata collection"
        ));
        
        metrics.put("validatorVersion", "1.0.0");
        metrics.put("lastUpdated", System.currentTimeMillis());
        
        return metrics;
    }
}