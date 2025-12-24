package com.ncslab.dto.simulation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for simulation results.json structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResultsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("version")
    private String version;

    @JsonProperty("scopes")
    private List<ScopeDataDto> scopes;
}
