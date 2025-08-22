package com.ncslab.dto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for save info data from JSON with Jackson annotations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class SaveInfoDto {
    
    @JsonProperty("modelRealName")
    private String modelRealName;
    
    @JsonProperty("copyNum")
    private int copyNum;
    
    @JsonProperty("modelId")
    private int modelId;
    
    @JsonProperty("publicFlag")
    private int publicFlag;
    
    @JsonProperty("testRig")
    private int testRig;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("packetSize")
    private int packetSize;
    
    @JsonProperty("stepTime")
    private double stepTime;
    
    @JsonProperty("targetPlatform")
    private int targetPlatform;
    
    @JsonProperty("uuid")
    private long uuid;
    
    @JsonProperty("userId")
    private int userId;
    
    // Default constructor for Jackson
    public SaveInfoDto() {}
    
    @Override
    public String toString() {
        return "SaveInfoDto{" +
                "modelRealName='" + modelRealName + '\'' +
                ", modelId=" + modelId +
                ", userId=" + userId +
                ", uuid=" + uuid +
                ", testRig=" + testRig +
                '}';
    }
}