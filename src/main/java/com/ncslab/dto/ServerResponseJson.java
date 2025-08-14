package com.ncslab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.json.JSONObject;

import java.util.Map;
import java.util.List;

/**
 * DTO for External Server Response JSON representation
 * Used for MFCalc, Python, Octave server communications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerResponseJson {
    
    @JsonProperty("status")
    private String status; // "success", "error", "timeout"
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("result")
    private Object result; // Execution result
    
    @JsonProperty("output")
    private String output; // Console output
    
    @JsonProperty("error")
    private String error; // Error message if any
    
    @JsonProperty("serverType")
    private String serverType; // "mfcalc", "python", "octave"
    
    @JsonProperty("executionTime")
    private Long executionTime; // in milliseconds
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("variables")
    private Map<String, Object> variables; // Variables from server workspace
    
    @JsonProperty("plots")
    private List<String> plots; // Plot file paths or data
    
    @JsonProperty("warnings")
    private List<String> warnings; // Warning messages
    
    @JsonProperty("code")
    private Integer code; // HTTP-style response code
    
    /**
     * Create ServerResponseJson from legacy JSONObject
     * @param jsonObject Legacy JSONObject
     * @return ServerResponseJson DTO or null if conversion fails
     */
    public static ServerResponseJson fromLegacyJson(JSONObject jsonObject) {
        try {
            ServerResponseJson response = new ServerResponseJson();
            
            // Map common fields
            response.setStatus(jsonObject.optString("status", "unknown"));
            response.setMessage(jsonObject.optString("message", ""));
            response.setOutput(jsonObject.optString("output", ""));
            response.setError(jsonObject.optString("error", ""));
            response.setCode(jsonObject.optInt("code", 200));
            
            // Handle result field (can be various types)
            if (jsonObject.has("result")) {
                response.setResult(jsonObject.get("result"));
            }
            
            // Handle execution time
            if (jsonObject.has("executionTime")) {
                response.setExecutionTime(jsonObject.optLong("executionTime", 0L));
            }
            
            return response;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Check if response indicates success
     * @return true if successful
     */
    public boolean isSuccess() {
        return "success".equals(status);
    }
    
    /**
     * Check if response indicates error
     * @return true if error
     */
    public boolean isError() {
        return "error".equals(status);
    }
    
    /**
     * Check if response indicates timeout
     * @return true if timeout
     */
    public boolean isTimeout() {
        return "timeout".equals(status);
    }
    
    /**
     * Get combined error information
     * @return error message combining error field and message if status is error
     */
    public String getErrorInfo() {
        if (!isError()) {
            return null;
        }
        
        StringBuilder errorInfo = new StringBuilder();
        if (error != null && !error.trim().isEmpty()) {
            errorInfo.append(error);
        }
        if (message != null && !message.trim().isEmpty()) {
            if (errorInfo.length() > 0) {
                errorInfo.append(" - ");
            }
            errorInfo.append(message);
        }
        
        return errorInfo.length() > 0 ? errorInfo.toString() : "Unknown error";
    }
    
    /**
     * Create success response
     * @param result Execution result
     * @param output Console output
     * @param serverType Server type
     * @return Success ServerResponseJson
     */
    public static ServerResponseJson createSuccess(Object result, String output, String serverType) {
        return ServerResponseJson.builder()
                .status("success")
                .result(result)
                .output(output)
                .serverType(serverType)
                .code(200)
                .build();
    }
    
    /**
     * Create error response
     * @param error Error message
     * @param serverType Server type
     * @return Error ServerResponseJson
     */
    public static ServerResponseJson createError(String error, String serverType) {
        return ServerResponseJson.builder()
                .status("error")
                .error(error)
                .message(error)
                .serverType(serverType)
                .code(400)
                .build();
    }
    
    /**
     * Create timeout response
     * @param serverType Server type
     * @return Timeout ServerResponseJson
     */
    public static ServerResponseJson createTimeout(String serverType) {
        return ServerResponseJson.builder()
                .status("timeout")
                .error("Request timed out")
                .message("Server request exceeded timeout limit")
                .serverType(serverType)
                .code(408)
                .build();
    }
    
    /**
     * Convert to legacy JSONObject format
     * @return JSONObject representation
     */
    public JSONObject toLegacyJson() {
        JSONObject json = new JSONObject();
        
        json.put("status", status != null ? status : "unknown");
        json.put("code", code != null ? code : 200);
        
        if (message != null) json.put("message", message);
        if (result != null) json.put("result", result);
        if (output != null) json.put("output", output);
        if (error != null) json.put("error", error);
        if (executionTime != null) json.put("executionTime", executionTime);
        
        return json;
    }
}