package com.ncslab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for block data from JSON with Jackson annotations.
 * Minimal implementation to support existing functionality.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class BlockJson {
    
    @JsonProperty("blockType")
    private String blockType;
    
    @JsonProperty("srcBlock")
    private String srcBlock;
    
    @JsonProperty("blockName")
    private String blockName;
    
    @JsonProperty("paramValues")
    private Map<String, Object> paramValues;
    
    @JsonProperty("blockPath")
    private String blockPath;
    
    @JsonProperty("blockUUID")
    private String blockUUID;
    
    // Default constructor for Jackson
    public BlockJson() {}
    
    // Convenience constructor
    public BlockJson(String blockType, String blockName, String blockPath) {
        this.blockType = blockType;
        this.blockName = blockName;
        this.blockPath = blockPath;
    }
    
    // Builder pattern for complex construction
    public static class Builder {
        private final BlockJson block = new BlockJson();
        
        public Builder blockType(String blockType) {
            block.blockType = blockType;
            return this;
        }
        
        public Builder blockName(String blockName) {
            block.blockName = blockName;
            return this;
        }
        
        public Builder blockPath(String blockPath) {
            block.blockPath = blockPath;
            return this;
        }
        
        public Builder blockUUID(String blockUUID) {
            block.blockUUID = blockUUID;
            return this;
        }
        
        public Builder srcBlock(String srcBlock) {
            block.srcBlock = srcBlock;
            return this;
        }
        
        public Builder paramValues(Map<String, Object> paramValues) {
            block.paramValues = paramValues;
            return this;
        }
        
        public Builder addParam(String key, Object value) {
            if (block.paramValues == null) {
                block.paramValues = new HashMap<>();
            }
            block.paramValues.put(key, value);
            return this;
        }
        
        public BlockJson build() {
            return block;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Conversion utility for legacy JSONObject  
    public static BlockJson fromLegacyJson(JSONObject jsonObject) {
        if (jsonObject == null) return null;
        
        try {
            BlockJson block = new BlockJson();
            block.blockType = jsonObject.optString("blockType");
            block.blockName = jsonObject.optString("blockName");
            block.blockPath = jsonObject.optString("blockPath");
            block.blockUUID = jsonObject.optString("blockUUID");
            block.srcBlock = jsonObject.optString("srcBlock");
            
            // Convert paramValues if present
            if (jsonObject.has("paramValues")) {
                JSONObject paramValues = jsonObject.optJSONObject("paramValues");
                if (paramValues != null) {
                    block.paramValues = new HashMap<>();
                    for (String key : paramValues.keySet()) {
                        block.paramValues.put(key, paramValues.get(key));
                    }
                }
            }
            
            return block;
        } catch (Exception e) {
            System.err.println("Failed to convert JSONObject to BlockJson: " + e.getMessage());
            return null;
        }
    }

    // 逆转换：将BlockJson转换为JSONObject
    /**
     * Convert to legacy JSONObject format
     * @deprecated Use Jackson serialization with JsonUtils.serializeDto() instead
     * @return JSONObject representation
     */
    @Deprecated
    public JSONObject toLegacyJson() {
        
        try {
            JSONObject jsonObject = new JSONObject();
            
            // 设置基本字段
            if (blockType != null) jsonObject.put("blockType", blockType);
            if (blockName != null) jsonObject.put("blockName", blockName);
            if (blockPath != null) jsonObject.put("blockPath", blockPath);
            if (blockUUID != null) jsonObject.put("blockUUID", blockUUID);
            if (srcBlock != null) jsonObject.put("srcBlock", srcBlock);
            
            // 处理paramValues映射 - 总是包含此字段，即使为空
            JSONObject paramValuesJson = new JSONObject();
            if (paramValues != null && !paramValues.isEmpty()) {
                for (Map.Entry<String, Object> entry : paramValues.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        paramValuesJson.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            jsonObject.put("paramValues", paramValuesJson); // Always include, even if empty
            
            return jsonObject;
        } catch (Exception e) {
            System.err.println("Failed to convert BlockJson to JSONObject for block: " + blockName);
            System.err.println("Error details: " + e.getMessage());
            System.err.println("BlockType: " + blockType);
            System.err.println("BlockPath: " + blockPath);
            System.err.println("ParamValues size: " + (paramValues != null ? paramValues.size() : "null"));
            e.printStackTrace();
            return null;
        }
    }
    
    // Validation methods
    public boolean isValid() {
        return blockType != null && !blockType.trim().isEmpty() &&
               blockName != null && !blockName.trim().isEmpty();
    }
    
    public String getValidationError() {
        if (blockType == null || blockType.trim().isEmpty()) {
            return "Block type is required";
        }
        if (blockName == null || blockName.trim().isEmpty()) {
            return "Block name is required";
        }
        return null;
    }
    
    // Parameter access helpers
    public Object getParam(String key) {
        return paramValues != null ? paramValues.get(key) : null;
    }
    
    public String getParamAsString(String key) {
        Object value = getParam(key);
        return value != null ? value.toString() : null;
    }
    
    public Double getParamAsDouble(String key) {
        Object value = getParam(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return value != null ? Double.parseDouble(value.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Integer getParamAsInteger(String key) {
        Object value = getParam(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value != null ? Integer.parseInt(value.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public boolean hasParam(String key) {
        return paramValues != null && paramValues.containsKey(key);
    }
    
    @Override
    public String toString() {
        return "BlockJson{" +
                "blockType='" + blockType + '\'' +
                ", blockName='" + blockName + '\'' +
                ", blockPath='" + blockPath + '\'' +
                ", blockUUID='" + blockUUID + '\'' +
                ", paramCount=" + (paramValues != null ? paramValues.size() : 0) +
                '}';
    }
}