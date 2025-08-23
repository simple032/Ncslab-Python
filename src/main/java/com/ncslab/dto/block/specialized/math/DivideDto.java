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
 * DTO representation of Divide block for element-wise division operations.
 * 
 * The Divide block performs element-wise division of its inputs.
 * Output = input1 / input2 or input1 / input2 / input3 / ...
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Divide")
@MigrationCompatible(originalClass = "com.ncslab.block.math.Divide")
public class DivideDto extends BlockDto {
    
    /**
     * Number of inputs to the divide block
     * Default: 2
     * Validation: Must be >= 2
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
    
    /**
     * Action when divisor is zero
     * Default: "None"
     * Options: "None", "Warning", "Error"
     */
    @Builder.Default
    private TypedParameter divideByZeroAction = TypedParameter.of("None");
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Integer getNumInputsValue() {
        return numInputs != null ? numInputs.getAsInteger() : 2;
    }
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getDivideMethodValue() {
        return "Element-wise(./.)"; // Fixed for this DTO
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as first input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    public String getDivideByZeroActionValue() {
        return divideByZeroAction != null ? divideByZeroAction.getAsString() : "None";
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate number of inputs
        if (numInputs != null) {
            Integer inputs = numInputs.getAsInteger();
            if (inputs == null || inputs < 2) {
                result.addError("Number of inputs must be at least 2");
            }
        }
        
        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }
        
        // Validate divide by zero action
        if (divideByZeroAction != null) {
            String action = divideByZeroAction.getAsString();
            if (action != null && !action.equals("None") && !action.equals("Warning") && !action.equals("Error")) {
                result.addError("Divide by zero action must be 'None', 'Warning', or 'Error'");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
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
    public DivideDto copy() {
        return DivideDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .numInputs(numInputs != null ? numInputs.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .divideByZeroAction(divideByZeroAction != null ? divideByZeroAction.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("NumInputs", numInputs)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .put("DivideByZeroAction", divideByZeroAction)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("DivideDto{id=%d, name='%s', type='%s', inputs=%d}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getInputPortCount());
    }
}