package com.ncslab.dto.block.specialized.continuous;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO representation of Integrator block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the Integrator block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - InitialCondition: Initial output value at t=0
 * - ExternalReset: External reset mode (none, rising, falling, either, level, sampled level)
 * - InitialConditionSource: Source of initial condition (internal, external)
 * - LimitOutput: Whether to limit output values
 * - UpperSaturationLimit: Upper limit for output
 * - LowerSaturationLimit: Lower limit for output
 * - ShowSaturationPort: Show saturation status port
 * - ShowStatePort: Show state output port
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 5
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MigrationCompatible(originalClass = "com.ncslab.block.continuous.Integrator")
public class IntegratorDto extends BlockDto {
    
    // ===== INTEGRATOR BLOCK SPECIFIC PARAMETERS =====
    
    /**
     * Initial output value at t=0
     * Default: 0
     * Validation: Must be finite
     */
    private TypedParameter initialCondition;
    
    /**
     * External reset mode
     * Default: "none"
     * Options: "none", "rising", "falling", "either", "level", "sampled level"
     */
    private TypedParameter externalReset;
    
    /**
     * Source of initial condition
     * Default: "internal"
     * Options: "internal", "external"
     */
    private TypedParameter initialConditionSource;
    
    /**
     * Whether to limit output values
     * Default: false (off)
     */
    private TypedParameter limitOutput;
    
    /**
     * Upper limit for output
     * Default: Double.POSITIVE_INFINITY
     */
    private TypedParameter upperSaturationLimit;
    
    /**
     * Lower limit for output
     * Default: Double.NEGATIVE_INFINITY
     */
    private TypedParameter lowerSaturationLimit;
    
    /**
     * Show saturation status port
     * Default: false (off)
     */
    private TypedParameter showSaturationPort;
    
    /**
     * Show state output port
     * Default: false (off)
     */
    private TypedParameter showStatePort;
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    private TypedParameter outDataTypeStr;
    
    /**
     * Handle integer overflow
     * Default: false (off)
     */
    private TypedParameter saturateOnIntegerOverflow;
    
    // ===== FACTORY METHODS =====
    
    /**
     * Create DTO from legacy parameters map (for migration support)
     */
    public static IntegratorDto fromLegacyParameters(java.util.Map<String, String> params) {
        IntegratorDto.IntegratorDtoBuilder builder = IntegratorDto.builder()
                .blockName(params.getOrDefault("blockName", "Integrator"))
                .blockPath(params.getOrDefault("blockPath", ""))
                .blockUUID(params.getOrDefault("blockUUID", ""))
                .blockType("Integrator");
        
        IntegratorDto dto = builder.build();
        
        // Map parameters to TypedParameter
        if (params.containsKey("InitialCondition")) {
            try {
                dto.initialCondition = TypedParameter.of(Double.parseDouble(params.get("InitialCondition")));
            } catch (NumberFormatException e) {
                dto.initialCondition = TypedParameter.of(params.get("InitialCondition")); // Keep as string if not numeric
            }
        }
        
        if (params.containsKey("ExternalReset")) {
            dto.externalReset = TypedParameter.of(params.get("ExternalReset"));
        }
        
        if (params.containsKey("InitialConditionSource")) {
            dto.initialConditionSource = TypedParameter.of(params.get("InitialConditionSource"));
        }
        
        if (params.containsKey("LimitOutput")) {
            dto.limitOutput = TypedParameter.of("on".equals(params.get("LimitOutput")));
        }
        
        if (params.containsKey("UpperSaturationLimit")) {
            String upperStr = params.get("UpperSaturationLimit");
            Double upperVal = "inf".equals(upperStr) ? Double.POSITIVE_INFINITY : Double.parseDouble(upperStr);
            dto.upperSaturationLimit = TypedParameter.of(upperVal);
        }
        
        if (params.containsKey("LowerSaturationLimit")) {
            String lowerStr = params.get("LowerSaturationLimit");
            Double lowerVal = "-inf".equals(lowerStr) ? Double.NEGATIVE_INFINITY : Double.parseDouble(lowerStr);
            dto.lowerSaturationLimit = TypedParameter.of(lowerVal);
        }
        
        if (params.containsKey("ShowSaturationPort")) {
            dto.showSaturationPort = TypedParameter.of("on".equals(params.get("ShowSaturationPort")));
        }
        
        if (params.containsKey("ShowStatePort")) {
            dto.showStatePort = TypedParameter.of("on".equals(params.get("ShowStatePort")));
        }
        
        if (params.containsKey("OutDataTypeStr")) {
            dto.outDataTypeStr = TypedParameter.of(params.get("OutDataTypeStr"));
        }
        
        if (params.containsKey("SaturateOnIntegerOverflow")) {
            dto.saturateOnIntegerOverflow = TypedParameter.of("on".equals(params.get("SaturateOnIntegerOverflow")));
        }
        
        return dto;
    }
    
    /**
     * Create builder with SIMULINK-compatible defaults
     */
    public static IntegratorDtoBuilder builderWithDefaults() {
        IntegratorDto dto = IntegratorDto.builder()
                .blockType("Integrator")
                .sampleTime(0.0)
                .build();
        
        // Set default typed parameters
        dto.initialCondition = TypedParameter.of(0.0);
        dto.externalReset = TypedParameter.of("none");
        dto.initialConditionSource = TypedParameter.of("internal");
        dto.limitOutput = TypedParameter.of(false);
        dto.upperSaturationLimit = TypedParameter.of(Double.POSITIVE_INFINITY);
        dto.lowerSaturationLimit = TypedParameter.of(Double.NEGATIVE_INFINITY);
        dto.showSaturationPort = TypedParameter.of(false);
        dto.showStatePort = TypedParameter.of(false);
        dto.outDataTypeStr = TypedParameter.of("Inherit: Same as input");
        dto.saturateOnIntegerOverflow = TypedParameter.of(false);
        
        return IntegratorDto.builder()
                .blockId(dto.getBlockId())
                .blockType(dto.getBlockType())
                .blockName(dto.getBlockName())
                .blockPath(dto.getBlockPath())
                .blockUUID(dto.getBlockUUID())
                .sampleTime(dto.getSampleTime())
                .initialCondition(dto.initialCondition)
                .externalReset(dto.externalReset)
                .initialConditionSource(dto.initialConditionSource)
                .limitOutput(dto.limitOutput)
                .upperSaturationLimit(dto.upperSaturationLimit)
                .lowerSaturationLimit(dto.lowerSaturationLimit)
                .showSaturationPort(dto.showSaturationPort)
                .showStatePort(dto.showStatePort)
                .outDataTypeStr(dto.outDataTypeStr)
                .saturateOnIntegerOverflow(dto.saturateOnIntegerOverflow);
    }
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getInitialConditionAsDouble() {
        return initialCondition != null ? initialCondition.getValue(Double.class) : 0.0;
    }
    
    public String getExternalResetValue() {
        return externalReset != null ? externalReset.getValue(String.class) : "none";
    }
    
    public String getInitialConditionSourceValue() {
        return initialConditionSource != null ? initialConditionSource.getValue(String.class) : "internal";
    }
    
    public Boolean getLimitOutputValue() {
        return limitOutput != null ? limitOutput.getValue(Boolean.class) : false;
    }
    
    public Double getUpperSaturationLimitValue() {
        return upperSaturationLimit != null ? upperSaturationLimit.getValue(Double.class) : Double.POSITIVE_INFINITY;
    }
    
    public Double getLowerSaturationLimitValue() {
        return lowerSaturationLimit != null ? lowerSaturationLimit.getValue(Double.class) : Double.NEGATIVE_INFINITY;
    }
    
    public Boolean getShowSaturationPortValue() {
        return showSaturationPort != null ? showSaturationPort.getValue(Boolean.class) : false;
    }
    
    public Boolean getShowStatePortValue() {
        return showStatePort != null ? showStatePort.getValue(Boolean.class) : false;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getValue(String.class) : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getValue(Boolean.class) : false;
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation
        
        // Validate initial condition
        if (initialCondition != null) {
            Object icValue = initialCondition.getValue();
            if (icValue instanceof Number) {
                double numValue = ((Number) icValue).doubleValue();
                if (Double.isNaN(numValue) || Double.isInfinite(numValue)) {
                    result.addError("Initial condition must be finite");
                }
            }
        }
        
        // Validate external reset mode
        if (externalReset != null) {
            String resetMode = externalReset.getValue(String.class);
            if (resetMode != null) {
                List<String> validResetModes = List.of("none", "rising", "falling", "either", "level", "sampled level");
                if (!validResetModes.contains(resetMode)) {
                    result.addError("External reset mode must be one of: " + validResetModes);
                }
            }
        }
        
        // Validate initial condition source
        if (initialConditionSource != null) {
            String icSource = initialConditionSource.getValue(String.class);
            if (icSource != null && !icSource.equals("internal") && !icSource.equals("external")) {
                result.addError("Initial condition source must be 'internal' or 'external'");
            }
        }
        
        // Validate saturation limits
        Double upperLimit = getUpperSaturationLimitValue();
        Double lowerLimit = getLowerSaturationLimitValue();
        if (upperLimit != null && lowerLimit != null) {
            if (!Double.isInfinite(upperLimit) && !Double.isInfinite(lowerLimit)) {
                if (upperLimit <= lowerLimit) {
                    result.addError("Upper saturation limit must be greater than lower saturation limit");
                }
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if saturation is enabled
     */
    public boolean isSaturationEnabled() {
        return Boolean.TRUE.equals(getLimitOutputValue());
    }
    
    /**
     * Check if external reset is enabled
     */
    public boolean hasExternalReset() {
        String resetMode = getExternalResetValue();
        return resetMode != null && !resetMode.equals("none");
    }
    
    /**
     * Check if external initial condition is used
     */
    public boolean hasExternalInitialCondition() {
        return "external".equals(getInitialConditionSourceValue());
    }
    
    /**
     * Get the number of input ports based on configuration
     */
    public int getInputPortCount() {
        int count = 1; // Main input always present
        if (hasExternalReset()) count++;
        if (hasExternalInitialCondition()) count++;
        return count;
    }
    
    /**
     * Get the number of output ports based on configuration
     */
    public int getOutputPortCount() {
        int count = 1; // Main output always present
        if (Boolean.TRUE.equals(getShowStatePortValue())) count++;
        if (Boolean.TRUE.equals(getShowSaturationPortValue())) count++;
        return count;
    }
    
    @Override
    public IntegratorDto copy() {
        return IntegratorDto.builder()
                .blockId(getBlockId())
                .blockType(getBlockType())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
                .externalReset(externalReset != null ? externalReset.copy() : null)
                .initialConditionSource(initialConditionSource != null ? initialConditionSource.copy() : null)
                .limitOutput(limitOutput != null ? limitOutput.copy() : null)
                .upperSaturationLimit(upperSaturationLimit != null ? upperSaturationLimit.copy() : null)
                .lowerSaturationLimit(lowerSaturationLimit != null ? lowerSaturationLimit.copy() : null)
                .showSaturationPort(showSaturationPort != null ? showSaturationPort.copy() : null)
                .showStatePort(showStatePort != null ? showStatePort.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();
        
        if (initialCondition != null) {
            params.put("InitialCondition", String.valueOf(initialCondition.getValue()));
        }
        if (externalReset != null) {
            params.put("ExternalReset", String.valueOf(externalReset.getValue()));
        }
        if (initialConditionSource != null) {
            params.put("InitialConditionSource", String.valueOf(initialConditionSource.getValue()));
        }
        if (limitOutput != null) {
            Boolean limitVal = limitOutput.getValue(Boolean.class);
            params.put("LimitOutput", Boolean.TRUE.equals(limitVal) ? "on" : "off");
        }
        if (upperSaturationLimit != null) {
            Double upperVal = upperSaturationLimit.getValue(Double.class);
            params.put("UpperSaturationLimit", (upperVal != null && Double.isInfinite(upperVal)) ? "inf" : String.valueOf(upperVal));
        }
        if (lowerSaturationLimit != null) {
            Double lowerVal = lowerSaturationLimit.getValue(Double.class);
            params.put("LowerSaturationLimit", (lowerVal != null && Double.isInfinite(lowerVal)) ? "-inf" : String.valueOf(lowerVal));
        }
        if (showSaturationPort != null) {
            Boolean showSatVal = showSaturationPort.getValue(Boolean.class);
            params.put("ShowSaturationPort", Boolean.TRUE.equals(showSatVal) ? "on" : "off");
        }
        if (showStatePort != null) {
            Boolean showStateVal = showStatePort.getValue(Boolean.class);
            params.put("ShowStatePort", Boolean.TRUE.equals(showStateVal) ? "on" : "off");
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", String.valueOf(getSampleTime()));
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", String.valueOf(outDataTypeStr.getValue()));
        }
        if (saturateOnIntegerOverflow != null) {
            Boolean satVal = saturateOnIntegerOverflow.getValue(Boolean.class);
            params.put("SaturateOnIntegerOverflow", Boolean.TRUE.equals(satVal) ? "on" : "off");
        }
        
        return params;
    }
    
    @Override
    public String toString() {
        return String.format("IntegratorDto{id=%d, name='%s', type='%s', ic=%s, reset='%s', inPorts=%d, outPorts=%d}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getInitialConditionAsDouble(), 
                           getExternalResetValue(), 
                           getInputPortCount(), 
                           getOutputPortCount());
    }
}