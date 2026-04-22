package com.ncslab.dto.block.specialized.stateflow;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.annotations.MigrationCompatible;
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
 * <p>Stateflow Chart is a special subsystem block that contains a state machine
 * with states, transitions, variables, and events. It is similar to Simulink's
 * Stateflow Chart block.</p>
 * 
 * <p>The {@code stateflowData} field contains the complete statechart definition
 * in a structured format aligned with the frontend {@code StatechartData}:</p>
 * <pre>
 * {
 *   "id": "chart-xxx",
 *   "name": "Chart",
 *   "properties": { ... },
 *   "variables": [ ... ],
 *   "events": [ ... ],
 *   "states": [ ... ],
 *   "transitions": [ ... ]
 * }
 * </pre>
 * 
 * @author NCSLab Development Team
 * @version 1.0
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
     * Aligned with frontend StatechartData format.
     */
    private Map<String, Object> stateflowData;

    /**
     * Convenience accessor: chart name from stateflowData.
     */
    public String getChartName() {
        if (stateflowData == null) return null;
        Object name = stateflowData.get("name");
        return name != null ? name.toString() : null;
    }

    /**
     * Convenience accessor: chart ID from stateflowData.
     */
    public String getChartId() {
        if (stateflowData == null) return null;
        Object id = stateflowData.get("id");
        return id != null ? id.toString() : null;
    }

    /**
     * Convenience accessor: variables list from stateflowData.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getVariables() {
        if (stateflowData == null) return null;
        Object vars = stateflowData.get("variables");
        if (vars instanceof List) {
            return (List<Map<String, Object>>) vars;
        }
        return null;
    }

    /**
     * Convenience accessor: events list from stateflowData.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getEvents() {
        if (stateflowData == null) return null;
        Object evts = stateflowData.get("events");
        if (evts instanceof List) {
            return (List<Map<String, Object>>) evts;
        }
        return null;
    }

    /**
     * Convenience accessor: states list from stateflowData.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getStates() {
        if (stateflowData == null) return null;
        Object sts = stateflowData.get("states");
        if (sts instanceof List) {
            return (List<Map<String, Object>>) sts;
        }
        return null;
    }

    /**
     * Convenience accessor: transitions list from stateflowData.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTransitions() {
        if (stateflowData == null) return null;
        Object trans = stateflowData.get("transitions");
        if (trans instanceof List) {
            return (List<Map<String, Object>>) trans;
        }
        return null;
    }

    /**
     * Convenience accessor: properties from stateflowData.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getChartProperties() {
        if (stateflowData == null) return null;
        Object props = stateflowData.get("properties");
        if (props instanceof Map) {
            return (Map<String, Object>) props;
        }
        return null;
    }
}
