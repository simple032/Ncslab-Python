package com.ncslab.dto.communication;

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
public class ServerResponseDto {
    
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
     * @return Success ServerResponseDto
     */
    public static ServerResponseDto createSuccess(Object result, String output, String serverType) {
        return ServerResponseDto.builder()
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
     * @return Error ServerResponseDto
     */
    public static ServerResponseDto createError(String error, String serverType) {
        return ServerResponseDto.builder()
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
     * @return Timeout ServerResponseDto
     */
    public static ServerResponseDto createTimeout(String serverType) {
        return ServerResponseDto.builder()
                .status("timeout")
                .error("Request timed out")
                .message("Server request exceeded timeout limit")
                .serverType(serverType)
                .code(408)
                .build();
    }
    
}