package com.ncslab.dto.block.specialized.continuous;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
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
    
    
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getFilterCoefficientValue() {
        return filterCoefficient != null ? filterCoefficient.getAsDouble() : 1.0;
    }
    
    public Double getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getAsDouble() : 0.0;
    }
    
    public String getCoefficientSourceValue() {
        return coefficientSource != null ? coefficientSource.getAsString() : "internal";
    }
    
    public String getExternalResetValue() {
        return externalReset != null ? externalReset.getAsString() : "none";
    }
    
    public String getInitialConditionSourceValue() {
        return initialConditionSource != null ? initialConditionSource.getAsString() : "internal";
    }
    
    public Boolean getShowStatePortValue() {
        return showStatePort != null ? showStatePort.getAsBoolean() : false;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation
        
        // Validate filter coefficient
        if (filterCoefficient != null) {
            Double coeff = filterCoefficient.getAsDouble();
            if (coeff == null || coeff <= 0 || Double.isNaN(coeff) || Double.isInfinite(coeff)) {
                result.addError("Filter coefficient must be a positive finite number");
            }
        }
        
        // Validate initial condition
        if (initialCondition != null) {
            Double ic = initialCondition.getAsDouble();
            if (ic != null && (Double.isNaN(ic) || Double.isInfinite(ic))) {
                result.addError("Initial condition must be finite");
            }
        }
        
        // Validate coefficient source
        if (coefficientSource != null) {
            String source = coefficientSource.getAsString();
            if (source != null && !source.equals("internal") && !source.equals("external")) {
                result.addError("Coefficient source must be 'internal' or 'external'");
            }
        }
        
        // Validate external reset mode
        if (externalReset != null) {
            String resetMode = externalReset.getAsString();
            if (resetMode != null) {
                List<String> validResetModes = List.of("none", "rising", "falling", "either", "level");
                if (!validResetModes.contains(resetMode)) {
                    result.addError("External reset mode must be one of: " + validResetModes);
                }
            }
        }
        
        // Validate initial condition source
        if (initialConditionSource != null) {
            String icSource = initialConditionSource.getAsString();
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