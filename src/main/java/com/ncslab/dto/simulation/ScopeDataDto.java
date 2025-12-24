package com.ncslab.dto.simulation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for individual scope data from simulation results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScopeDataDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    private String name;

    @JsonProperty("path")
    private String path;

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("width")
    private int width;

    @JsonProperty("height")
    private int height;

    @JsonProperty("length")
    private int length;

    @JsonProperty("time")
    private List<Double> time;

    @JsonProperty("data")
    private List<Double> data;
}
