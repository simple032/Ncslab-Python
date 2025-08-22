package com.ncslab.dto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.json.JSONObject;

/**
 * DTO for MdlData (Model Data Container)
 * Main container for WebSocket model data with proper typed parsing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MdlDataDto {
    
    @JsonProperty("jsonData")
    private String jsonDataString; // Raw JSON string
    
    @JsonProperty("plantInfo")
    private PlantInfoDto plantInfo;
    
    // Parsed model data (transient - computed from jsonDataString)
    private ModelDataDto modelData;
    
    /**
     * Parse the jsonDataString into ModelDataDto
     * @param objectMapper Jackson ObjectMapper for parsing
     * @return Parsed ModelDataDto or null if parsing fails
     */
    public ModelDataDto getModelData(ObjectMapper objectMapper) {
        if (modelData == null && jsonDataString != null) {
            try {
                modelData = objectMapper.readValue(jsonDataString, ModelDataDto.class);
            } catch (JsonProcessingException e) {
                // Fallback - try to parse manually if needed
                System.err.println("Failed to parse jsonDataString: " + e.getMessage());
                return null;
            }
        }
        return modelData;
    }
    
    /**
     * Parse the jsonDataString into ModelDataDto using default ObjectMapper
     * @return Parsed ModelDataDto or null if parsing fails
     */
    public ModelDataDto getModelData() {
        return getModelData(new ObjectMapper());
    }
    
    /**
     * Set model data and update the jsonDataString
     * @param modelData ModelDataDto to set
     * @param objectMapper Jackson ObjectMapper for serialization
     */
    public void setModelData(ModelDataDto modelData, ObjectMapper objectMapper) {
        this.modelData = modelData;
        if (modelData != null) {
            try {
                this.jsonDataString = objectMapper.writeValueAsString(modelData);
            } catch (JsonProcessingException e) {
                System.err.println("Failed to serialize modelData: " + e.getMessage());
            }
        }
    }
    
    /**
     * Set model data and update the jsonDataString using default ObjectMapper
     * @param modelData ModelDataDto to set
     */
    public void setModelData(ModelDataDto modelData) {
        setModelData(modelData, new ObjectMapper());
    }
    
    /**
     * Create MdlDataDto from legacy Map structure
     * @param mdlDataMap Legacy Map<String, Object> structure
     * @return MdlDataDto or null if conversion fails
     */
    public static MdlDataDto fromLegacyMap(java.util.Map<String, Object> mdlDataMap) {
        if (mdlDataMap == null) {
            return null;
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            
            MdlDataDto mdlData = new MdlDataDto();
            
            // Extract jsonData string
            Object jsonDataObj = mdlDataMap.get("jsonData");
            if (jsonDataObj instanceof String) {
                mdlData.setJsonDataString((String) jsonDataObj);
            }
            
            // Extract and convert plantInfo
            Object plantInfoObj = mdlDataMap.get("plantInfo");
            if (plantInfoObj != null) {
                // Convert to JSON first, then to DTO
                String plantInfoJson = mapper.writeValueAsString(plantInfoObj);
                PlantInfoDto plantInfo = mapper.readValue(plantInfoJson, PlantInfoDto.class);
                mdlData.setPlantInfo(plantInfo);
            }
            
            return mdlData;
            
        } catch (Exception e) {
            System.err.println("Failed to convert legacy MdlData map: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Create MdlDataDto from legacy JSONObject
     * @param jsonObject Legacy JSONObject
     * @return MdlDataDto or null if conversion fails
     */
    public static MdlDataDto fromLegacyJson(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        
        try {
            // Convert JSONObject to Map first
            java.util.Map<String, Object> mdlDataMap = jsonObject.toMap();
            return fromLegacyMap(mdlDataMap);
            
        } catch (Exception e) {
            System.err.println("Failed to convert legacy MdlData JSON: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert to legacy Map format for backward compatibility
     * @return Map<String, Object> representation
     */
    public java.util.Map<String, Object> toLegacyMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        
        if (jsonDataString != null) {
            map.put("jsonData", jsonDataString);
        }
        
        if (plantInfo != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String plantInfoJson = mapper.writeValueAsString(plantInfo);
                // Convert back to map for legacy compatibility
                PlantInfoDto plantInfoCopy = mapper.readValue(plantInfoJson, PlantInfoDto.class);
                map.put("plantInfo", mapper.convertValue(plantInfoCopy, java.util.Map.class));
            } catch (JsonProcessingException e) {
                System.err.println("Failed to serialize plantInfo for legacy conversion: " + e.getMessage());
            }
        }
        
        return map;
    }
    
    /**
     * Get user ID from model data
     * @return User ID or null if not available
     */
    public Integer getUserId() {
        ModelDataDto model = getModelData();
        return model != null ? model.getUserId() : null;
    }
    
    /**
     * Get model ID from model data
     * @return Model ID or null if not available
     */
    public Integer getModelId() {
        ModelDataDto model = getModelData();
        return model != null ? model.getModelId() : null;
    }
    
    /**
     * Get model name from model data
     * @return Model name or null if not available
     */
    public String getModelName() {
        ModelDataDto model = getModelData();
        return model != null ? model.getModelName() : null;
    }
    
    /**
     * Validate the MdlDataDto structure
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        if (jsonDataString == null || jsonDataString.trim().isEmpty()) {
            return false;
        }
        
        // Try to parse model data to validate JSON structure
        ModelDataDto model = getModelData();
        if (model == null) {
            return false;
        }
        
        // Basic validation - must have essential fields
        return model.getUserId() != null && model.getModelId() != null;
    }
}