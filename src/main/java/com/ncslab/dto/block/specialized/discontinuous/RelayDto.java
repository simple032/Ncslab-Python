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
 * DTO representation of Relay discontinuous block.
 * 
 * The Relay block implements a hysteresis switch that switches 
 * between two output values based on input thresholds.
 * It has different switch-on and switch-off points to prevent
 * chattering near the switching threshold.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Relay")
@MigrationCompatible(originalClass = "com.ncslab.block.discontinuous.Relay")
public class RelayDto extends BlockDto {
    
    /**
     * Input value at which the relay switches on
     * Default: 1.0
     */
    @Builder.Default
    private TypedParameter onSwitchValue = TypedParameter.of(1.0);
    
    /**
     * Input value at which the relay switches off
     * Default: 0.0
     * Validation: Should be < onSwitchValue for normal operation
     */
    @Builder.Default
    private TypedParameter offSwitchValue = TypedParameter.of(0.0);
    
    /**
     * Output value when relay is on
     * Default: 1.0
     */
    @Builder.Default
    private TypedParameter onOutputValue = TypedParameter.of(1.0);
    
    /**
     * Output value when relay is off
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter offOutputValue = TypedParameter.of(0.0);
    
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
    
    public Double getOnSwitchValueValue() {
        return onSwitchValue != null ? onSwitchValue.getAsDouble() : 1.0;
    }
    
    public Double getOffSwitchValueValue() {
        return offSwitchValue != null ? offSwitchValue.getAsDouble() : 0.0;
    }
    
    public Double getOnOutputValueValue() {
        return onOutputValue != null ? onOutputValue.getAsDouble() : 1.0;
    }
    
    public Double getOffOutputValueValue() {
        return offOutputValue != null ? offOutputValue.getAsDouble() : 0.0;
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
    public Double getSwitchOnPointValue() {
        return getOnSwitchValueValue();
    }
    
    public Double getSwitchOffPointValue() {
        return getOffSwitchValueValue();
    }
    
    public Double getOutputWhenOnValue() {
        return getOnOutputValueValue();
    }
    
    public Double getOutputWhenOffValue() {
        return getOffOutputValueValue();
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate switch points
        Double onPoint = getOnSwitchValueValue();
        Double offPoint = getOffSwitchValueValue();
        
        if (onPoint != null && offPoint != null) {
            if (offPoint >= onPoint) {
                result.addError("Switch-off point should typically be < switch-on point for normal hysteresis operation");
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
        if (onPoint != null && (onPoint.isNaN() || onPoint.isInfinite())) {
            result.addError("On switch value must be finite");
        }
        
        if (offPoint != null && (offPoint.isNaN() || offPoint.isInfinite())) {
            result.addError("Off switch value must be finite");
        }
        
        Double onOutput = getOnOutputValueValue();
        if (onOutput != null && (onOutput.isNaN() || onOutput.isInfinite())) {
            result.addError("On output value must be finite");
        }
        
        Double offOutput = getOffOutputValueValue();
        if (offOutput != null && (offOutput.isNaN() || offOutput.isInfinite())) {
            result.addError("Off output value must be finite");
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
     * Get the hysteresis width
     */
    public double getHysteresisWidth() {
        return getOnSwitchValueValue() - getOffSwitchValueValue();
    }
    
    /**
     * Check if relay has normal hysteresis (on > off)
     */
    public boolean hasNormalHysteresis() {
        return getOnSwitchValueValue() > getOffSwitchValueValue();
    }
    
    /**
     * Get output range
     */
    public double getOutputRange() {
        return Math.abs(getOnOutputValueValue() - getOffOutputValueValue());
    }
    
    @Override
    public RelayDto copy() {
        return RelayDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .onSwitchValue(onSwitchValue != null ? onSwitchValue.copy() : null)
                .offSwitchValue(offSwitchValue != null ? offSwitchValue.copy() : null)
                .onOutputValue(onOutputValue != null ? onOutputValue.copy() : null)
                .offOutputValue(offOutputValue != null ? offOutputValue.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("OnSwitchValue", onSwitchValue)
                .put("OffSwitchValue", offSwitchValue)
                .put("OnOutputValue", onOutputValue)
                .put("OffOutputValue", offOutputValue)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("RelayDto{id=%d, name='%s', type='%s', switch=[%.3f/%.3f], output=[%.3f/%.3f]}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getOffSwitchValueValue(),
                           getOnSwitchValueValue(),
                           getOffOutputValueValue(),
                           getOnOutputValueValue());
    }
}