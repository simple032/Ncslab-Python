package com.ncslab.dto.block.specialized.logic;

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

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * DTO representation of CompareToZero logic block.
 * 
 * The CompareToZero block compares the input signal to zero using
 * a specified relational operator (==, !=, <, <=, >, >=).
 * The output is a logical (boolean) signal.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("CompareToZero")
@MigrationCompatible(originalClass = "com.ncslab.block.logicAndBit.CompareToZero")
public class CompareToZeroDto extends BlockDto {
    
    /**
     * Relational operator for comparison
     * Default: ">="
     * Options: "==", "!=", "<", "<=", ">", ">="
     */
    @Builder.Default
    private TypedParameter relationalOperator = TypedParameter.of(">=");
    
    /**
     * Output logic data type
     * Default: "boolean"
     */
    @Builder.Default
    private TypedParameter logicDataType = TypedParameter.of("boolean");
    
    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);
    
    /**
     * Output data type specification
     * Default: "Inherit: Logical (see Configuration Parameters: Optimization)"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Logical (see Configuration Parameters: Optimization)");
    
    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);
    
    // Valid relational operators
    private static final List<String> VALID_OPERATORS = Arrays.asList("==", "!=", "<", "<=", ">", ">=");
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getRelationalOperatorValue() {
        return relationalOperator != null ? relationalOperator.getAsString() : ">=";
    }
    
    public String getLogicDataTypeValue() {
        return logicDataType != null ? logicDataType.getAsString() : "boolean";
    }
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Logical (see Configuration Parameters: Optimization)";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // Legacy compatibility method
    public String getRelopValue() {
        return getRelationalOperatorValue();
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate relational operator
        if (relationalOperator != null) {
            String operator = relationalOperator.getAsString();
            if (operator != null && !VALID_OPERATORS.contains(operator)) {
                result.addError("Relational operator must be one of: " + VALID_OPERATORS);
            }
        }
        
        // Validate logic data type
        if (logicDataType != null) {
            String dataType = logicDataType.getAsString();
            if (dataType != null && !dataType.equals("boolean") && !dataType.equals("logical")) {
                result.addError("Logic data type should typically be 'boolean' or 'logical'");
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
     * Check if operator is equality comparison
     */
    public boolean isEqualityComparison() {
        String op = getRelationalOperatorValue();
        return "==".equals(op) || "!=".equals(op);
    }
    
    /**
     * Check if operator is inequality comparison
     */
    public boolean isInequalityComparison() {
        String op = getRelationalOperatorValue();
        return "<".equals(op) || "<=".equals(op) || ">".equals(op) || ">=".equals(op);
    }
    
    /**
     * Check if operator includes equality (<=, >=, ==)
     */
    public boolean includesEquality() {
        String op = getRelationalOperatorValue();
        return "<=".equals(op) || ">=".equals(op) || "==".equals(op);
    }
    
    /**
     * Check if operator is strict inequality (<, >)
     */
    public boolean isStrictInequality() {
        String op = getRelationalOperatorValue();
        return "<".equals(op) || ">".equals(op);
    }
    
    /**
     * Evaluate comparison for a given input value
     */
    public boolean evaluateComparison(double input) {
        String op = getRelationalOperatorValue();
        switch (op) {
            case "==": return input == 0.0;
            case "!=": return input != 0.0;
            case "<":  return input < 0.0;
            case "<=": return input <= 0.0;
            case ">":  return input > 0.0;
            case ">=": return input >= 0.0;
            default:   return false;
        }
    }
    
    @Override
    public CompareToZeroDto copy() {
        return CompareToZeroDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .relationalOperator(relationalOperator != null ? relationalOperator.copy() : null)
                .logicDataType(logicDataType != null ? logicDataType.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("RelationalOperator", relationalOperator)
                .put("LogicDataType", logicDataType)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("CompareToZeroDto{id=%d, name='%s', type='%s', operator='%s', logicType='%s'}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getRelationalOperatorValue(),
                           getLogicDataTypeValue());
    }
}