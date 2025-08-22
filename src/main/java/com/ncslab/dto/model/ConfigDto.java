package com.ncslab.dto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for configuration data from JSON with Jackson annotations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ConfigDto {
    
    @JsonProperty("Step")
    private String step; // "VariableStep" or "FixedStep"
    
    @JsonProperty("FixedStep")
    private Object fixedStep; // Can be string "auto" or numeric value
    
    @JsonProperty("Solver")
    private String solver; // "VariableStepAuto", "ode45", etc.
    
    @JsonProperty("StartTime")
    private String startTime;
    
    @JsonProperty("StopTime")
    private String stopTime;
    
    @JsonProperty("MaxDataPoints")
    private String maxDataPoints;
    
    @JsonProperty("MaxStep")
    private Object maxStep; // Can be string "auto" or numeric value
    
    @JsonProperty("MinStep")
    private Object minStep; // Can be string "auto" or numeric value
    
    @JsonProperty("InitialStep")
    private Object initialStep; // Can be string "auto" or numeric value
    
    @JsonProperty("RelTol")
    private Object relTol; // Can be string or numeric value
    
    @JsonProperty("AbsTol")
    private Object absTol; // Can be string "auto" or numeric value
    
    @JsonProperty("TemplateMakefile")
    private String templateMakefile;
    
    @JsonProperty("SystemTargetFile")
    private String systemTargetFile;
    
    // Default constructor for Jackson
    public ConfigDto() {}
    
    @Override
    public String toString() {
        return "ConfigDto{" +
                "step='" + step + '\'' +
                ", fixedStep=" + fixedStep +
                ", solver='" + solver + '\'' +
                ", startTime='" + startTime + '\'' +
                ", stopTime='" + stopTime + '\'' +
                ", maxDataPoints='" + maxDataPoints + '\'' +
                ", maxStep=" + maxStep +
                ", minStep=" + minStep +
                ", initialStep=" + initialStep +
                ", relTol=" + relTol +
                ", absTol=" + absTol +
                '}';
    }
}