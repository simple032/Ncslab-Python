package com.ncslab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class LineJson {
    
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
    private String fromBlockUUID;
    
    @JsonProperty("toBlockUUID")
    private String toBlockUUID;
    
    // Default constructor for Jackson
    public LineJson() {}
    
    // Convenience constructor
    public LineJson(String fromBlockName, String toBlockName, String linePath) {
        this.fromBlockName = fromBlockName;
        this.toBlockName = toBlockName;
        this.linePath = linePath;
    }
    
    // Conversion utility for legacy JSONObject
    public static LineJson fromLegacyJson(JSONObject jsonObject) {
        if (jsonObject == null) return null;
        
        try {
            LineJson line = new LineJson();
            line.fromBlockName = jsonObject.optString("fromBlockName");
            line.toBlockName = jsonObject.optString("toBlockName");
            line.linePath = jsonObject.optString("linePath");
            line.fromBlockUUID = jsonObject.optString("fromBlockUUID");
            line.toBlockUUID = jsonObject.optString("toBlockUUID");
            
            // Handle port numbers - they can be strings or integers
            if (jsonObject.has("fromPortNo")) {
                line.fromPortNo = jsonObject.get("fromPortNo");
            }
            if (jsonObject.has("toPortNo")) {
                line.toPortNo = jsonObject.get("toPortNo");
            }
            
            return line;
        } catch (Exception e) {
            System.err.println("Failed to convert JSONObject to LineJson: " + e.getMessage());
            return null;
        }
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
        return "LineJson{" +
                "fromBlockName='" + fromBlockName + '\'' +
                ", fromPortNo='" + fromPortNo + '\'' +
                ", toBlockName='" + toBlockName + '\'' +
                ", toPortNo='" + toPortNo + '\'' +
                ", linePath='" + linePath + '\'' +
                '}';
    }
}