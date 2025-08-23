package com.ncslab.dto.block.specialized.source;

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
 * DTO representation of Step source block.
 * 
 * The Step block generates a step signal that transitions from an initial
 * value to a final value at a specified step time.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Step")
@MigrationCompatible(originalClass = "com.ncslab.block.source.Step")
public class StepDto extends BlockDto {
    
    /**
     * Step time when transition occurs
     * Default: 1.0
     */
    @Builder.Default
    private TypedParameter time = TypedParameter.of(1.0);
    
    /**
     * Initial value before step time
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter initialValue = TypedParameter.of(0.0);
    
    /**
     * Final value after step time
     * Default: 1.0
     */
    @Builder.Default
    private TypedParameter finalValue = TypedParameter.of(1.0);
    
    /**
     * Sample time for the block operation
     * Default: 0.0 (continuous)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(0.0);
    
    /**
     * Output data type specification
     * Default: "double"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("double");
    
    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getTimeValue() {
        return time != null ? time.getAsDouble() : 1.0;
    }
    
    public Double getInitialValueValue() {
        return initialValue != null ? initialValue.getAsDouble() : 0.0;
    }
    
    public Double getFinalValueValue() {
        return finalValue != null ? finalValue.getAsDouble() : 1.0;
    }
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : 0.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "double";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate step time
        if (time != null) {
            Double stepTime = time.getAsDouble();
            if (stepTime != null && stepTime < 0.0) {
                result.addError("Step time must be >= 0.0");
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
        if (initialValue != null) {
            Double initial = initialValue.getAsDouble();
            if (initial != null && (initial.isNaN() || initial.isInfinite())) {
                result.addError("Initial value must be finite");
            }
        }
        
        if (finalValue != null) {
            Double finalVal = finalValue.getAsDouble();
            if (finalVal != null && (finalVal.isNaN() || finalVal.isInfinite())) {
                result.addError("Final value must be finite");
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
    
    /**
     * Get the step amplitude (final - initial)
     */
    public double getStepAmplitude() {
        return getFinalValueValue() - getInitialValueValue();
    }
    
    @Override
    public StepDto copy() {
        return StepDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .time(time != null ? time.copy() : null)
                .initialValue(initialValue != null ? initialValue.copy() : null)
                .finalValue(finalValue != null ? finalValue.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Time", time)
                .put("InitialValue", initialValue)
                .put("FinalValue", finalValue)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("StepDto{id=%d, name='%s', type='%s', stepTime=%.3f, initial=%.3f, final=%.3f}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getTimeValue(),
                           getInitialValueValue(),
                           getFinalValueValue());
    }
}