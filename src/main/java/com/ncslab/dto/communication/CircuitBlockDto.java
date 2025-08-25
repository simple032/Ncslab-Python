package com.ncslab.dto.communication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * DTO for Circuit Block JSON representation
 * Extends BlockDto with circuit-specific fields and methods
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitBlockDto extends BlockDto {
    
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
     * Manual builder method for CircuitBlockDto
     * @return new CircuitBlockDto instance
     */
    public static CircuitBlockDto circuitBuilder() {
        return new CircuitBlockDto();
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
        
        // Add base parameters if they exist
        if (getParameters() != null) {
            for (Map.Entry<String, TypedParameter> entry : getParameters().entrySet()) {
                Object value = entry.getValue().getAsString();
                if (value != null) {
                    params.put(entry.getKey(), value);
                }
            }
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
        // Use the validation framework
        if (!validate().isValid()) {
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
        var validationResult = validate();
        if (!validationResult.isValid()) {
            return validationResult.getErrors().stream()
                    .map(error -> error.getMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Unknown validation error");
        }
        
        if (!isCircuitBlock()) {
            return "Not a valid circuit block (must start with fl_lib or elec_lib)";
        }
        
        if ("element".equals(circuitType) && getElementValue() == null) {
            return "Circuit element must have a parameter value (R, C, or L)";
        }
        
        return null;
    }
    
    /**
     * Implement the required abstract copy method from BlockDto
     * @return Deep copy of this CircuitBlockDto
     */
    @Override
    public BlockDto copy() {
        return CircuitBlockDto.builder()
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .srcBlock(getSrcBlock())
                .parameters(getParameters() != null ? new HashMap<>(getParameters()) : null)
                .position(getPosition())
                .appearance(getAppearance())
                .inputPorts(getInputPorts() != null ? new ArrayList<>(getInputPorts()) : null)
                .outputPorts(getOutputPorts() != null ? new ArrayList<>(getOutputPorts()) : null)
                .sampleTime(getSampleTime())
                .priority(getPriority())
                .description(getDescription())
                .tags(getTags() != null ? new HashSet<>(getTags()) : null)
                .customProperties(getCustomProperties() != null ? new HashMap<>(getCustomProperties()) : null)
                .createdAt(getCreatedAt())
                .modifiedAt(getModifiedAt())
                .version(getVersion())
                .circuitType(circuitType)
                .blockModeType(blockModeType)
                .elementType(elementType)
                .voltage(voltage)
                .current(current)
                .resistance(resistance)
                .capacitance(capacitance)
                .inductance(inductance)
                .frequency(frequency)
                .amplitude(amplitude)
                .phase(phase)
                .portCount(portCount)
                .positivePort(positivePort)
                .negativePort(negativePort)
                .build();
    }
    
    /**
     * Create CircuitBlockDto from legacy BlockDto
     * @param blockParsingDto Legacy block representation
     * @return Converted CircuitBlockDto
     */
    public static CircuitBlockDto fromLegacyBlockDto(BlockDto blockParsingDto) {
        if (blockParsingDto == null) {
            return null;
        }
        
        CircuitBlockDto circuitBlock = new CircuitBlockDto();
        
        // Copy base fields
        circuitBlock.setSrcBlock(blockParsingDto.getSrcBlock());
        circuitBlock.setBlockName(blockParsingDto.getBlockName());
        circuitBlock.setBlockPath(blockParsingDto.getBlockPath());
        circuitBlock.setBlockUUID(blockParsingDto.getBlockUUID());
        
        // Convert paramValues to typed parameters
        if (blockParsingDto.getParamValues() != null) {
            Map<String, TypedParameter> typedParams = new HashMap<>();
            for (Map.Entry<String, Object> entry : blockParsingDto.getParamValues().entrySet()) {
                typedParams.put(entry.getKey(), TypedParameter.of(entry.getValue()));
            }
            circuitBlock.setParameters(typedParams);
            
            // Extract circuit-specific fields
            Map<String, Object> paramValues = blockParsingDto.getParamValues();
            if (paramValues.containsKey("R")) {
                Object value = paramValues.get("R");
                circuitBlock.setResistance(value instanceof Number ? ((Number) value).doubleValue() : null);
            }
            if (paramValues.containsKey("C")) {
                Object value = paramValues.get("C");
                circuitBlock.setCapacitance(value instanceof Number ? ((Number) value).doubleValue() : null);
            }
            if (paramValues.containsKey("L")) {
                Object value = paramValues.get("L");
                circuitBlock.setInductance(value instanceof Number ? ((Number) value).doubleValue() : null);
            }
            if (paramValues.containsKey("Voltage")) {
                Object value = paramValues.get("Voltage");
                circuitBlock.setVoltage(value instanceof Number ? ((Number) value).doubleValue() : null);
            }
            if (paramValues.containsKey("Current")) {
                Object value = paramValues.get("Current");
                circuitBlock.setCurrent(value instanceof Number ? ((Number) value).doubleValue() : null);
            }
            if (paramValues.containsKey("Frequency")) {
                Object value = paramValues.get("Frequency");
                circuitBlock.setFrequency(value instanceof Number ? ((Number) value).doubleValue() : null);
            }
            if (paramValues.containsKey("Amplitude")) {
                Object value = paramValues.get("Amplitude");
                circuitBlock.setAmplitude(value instanceof Number ? ((Number) value).doubleValue() : null);
            }
            if (paramValues.containsKey("Phase")) {
                Object value = paramValues.get("Phase");
                circuitBlock.setPhase(value instanceof Number ? ((Number) value).doubleValue() : null);
            }
        }
        
        // Determine circuit type from srcBlock
        String srcBlock = blockParsingDto.getSrcBlock();
        if (srcBlock != null) {
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
        }
        
        // Default values
        circuitBlock.setBlockModeType("Anything");
        circuitBlock.setPortCount(2); // Most circuit elements have 2 ports
        
        return circuitBlock;
    }
}