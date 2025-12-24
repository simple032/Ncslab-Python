package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

/**
 * DTO for UI component messages from MFCalc server.
 * Represents the JSON structure sent by the network interface for UI components.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UIComponentMessage {

    /**
     * The function name that created this UI component (e.g., "uifigure", "uialert", "uiconfirm")
     */
    @JsonProperty("function")
    private String function;

    /**
     * Timestamp when the component was created
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * Component parameters including component_id, parent_id, and other properties
     */
    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    /**
     * Get the component ID from parameters
     * @return component ID if available, null otherwise
     */
    public String getComponentId() {
        if (parameters != null) {
            Object id = parameters.get("component_id");
            return id != null ? id.toString() : null;
        }
        return null;
    }

    /**
     * Get the parent ID from parameters
     * @return parent ID if available, null otherwise
     */
    public String getParentId() {
        if (parameters != null) {
            Object id = parameters.get("parent_id");
            return id != null ? id.toString() : null;
        }
        return null;
    }

    /**
     * Check if this component has a parent
     * @return true if parent_id exists in parameters
     */
    public boolean hasParent() {
        return getParentId() != null;
    }

    /**
     * Get a parameter value by key
     * @param key parameter key
     * @return parameter value, null if not found
     */
    public Object getParameter(String key) {
        if (parameters != null) {
            return parameters.get(key);
        }
        return null;
    }

    /**
     * Get a string parameter value by key
     * @param key parameter key
     * @return parameter value as string, null if not found
     */
    public String getStringParameter(String key) {
        Object value = getParameter(key);
        return value != null ? value.toString() : null;
    }
}