package com.ncslab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * DTO for the main model JSON structure with Jackson annotations.
 * Minimal implementation to support existing NCSLabModel functionality.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ModelJson {
    
    @JsonProperty("userId")
    private int userId;
    
    @JsonProperty("testRig")
    private int testRig;
    
    @JsonProperty("copyNum")
    private int copyNum;
    
    @JsonProperty("modelId")
    private int modelId;
    
    @JsonProperty("modelName")
    private String modelName;
    
    @JsonProperty("uuid")
    private long uuid;
    
    @JsonProperty("modelRealName")
    private String modelRealName;
    
    @JsonProperty("templateName")
    private String templateName;
    
    @JsonProperty("config")
    private ConfigJson config;
    
    @JsonProperty("blocks")
    private List<BlockJson> blocks;
    
    @JsonProperty("lines")
    private List<LineJson> lines;
    
    @JsonProperty("option")
    private Map<String, Object> option;
    
    @JsonProperty("saveInfo")
    private SaveInfoJson saveInfo;
    
    // Default constructor for Jackson
    public ModelJson() {}
    
    // Convenience methods
    public boolean hasBlocks() {
        return blocks != null && !blocks.isEmpty();
    }
    
    public boolean hasLines() {
        return lines != null && !lines.isEmpty();
    }
    
    // Builder pattern for complex construction
    public static class Builder {
        private final ModelJson model = new ModelJson();
        
        public Builder userId(int userId) {
            model.userId = userId;
            return this;
        }
        
        public Builder modelName(String modelName) {
            model.modelName = modelName;
            return this;
        }
        
        public Builder modelId(int modelId) {
            model.modelId = modelId;
            return this;
        }
        
        public Builder modelRealName(String modelRealName) {
            model.modelRealName = modelRealName;
            return this;
        }
        
        public Builder config(ConfigJson config) {
            model.config = config;
            return this;
        }
        
        public Builder blocks(List<BlockJson> blocks) {
            model.blocks = blocks;
            return this;
        }
        
        public Builder lines(List<LineJson> lines) {
            model.lines = lines;
            return this;
        }
        
        public ModelJson build() {
            return model;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Conversion utility for legacy JSONObject
    public static ModelJson fromLegacyJson(JSONObject jsonObject) {
        if (jsonObject == null) return null;
        
        try {
            // Use existing JsonUtils to parse from string
            return com.ncslab.util.JsonUtils.parseModelJson(jsonObject.toString());
        } catch (Exception e) {
            // Log error but don't throw - return null for graceful degradation
            System.err.println("Failed to convert JSONObject to ModelJson: " + e.getMessage());
            return null;
        }
    }
    
    // Validation methods
    public boolean isValid() {
        return modelName != null && !modelName.trim().isEmpty();
    }
    
    public String getValidationError() {
        if (modelName == null || modelName.trim().isEmpty()) {
            return "Model name is required";
        }
        if (userId <= 0) {
            return "Valid user ID is required";
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "ModelJson{" +
                "modelName='" + modelName + '\'' +
                ", userId=" + userId +
                ", modelId=" + modelId +
                ", blocksCount=" + (blocks != null ? blocks.size() : 0) +
                ", linesCount=" + (lines != null ? lines.size() : 0) +
                '}';
    }
}