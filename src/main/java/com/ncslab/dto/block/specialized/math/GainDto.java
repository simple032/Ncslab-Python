package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
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
 * DTO representation of Gain math block.
 * 
 * The Gain block multiplies the input by a scalar or matrix gain value.
 * It supports both element-wise and matrix multiplication modes.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Gain")
@MigrationCompatible(originalClass = "com.ncslab.block.math.Gain")
public class GainDto extends BlockDto {
    
    /**
     * Gain value (scalar or matrix)
     * Default: 1.0
     * Note: No @Builder.Default - values populated from paramValues during deserialization
     */
    private TypedParameter gain;

    /**
     * Multiplication mode
     * Default: "Element-wise(K.*u)"
     * Options: "Element-wise(K.*u)", "Matrix(K*u)"
     * Note: No @Builder.Default - values populated from paramValues during deserialization
     */
    private TypedParameter multiplication;

    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     * Note: No @Builder.Default - values populated from paramValues during deserialization
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     * Note: No @Builder.Default - values populated from paramValues during deserialization
     */
    private TypedParameter outDataTypeStr;

    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     * Note: No @Builder.Default - values populated from paramValues during deserialization
     */
    private TypedParameter saturateOnIntegerOverflow;

    /**
     * Post-deserialization hook to populate typed fields from paramValues Map.
     * This method is called automatically after Jackson finishes deserializing the JSON.
     * It extracts values from the legacy paramValues Map and converts them to TypedParameters.
     */
    @com.fasterxml.jackson.annotation.JsonSetter("paramValues")
    public void populateFromParamValues(java.util.Map<String, Object> paramValues) {
        super.setParamValues(paramValues);  // Call parent setter to maintain compatibility

        if (paramValues == null) {
            return;
        }

        // Extract and convert each parameter from paramValues
        if (paramValues.containsKey("Gain")) {
            Object gainValue = paramValues.get("Gain");
            this.gain = TypedParameter.of(gainValue);
        }

        if (paramValues.containsKey("Multiplication")) {
            Object multValue = paramValues.get("Multiplication");
            this.multiplication = TypedParameter.of(multValue);
        }

        if (paramValues.containsKey("SampleTime")) {
            Object stValue = paramValues.get("SampleTime");
            this.sampleTime = TypedParameter.of(stValue);
        }

        if (paramValues.containsKey("OutDataTypeStr")) {
            Object outDtValue = paramValues.get("OutDataTypeStr");
            this.outDataTypeStr = TypedParameter.of(outDtValue);
        }

        if (paramValues.containsKey("SaturateOnIntegerOverflow")) {
            Object satValue = paramValues.get("SaturateOnIntegerOverflow");
            this.saturateOnIntegerOverflow = TypedParameter.of(satValue);
        }
    }

    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getGainValue() {
        return gain != null ? gain.getAsDouble() : 1.0;
    }
    
    public String getGainValueAsString() {
        return gain != null ? gain.getAsString() : "1";
    }
    
    public String getMultiplicationValue() {
        return multiplication != null ? multiplication.getAsString() : "Element-wise(K.*u)";
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
        
        // Validate gain parameter
        if (gain != null) {
            try {
                Double gainVal = gain.getAsDouble();
                if (gainVal != null && (gainVal.isNaN() || gainVal.isInfinite())) {
                    result.addError("Gain value must be finite");
                }
            } catch (Exception e) {
                // Gain might be a string/matrix, which is valid
            }
        }
        
        // Validate multiplication mode
        if (multiplication != null) {
            String multValue = multiplication.getAsString();
            if (multValue != null && 
                !multValue.equals("Element-wise(K.*u)") && 
                !multValue.equals("Matrix(K*u)")) {
                result.addError("Multiplication mode must be 'Element-wise(K.*u)' or 'Matrix(K*u)'");
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
     * Check if matrix multiplication is enabled
     */
    public boolean isMatrixMultiplication() {
        return "Matrix(K*u)".equals(getMultiplicationValue());
    }
    
    /**
     * Check if gain is unity (no scaling)
     */
    public boolean isUnityGain() {
        try {
            Double gainVal = getGainValue();
            return gainVal != null && Math.abs(gainVal - 1.0) < 1e-15;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if gain is zero
     */
    public boolean isZeroGain() {
        try {
            Double gainVal = getGainValue();
            return gainVal != null && Math.abs(gainVal) < 1e-15;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public GainDto copy() {
        return GainDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .gain(gain != null ? gain.copy() : null)
                .multiplication(multiplication != null ? multiplication.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Gain", gain)
                .put("Multiplication", multiplication)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("GainDto{id=%d, name='%s', type='%s', gain=%s, multiplication='%s'}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getGainValueAsString(), 
                           getMultiplicationValue());
    }
}