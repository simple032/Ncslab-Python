package com.ncslab.dto.block.specialized.discontinuous;

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
 * DTO representation of Saturation discontinuous block.
 * 
 * The Saturation block limits the range of a signal by clipping
 * values to upper and lower saturation limits.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Saturation")
@MigrationCompatible(originalClass = "com.ncslab.block.discontinuous.Saturation")
public class SaturationDto extends BlockDto {
    
    /**
     * Upper saturation limit
     * Default: 1.0
     */
    @Builder.Default
    private TypedParameter upperLimit = TypedParameter.of(1.0);
    
    /**
     * Lower saturation limit
     * Default: -1.0
     * Validation: Must be <= upperLimit
     */
    @Builder.Default
    private TypedParameter lowerLimit = TypedParameter.of(-1.0);
    
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
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getUpperLimitValue() {
        return upperLimit != null ? upperLimit.getAsDouble() : 1.0;
    }
    
    public Double getLowerLimitValue() {
        return lowerLimit != null ? lowerLimit.getAsDouble() : -1.0;
    }
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // Legacy compatibility methods for SIMULINK parameter names
    public Double getUpperSaturationLimitValue() {
        return getUpperLimitValue();
    }
    
    public Double getLowerSaturationLimitValue() {
        return getLowerLimitValue();
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate saturation limits
        Double upper = getUpperLimitValue();
        Double lower = getLowerLimitValue();
        
        if (upper != null && lower != null) {
            if (lower > upper) {
                result.addError("Lower limit must be <= upper limit");
            }
        }
        
        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }
        
        // Validate numeric values are finite
        if (upper != null && (upper.isNaN() || upper.isInfinite())) {
            result.addError("Upper limit must be finite");
        }
        
        if (lower != null && (lower.isNaN() || lower.isInfinite())) {
            result.addError("Lower limit must be finite");
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
    
    /**
     * Get the range of the saturation limits
     */
    public double getSaturationRange() {
        return getUpperLimitValue() - getLowerLimitValue();
    }
    
    /**
     * Check if saturation is symmetric around zero
     */
    public boolean isSymmetric() {
        return Math.abs(getUpperLimitValue() + getLowerLimitValue()) < 1e-15;
    }
    
    /**
     * Check if input value is within saturation limits
     */
    public boolean isWithinLimits(double input) {
        return input >= getLowerLimitValue() && input <= getUpperLimitValue();
    }
    
    /**
     * Apply saturation to input value
     */
    public double saturate(double input) {
        if (input > getUpperLimitValue()) {
            return getUpperLimitValue();
        } else if (input < getLowerLimitValue()) {
            return getLowerLimitValue();
        } else {
            return input;
        }
    }
    
    @Override
    public SaturationDto copy() {
        return SaturationDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .upperLimit(upperLimit != null ? upperLimit.copy() : null)
                .lowerLimit(lowerLimit != null ? lowerLimit.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("UpperLimit", upperLimit)
                .put("LowerLimit", lowerLimit)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("SaturationDto{id=%d, name='%s', type='%s', limits=[%.3f, %.3f]}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getLowerLimitValue(),
                           getUpperLimitValue());
    }
}