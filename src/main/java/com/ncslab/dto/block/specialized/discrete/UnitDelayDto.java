package com.ncslab.dto.block.specialized.discrete;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.block.discrete.DiscreteBlockDto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

import org.apache.commons.lang3.reflect.Typed;

import java.util.HashMap;

/**
 * DTO representation of UnitDelay block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the UnitDelay block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - InitialCondition: Initial condition for the delay (default: 0)
 * - SampleTime: Sample time for discrete operation (must be positive or -1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * Unit Delay Behavior:
 * y[k] = u[k-1]
 * 
 * The unit delay block stores the input from the previous time step and outputs it
 * at the current time step. At the first time step, it outputs the initial condition.
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 8
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("UnitDelay")
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.UnitDelay")
public class UnitDelayDto extends DiscreteBlockDto {
    
    // ===== UNIT DELAY SPECIFIC PARAMETERS =====
    
    /**
     * Initial condition for the delay
     * Default: 0
     * Validation: Must be finite number
     */
    private TypedParameter initialCondition;
    
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
    
    public Double getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getValue(Double.class) : 0.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getValue(Boolean.class) : false;
    }
    
    // ===== DISCRETE-TIME VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation
        
        // Validate initial condition
        if (initialCondition != null) {
            try {
                Double icVal = initialCondition.getValue(Double.class);
                if (icVal != null && (Double.isNaN(icVal) || Double.isInfinite(icVal))) {
                    result.addError("Initial condition must be finite");
                }
            } catch (Exception e) {
                result.addError("Invalid initial condition format: " + e.getMessage());
            }
        }
        
        // Validate discrete sample time
        if (getSampleTime() != null) {
            double sampleTime = getSampleTime().getAsDouble();
            if (sampleTime != -1.0 && sampleTime <= 0.0) {
                result.addError("Sample time must be positive or -1 (inherited)");
            }
            if (Double.isNaN(sampleTime) || Double.isInfinite(sampleTime)) {
                result.addError("Sample time must be finite");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====    
    
    /**
     * Create unit delay with specific initial condition and sample time
     */
    public static UnitDelayDto create(String name, String path, double initialCondition, double sampleTime) {
        UnitDelayDto dto = new UnitDelayDto();
        dto.setBlockName(name);
        dto.setBlockPath(path);
        dto.setSampleTime(TypedParameter.of(sampleTime));
        
        // Set default parameters
        dto.setOutDataTypeStr(TypedParameter.of("Inherit: Same as input"));
        dto.setSaturateOnIntegerOverflow(TypedParameter.of(false));
        
        // Set specific parameters
        dto.setInitialCondition(TypedParameter.of(initialCondition));
        
        return dto;
    }
    
    /**
     * Create unit delay with inherited sample time
     */
    public static UnitDelayDto createInherited(String name, String path, double initialCondition) {
        return create(name, path, initialCondition, -1.0);
    }
    
    @Override
    public UnitDelayDto copy() {
        return UnitDelayDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
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
        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
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
        return String.format("UnitDelayDto{id=%d, name='%s', type='%s', initialCondition=%s, sampleTime=%s}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getInitialConditionValue(),
                           getSampleTime());
    }
}