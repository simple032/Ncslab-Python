package com.ncslab.dto.communication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;
import java.util.List;

/**
 * DTO for External Server Request JSON representation
 * Used for MFCalc, Python, Octave server communications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerRequestDto {
    
    @JsonProperty("command")
    private String command;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("script")
    private String script;
    
    @JsonProperty("parameters")
    private Map<String, Object> parameters;
    
    @JsonProperty("timeout")
    private Integer timeout; // in milliseconds
    
    @JsonProperty("serverType")
    private String serverType; // "mfcalc", "python", "octave"
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("workspace")
    private Map<String, Object> workspace; // Variables in server workspace
    
    @JsonProperty("files")
    private List<String> files; // File paths for server operations
    
    @JsonProperty("options")
    private Map<String, Object> options; // Server-specific options
    
    /**
     * Validation method for server requests
     * @return true if valid request
     */
    public boolean isValid() {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        
        if (serverType == null || serverType.trim().isEmpty()) {
            return false;
        }
        
        // Validate server type
        if (!isValidServerType(serverType)) {
            return false;
        }
        
        // Script is required for script execution commands
        if ("execute".equals(command) && (script == null || script.trim().isEmpty())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if server type is supported
     * @param type Server type
     * @return true if supported
     */
    private boolean isValidServerType(String type) {
        return "mfcalc".equals(type) || "python".equals(type) || "octave".equals(type);
    }
    
    /**
     * Get validation error message
     * @return error message or null if valid
     */
    public String getValidationError() {
        if (command == null || command.trim().isEmpty()) {
            return "Command is required";
        }
        
        if (userId == null || userId.trim().isEmpty()) {
            return "User ID is required";
        }
        
        if (serverType == null || serverType.trim().isEmpty()) {
            return "Server type is required";
        }
        
        if (!isValidServerType(serverType)) {
            return "Server type must be one of: mfcalc, python, octave";
        }
        
        if ("execute".equals(command) && (script == null || script.trim().isEmpty())) {
            return "Script is required for execute command";
        }
        
        return null;
    }
    
    /**
     * Create MFCalc-specific request
     * @param userId User ID
     * @param script MFCalc script
     * @return ServerRequestDto for MFCalc
     */
    public static ServerRequestDto createMfcalcRequest(String userId, String script) {
        return ServerRequestDto.builder()
                .command("execute")
                .userId(userId)
                .script(script)
                .serverType("mfcalc")
                .timeout(30000) // 30 seconds default
                .build();
    }
    
    /**
     * Create Python-specific request
     * @param userId User ID
     * @param script Python script
     * @return ServerRequestDto for Python
     */
    public static ServerRequestDto createPythonRequest(String userId, String script) {
        return ServerRequestDto.builder()
                .command("execute")
                .userId(userId)
                .script(script)
                .serverType("python")
                .timeout(60000) // 60 seconds default
                .build();
    }
    
    /**
     * Create Octave-specific request
     * @param userId User ID
     * @param script Octave script
     * @return ServerRequestDto for Octave
     */
    public static ServerRequestDto createOctaveRequest(String userId, String script) {
        return ServerRequestDto.builder()
                .command("execute")
                .userId(userId)
                .script(script)
                .serverType("octave")
                .timeout(45000) // 45 seconds default
                .build();
    }
}