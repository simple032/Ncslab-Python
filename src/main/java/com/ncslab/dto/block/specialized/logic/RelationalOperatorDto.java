package com.ncslab.dto.block.specialized.logic;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.block.BlockPositionDto;
import com.ncslab.dto.block.BlockDimensionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * DTO for RelationalOperator block - Relational operations (==, !=, <, <=, >, >=).
 * 
 * <p>This block performs relational comparisons between two inputs with SIMULINK-compatible parameters:
 * <ul>
 *   <li><b>Operator</b>: Relational operation to perform (==, !=, <, <=, >, >=)</li>
 *   <li><b>LogicDataType</b>: Output data type for logic operations</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited, 0 for continuous)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type specification</li>
 *   <li><b>SaturateOnIntegerOverflow</b>: Handle integer overflow behavior</li>
 * </ul>
 * </p>
 * 
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Operator must be one of: ==, !=, <, <=, >, >=</li>
 *   <li>SampleTime must be >= 0 or -1 (inherited)</li>
 *   <li>Block always has exactly 2 input ports and 1 output port</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Jacksonized
@JsonTypeName("RelationalOperator")
public class RelationalOperatorDto extends BlockDto {

    /**
     * Relational operation to perform.
     * Must be one of: ==, !=, <, <=, >, >=
     * Note: MATLAB/SIMULINK uses ~= for not equal, but we normalize to !=
     */
    private TypedParameter operator;

    /**
     * Output data type for logic operations.
     * Typically "boolean" for logical comparisons.
     */
    private TypedParameter logicDataType;

    /**
     * Sample time for discrete operation.
     * -1 for inherited, 0 for continuous, >0 for discrete
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification.
     * Controls the data type of the block output.
     */
    private TypedParameter outDataTypeStr;

    /**
     * Handle integer overflow behavior.
     * When enabled, saturates on integer overflow instead of wrapping.
     */
    private TypedParameter saturateOnIntegerOverflow;

    /**
     * Creates RelationalOperatorDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     */
    public RelationalOperatorDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super("RelationalOperator", blockName, blockPath, position, dimension);
        initializeWithDefaults();
    }

    /**
     * Creates RelationalOperatorDto with comprehensive relational configuration.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param operator Relational operation to perform
     */
    public RelationalOperatorDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension,
                                String operator) {
        super("RelationalOperator", blockName, blockPath, position, dimension);
        this.operator = TypedParameter.of(normalizeOperator(operator));
        this.logicDataType = TypedParameter.of("boolean");
        this.sampleTime = TypedParameter.of(-1.0);
        this.outDataTypeStr = TypedParameter.of("Inherit: Logical (see Configuration Parameters: Optimization)");
        this.saturateOnIntegerOverflow = TypedParameter.of("off");
    }

    /**
     * Initialize DTO with SIMULINK-compatible default values.
     */
    private void initializeWithDefaults() {
        this.operator = TypedParameter.of("==");
        this.logicDataType = TypedParameter.of("boolean");
        this.sampleTime = TypedParameter.of(-1.0); // Inherited
        this.outDataTypeStr = TypedParameter.of("Inherit: Logical (see Configuration Parameters: Optimization)");
        this.saturateOnIntegerOverflow = TypedParameter.of("off");
    }

    /**
     * Normalize MATLAB/SIMULINK operators to standard form.
     * Converts ~= to != for internal consistency.
     */
    private static String normalizeOperator(String operator) {
        return "~=".equals(operator) ? "!=" : operator;
    }

    /**
     * Factory method for creating RelationalOperatorDto from parameter map.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param parameters Typed parameter map
     * @return Configured RelationalOperatorDto instance
     */
    public static RelationalOperatorDto fromParameters(String blockName, String blockPath, 
                                                      BlockPositionDto position, BlockDimensionDto dimension,
                                                      TypedParameterMap parameters) {
        RelationalOperatorDto dto = new RelationalOperatorDto(blockName, blockPath, position, dimension);
        
        // Handle both "Operator" and legacy "relop" parameter names
        TypedParameter operatorParam = parameters.getTypedParameter("Operator", String.class);
        if (operatorParam == null) {
            operatorParam = parameters.getTypedParameter("relop", String.class);
        }
        if (operatorParam == null) {
            operatorParam = TypedParameter.of("==");
        }
        dto.operator = TypedParameter.of(normalizeOperator(operatorParam.getAsString()));
        
        dto.logicDataType = parameters.getTypedParameter("LogicDataType", String.class, "boolean");
        dto.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
        dto.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class, "Inherit: Logical (see Configuration Parameters: Optimization)");
        dto.saturateOnIntegerOverflow = parameters.getTypedParameter("SaturateOnIntegerOverflow", String.class, "off");
        
        return dto;
    }
    // === Validation Methods ===

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();
        
        // Validate operator
        if (operator != null && operator.getAsString() != null) {
            String op = operator.getAsString();
            if (!Arrays.asList("==", "!=", "~=", "<", "<=", ">", ">=").contains(op)) {
                errors.add("Operator must be one of: ==, !=, ~=, <, <=, >, >=");
            }
        }
        
        // Validate sample time
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            Double stValue = sampleTime.getAsDouble();
            if (stValue < -1.0 || Double.isNaN(stValue) || Double.isInfinite(stValue)) {
                errors.add("Sample time must be >= 0 or -1 (inherited)");
            }
        }
        
        return errors;
    }

    @Override
    public boolean isValidConfiguration() {
        return validateParameters().isEmpty() &&
               operator != null && operator.getAsString() != null &&
               sampleTime != null && sampleTime.getAsDouble() != null;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the relational operator type.
     *
     * @return Relational operation (==, !=, <, <=, >, >=)
     */
    public String getOperatorValue() {
        return operator != null ? operator.getAsString() : "==";
    }

    /**
     * Gets the normalized operator (converts ~= to !=).
     *
     * @return Normalized relational operation
     */
    public String getNormalizedOperatorValue() {
        return normalizeOperator(getOperatorValue());
    }

    /**
     * Gets the logic data type.
     *
     * @return Logic data type for output
     */
    public String getLogicDataTypeValue() {
        return logicDataType != null ? logicDataType.getAsString() : "boolean";
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time for discrete operation
     */
    public double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    /**
     * Gets the output data type specification.
     *
     * @return Output data type string
     */
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : 
               "Inherit: Logical (see Configuration Parameters: Optimization)";
    }

    /**
     * Checks if integer overflow saturation is enabled.
     *
     * @return true if saturation is enabled
     */
    public boolean isSaturationEnabled() {
        return saturateOnIntegerOverflow != null && 
               "on".equals(saturateOnIntegerOverflow.getAsString());
    }

    /**
     * Checks if this is an equality operation.
     *
     * @return true if operator is == or !=
     */
    public boolean isEqualityOperation() {
        String op = getNormalizedOperatorValue();
        return "==".equals(op) || "!=".equals(op);
    }

    /**
     * Checks if this is an inequality operation.
     *
     * @return true if operator is <, <=, >, >=
     */
    public boolean isInequalityOperation() {
        String op = getNormalizedOperatorValue();
        return "<".equals(op) || "<=".equals(op) || ">".equals(op) || ">=".equals(op);
    }

    // === Helper Methods ===

    @Override
    public RelationalOperatorDto copy() {
        return RelationalOperatorDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .position(getPosition())
                .dimension(getDimension())
                .operator(operator != null ? operator.copy() : null)
                .logicDataType(logicDataType != null ? logicDataType.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }

    /**
     * Creates a TypedParameterMap from this DTO's parameters.
     *
     * @return TypedParameterMap containing all block parameters
     */
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Operator", operator)
                .put("LogicDataType", logicDataType)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "Operator", "Relational operation to perform (==, !=, <, <=, >, >=)",
            "LogicDataType", "Output data type for logic operations",
            "SampleTime", "Sample time for discrete operation (-1 for inherited)",
            "OutDataTypeStr", "Output data type specification",
            "SaturateOnIntegerOverflow", "Handle integer overflow behavior (on/off)"
        );
    }

    @Override
    public String toString() {
        return String.format("RelationalOperatorDto{blockName='%s', operator='%s', logicDataType='%s', sampleTime=%.3f}",
                           getBlockName(), getOperatorValue(), getLogicDataTypeValue(), getSampleTimeValue());
    }
}