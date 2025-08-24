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
 * DTO representation of Logarithm block for logarithmic operations.
 * 
 * The Logarithm block computes the logarithm of its input signal.
 * Supports natural log, common log (base 10), and custom base.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Logarithm")
@MigrationCompatible(originalClass = "com.ncslab.block.math.Logarithm")
public class LogarithmDto extends BlockDto {
    
    /**
     * Type of logarithm operation
     * Default: "ln" (natural logarithm)
     * Options: "ln" (natural), "log10" (base 10), "log2" (base 2), "logn" (custom base)
     */
    @Builder.Default
    private TypedParameter logType = TypedParameter.of("ln");
    
    /**
     * Base for custom logarithm (only used when logType is "logn")
     * Default: 10.0
     */
    @Builder.Default
    private TypedParameter customBase = TypedParameter.of(10.0);
    
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
     * Default: true (on)
     */
    @Builder.Default
    private TypedParameter zeroCrossing = TypedParameter.of(true);
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getLogTypeValue() {
        return logType != null ? logType.getAsString() : "ln";
    }
    
    public Double getCustomBaseValue() {
        return customBase != null ? customBase.getAsDouble() : 10.0;
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
    
    public Boolean getZeroCrossingValue() {
        return zeroCrossing != null ? zeroCrossing.getAsBoolean() : true;
    }
    
    // Legacy compatibility methods  
    public Double getCustomBase() {
        return getCustomBaseValue();
    }
    
    public String getZeroCrossing() {
        return getZeroCrossingValue() ? "on" : "off";
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate log type
        if (logType != null) {
            String type = logType.getAsString();
            if (type != null && !type.equals("ln") && !type.equals("log10") && !type.equals("log2") && !type.equals("logn")) {
                result.addError("Log type must be 'ln', 'log10', 'log2', or 'logn'");
            }
        }
        
        // Validate custom base (only for logn type)
        if ("logn".equals(getLogTypeValue()) && customBase != null) {
            Double base = customBase.getAsDouble();
            if (base == null || base <= 0.0 || base == 1.0) {
                result.addError("Custom base must be positive and not equal to 1");
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
     * Check if using natural logarithm
     */
    public boolean isNaturalLog() {
        return "ln".equals(getLogTypeValue());
    }
    
    /**
     * Check if using common logarithm (base 10)
     */
    public boolean isCommonLog() {
        return "log10".equals(getLogTypeValue());
    }
    
    /**
     * Check if using base-2 logarithm
     */
    public boolean isLog2() {
        return "log2".equals(getLogTypeValue());
    }
    
    /**
     * Check if using custom base logarithm
     */
    public boolean isCustomLog() {
        return "logn".equals(getLogTypeValue());
    }
    
    @Override
    public LogarithmDto copy() {
        return LogarithmDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .logType(logType != null ? logType.copy() : null)
                .customBase(customBase != null ? customBase.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .zeroCrossing(zeroCrossing != null ? zeroCrossing.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("LogType", logType)
                .put("CustomBase", customBase)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .put("ZeroCrossing", zeroCrossing)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("LogarithmDto{id=%d, name='%s', type='%s', logType='%s'}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getLogTypeValue());
    }
}