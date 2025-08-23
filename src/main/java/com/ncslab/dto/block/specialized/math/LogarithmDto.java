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
     * Default: "natural" (ln)
     * Options: "natural", "common" (base 10), "custom"
     */
    @Builder.Default
    private TypedParameter logType = TypedParameter.of("natural");
    
    /**
     * Base for custom logarithm (only used when logType is "custom")
     * Default: 2.0
     */
    @Builder.Default
    private TypedParameter logBase = TypedParameter.of(2.0);
    
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
    
    public String getLogTypeValue() {
        return logType != null ? logType.getAsString() : "natural";
    }
    
    public Double getLogBaseValue() {
        return logBase != null ? logBase.getAsDouble() : 2.0;
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
    
    public String getZeroCrossing() {
        return zeroCrossing != null ? zeroCrossing.getAsString() : "use all";
    }
    
    // Legacy compatibility method
    public Double getCustomBase() {
        return getLogBaseValue();
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate log type
        if (logType != null) {
            String type = logType.getAsString();
            if (type != null && !type.equals("natural") && !type.equals("common") && !type.equals("custom")) {
                result.addError("Log type must be 'natural', 'common', or 'custom'");
            }
        }
        
        // Validate log base (only for custom type)
        if ("custom".equals(getLogTypeValue()) && logBase != null) {
            Double base = logBase.getAsDouble();
            if (base == null || base <= 0.0 || base == 1.0) {
                result.addError("Log base must be positive and not equal to 1");
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
        return "natural".equals(getLogTypeValue());
    }
    
    /**
     * Check if using common logarithm (base 10)
     */
    public boolean isCommonLog() {
        return "common".equals(getLogTypeValue());
    }
    
    /**
     * Check if using custom base logarithm
     */
    public boolean isCustomLog() {
        return "custom".equals(getLogTypeValue());
    }
    
    @Override
    public LogarithmDto copy() {
        return LogarithmDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .logType(logType != null ? logType.copy() : null)
                .logBase(logBase != null ? logBase.copy() : null)
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
                .put("LogBase", logBase)
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