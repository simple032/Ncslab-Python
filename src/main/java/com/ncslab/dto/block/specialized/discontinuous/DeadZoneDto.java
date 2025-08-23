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
 * DTO representation of DeadZone discontinuous block.
 * 
 * The DeadZone block generates zero output when the input is within
 * a specified range (the dead zone). Outside this range, the output
 * equals the input minus the dead zone boundary.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DeadZone")
@MigrationCompatible(originalClass = "com.ncslab.block.discontinuous.DeadZone")
public class DeadZoneDto extends BlockDto {
    
    /**
     * Lower boundary of the dead zone
     * Default: -0.5
     */
    @Builder.Default
    private TypedParameter lowerValue = TypedParameter.of(-0.5);
    
    /**
     * Upper boundary of the dead zone
     * Default: 0.5
     * Validation: Must be > lowerValue
     */
    @Builder.Default
    private TypedParameter upperValue = TypedParameter.of(0.5);
    
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
    
    public Double getLowerValueValue() {
        return lowerValue != null ? lowerValue.getAsDouble() : -0.5;
    }
    
    public Double getUpperValueValue() {
        return upperValue != null ? upperValue.getAsDouble() : 0.5;
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
    
    // Legacy compatibility methods
    public Double getStartOfDeadZoneValue() {
        return getLowerValueValue();
    }
    
    public Double getEndOfDeadZoneValue() {
        return getUpperValueValue();
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate dead zone boundaries
        Double lower = getLowerValueValue();
        Double upper = getUpperValueValue();
        
        if (lower != null && upper != null) {
            if (lower >= upper) {
                result.addError("Upper value must be greater than lower value");
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
        if (lower != null && (lower.isNaN() || lower.isInfinite())) {
            result.addError("Lower value must be finite");
        }
        
        if (upper != null && (upper.isNaN() || upper.isInfinite())) {
            result.addError("Upper value must be finite");
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
     * Get the width of the dead zone
     */
    public double getDeadZoneWidth() {
        return getUpperValueValue() - getLowerValueValue();
    }
    
    /**
     * Check if input value is within the dead zone
     */
    public boolean isInDeadZone(double input) {
        return input >= getLowerValueValue() && input <= getUpperValueValue();
    }
    
    /**
     * Calculate output for given input
     */
    public double calculateOutput(double input) {
        if (isInDeadZone(input)) {
            return 0.0;
        } else if (input > getUpperValueValue()) {
            return input - getUpperValueValue();
        } else {
            return input - getLowerValueValue();
        }
    }
    
    @Override
    public DeadZoneDto copy() {
        return DeadZoneDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .lowerValue(lowerValue != null ? lowerValue.copy() : null)
                .upperValue(upperValue != null ? upperValue.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("LowerValue", lowerValue)
                .put("UpperValue", upperValue)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("DeadZoneDto{id=%d, name='%s', type='%s', range=[%.3f, %.3f]}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getLowerValueValue(),
                           getUpperValueValue());
    }
}