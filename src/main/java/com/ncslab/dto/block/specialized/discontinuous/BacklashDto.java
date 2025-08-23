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
 * DTO representation of Backlash discontinuous block.
 * 
 * The Backlash block models mechanical backlash where the output
 * doesn't immediately follow the input when the input changes direction.
 * There's a "dead zone" where input can vary without affecting output.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Backlash")
@MigrationCompatible(originalClass = "com.ncslab.block.discontinuous.Backlash")
public class BacklashDto extends BlockDto {
    
    /**
     * Width of the backlash gap
     * Default: 0.5
     * Validation: Must be >= 0
     */
    @Builder.Default
    private TypedParameter backlashWidth = TypedParameter.of(0.5);
    
    /**
     * Initial output value
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter initialOutput = TypedParameter.of(0.0);
    
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
    
    public Double getBacklashWidthValue() {
        return backlashWidth != null ? backlashWidth.getAsDouble() : 0.5;
    }
    
    public Double getInitialOutputValue() {
        return initialOutput != null ? initialOutput.getAsDouble() : 0.0;
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
        
        // Validate backlash width
        if (backlashWidth != null) {
            Double width = backlashWidth.getAsDouble();
            if (width != null && width < 0.0) {
                result.addError("Backlash width must be >= 0.0");
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
     * Check if backlash is effectively disabled (width = 0)
     */
    public boolean isBacklashDisabled() {
        return getBacklashWidthValue() == 0.0;
    }
    
    @Override
    public BacklashDto copy() {
        return BacklashDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .backlashWidth(backlashWidth != null ? backlashWidth.copy() : null)
                .initialOutput(initialOutput != null ? initialOutput.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("BacklashWidth", backlashWidth)
                .put("InitialOutput", initialOutput)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("BacklashDto{id=%d, name='%s', type='%s', width=%.3f, initial=%.3f}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getBacklashWidthValue(),
                           getInitialOutputValue());
    }
}