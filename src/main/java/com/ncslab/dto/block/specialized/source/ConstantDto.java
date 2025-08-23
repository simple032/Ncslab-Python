package com.ncslab.dto.block.specialized.source;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO representation of Constant block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the Constant block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - Value: Constant value (scalar or matrix)
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - FramePeriod: Frame period for frame-based operations
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 5
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MigrationCompatible(originalClass = "com.ncslab.block.source.Constant")
public class ConstantDto extends BlockDto {
    
    // ===== CONSTANT BLOCK SPECIFIC PARAMETERS =====
    
    /**
     * Constant value (scalar or matrix)
     * Default: 1
     * Validation: Must be finite
     */
    private TypedParameter value;
    
    /**
     * Frame period for frame-based operations
     * Default: 1
     * Validation: Must be positive
     */
    private TypedParameter framePeriod;
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as parameter"
     */
    private TypedParameter outDataTypeStr;
    
    /**
     * Handle integer overflow
     * Default: false (off)
     */
    private TypedParameter saturateOnIntegerOverflow;
    
    // ===== FACTORY METHODS =====
   
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getValueAsDouble() {
        return value != null ? value.getAsDouble() : null;
    }
    
    public String getValueAsString() {
        return value != null ? value.getAsString() : null;
    }
    
    public Object getValueAsObject() {
        return value != null ? value.getValue() : null;
    }
    
    public Double getFramePeriodValue() {
        return framePeriod != null ? framePeriod.getAsDouble() : 1.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as parameter";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation
        
        // Validate value parameter
        if (value == null) {
            result.addError("Value parameter is required");
        } else {
            Object val = value.getValue();
            if (val instanceof Number) {
                double numVal = ((Number) val).doubleValue();
                if (Double.isNaN(numVal) || Double.isInfinite(numVal)) {
                    result.addError("Value must be finite");
                }
            }
        }
        
        // Validate frame period
        if (framePeriod != null) {
            Double fpVal = framePeriod.getAsDouble();
            if (fpVal != null && (fpVal <= 0.0 || Double.isNaN(fpVal) || Double.isInfinite(fpVal))) {
                result.addError("Frame period must be positive");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    @Override
    public ConstantDto copy() {
        return ConstantDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .value(value != null ? value.copy() : null)
                .framePeriod(framePeriod != null ? framePeriod.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public java.util.Map<String, String> toLegacyParameters() {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        
        if (value != null) {
            params.put("Value", String.valueOf(value.getValue()));
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", String.valueOf(getSampleTime()));
        }
        if (framePeriod != null) {
            params.put("FramePeriod", String.valueOf(framePeriod.getValue()));
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", String.valueOf(outDataTypeStr.getValue()));
        }
        if (saturateOnIntegerOverflow != null) {
            Boolean satVal = saturateOnIntegerOverflow.getAsBoolean();
            params.put("SaturateOnIntegerOverflow", Boolean.TRUE.equals(satVal) ? "on" : "off");
        }
        
        return params;
    }
    
    @Override
    public String toString() {
        return String.format("ConstantDto{id=%d, name='%s', type='%s', value=%s, sampleTime=%s}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getValueAsObject(), 
                           getSampleTime());
    }
}