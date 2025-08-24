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
 * DTO for LogicalOperator block - Logic operations (AND, OR, NAND, NOR, XOR, NOT).
 * 
 * <p>This block performs logical operations on boolean or numeric inputs with SIMULINK-compatible parameters:
 * <ul>
 *   <li><b>Operator</b>: Logic operation to perform (AND, OR, NAND, NOR, XOR, NOT)</li>
 *   <li><b>Inputs</b>: Number of input ports (2 or more for most operations, 1 for NOT)</li>
 *   <li><b>AllPortsSameDT</b>: Force all ports to have same data type</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited, 0 for continuous)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type specification</li>
 *   <li><b>SaturateOnIntegerOverflow</b>: Handle integer overflow behavior</li>
 * </ul>
 * </p>
 * 
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Operator must be one of: AND, OR, NAND, NOR, XOR, NOT</li>
 *   <li>Number of inputs must be at least 1</li>
 *   <li>For NOT operation, only 1 input is allowed</li>
 *   <li>SampleTime must be >= 0 or -1 (inherited)</li>
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
@JsonTypeName("LogicalOperator")
public class LogicalOperatorDto extends BlockDto {

    /**
     * Logic operation to perform.
     * Must be one of: AND, OR, NAND, NOR, XOR, NOT
     */
    private TypedParameter operator;

    /**
     * Number of input ports.
     * Must be at least 1. For NOT operation, must be exactly 1.
     */
    private TypedParameter inputs;

    /**
     * Force all ports to have same data type.
     * When enabled, all input and output ports use the same data type.
     */
    private TypedParameter allPortsSameDT;

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
     * Creates LogicalOperatorDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     */
    public LogicalOperatorDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName,blockPath, position, dimension);
        initializeWithDefaults();
    }

    /**
     * Creates LogicalOperatorDto with comprehensive logic configuration.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param operator Logic operation to perform
     * @param inputs Number of input ports
     */
    public LogicalOperatorDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension,
                             String operator, int inputs) {
        super(blockName,blockPath, position, dimension);
        this.operator = TypedParameter.of(operator);
        this.inputs = TypedParameter.of(inputs);
        this.allPortsSameDT = TypedParameter.of("on");
        this.sampleTime = TypedParameter.of(-1.0);
        this.outDataTypeStr = TypedParameter.of("boolean");
        this.saturateOnIntegerOverflow = TypedParameter.of("off");
    }

    /**
     * Initialize DTO with SIMULINK-compatible default values.
     */
    private void initializeWithDefaults() {
        this.operator = TypedParameter.of("AND");
        this.inputs = TypedParameter.of(2);
        this.allPortsSameDT = TypedParameter.of("on");
        this.sampleTime = TypedParameter.of(-1.0); // Inherited
        this.outDataTypeStr = TypedParameter.of("boolean");
        this.saturateOnIntegerOverflow = TypedParameter.of("off");
    }

    /**
     * Factory method for creating LogicalOperatorDto from parameter map.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param parameters Typed parameter map
     * @return Configured LogicalOperatorDto instance
     */
    public static LogicalOperatorDto fromParameters(String blockName, String blockPath, 
                                                   BlockPositionDto position, BlockDimensionDto dimension,
                                                   TypedParameterMap parameters) {
        LogicalOperatorDto dto = new LogicalOperatorDto(blockName, blockPath, position, dimension);
        
        dto.operator = parameters.getTypedParameter("Operator", String.class)
                                .orElse(TypedParameter.of("AND"));
        dto.inputs = parameters.getTypedParameter("Inputs", Integer.class)
                              .orElse(TypedParameter.of(2));
        dto.allPortsSameDT = parameters.getTypedParameter("AllPortsSameDT", String.class)
                                      .orElse(TypedParameter.of("on"));
        dto.sampleTime = parameters.getTypedParameter("SampleTime", Double.class)
                                  .orElse(TypedParameter.of(-1.0));
        dto.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class)
                                      .orElse(TypedParameter.of("boolean"));
        dto.saturateOnIntegerOverflow = parameters.getTypedParameter("SaturateOnIntegerOverflow", String.class)
                                                 .orElse(TypedParameter.of("off"));
        
        return dto;
    }

    // === Validation Methods ===

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();
        
        // Validate operator
        if (operator != null && operator.getAsString() != null) {
            String op = operator.getAsString();
            if (!Arrays.asList("AND", "OR", "NAND", "NOR", "XOR", "NOT").contains(op)) {
                errors.add("Operator must be one of: AND, OR, NAND, NOR, XOR, NOT");
            }
        }
        
        // Validate inputs count
        if (inputs != null && inputs.getAsInteger() != null) {
            int inputCount = inputs.getAsInteger();
            if (inputCount < 1) {
                errors.add("Number of inputs must be at least 1");
            }
            
            // Special validation for NOT operation
            if (operator != null && "NOT".equals(operator.getAsString()) && inputCount > 1) {
                errors.add("NOT operation requires exactly 1 input");
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
               inputs != null && inputs.getAsInteger() != null && inputs.getAsInteger() > 0 &&
               sampleTime != null && sampleTime.getAsDouble() != null;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the logic operator type.
     *
     * @return Logic operation (AND, OR, NAND, NOR, XOR, NOT)
     */
    public String getOperatorValue() {
        return operator != null ? operator.getAsString() : "AND";
    }

    /**
     * Gets the number of input ports.
     *
     * @return Number of input ports
     */
    public int getInputsValue() {
        return inputs != null ? inputs.getAsInteger() : 2;
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
     * Checks if all ports must have the same data type.
     *
     * @return true if all ports must have same data type
     */
    public boolean isAllPortsSameDTEnabled() {
        return allPortsSameDT != null && "on".equals(allPortsSameDT.getAsString());
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
     * Checks if this is a NOT operation.
     *
     * @return true if operator is NOT
     */
    public boolean isNotOperation() {
        return "NOT".equals(getOperatorValue());
    }

    /**
     * Checks if this is a binary operation (requires 2+ inputs).
     *
     * @return true if operator requires multiple inputs
     */
    public boolean isBinaryOperation() {
        return !isNotOperation();
    }

    // === Helper Methods ===

    @Override
    public LogicalOperatorDto copy() {
        return LogicalOperatorDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .position(getPosition())
                .dimension(getDimension())
                .operator(operator != null ? operator.copy() : null)
                .inputs(inputs != null ? inputs.copy() : null)
                .allPortsSameDT(allPortsSameDT != null ? allPortsSameDT.copy() : null)
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
                .put("Inputs", inputs)
                .put("AllPortsSameDT", allPortsSameDT)
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
            "Operator", "Logic operation to perform (AND, OR, NAND, NOR, XOR, NOT)",
            "Inputs", "Number of input ports (1 for NOT, 2+ for others)",
            "AllPortsSameDT", "Force all ports to have same data type (on/off)",
            "SampleTime", "Sample time for discrete operation (-1 for inherited)",
            "OutDataTypeStr", "Output data type specification",
            "SaturateOnIntegerOverflow", "Handle integer overflow behavior (on/off)"
        );
    }

    @Override
    public String toString() {
        return String.format("LogicalOperatorDto{blockName='%s', operator='%s', inputs=%d, sampleTime=%.3f}",
                           getBlockName(), getOperatorValue(), getInputsValue(), getSampleTimeValue());
    }
}