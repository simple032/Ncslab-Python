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
 * DTO representation of Add block for mathematical addition operations.
 * 
 * The Add block performs element-wise addition of its inputs. It can have
 * multiple inputs and produces a single output that is the sum of all inputs.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Add")
@MigrationCompatible(originalClass = "com.ncslab.block.math.Add")
public class AddDto extends BlockDto {
    
    /**
     * Number of inputs to the add block
     * Default: 2
     * Validation: Must be >= 1
     */
    @Builder.Default
    private TypedParameter numInputs = TypedParameter.of(2);
    
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
    public boolean getSign(int inputIndex) {
        return false; // Placeholder for future extension
    }

    public Integer getNumInputsValue() {
        return numInputs != null ? numInputs.getAsInteger() : 2;
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

    public String getInputsValue() {
        return numInputs != null ? numInputs.getAsString() : "++";
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
    public AddDto copy() {
        return AddDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .numInputs(numInputs != null ? numInputs.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("NumInputs", numInputs)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("AddDto{id=%d, name='%s', type='%s', inputs=%d}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getInputPortCount());
    }
}