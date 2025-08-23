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
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO representation of Sqrt block for square root operations.
 * 
 * The Sqrt block computes the square root of its input signal.
 * Output = sqrt(input)
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Sqrt")
@MigrationCompatible(originalClass = "com.ncslab.block.math.Sqrt")
public class SqrtDto extends BlockDto {
    
    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    
    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);
    
    /**
     * Action when input is negative
     * Default: "None"
     * Options: "None", "Warning", "Error"
     */
    @Builder.Default
    private TypedParameter negativeInputAction = TypedParameter.of("None");
    
    /**
     * Function type for sqrt operation
     * Default: "sqrt"
     */
    @Builder.Default
    private TypedParameter function = TypedParameter.of("sqrt");
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    public String getNegativeInputActionValue() {
        return negativeInputAction != null ? negativeInputAction.getAsString() : "None";
    }
    
    public String getFunctionValue() {
        return function != null ? function.getAsString() : "sqrt";
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }
        
        // Validate negative input action
        if (negativeInputAction != null) {
            String action = negativeInputAction.getAsString();
            if (action != null && !action.equals("None") && !action.equals("Warning") && !action.equals("Error")) {
                result.addError("Negative input action must be 'None', 'Warning', or 'Error'");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
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
    public SqrtDto copy() {
        return SqrtDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .negativeInputAction(negativeInputAction != null ? negativeInputAction.copy() : null)
                .function(function != null ? function.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .put("NegativeInputAction", negativeInputAction)
                .put("Function", function)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("SqrtDto{id=%d, name='%s', type='%s'}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType());
    }
}