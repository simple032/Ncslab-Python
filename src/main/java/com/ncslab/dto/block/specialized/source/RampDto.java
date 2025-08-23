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
 * DTO representation of Ramp source block.
 * 
 * The Ramp block generates a ramp signal that starts at a specified time
 * with a specified slope. Output increases linearly after start time.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Ramp")
@MigrationCompatible(originalClass = "com.ncslab.block.source.Ramp")
public class RampDto extends BlockDto {
    
    /**
     * Rate of change of the ramp signal
     * Default: 1.0
     */
    @Builder.Default
    private TypedParameter slope = TypedParameter.of(1.0);
    
    /**
     * Time when ramp starts
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter start = TypedParameter.of(0.0);
    
    /**
     * Initial output value before ramp starts
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter initialOutput = TypedParameter.of(0.0);
    
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
    
    public Double getSlopeValue() {
        return slope != null ? slope.getAsDouble() : 1.0;
    }
    
    public Double getStartValue() {
        return start != null ? start.getAsDouble() : 0.0;
    }
    
    public Double getInitialOutputValue() {
        return initialOutput != null ? initialOutput.getAsDouble() : 0.0;
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
        
        // Validate start time
        if (start != null) {
            Double startTime = start.getAsDouble();
            if (startTime != null && startTime < 0.0) {
                result.addError("Start time must be >= 0.0");
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
        if (slope != null) {
            Double slopeVal = slope.getAsDouble();
            if (slopeVal != null && (slopeVal.isNaN() || slopeVal.isInfinite())) {
                result.addError("Slope must be finite");
            }
        }
        
        if (initialOutput != null) {
            Double initial = initialOutput.getAsDouble();
            if (initial != null && (initial.isNaN() || initial.isInfinite())) {
                result.addError("Initial output must be finite");
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
     * Calculate output at given time
     */
    public double calculateOutput(double time) {
        if (time < getStartValue()) {
            return getInitialOutputValue();
        } else {
            return getInitialOutputValue() + getSlopeValue() * (time - getStartValue());
        }
    }
    
    @Override
    public RampDto copy() {
        return RampDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .slope(slope != null ? slope.copy() : null)
                .start(start != null ? start.copy() : null)
                .initialOutput(initialOutput != null ? initialOutput.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Slope", slope)
                .put("Start", start)
                .put("InitialOutput", initialOutput)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("RampDto{id=%d, name='%s', type='%s', slope=%.3f, start=%.3f}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getSlopeValue(),
                           getStartValue());
    }
}