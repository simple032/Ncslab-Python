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

    @JsonProperty("user_id")
    private String userId; // User ID for maintaining user-specific context

    @JsonProperty("data")
    private Map<String, Object> data; // Request data
    
    /**
     * Create request for running a script
     * @param script The script to run
     * @param userId User ID for maintaining user-specific context
     */
    public static MfcalcRequestDto createRunScript(String script, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("script", script);

        return MfcalcRequestDto.builder()
                .messageType("run_script")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .userId(userId)
                .data(data)
                .build();
    }

    /**
     * Create request for running a script (without userId for backward compatibility)
     * @deprecated Use createRunScript(String script, String userId) instead
     */
    @Deprecated
    public static MfcalcRequestDto createRunScript(String script) {
        return createRunScript(script, null);
    }
    
    /**
     * Create request for debugging a script
     * @param script The script to debug
     * @param breakpoints Array of breakpoint line numbers
     * @param userId User ID for maintaining user-specific context
     */
    public static MfcalcRequestDto createDebugScript(String script, int[] breakpoints, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("script", script);
        data.put("breakpoints", breakpoints);

        return MfcalcRequestDto.builder()
                .messageType("debug_script")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .userId(userId)
                .data(data)
                .build();
    }

    /**
     * Create request for debugging a script (without userId for backward compatibility)
     * @deprecated Use createDebugScript(String script, int[] breakpoints, String userId) instead
     */
    @Deprecated
    public static MfcalcRequestDto createDebugScript(String script, int[] breakpoints) {
        return createDebugScript(script, breakpoints, null);
    }
    
    /**
     * Create request for running a command
     * @param command The command to run
     * @param userId User ID for maintaining user-specific context
     */
    public static MfcalcRequestDto createRunCommand(String command, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("command", command);

        return MfcalcRequestDto.builder()
                .messageType("run_command")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .userId(userId)
                .data(data)
                .build();
    }

    /**
     * Create request for running a command (without userId for backward compatibility)
     * @deprecated Use createRunCommand(String command, String userId) instead
     */
    @Deprecated
    public static MfcalcRequestDto createRunCommand(String command) {
        return createRunCommand(command, null);
    }
    
    /**
     * Create request for getting variables
     * @param userId User ID for maintaining user-specific context
     */
    public static MfcalcRequestDto createGetVariables(String userId) {
        return MfcalcRequestDto.builder()
                .messageType("get_variables")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .userId(userId)
                .data(new HashMap<>())
                .build();
    }

    /**
     * Create request for getting variables (without userId for backward compatibility)
     * @deprecated Use createGetVariables(String userId) instead
     */
    @Deprecated
    public static MfcalcRequestDto createGetVariables() {
        return createGetVariables(null);
    }
    
    /**
     * Create request for getting a specific variable
     * @param variableName The name of the variable to get
     * @param userId User ID for maintaining user-specific context
     */
    public static MfcalcRequestDto createGetVariable(String variableName, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("variable_name", variableName);

        return MfcalcRequestDto.builder()
                .messageType("get_variable")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .userId(userId)
                .data(data)
                .build();
    }

    /**
     * Create request for getting a specific variable (without userId for backward compatibility)
     * @deprecated Use createGetVariable(String variableName, String userId) instead
     */
    @Deprecated
    public static MfcalcRequestDto createGetVariable(String variableName) {
        return createGetVariable(variableName, null);
    }
    
    /**
     * Create request for setting a variable
     * @param variableName The name of the variable to set
     * @param value The value to set
     * @param userId User ID for maintaining user-specific context
     */
    public static MfcalcRequestDto createSetVariable(String variableName, Object value, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("variable_name", variableName);
        data.put("value", value);

        return MfcalcRequestDto.builder()
                .messageType("set_variables")
                .messageId(String.valueOf(System.currentTimeMillis()))
                .userId(userId)
                .data(data)
                .build();
    }

    /**
     * Create request for setting a variable (without userId for backward compatibility)
     * @deprecated Use createSetVariable(String variableName, Object value, String userId) instead
     */
    @Deprecated
    public static MfcalcRequestDto createSetVariable(String variableName, Object value) {
        return createSetVariable(variableName, value, null);
    }
    
    /**
     * Convert to JSON string for sending
     */
    public String toJsonString() {
        return JsonUtils.serializeDto(this);
    }
}