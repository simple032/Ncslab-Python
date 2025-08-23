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
 * DTO representation of Fcn (Function) block for custom mathematical expressions.
 * 
 * The Fcn block applies a mathematical expression to its inputs.
 * The expression can include standard mathematical functions and operators.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Fcn")
@MigrationCompatible(originalClass = "com.ncslab.block.function.Fcn")
public class FcnDto extends BlockDto {
    
    /**
     * Mathematical expression to evaluate
     * Default: "u[0]"
     * Example: "sin(u[0]) + cos(u[1])"
     */
    @Builder.Default
    private TypedParameter expr = TypedParameter.of("u[0]");
    
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
    
    public String getExprValue() {
        return expr != null ? expr.getAsString() : "u[0]";
    }
    
    public String getExpressionValue() {
        return expr != null ? expr.getAsString() : "u[0]";
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
        
        // Validate expression
        if (expr == null || expr.getAsString() == null || expr.getAsString().trim().isEmpty()) {
            result.addError("Expression cannot be empty");
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
     * Get the number of inputs referenced in the expression
     */
    public int getReferencedInputCount() {
        String expression = getExprValue();
        if (expression == null) return 0;
        
        int maxIndex = -1;
        int index = 0;
        while ((index = expression.indexOf("u[", index)) != -1) {
            int endBracket = expression.indexOf("]", index);
            if (endBracket != -1) {
                try {
                    String numStr = expression.substring(index + 2, endBracket);
                    int inputIndex = Integer.parseInt(numStr);
                    maxIndex = Math.max(maxIndex, inputIndex);
                } catch (NumberFormatException e) {
                    // Invalid index format, ignore
                }
            }
            index++;
        }
        
        return maxIndex + 1; // Convert from 0-based to count
    }
    
    @Override
    public FcnDto copy() {
        return FcnDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .expr(expr != null ? expr.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Expr", expr)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("FcnDto{id=%d, name='%s', type='%s', expr='%s'}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getExprValue());
    }
}