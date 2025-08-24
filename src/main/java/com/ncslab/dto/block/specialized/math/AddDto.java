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
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of Add block for mathematical addition operations.
 * 
 * The Add block performs element-wise addition of its inputs. It can have
 * multiple inputs and produces a single output that is the sum of all inputs.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Add")
@MigrationCompatible(originalClass = "com.ncslab.block.math.Add")
public class AddDto extends BlockDto {
    
    /**
     * Input sequence defining signs (e.g., "++", "+-", "++--")
     * Default: "++" (two positive inputs)
     * Validation: Must contain only '+' and '-' characters
     */
    @Builder.Default
    private TypedParameter inputs = TypedParameter.of("++");
    
    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as first input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as first input");
    
    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);
    
    // ===== PARAMETER ACCESS HELPERS =====
    public boolean getSign(int inputIndex) {
        String sequence = getInputsValue();
        if (sequence != null && inputIndex >= 0 && inputIndex < sequence.length()) {
            return sequence.charAt(inputIndex) == '+';
        }
        return true; // Default to positive if invalid index
    }

    public String getInputsValue() {
        return inputs != null ? inputs.getAsString() : "++";
    }
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as first input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate input sequence
        if (inputs != null) {
            String sequence = inputs.getAsString();
            if (sequence == null || sequence.trim().isEmpty()) {
                result.addError("Input sequence cannot be empty");
            } else {
                // Validate sequence contains only + and - characters
                for (char c : sequence.toCharArray()) {
                    if (c != '+' && c != '-') {
                        result.addError("Input sequence must contain only '+' and '-' characters");
                        break;
                    }
                }
                if (sequence.length() < 1) {
                    result.addError("Input sequence must have at least one input");
                }
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

    public int getNumInputsValue() {
        String sequence = getInputsValue();
        return sequence != null ? sequence.length() : 2;
    }
    
    /**
     * Get the actual number of input ports based on configuration
     */
    public int getInputPortCount() {
        return getNumInputsValue();
    }
    
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
    
    @Override
    public AddDto copy() {
        return AddDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .inputs(inputs != null ? inputs.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Inputs", inputs)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("AddDto{id=%d, name='%s', type='%s', inputs=%d}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getInputPortCount());
    }
}