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
 * DTO representation of Coulomb discontinuous block.
 * 
 * The Coulomb block implements Coulomb friction model where output is:
 * output = sign(input) * (gain * |input| + offset)
 * This models the behavior where static friction must be overcome.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Coulomb")
@MigrationCompatible(originalClass = "com.ncslab.block.discontinuous.Coulomb")
public class CoulombDto extends BlockDto {
    
    /**
     * Offset value for Coulomb friction
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter offset = TypedParameter.of(0.0);
    
    /**
     * Gain value for Coulomb friction
     * Default: 1.0
     * Validation: Must be >= 0
     */
    @Builder.Default
    private TypedParameter gain = TypedParameter.of(1.0);
    
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
    
    public Double getOffsetValue() {
        return offset != null ? offset.getAsDouble() : 0.0;
    }
    
    public Double getGainValue() {
        return gain != null ? gain.getAsDouble() : 1.0;
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
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate gain is non-negative
        if (gain != null) {
            Double gainVal = gain.getAsDouble();
            if (gainVal != null && gainVal < 0.0) {
                result.addError("Gain should typically be >= 0 for physical Coulomb friction");
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
        if (offset != null) {
            Double offsetVal = offset.getAsDouble();
            if (offsetVal != null && (offsetVal.isNaN() || offsetVal.isInfinite())) {
                result.addError("Offset must be finite");
            }
        }
        
        if (gain != null) {
            Double gainVal = gain.getAsDouble();
            if (gainVal != null && (gainVal.isNaN() || gainVal.isInfinite())) {
                result.addError("Gain must be finite");
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
     * Calculate Coulomb friction output for given input
     */
    public double calculateCoulombOutput(double input) {
        double signValue = input >= 0 ? 1.0 : -1.0;
        double absValue = Math.abs(input);
        return signValue * (getGainValue() * absValue + getOffsetValue());
    }
    
    /**
     * Check if offset is zero (no static friction)
     */
    public boolean hasNoStaticFriction() {
        return Math.abs(getOffsetValue()) < 1e-15;
    }
    
    /**
     * Check if gain is unity (no scaling)
     */
    public boolean hasUnityGain() {
        return Math.abs(getGainValue() - 1.0) < 1e-15;
    }
    
    @Override
    public CoulombDto copy() {
        return CoulombDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .offset(offset != null ? offset.copy() : null)
                .gain(gain != null ? gain.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Offset", offset)
                .put("Gain", gain)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("CoulombDto{id=%d, name='%s', type='%s', offset=%.3f, gain=%.3f}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getOffsetValue(),
                           getGainValue());
    }
}