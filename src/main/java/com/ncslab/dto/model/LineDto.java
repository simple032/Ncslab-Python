package com.ncslab.dto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

/**
 * DTO for line data from JSON with Jackson annotations.
 * Minimal implementation to support existing functionality.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class LineDto {
    
    @JsonProperty("fromBlockName")
    private String fromBlockName;
    
    @JsonProperty("fromPortNo")
    private Object fromPortNo;
    
    @JsonProperty("toBlockName")
    private String toBlockName;
    
    @JsonProperty("toPortNo")
    private Object toPortNo;
    
    @JsonProperty("linePath")
    private String linePath;
    
    @JsonProperty("fromBlockUUID")
    private String fromBlockUUID = "null";
    
    @JsonProperty("toBlockUUID")
    private String toBlockUUID = "null";
    
    // Default constructor for Jackson
    public LineDto() {}
    
    // Convenience constructor
    public LineDto(String fromBlockName, String toBlockName, String linePath) {
        this.fromBlockName = fromBlockName;
        this.toBlockName = toBlockName;
        this.linePath = linePath;
    }
    
    // Validation methods
    public boolean isValid() {
        return fromBlockName != null && !fromBlockName.trim().isEmpty() &&
               toBlockName != null && !toBlockName.trim().isEmpty();
    }
    
    public String getValidationError() {
        if (fromBlockName == null || fromBlockName.trim().isEmpty()) {
            return "From block name is required";
        }
        if (toBlockName == null || toBlockName.trim().isEmpty()) {
            return "To block name is required";
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "LineDto{" +
                "fromBlockName='" + fromBlockName + '\'' +
                ", fromPortNo='" + fromPortNo + '\'' +
                ", toBlockName='" + toBlockName + '\'' +
                ", toPortNo='" + toPortNo + '\'' +
                ", linePath='" + linePath + '\'' +
                '}';
    }
}