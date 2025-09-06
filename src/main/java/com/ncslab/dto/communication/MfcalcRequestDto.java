package com.ncslab.dto.communication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.ncslab.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for MfcalcClient request handling
 * Provides type-safe representation of MFCalc server requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MfcalcRequestDto {
    
    @JsonProperty("message_type")
    private String messageType; // "run_script", "debug_script", "run_command", etc.
    
    @JsonProperty("message_id")
    private String messageId;
    
    @JsonProperty("data")
    private Map<String, Object> data; // Request data
    
    /**
     * Create request for running a script
     */
    public static MfcalcRequestDto createRunScript(String script) {
        Map<String, Object> data = new HashMap<>();
        data.put("script", script);
        
        return MfcalcRequestDto.builder()
                .messageType("run_script")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .data(data)
                .build();
    }
    
    /**
     * Create request for debugging a script
     */
    public static MfcalcRequestDto createDebugScript(String script, int[] breakpoints) {
        Map<String, Object> data = new HashMap<>();
        data.put("script", script);
        data.put("breakpoints", breakpoints);
        
        return MfcalcRequestDto.builder()
                .messageType("debug_script")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .data(data)
                .build();
    }
    
    /**
     * Create request for running a command
     */
    public static MfcalcRequestDto createRunCommand(String command) {
        Map<String, Object> data = new HashMap<>();
        data.put("command", command);
        
        return MfcalcRequestDto.builder()
                .messageType("run_command")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .data(data)
                .build();
    }
    
    /**
     * Create request for getting variables
     */
    public static MfcalcRequestDto createGetVariables() {
        return MfcalcRequestDto.builder()
                .messageType("get_variables")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .data(new HashMap<>())
                .build();
    }
    
    /**
     * Create request for getting a specific variable
     */
    public static MfcalcRequestDto createGetVariable(String variableName) {
        Map<String, Object> data = new HashMap<>();
        data.put("variable_name", variableName);
        
        return MfcalcRequestDto.builder()
                .messageType("get_variable")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .data(data)
                .build();
    }
    
    /**
     * Create request for setting a variable
     */
    public static MfcalcRequestDto createSetVariable(String variableName, Object value) {
        Map<String, Object> data = new HashMap<>();
        data.put("variable_name", variableName);
        data.put("value", value);
        
        return MfcalcRequestDto.builder()
                .messageType("set_variables")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .data(data)
                .build();
    }
    
    /**
     * Convert to JSON string for sending
     */
    public String toJsonString() {
        return JsonUtils.serializeDto(this);
    }
}