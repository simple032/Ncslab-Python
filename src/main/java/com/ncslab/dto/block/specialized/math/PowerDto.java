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
 * Data Transfer Object for Power math block.
 * Power blocks compute power operations (element-wise or matrix power).
 *
 * @author NCSLab
 * @version 2.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Power")
@MigrationCompatible(originalClass = "com.ncslab.block.math.Power")
public class PowerDto extends BlockDto {

    /**
     * Method for power operation.
     * Default: "Element-wise(.^)" 
     * Valid values: "Element-wise(.^)" (element-wise power), "Matrix(^)" (matrix power)
     */
    @Builder.Default
    private TypedParameter powerMethod = TypedParameter.of("Element-wise(.^)");

    /**
     * Sample time for the power block.
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
        
        // Validate power method
        if (powerMethod != null) {
            String method = powerMethod.getAsString();
            if (method == null || method.trim().isEmpty()) {
                result.addError("Power method cannot be empty");
            } else if (!isValidPowerMethod(method.trim())) {
                result.addError("Power method must be one of: Element-wise(.^), Matrix(^)");
            }
        }
        
        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }
        
        // Validate output data type
        if (outDataTypeStr != null) {
            String outType = outDataTypeStr.getAsString();
            if (outType == null || outType.trim().isEmpty()) {
                result.addError("Output data type cannot be empty");
            }
        }
        
        return result;
    }

    private boolean isValidPowerMethod(String method) {
        return "Element-wise(.^)".equals(method) || "Matrix(^)".equals(method);
    }

    // === Helper Methods ===

    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getPowerMethodValue() {
        return powerMethod != null ? powerMethod.getAsString() : "Element-wise(.^)";
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

    // ===== UTILITY METHODS =====
    
    /**
     * Checks if this is element-wise power operation (.^).
     */
    public boolean isElementWise() {
        return "Element-wise(.^)".equals(getPowerMethodValue());
    }

    /**
     * Checks if this is matrix power operation (^).
     */
    public boolean isMatrixPower() {
        return "Matrix(^)".equals(getPowerMethodValue());
    }

    /**
     * Gets the number of input ports (always 2 for power operation).
     */
    public int getInputPortCount() {
        return 2;
    }

    @Override
    public PowerDto copy() {
        return PowerDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .powerMethod(powerMethod != null ? powerMethod.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("PowerMethod", powerMethod)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }

    @Override
    public String toString() {
        return String.format("PowerDto{id=%d, name='%s', type='%s', method='%s', sampleTime=%s}",
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getPowerMethodValue(),
                           getSampleTimeValue());
    }
}