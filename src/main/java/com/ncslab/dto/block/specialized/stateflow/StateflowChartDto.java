package com.ncslab.dto.block.specialized.stateflow;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.block.specialized.stateflow.data.StateflowDataDto;
import com.ncslab.dto.block.specialized.stateflow.data.VariableDto;
import com.ncslab.dto.block.specialized.stateflow.data.EventDto;
import com.ncslab.dto.block.specialized.stateflow.data.ChartPropertiesDto;
import com.ncslab.dto.block.specialized.stateflow.data.StateDto;
import com.ncslab.dto.block.specialized.stateflow.data.TransitionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * DTO for Stateflow Chart block.
 *
 * @author NCSLab Development Team
 * @version 1.1
 * @since 2025-04
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("StateflowChart")
@MigrationCompatible(originalClass = "com.ncslab.block.stateflow.StateflowChart")
public class StateflowChartDto extends BlockDto {

    /**
     * Complete statechart data structured for backend consumption.
     */
    private StateflowDataDto stateflowData;

    public String getChartName() {
        return stateflowData != null ? stateflowData.getName() : null;
    }

    public String getChartId() {
        return stateflowData != null ? stateflowData.getId() : null;
    }

    public List<VariableDto> getVariables() {
        return stateflowData != null ? stateflowData.getVariables() : null;
    }

    public List<EventDto> getEvents() {
        return stateflowData != null ? stateflowData.getEvents() : null;
    }

    public ChartPropertiesDto getChartProperties() {
        return stateflowData != null ? stateflowData.getProperties() : null;
    }

    public List<Map<String, Object>> getCells() {
        return stateflowData != null ? stateflowData.getCells() : null;
    }

    public List<StateDto> getStates() {
        return stateflowData != null ? stateflowData.getStates() : null;
    }

    public List<TransitionDto> getTransitions() {
        return stateflowData != null ? stateflowData.getTransitions() : null;
    }

    @Deprecated
    public Map<String, Object> getStateflowDataAsMap() {
        return null;
    }
}
