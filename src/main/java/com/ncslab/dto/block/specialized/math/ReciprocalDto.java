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
 * DTO representation of Reciprocal block for reciprocal operations.
 * 
 * The Reciprocal block computes the reciprocal (1/x) of its input signal.
 * Output = 1 / input
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Reciprocal")
@MigrationCompatible(originalClass = "com.ncslab.block.math.Reciprocal")
public class ReciprocalDto extends BlockDto {
    
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
     * Action when input is zero (division by zero)
     * Default: "None"
     * Options: "None", "Warning", "Error"
     */
    @Builder.Default
    private TypedParameter divideByZeroAction = TypedParameter.of("None");
    
    /**
     * Enable saturation on output
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter enableSaturation = TypedParameter.of(false);
    
    /**
     * Upper limit for output saturation
     * Default: inf
     */
    @Builder.Default
    private TypedParameter upperLimit = TypedParameter.of(Double.POSITIVE_INFINITY);
    
    /**
     * Lower limit for output saturation
     * Default: -inf
     */
    @Builder.Default
    private TypedParameter lowerLimit = TypedParameter.of(Double.NEGATIVE_INFINITY);
    
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
    
    public String getDivideByZeroActionValue() {
        return divideByZeroAction != null ? divideByZeroAction.getAsString() : "None";
    }
    
    public Boolean getEnableSaturationValue() {
        return enableSaturation != null ? enableSaturation.getAsBoolean() : false;
    }
    
    public Double getUpperLimitValue() {
        return upperLimit != null ? upperLimit.getAsDouble() : Double.POSITIVE_INFINITY;
    }
    
    public Double getLowerLimitValue() {
        return lowerLimit != null ? lowerLimit.getAsDouble() : Double.NEGATIVE_INFINITY;
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
        
        // Validate divide by zero action
        if (divideByZeroAction != null) {
            String action = divideByZeroAction.getAsString();
            if (action != null && !action.equals("None") && !action.equals("Warning") && !action.equals("Error")) {
                result.addError("Divide by zero action must be 'None', 'Warning', or 'Error'");
            }
        }
        
        // Validate saturation limits if enabled
        if (getEnableSaturationValue()) {
            Double upper = getUpperLimitValue();
            Double lower = getLowerLimitValue();
            if (upper != null && lower != null && lower >= upper) {
                result.addError("Lower limit must be < upper limit when saturation is enabled");
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
    public ReciprocalDto copy() {
        return ReciprocalDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .divideByZeroAction(divideByZeroAction != null ? divideByZeroAction.copy() : null)
                .enableSaturation(enableSaturation != null ? enableSaturation.copy() : null)
                .upperLimit(upperLimit != null ? upperLimit.copy() : null)
                .lowerLimit(lowerLimit != null ? lowerLimit.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .put("DivideByZeroAction", divideByZeroAction)
                .put("EnableSaturation", enableSaturation)
                .put("UpperLimit", upperLimit)
                .put("LowerLimit", lowerLimit)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("ReciprocalDto{id=%d, name='%s', type='%s'}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType());
    }
}