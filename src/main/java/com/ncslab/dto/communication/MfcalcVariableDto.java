package com.ncslab.dto.communication;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for MfcalcClient response handling
 * Provides type-safe representation of MFCalc server responses
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MfcalcVariableDto {
    @JsonProperty("name")
    private String name;
    @JsonProperty("value")
    private Object value;
    @JsonProperty("type")
    private String type;
    @JsonProperty("isHided")
    private int isHided;
}
