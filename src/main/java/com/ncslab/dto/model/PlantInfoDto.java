package com.ncslab.dto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * DTO for Plant Information
 * Represents hardware/plant configuration for simulation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantInfoDto {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("nameCN")
    private String nameCN;
    
    @JsonProperty("nameEN")
    private String nameEN;
    
    @JsonProperty("labid")
    private Integer labid;
    
    @JsonProperty("ip")
    private String ip;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("statusCode")
    private Integer statusCode;
    
    @JsonProperty("manager")
    private Integer manager;
    
    @JsonProperty("order")
    private Integer order;
    
    @JsonProperty("downloadPort")
    private Integer downloadPort;
    
    @JsonProperty("monitorPort")
    private Integer monitorPort;
    
    @JsonProperty("currentUser")
    private Integer currentUser;
    
    @JsonProperty("currentUsers")
    private String currentUsers; // JSON string representation of user array
    
    @JsonProperty("type")
    private Integer type;
    
    @JsonProperty("num")
    private Integer num;
    
    @JsonProperty("stepSize")
    private Double stepSize;
    
    @JsonProperty("matlabExt")
    private String matlabExt;
    
    @JsonProperty("shareCode")
    private Integer shareCode;
    
    @JsonProperty("iconId")
    private String iconId;
    
    @JsonProperty("threeDModel")
    private String threeDModel;
    
    @JsonProperty("serverUrl")
    private String serverUrl;
    
    @JsonProperty("protocols")
    private String protocols;
    
    @JsonProperty("cameras")
    private List<PlantCameraDto> cameras;
    
    @JsonProperty("endpoints")
    private List<PlantEndpointDto> endpoints;
    
    /**
     * Plant camera configuration DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlantCameraDto {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("url")
        private String url;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("enabled")
        private Boolean enabled;
    }
    
    /**
     * Plant endpoint configuration DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlantEndpointDto {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("url")
        private String url;
        
        @JsonProperty("method")
        private String method;
        
        @JsonProperty("enabled")
        private Boolean enabled;
    }
}