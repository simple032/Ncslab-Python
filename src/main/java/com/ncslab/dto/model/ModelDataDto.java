package com.ncslab.dto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * DTO for Model Data structure (inner jsonData)
 * Represents the complete simulation model configuration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelDataDto {
    
    @JsonProperty("userId")
    private Integer userId;
    
    @JsonProperty("testRig")
    private Integer testRig;
    
    @JsonProperty("copyNum")
    private Integer copyNum;
    
    @JsonProperty("modelId")
    private Integer modelId;
    
    @JsonProperty("modelName")
    private String modelName;
    
    @JsonProperty("uuid")
    private Long uuid;
    
    @JsonProperty("modelRealName")
    private String modelRealName;
    
    @JsonProperty("templateName")
    private String templateName;
    
    @JsonProperty("config")
    private SimulationConfigDto config;
    
    @JsonProperty("blocks")
    private List<ModelBlockDto> blocks;
    
    @JsonProperty("lines")
    private List<ModelLineDto> lines;
    
    @JsonProperty("option")
    private Map<String, Object> option;
    
    @JsonProperty("saveInfo")
    private ModelSaveInfoDto saveInfo;
    
    /**
     * Simulation configuration DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SimulationConfigDto {
        @JsonProperty("Step")
        private String step;
        
        @JsonProperty("FixedStep")
        private String fixedStep;
        
        @JsonProperty("Solver")
        private String solver;
        
        @JsonProperty("StartTime")
        private String startTime;
        
        @JsonProperty("StopTime")
        private String stopTime;
        
        @JsonProperty("MaxDataPoints")
        private String maxDataPoints;
        
        @JsonProperty("MaxStep")
        private String maxStep;
        
        @JsonProperty("MinStep")
        private String minStep;
        
        @JsonProperty("InitialStep")
        private String initialStep;
        
        @JsonProperty("RelTol")
        private String relTol;
        
        @JsonProperty("AbsTol")
        private String absTol;
    }
    
    /**
     * Model block DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelBlockDto {
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
    }
    
    /**
     * Model line/connection DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelLineDto {
        @JsonProperty("fromBlockName")
        private String fromBlockName;
        
        @JsonProperty("fromPortNo")
        private String fromPortNo;
        
        @JsonProperty("toBlockName")
        private String toBlockName;
        
        @JsonProperty("toPortNo")
        private String toPortNo;
        
        @JsonProperty("linePath")
        private String linePath;
        
        @JsonProperty("fromBlockUUID")
        private String fromBlockUUID;
        
        @JsonProperty("toBlockUUID")
        private String toBlockUUID;
    }
    
    /**
     * Model save information DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelSaveInfoDto {
        @JsonProperty("uuid")
        private Long uuid;
        
        @JsonProperty("userId")
        private Integer userId;
        
        @JsonProperty("modelId")
        private Integer modelId;
        
        @JsonProperty("modelRealName")
        private String modelRealName;
        
        @JsonProperty("stepTime")
        private Double stepTime;
        
        @JsonProperty("packetSize")
        private Integer packetSize;
        
        @JsonProperty("targetPlatform")
        private Integer targetPlatform;
        
        @JsonProperty("publicFlag")
        private Integer publicFlag;
        
        @JsonProperty("testRig")
        private Integer testRig;
        
        @JsonProperty("copyNum")
        private Integer copyNum;
        
        @JsonProperty("description")
        private String description;
    }
}