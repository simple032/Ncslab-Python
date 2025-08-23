package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of MinMax block for minimum/maximum operations.
 * 
 * The MinMax block finds the minimum or maximum values among its inputs.
 * Can operate on element-wise or overall min/max.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("MinMax")
@MigrationCompatible(originalClass = "com.ncslab.block.math.MinMax")
public class MinMaxDto extends BlockDto {
    
    /**
     * Function type: minimum or maximum
     * Default: "min"
     * Options: "min", "max"
     */
    @Builder.Default
    private TypedParameter function = TypedParameter.of("min");
    
    /**
     * Number of inputs to the block
     * Default: 1
     */
    @Builder.Default
    private TypedParameter numInputs = TypedParameter.of(1);
    
    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as first input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as first input");
    
    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getFunctionValue() {
        return function != null ? function.getAsString() : "min";
    }
    
    public Integer getNumInputsValue() {
        return numInputs != null ? numInputs.getAsInteger() : 1;
    }
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as first input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate function type
        if (function != null) {
            String func = function.getAsString();
            if (func != null && !func.equals("min") && !func.equals("max")) {
                result.addError("Function must be 'min' or 'max'");
            }
        }
        
        // Validate number of inputs
        if (numInputs != null) {
            Integer inputs = numInputs.getAsInteger();
            if (inputs == null || inputs < 1) {
                result.addError("Number of inputs must be at least 1");
            }
        }
        
        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if this block finds minimum values
     */
    public boolean isMinimum() {
        return "min".equals(getFunctionValue());
    }
    
    /**
     * Check if this block finds maximum values
     */
    public boolean isMaximum() {
        return "max".equals(getFunctionValue());
    }
    
    /**
     * Get the actual number of input ports based on configuration
     */
    public int getInputPortCount() {
        return getNumInputsValue();
    }
    
    /**
     * Check if this block operates in continuous time
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }
    
    /**
     * Check if this block inherits its sample time
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }
    
    @Override
    public MinMaxDto copy() {
        return MinMaxDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .function(function != null ? function.copy() : null)
                .numInputs(numInputs != null ? numInputs.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Function", function)
                .put("NumInputs", numInputs)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("MinMaxDto{id=%d, name='%s', type='%s', function='%s', inputs=%d}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getFunctionValue(),
                           getInputPortCount());
    }
}