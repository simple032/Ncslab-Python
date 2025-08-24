package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

/**
 * Data Transfer Object for Exponential math block.
 * Exponential blocks compute exponential functions (exp, exp10, exp2, expn).
 *
 * @author NCSLab
 * @version 2.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Exponential")
@MigrationCompatible(originalClass = "com.ncslab.block.math.Exponential")
public class ExponentialDto extends BlockDto {

    /**
     * Type of exponential operation.
     * Default: "exp" (natural exponential e^x)
     * Valid values: "exp" (e^x), "exp10" (10^x), "exp2" (2^x), "expn" (custom base)
     */
    @Builder.Default
    private TypedParameter expType = TypedParameter.of("exp");

    /**
     * Custom base for "expn" exponential type.
     * Default: 10.0
     * Only used when expType is "expn"
     */
    @Builder.Default
    private TypedParameter customBase = TypedParameter.of(10.0);

    /**
     * Enable zero crossing detection.
     * Default: true (on)
     * Improves accuracy for signals that cross zero
     */
    @Builder.Default
    private TypedParameter zeroCrossing = TypedParameter.of(true);

    /**
     * Sample time for the exponential block.
     * Default: -1 (inherited)
     * -1 for inherited, 0 for continuous, >0 for discrete
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    /**
     * Output data type specification.
     * Default: "Inherit: Same as input"
     * Common values: "Inherit: Same as input", "double", "single"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");

    /**
     * Whether to saturate on integer overflow.
     * Default: false (off)
     * When enabled, prevents integer overflow by clamping to max/min values
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);

    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate exponential type
        if (expType != null) {
            String type = expType.getAsString();
            if (type == null || type.trim().isEmpty()) {
                result.addError("Exponential type cannot be empty");
            } else if (!isValidExpType(type.trim())) {
                result.addError("Exponential type must be one of: exp, exp10, exp2, expn");
            }
        }
        
        // Validate custom base (for expn type)
        if ("expn".equals(getExpTypeValue()) && customBase != null) {
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

    private boolean isValidExpType(String type) {
        return "exp".equals(type) || "exp10".equals(type) || "exp2".equals(type) || "expn".equals(type);
    }

    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getExpTypeValue() {
        return expType != null ? expType.getAsString() : "exp";
    }

    public Double getCustomBaseValue() {
        return customBase != null ? customBase.getAsDouble() : 10.0;
    }

    public Boolean getZeroCrossingValue() {
        return zeroCrossing != null ? zeroCrossing.getAsBoolean() : true;
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
    public double getCustomBase() {
        return getCustomBaseValue();
    }
    
    public String getZeroCrossing() {
        return getZeroCrossingValue() ? "on" : "off";
    }

    /**
     * Checks if this is a natural exponential (e^x).
     *
     * @return true if exp type is "exp", false otherwise
     */
    public boolean isNaturalExp() {
        return "exp".equals(getExpTypeValue());
    }

    /**
     * Checks if this is a base-10 exponential (10^x).
     *
     * @return true if exp type is "exp10", false otherwise
     */
    public boolean isBase10Exp() {
        return "exp10".equals(getExpTypeValue());
    }

    /**
     * Checks if this is a base-2 exponential (2^x).
     *
     * @return true if exp type is "exp2", false otherwise
     */
    public boolean isBase2Exp() {
        return "exp2".equals(getExpTypeValue());
    }

    /**
     * Checks if this is a custom base exponential (n^x).
     */
    public boolean isCustomBaseExp() {
        return "expn".equals(getExpTypeValue());
    }

    @Override
    public ExponentialDto copy() {
        return ExponentialDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .expType(expType != null ? expType.copy() : null)
                .customBase(customBase != null ? customBase.copy() : null)
                .zeroCrossing(zeroCrossing != null ? zeroCrossing.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ExpType", expType)
                .put("CustomBase", customBase)
                .put("ZeroCrossing", zeroCrossing)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }

    @Override
    public String toString() {
        return String.format("ExponentialDto{id=%d, name='%s', type='%s', expType='%s', sampleTime=%s}",
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getExpTypeValue(),
                           getSampleTimeValue());
    }
}