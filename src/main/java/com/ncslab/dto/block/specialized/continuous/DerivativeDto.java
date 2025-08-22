package com.ncslab.dto.block.specialized.continuous;

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

/**
 * DTO representation of Derivative block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the Derivative block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - FilterCoefficient: Filter coefficient for filtered derivative (T in s/(Ts+1))
 * - InitialCondition: Initial condition for internal state
 * - CoefficientSource: Source of filter coefficient (internal, external)
 * - ExternalReset: External reset mode (none, rising, falling, either, level)
 * - InitialConditionSource: Source of initial condition (internal, external)
 * - ShowStatePort: Show state output port
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * Note: Implements filtered derivative G=s/(Ts+1) where T->0 gives ideal derivative
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MigrationCompatible(originalClass = "com.ncslab.block.continuous.Derivative")
public class DerivativeDto extends BlockDto {
    
    // ===== DERIVATIVE BLOCK SPECIFIC PARAMETERS =====
    
    /**
     * Filter coefficient for filtered derivative (T in s/(Ts+1))
     * Default: 1
     * Validation: Must be positive
     */
    private TypedParameter filterCoefficient;
    
    /**
     * Initial condition for internal state
     * Default: 0
     * Validation: Must be finite
     */
    private TypedParameter initialCondition;
    
    /**
     * Source of filter coefficient
     * Default: "internal"
     * Options: "internal", "external"
     */
    private TypedParameter coefficientSource;
    
    /**
     * External reset mode
     * Default: "none"
     * Options: "none", "rising", "falling", "either", "level"
     */
    private TypedParameter externalReset;
    
    /**
     * Source of initial condition
     * Default: "internal"
     * Options: "internal", "external"
     */
    private TypedParameter initialConditionSource;
    
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
    public static DerivativeDto fromLegacyParameters(Map<String, String> params) {
        DerivativeDto.DerivativeDtoBuilder builder = DerivativeDto.builder()
                .blockName(params.getOrDefault("blockName", "Derivative"))
                .blockPath(params.getOrDefault("blockPath", ""))
                .blockUUID(params.getOrDefault("blockUUID", ""))
                .blockType("Derivative");
        
        DerivativeDto dto = builder.build();
        
        // Map parameters to TypedParameter
        if (params.containsKey("FilterCoefficient")) {
            dto.filterCoefficient = TypedParameter.of(Double.parseDouble(params.get("FilterCoefficient")));
        } else if (params.containsKey("c")) {
            // Legacy compatibility - 'c' parameter
            dto.filterCoefficient = TypedParameter.of(Double.parseDouble(params.get("c")));
        }
        
        if (params.containsKey("InitialCondition")) {
            dto.initialCondition = TypedParameter.of(Double.parseDouble(params.get("InitialCondition")));
        }
        
        if (params.containsKey("CoefficientSource")) {
            dto.coefficientSource = TypedParameter.of(params.get("CoefficientSource"));
        }
        
        if (params.containsKey("ExternalReset")) {
            dto.externalReset = TypedParameter.of(params.get("ExternalReset"));
        }
        
        if (params.containsKey("InitialConditionSource")) {
            dto.initialConditionSource = TypedParameter.of(params.get("InitialConditionSource"));
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
    public static DerivativeDtoBuilder builderWithDefaults() {
        DerivativeDto dto = DerivativeDto.builder()
                .blockType("Derivative")
                .sampleTime(0.0) // Continuous
                .build();
        
        // Set default typed parameters
        dto.filterCoefficient = TypedParameter.of(1.0);
        dto.initialCondition = TypedParameter.of(0.0);
        dto.coefficientSource = TypedParameter.of("internal");
        dto.externalReset = TypedParameter.of("none");
        dto.initialConditionSource = TypedParameter.of("internal");
        dto.showStatePort = TypedParameter.of(false);
        dto.outDataTypeStr = TypedParameter.of("Inherit: Same as input");
        dto.saturateOnIntegerOverflow = TypedParameter.of(false);
        
        return DerivativeDto.builder()
                .blockId(dto.getBlockId())
                .blockType(dto.getBlockType())
                .blockName(dto.getBlockName())
                .blockPath(dto.getBlockPath())
                .blockUUID(dto.getBlockUUID())
                .sampleTime(dto.getSampleTime())
                .filterCoefficient(dto.filterCoefficient)
                .initialCondition(dto.initialCondition)
                .coefficientSource(dto.coefficientSource)
                .externalReset(dto.externalReset)
                .initialConditionSource(dto.initialConditionSource)
                .showStatePort(dto.showStatePort)
                .outDataTypeStr(dto.outDataTypeStr)
                .saturateOnIntegerOverflow(dto.saturateOnIntegerOverflow);
    }
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getFilterCoefficientValue() {
        return filterCoefficient != null ? filterCoefficient.getValue(Double.class) : 1.0;
    }
    
    public Double getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getValue(Double.class) : 0.0;
    }
    
    public String getCoefficientSourceValue() {
        return coefficientSource != null ? coefficientSource.getValue(String.class) : "internal";
    }
    
    public String getExternalResetValue() {
        return externalReset != null ? externalReset.getValue(String.class) : "none";
    }
    
    public String getInitialConditionSourceValue() {
        return initialConditionSource != null ? initialConditionSource.getValue(String.class) : "internal";
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
        
        // Validate filter coefficient
        if (filterCoefficient != null) {
            Double coeff = filterCoefficient.getValue(Double.class);
            if (coeff == null || coeff <= 0 || Double.isNaN(coeff) || Double.isInfinite(coeff)) {
                result.addError("Filter coefficient must be a positive finite number");
            }
        }
        
        // Validate initial condition
        if (initialCondition != null) {
            Double ic = initialCondition.getValue(Double.class);
            if (ic != null && (Double.isNaN(ic) || Double.isInfinite(ic))) {
                result.addError("Initial condition must be finite");
            }
        }
        
        // Validate coefficient source
        if (coefficientSource != null) {
            String source = coefficientSource.getValue(String.class);
            if (source != null && !source.equals("internal") && !source.equals("external")) {
                result.addError("Coefficient source must be 'internal' or 'external'");
            }
        }
        
        // Validate external reset mode
        if (externalReset != null) {
            String resetMode = externalReset.getValue(String.class);
            if (resetMode != null) {
                List<String> validResetModes = List.of("none", "rising", "falling", "either", "level");
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
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if external coefficient input is enabled
     */
    public boolean hasExternalCoefficient() {
        return "external".equals(getCoefficientSourceValue());
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
        if (hasExternalCoefficient()) count++;
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
        return count;
    }
    
    @Override
    public DerivativeDto copy() {
        return DerivativeDto.builder()
                .blockId(getBlockId())
                .blockType(getBlockType())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .filterCoefficient(filterCoefficient != null ? filterCoefficient.copy() : null)
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
                .coefficientSource(coefficientSource != null ? coefficientSource.copy() : null)
                .externalReset(externalReset != null ? externalReset.copy() : null)
                .initialConditionSource(initialConditionSource != null ? initialConditionSource.copy() : null)
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
        
        if (filterCoefficient != null) {
            params.put("FilterCoefficient", String.valueOf(filterCoefficient.getValue()));
            params.put("c", String.valueOf(filterCoefficient.getValue())); // Legacy compatibility
        }
        if (initialCondition != null) {
            params.put("InitialCondition", String.valueOf(initialCondition.getValue()));
        }
        if (coefficientSource != null) {
            params.put("CoefficientSource", String.valueOf(coefficientSource.getValue()));
        }
        if (externalReset != null) {
            params.put("ExternalReset", String.valueOf(externalReset.getValue()));
        }
        if (initialConditionSource != null) {
            params.put("InitialConditionSource", String.valueOf(initialConditionSource.getValue()));
        }
        if (showStatePort != null) {
            Boolean showVal = showStatePort.getValue(Boolean.class);
            params.put("ShowStatePort", Boolean.TRUE.equals(showVal) ? "on" : "off");
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
        return String.format("DerivativeDto{id=%d, name='%s', type='%s', filterCoeff=%s, inPorts=%d, outPorts=%d}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getFilterCoefficientValue(), 
                           getInputPortCount(), 
                           getOutputPortCount());
    }
}