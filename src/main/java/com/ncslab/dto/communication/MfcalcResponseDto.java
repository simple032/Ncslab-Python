package com.ncslab.dto.communication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import com.ncslab.util.JsonUtils;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * DTO for MfcalcClient response handling
 * Provides type-safe representation of MFCalc server responses
 */
@Slf4j
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

    @JsonProperty("log")
    private String outputLog; // Log messages (which will be replaced by output in future)
    
    @JsonProperty("variables")
    private List<Map<String, Object>> variables; // For getVariables() response
    
    @JsonProperty("variable_value")
    private Object variableValue; // For getVariable() response
    
    @JsonProperty("execution_time")
    private Long executionTime;
    
    @JsonProperty("breakpoints")
    private int[] breakpoints; // For debug responses
    
    @JsonProperty("figures")
    private FiguresData figures; // For plot/figure data from MFCalc
    
    /**
     * Inner class to represent figures data structure
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FiguresData {
        @JsonProperty("version")
        private String version;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("figure_count")
        private Integer figureCount;
        
        @JsonProperty("figures")
        private List<Figure> figures;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Figure {
            @JsonProperty("figure_id")
            private Integer figureId;
            
            @JsonProperty("plot")
            private Plot plot;
            
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Plot {
                @JsonProperty("title")
                private String title;
                
                @JsonProperty("xlabel")
                private String xlabel;
                
                @JsonProperty("ylabel")
                private String ylabel;
                
                @JsonProperty("line_count")
                private Integer lineCount;
                
                @JsonProperty("lines")
                private List<Line> lines;
                
                @Data
                @NoArgsConstructor
                @AllArgsConstructor
                @Builder
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Line {
                    @JsonProperty("point_count")
                    private Integer pointCount;
                    
                    @JsonProperty("style")
                    private String style; // TODO: mfcalc server传输回来的字符串有问题
                    
                    @JsonProperty("x_data")
                    private double[] xData;
                    
                    @JsonProperty("y_data")
                    private double[] yData;
                }
            }
        }
    }
    
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
     * Create MfcalcResponseDto from JSON string response
     * @param jsonResponse JSON string from MfcalcClient
     * @return MfcalcResponseDto instance
     */
    public static MfcalcResponseDto fromJsonString(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            return MfcalcResponseDto.builder()
                    .status("error")
                    .error("Null or empty response from MFCalc server")
                    .build();
        }
        
        String trimmedResponse = jsonResponse.trim();
        
        // Check if response is just a plain number (non-JSON response)
        if (trimmedResponse.matches("^\\d+$")) {
            log.warn("MFCalc returned plain number response: {}", trimmedResponse);
            return MfcalcResponseDto.builder()
                    .status("error")
                    .error("Invalid response format from MFCalc: plain number " + trimmedResponse)
                    .build();
        }
        
        // Check if response doesn't look like JSON
        if (!trimmedResponse.startsWith("{") && !trimmedResponse.startsWith("[")) {
            log.warn("MFCalc returned non-JSON response: {}", 
                    trimmedResponse.length() > 100 ? trimmedResponse.substring(0, 100) + "..." : trimmedResponse);
            return MfcalcResponseDto.builder()
                    .status("error")
                    .error("Invalid response format from MFCalc: not a JSON object")
                    .build();
        }
        
        try {
            // Use Jackson to deserialize the JSON string directly to DTO
            MfcalcResponseDto dto = JsonUtils.deserializeDto(jsonResponse, MfcalcResponseDto.class);
            
            // Check if dto is null (shouldn't happen but defensive programming)
            if (dto == null) {
                log.error("JsonUtils.deserializeDto returned null for response: {}", 
                        jsonResponse.length() > 200 ? jsonResponse.substring(0, 200) + "..." : jsonResponse);
                return MfcalcResponseDto.builder()
                        .status("error")
                        .error("Failed to parse response: deserialization returned null")
                        .build();
            }
            
            // Post-process variables if data is a List
            if (dto.getData() instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> varList = (List<Map<String, Object>>) dto.getData();
                dto.setVariables(varList);
            }
            
            return dto;
        } catch (Exception e) {
            log.error("Failed to parse MFCalc response: {}", e.getMessage());
            
            // Check if it's a JSON parsing issue with corrupted data
            if (e.getMessage() != null && e.getMessage().contains("character escape sequence")) {
                log.error("Response appears to have corrupted/malformed JSON data");
                return MfcalcResponseDto.builder()
                        .status("error")
                        .error("Malformed JSON response from MFCalc: " + e.getMessage())
                        .build();
            }
            
            return MfcalcResponseDto.builder()
                    .status("error")
                    .error("Failed to parse response: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Create MfcalcResponseDto from JSONObject response (deprecated - for backward compatibility)
     * @param jsonResponse JSONObject from MfcalcClient
     * @return MfcalcResponseDto instance
     * @deprecated Use fromJsonString instead
     */
    @Deprecated
    public static MfcalcResponseDto fromJsonObject(org.json.JSONObject jsonResponse) {
        if (jsonResponse == null) {
            return MfcalcResponseDto.builder()
                    .status("error")
                    .error("Null response from MFCalc server")
                    .build();
        }
        return fromJsonString(jsonResponse.toString());
    }
    
    /**
     * Create success response for script execution
     * @param messageType Type of message
     * @param messageId Message ID
     * @param result Execution result
     * @param output Console output
     * @return Success MfcalcResponseDto
     */
    public static MfcalcResponseDto createScriptSuccess(String messageType, String messageId, JSONObject result, String output) {
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