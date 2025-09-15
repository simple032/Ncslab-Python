package com.ncslab.dto.communication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for mfcalc servlet HTTP request handling
 * Provides type-safe representation of HTTP requests to mfcalc servlet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MfcalcServletRequestDto {
    
    @JsonProperty("data")
    private String data; // The main code/command to execute
    
    @JsonProperty("userId")
    private Integer userId; // User ID for client management
    
    @JsonProperty("method")
    private String method; // Method type: "runScript", "getVariables", etc.
    
    /**
     * Get method with default value
     * @return method string, defaults to "runScript" if null
     */
    public String getMethodOrDefault() {
        return method != null ? method : "runScript";
    }
    
    /**
     * Validate required fields
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return data != null && !data.trim().isEmpty() && userId != null;
    }
    
    /**
     * Get validation error message
     * @return error message if invalid, null if valid
     */
    public String getValidationError() {
        if (data == null || data.trim().isEmpty()) {
            return "Data field is required and cannot be empty";
        }
        if (userId == null) {
            return "UserId field is required";
        }
        return null;
    }
}