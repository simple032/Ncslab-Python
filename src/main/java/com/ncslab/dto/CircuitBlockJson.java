package com.ncslab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

/**
 * DTO for Circuit Block JSON representation
 * Extends BlockJson with circuit-specific fields and methods
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CircuitBlockJson extends BlockJson {
    
    // Circuit-specific fields
    @JsonProperty("circuitType")
    private String circuitType; // "element", "elect", "io"
    
    @JsonProperty("blockModeType")
    private String blockModeType; // "Branch", "Link", "Anything"
    
    @JsonProperty("elementType")
    private String elementType; // "Resistor", "Capacitor", "Inductor", etc.
    
    @JsonProperty("voltage")
    private Double voltage;
    
    @JsonProperty("current")
    private Double current;
    
    @JsonProperty("resistance")
    private Double resistance;
    
    @JsonProperty("capacitance")
    private Double capacitance;
    
    @JsonProperty("inductance")
    private Double inductance;
    
    @JsonProperty("frequency")
    private Double frequency;
    
    @JsonProperty("amplitude")
    private Double amplitude;
    
    @JsonProperty("phase")
    private Double phase;
    
    // Circuit port information
    @JsonProperty("portCount")
    private Integer portCount;
    
    @JsonProperty("positivePort")
    private String positivePort;
    
    @JsonProperty("negativePort")
    private String negativePort;
    
    /**
     * Manual builder method for CircuitBlockJson
     * @return new CircuitBlockJson instance
     */
    public static CircuitBlockJson circuitBuilder() {
        return new CircuitBlockJson();
    }
    
    /**
     * Create CircuitBlockJson from legacy JSONObject
     * @param jsonObject Legacy JSONObject
     * @return CircuitBlockJson DTO or null if conversion fails
     */
    public static CircuitBlockJson fromLegacyJson(JSONObject jsonObject) {
        try {
            CircuitBlockJson circuitBlock = new CircuitBlockJson();
            
            // Copy base BlockJson fields
            BlockJson baseBlock = BlockJson.fromLegacyJson(jsonObject);
            if (baseBlock != null) {
                circuitBlock.setBlockType(baseBlock.getBlockType());
                circuitBlock.setSrcBlock(baseBlock.getSrcBlock());
                circuitBlock.setBlockName(baseBlock.getBlockName());
                circuitBlock.setBlockPath(baseBlock.getBlockPath());
                circuitBlock.setBlockUUID(baseBlock.getBlockUUID());
                circuitBlock.setParamValues(baseBlock.getParamValues());
            }
            
            // Extract circuit-specific fields from paramValues or direct JSON
            JSONObject paramValues = jsonObject.optJSONObject("paramValues");
            if (paramValues != null) {
                // Circuit element parameters
                if (paramValues.has("R")) {
                    circuitBlock.setResistance(paramValues.optDouble("R", 0.0));
                }
                if (paramValues.has("C")) {
                    circuitBlock.setCapacitance(paramValues.optDouble("C", 0.0));
                }
                if (paramValues.has("L")) {
                    circuitBlock.setInductance(paramValues.optDouble("L", 0.0));
                }
                if (paramValues.has("Voltage")) {
                    circuitBlock.setVoltage(paramValues.optDouble("Voltage", 0.0));
                }
                if (paramValues.has("Current")) {
                    circuitBlock.setCurrent(paramValues.optDouble("Current", 0.0));
                }
                if (paramValues.has("Frequency")) {
                    circuitBlock.setFrequency(paramValues.optDouble("Frequency", 0.0));
                }
                if (paramValues.has("Amplitude")) {
                    circuitBlock.setAmplitude(paramValues.optDouble("Amplitude", 0.0));
                }
                if (paramValues.has("Phase")) {
                    circuitBlock.setPhase(paramValues.optDouble("Phase", 0.0));
                }
            }
            
            // Determine circuit type from srcBlock
            String srcBlock = jsonObject.optString("srcBlock", "");
            if (srcBlock.startsWith("fl_lib")) {
                circuitBlock.setCircuitType("element");
                // Extract element type from srcBlock path
                String[] pathParts = srcBlock.split("/");
                if (pathParts.length > 2) {
                    circuitBlock.setElementType(pathParts[pathParts.length - 1]);
                }
            } else if (srcBlock.startsWith("elec_lib")) {
                circuitBlock.setCircuitType("elect");
            }
            
            // Default values
            circuitBlock.setBlockModeType("Anything");
            circuitBlock.setPortCount(2); // Most circuit elements have 2 ports
            
            return circuitBlock;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Check if this block is a circuit block (fl_lib or elec_lib)
     * @return true if this is a circuit block
     */
    public boolean isCircuitBlock() {
        String srcBlock = getSrcBlock();
        return srcBlock != null && (srcBlock.startsWith("fl_lib") || srcBlock.startsWith("elec_lib"));
    }
    
    /**
     * Get circuit element value based on type
     * @return element value or null if not applicable
     */
    public Double getElementValue() {
        if ("Resistor".equals(elementType)) {
            return resistance;
        } else if ("Capacitor".equals(elementType)) {
            return capacitance;
        } else if ("Inductor".equals(elementType)) {
            return inductance;
        }
        return null;
    }
    
    /**
     * Set circuit element value based on type
     * @param value Element value
     */
    public void setElementValue(Double value) {
        if ("Resistor".equals(elementType)) {
            setResistance(value);
        } else if ("Capacitor".equals(elementType)) {
            setCapacitance(value);
        } else if ("Inductor".equals(elementType)) {
            setInductance(value);
        }
    }
    
    /**
     * Create paramValues map with circuit-specific parameters
     * @return Map containing circuit parameters
     */
    public Map<String, Object> createCircuitParamValues() {
        Map<String, Object> params = new HashMap<>();
        
        // Add base paramValues if they exist
        if (getParamValues() != null) {
            params.putAll(getParamValues());
        }
        
        // Add circuit-specific parameters
        if (resistance != null) params.put("R", resistance);
        if (capacitance != null) params.put("C", capacitance);
        if (inductance != null) params.put("L", inductance);
        if (voltage != null) params.put("Voltage", voltage);
        if (current != null) params.put("Current", current);
        if (frequency != null) params.put("Frequency", frequency);
        if (amplitude != null) params.put("Amplitude", amplitude);
        if (phase != null) params.put("Phase", phase);
        
        return params;
    }
    
    /**
     * Validation method specific to circuit blocks
     * @return true if valid circuit block
     */
    public boolean isValidCircuitBlock() {
        if (!isValid()) {
            return false;
        }
        
        // Circuit blocks must have proper srcBlock
        if (!isCircuitBlock()) {
            return false;
        }
        
        // Element blocks should have at least one parameter
        if ("element".equals(circuitType)) {
            return getElementValue() != null;
        }
        
        return true;
    }
    
    /**
     * Get validation error message for circuit blocks
     * @return error message or null if valid
     */
    public String getCircuitValidationError() {
        String baseError = getValidationError();
        if (baseError != null) {
            return baseError;
        }
        
        if (!isCircuitBlock()) {
            return "Not a valid circuit block (must start with fl_lib or elec_lib)";
        }
        
        if ("element".equals(circuitType) && getElementValue() == null) {
            return "Circuit element must have a parameter value (R, C, or L)";
        }
        
        return null;
    }
}