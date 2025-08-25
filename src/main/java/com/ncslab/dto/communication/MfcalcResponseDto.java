package com.ncslab.dto.communication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.Map;

/**
 * DTO for MfcalcClient response handling
 * Provides type-safe representation of MFCalc server responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MfcalcResponseDto {
    
    @JsonProperty("message_type")
    private String messageType; // "run_script", "debug_script", "run_command", etc.
    
    @JsonProperty("message_id")
    private String messageId;
    
    @JsonProperty("status")
    private String status; // "success", "error", "timeout"
    
    @JsonProperty("data")
    private Object data; // Response data - can be various types
    
    @JsonProperty("error")
    private String error; // Error message if any
    
    @JsonProperty("output")
    private String output; // Console output
    
    @JsonProperty("variables")
    private List<Map<String, Object>> variables; // For getVariables() response
    
    @JsonProperty("variable_value")
    private Object variableValue; // For getVariable() response
    
    @JsonProperty("execution_time")
    private Long executionTime;
    
    @JsonProperty("breakpoints")
    private int[] breakpoints; // For debug responses
    
    /**
     * Check if response indicates success
     * @return true if successful
     */
    public boolean isSuccess() {
        return "success".equals(status) || status == null; // MFCalc might not always set status
    }
    
    /**
     * Check if response indicates error
     * @return true if error
     */
    public boolean isError() {
        return "error".equals(status) || error != null;
    }
    
    /**
     * Get error information
     * @return error message
     */
    public String getErrorInfo() {
        if (error != null && !error.trim().isEmpty()) {
            return error;
        }
        return isError() ? "Unknown MFCalc error" : null;
    }
    
    /**
     * Create MfcalcResponseDto from JSONObject response
     * @param jsonResponse JSONObject from MfcalcClient
     * @return MfcalcResponseDto instance
     */
    public static MfcalcResponseDto fromJsonObject(JSONObject jsonResponse) {
        if (jsonResponse == null) {
            return MfcalcResponseDto.builder()
                    .status("error")
                    .error("Null response from MFCalc server")
                    .build();
        }
        
        MfcalcResponseDto.MfcalcResponseDtoBuilder builder = MfcalcResponseDto.builder();
        
        // Extract basic fields
        if (jsonResponse.has("message_type")) {
            builder.messageType(jsonResponse.getString("message_type"));
        }
        if (jsonResponse.has("message_id")) {
            builder.messageId(jsonResponse.getString("message_id"));
        }
        if (jsonResponse.has("status")) {
            builder.status(jsonResponse.getString("status"));
        }
        if (jsonResponse.has("error")) {
            builder.error(jsonResponse.getString("error"));
        }
        if (jsonResponse.has("output")) {
            builder.output(jsonResponse.getString("output"));
        }
        if (jsonResponse.has("execution_time")) {
            builder.executionTime(jsonResponse.getLong("execution_time"));
        }
        
        // Handle data field - can be various types
        if (jsonResponse.has("data")) {
            Object dataObj = jsonResponse.get("data");
            builder.data(dataObj);
            
            // For getVariables() response, data is typically a JSONArray
            if (dataObj instanceof JSONArray) {
                JSONArray dataArray = (JSONArray) dataObj;
                builder.variables(dataArray.toList().stream()
                    .filter(obj -> obj instanceof Map)
                    .map(obj -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) obj;
                        return map;
                    })
                    .toList());
            }
        }
        
        // Handle breakpoints for debug responses
        if (jsonResponse.has("breakpoints")) {
            Object breakpointsObj = jsonResponse.get("breakpoints");
            if (breakpointsObj instanceof JSONArray) {
                JSONArray breakpointsArray = (JSONArray) breakpointsObj;
                int[] breakpoints = new int[breakpointsArray.length()];
                for (int i = 0; i < breakpointsArray.length(); i++) {
                    breakpoints[i] = breakpointsArray.getInt(i);
                }
                builder.breakpoints(breakpoints);
            }
        }
        
        return builder.build();
    }
    
    /**
     * Create success response for script execution
     * @param messageType Type of message
     * @param messageId Message ID
     * @param result Execution result
     * @param output Console output
     * @return Success MfcalcResponseDto
     */
    public static MfcalcResponseDto createScriptSuccess(String messageType, String messageId, Object result, String output) {
        return MfcalcResponseDto.builder()
                .messageType(messageType)
                .messageId(messageId)
                .status("success")
                .data(result)
                .output(output)
                .build();
    }
    
    /**
     * Create error response
     * @param messageType Type of message
     * @param messageId Message ID
     * @param error Error message
     * @return Error MfcalcResponseDto
     */
    public static MfcalcResponseDto createError(String messageType, String messageId, String error) {
        return MfcalcResponseDto.builder()
                .messageType(messageType)
                .messageId(messageId)
                .status("error")
                .error(error)
                .build();
    }
    
}