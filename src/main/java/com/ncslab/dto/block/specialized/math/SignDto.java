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
 * DTO representation of Sign block for sign determination operations.
 * 
 * The Sign block determines the sign of its input signal.
 * Output: +1 for positive input, -1 for negative input, 0 for zero input
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Sign")
@MigrationCompatible(originalClass = "com.ncslab.block.math.Sign")
public class SignDto extends BlockDto {
    
    /**
     * Treatment for zero input
     * Default: "zero" (output 0 for zero input)
     * Options: "zero", "positive" (output +1 for zero), "negative" (output -1 for zero)
     */
    @Builder.Default
    private TypedParameter zeroOutput = TypedParameter.of("zero");
    
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
     * Zero crossing detection
     * Default: "use all"
     */
    @Builder.Default
    private TypedParameter zeroCrossing = TypedParameter.of("use all");
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getZeroOutputValue() {
        return zeroOutput != null ? zeroOutput.getAsString() : "zero";
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
    
    public String getZeroCrossingValue() {
        return zeroCrossing != null ? zeroCrossing.getAsString() : "off";
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate zero output treatment
        if (zeroOutput != null) {
            String treatment = zeroOutput.getAsString();
            if (treatment != null && !treatment.equals("zero") && !treatment.equals("positive") && !treatment.equals("negative")) {
                result.addError("Zero output treatment must be 'zero', 'positive', or 'negative'");
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
     * Get the sign output value for zero input
     */
    public int getZeroSignValue() {
        String treatment = getZeroOutputValue();
        switch (treatment) {
            case "positive": return 1;
            case "negative": return -1;
            case "zero":
            default: return 0;
        }
    }
    
    @Override
    public SignDto copy() {
        return SignDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .zeroOutput(zeroOutput != null ? zeroOutput.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .zeroCrossing(zeroCrossing != null ? zeroCrossing.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ZeroOutput", zeroOutput)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .put("ZeroCrossing", zeroCrossing)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("SignDto{id=%d, name='%s', type='%s', zeroOutput='%s'}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getZeroOutputValue());
    }
}