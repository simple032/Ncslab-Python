package com.ncslab.dto.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncslab.dto.model.ConfigDto;
import com.ncslab.dto.model.LineDto;
import com.ncslab.dto.model.SaveInfoDto;
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
public class ModelDto {
    
    @JsonProperty("userName")
    private String userName;

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
    private ConfigDto config;
    
    @JsonProperty("blocks")
    private List<BlockDto> blocks;
    
    @JsonProperty("lines")
    private List<LineDto> lines;
    
    @JsonProperty("option")
    private Map<String, Object> option;
    
    @JsonProperty("saveInfo")
    private SaveInfoDto saveInfo;
    
    // Default constructor for Jackson
    public ModelDto() {}
    
    // Convenience methods
    public boolean hasBlocks() {
        return blocks != null && !blocks.isEmpty();
    }
    
    public boolean hasLines() {
        return lines != null && !lines.isEmpty();
    }
    
    // Builder pattern for complex construction
    public static class Builder {
        private final ModelDto model = new ModelDto();
        
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
        
        public Builder config(ConfigDto config) {
            model.config = config;
            return this;
        }
        
        public Builder blocks(List<BlockDto> blocks) {
            model.blocks = blocks;
            return this;
        }
        
        public Builder lines(List<LineDto> lines) {
            model.lines = lines;
            return this;
        }
        
        public ModelDto build() {
            return model;
        }
    }
    
    public static Builder builder() {
        return new Builder();
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
        return "ModelDto{" +
                "modelName='" + modelName + '\'' +
                ", userId=" + userId +
                ", modelId=" + modelId +
                ", blocksCount=" + (blocks != null ? blocks.size() : 0) +
                ", linesCount=" + (lines != null ? lines.size() : 0) +
                '}';
    }
}