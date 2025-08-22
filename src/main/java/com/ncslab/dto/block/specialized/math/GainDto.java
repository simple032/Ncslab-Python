package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;
import java.util.HashMap;

/**
 * DTO representation of Gain block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the Gain block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - Gain: Gain value (scalar or matrix)
 * - Multiplication: Element-wise or Matrix multiplication mode
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
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
@MigrationCompatible(originalClass = "com.ncslab.block.math.Gain")
public class GainDto extends BlockDto {
    
    // ===== GAIN BLOCK SPECIFIC PARAMETERS =====
    
    /**
     * Gain value (scalar or matrix)
     * Default: 1
     * Validation: Must be finite
     */
    private TypedParameter gain;
    
    /**
     * Element-wise or Matrix multiplication mode
     * Default: "Element-wise(K.*u)"
     * Options: "Element-wise(K.*u)", "Matrix(K*u)"
     */
    private TypedParameter multiplication;
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    private TypedParameter outDataTypeStr;
    
    /**
     * Handle integer overflow
     * Default: false (off)
     */
    private TypedParameter saturateOnIntegerOverflow;
    
    // ===== FACTORY METHODS =====
    
    /**
     * Create DTO from legacy parameters map (for migration support)
     */
    public static GainDto fromLegacyParameters(java.util.Map<String, String> params) {
        GainDto.GainDtoBuilder builder = GainDto.builder()
                .blockName(params.getOrDefault("blockName", "Gain"))
                .blockPath(params.getOrDefault("blockPath", ""))
                .blockUUID(params.getOrDefault("blockUUID", ""))
                .blockType("Gain");
        
        GainDto dto = builder.build();
        
        // Map parameters to TypedParameter
        if (params.containsKey("Gain")) {
            try {
                dto.gain = TypedParameter.of(Double.parseDouble(params.get("Gain")));
            } catch (NumberFormatException e) {
                dto.gain = TypedParameter.of(params.get("Gain")); // Keep as string if not numeric
            }
        }
        
        if (params.containsKey("Multiplication")) {
            dto.multiplication = TypedParameter.of(params.get("Multiplication"));
        }
        
        if (params.containsKey("OutDataTypeStr")) {
            dto.outDataTypeStr = TypedParameter.of(params.get("OutDataTypeStr"));
        }
        
        if (params.containsKey("SaturateOnIntegerOverflow")) {
            dto.saturateOnIntegerOverflow = TypedParameter.of("on".equals(params.get("SaturateOnIntegerOverflow")));
        }
        
        return dto;
    }
    
    /**
     * Create builder with SIMULINK-compatible defaults
     */
    public static GainDtoBuilder builderWithDefaults() {
        GainDto dto = GainDto.builder()
                .blockType("Gain")
                .sampleTime(-1.0)
                .build();
        
        // Set default typed parameters
        dto.gain = TypedParameter.of(1.0);
        dto.multiplication = TypedParameter.of("Element-wise(K.*u)");
        dto.outDataTypeStr = TypedParameter.of("Inherit: Same as input");
        dto.saturateOnIntegerOverflow = TypedParameter.of(false);
        
        return GainDto.builder()
                .blockId(dto.getBlockId())
                .blockType(dto.getBlockType())
                .blockName(dto.getBlockName())
                .blockPath(dto.getBlockPath())
                .blockUUID(dto.getBlockUUID())
                .sampleTime(dto.getSampleTime())
                .gain(dto.gain)
                .multiplication(dto.multiplication)
                .outDataTypeStr(dto.outDataTypeStr)
                .saturateOnIntegerOverflow(dto.saturateOnIntegerOverflow);
    }
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getGainAsDouble() {
        return gain != null ? gain.getValue(Double.class) : null;
    }
    
    public String getGainAsString() {
        return gain != null ? gain.getValue(String.class) : null;
    }
    
    public Object getGainAsObject() {
        return gain != null ? gain.getValue() : null;
    }
    
    public String getMultiplicationValue() {
        return multiplication != null ? multiplication.getValue(String.class) : "Element-wise(K.*u)";
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getValue(String.class) : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getValue(Boolean.class) : false;
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation
        
        // Validate gain parameter
        if (gain == null) {
            result.addError("Gain parameter is required");
        } else {
            Object val = gain.getValue();
            if (val instanceof Number) {
                double gainValue = ((Number) val).doubleValue();
                if (Double.isNaN(gainValue) || Double.isInfinite(gainValue)) {
                    result.addError("Gain value must be finite");
                }
            }
        }
        
        // Validate multiplication mode
        if (multiplication != null) {
            String multValue = multiplication.getValue(String.class);
            if (multValue != null && !multValue.equals("Element-wise(K.*u)") && !multValue.equals("Matrix(K*u)")) {
                result.addError("Multiplication mode must be 'Element-wise(K.*u)' or 'Matrix(K*u)'");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if matrix multiplication is enabled
     */
    public boolean isMatrixMultiplication() {
        return "Matrix(K*u)".equals(getMultiplicationValue());
    }
    
    @Override
    public GainDto copy() {
        return GainDto.builder()
                .blockId(getBlockId())
                .blockType(getBlockType())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .gain(gain != null ? gain.copy() : null)
                .multiplication(multiplication != null ? multiplication.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();
        
        if (gain != null) {
            params.put("Gain", String.valueOf(gain.getValue()));
        }
        if (multiplication != null) {
            params.put("Multiplication", String.valueOf(multiplication.getValue()));
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", String.valueOf(getSampleTime()));
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", String.valueOf(outDataTypeStr.getValue()));
        }
        if (saturateOnIntegerOverflow != null) {
            Boolean satVal = saturateOnIntegerOverflow.getValue(Boolean.class);
            params.put("SaturateOnIntegerOverflow", Boolean.TRUE.equals(satVal) ? "on" : "off");
        }
        
        return params;
    }
    
    @Override
    public String toString() {
        return String.format("GainDto{id=%d, name='%s', type='%s', gain=%s, multiplication='%s'}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getGainAsObject(), 
                           getMultiplicationValue());
    }
}