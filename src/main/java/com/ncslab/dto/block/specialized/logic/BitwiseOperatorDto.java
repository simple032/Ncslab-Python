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
 * DTO for BitwiseOperator block - Bitwise operations on integer data types.
 *
 * <p>This block performs bitwise operations on integer inputs with SIMULINK-compatible parameters:
 * <ul>
 *   <li><b>Operator</b>: Bitwise operation to perform (AND, OR, XOR, NOT, NAND, NOR)</li>
 *   <li><b>Inputs</b>: Number of input ports (2 or more for most operations, 1 for NOT)</li>
 *   <li><b>UseBitMask</b>: Enable bit mask for selective bit operations</li>
 *   <li><b>BitMask</b>: Hexadecimal mask value (e.g., "0xFF", "0xFFFF")</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited, 0 for continuous)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type specification (uint8, uint16, uint32, int8, int16, int32)</li>
 * </ul>
 * </p>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Operator must be one of: AND, OR, XOR, NOT, NAND, NOR</li>
 *   <li>Number of inputs must be at least 1</li>
 *   <li>For NOT operation, only 1 input is allowed</li>
 *   <li>SampleTime must be >= 0 or -1 (inherited)</li>
 *   <li>BitMask must be valid hexadecimal when UseBitMask is enabled</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Jacksonized
@JsonTypeName("BitwiseOperator")
public class BitwiseOperatorDto extends BlockDto {

    /**
     * Bitwise operation to perform.
     * Must be one of: AND, OR, XOR, NOT, NAND, NOR
     */
    private TypedParameter operator;

    /**
     * Number of input ports.
     * Must be at least 1. For NOT operation, must be exactly 1.
     */
    private TypedParameter inputs;

    /**
     * Enable bit mask for selective bit operations.
     * When enabled, applies BitMask to inputs before operation.
     */
    private TypedParameter useBitMask;

    /**
     * Hexadecimal mask value.
     * Only used when UseBitMask is enabled (e.g., "0xFF", "0xFFFF").
     */
    private TypedParameter bitMask;

    /**
     * Sample time for discrete operation.
     * -1 for inherited, 0 for continuous, >0 for discrete
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification.
     * Must be integer type: uint8, uint16, uint32, int8, int16, int32
     */
    private TypedParameter outDataTypeStr;

    /**
     * Creates BitwiseOperatorDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     */
    public BitwiseOperatorDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
        initializeWithDefaults();
    }

    /**
     * Creates BitwiseOperatorDto with comprehensive bitwise configuration.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param operator Bitwise operation to perform
     * @param inputs Number of input ports
     */
    public BitwiseOperatorDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension,
                              String operator, int inputs) {
        super(blockName, blockPath, position, dimension);
        this.operator = TypedParameter.of(operator);
        this.inputs = TypedParameter.of(inputs);
        this.useBitMask = TypedParameter.of("off");
        this.bitMask = TypedParameter.of("0xFF");
        this.sampleTime = TypedParameter.of(-1.0);
        this.outDataTypeStr = TypedParameter.of("uint32");
    }

    /**
     * Initialize DTO with SIMULINK-compatible default values.
     */
    private void initializeWithDefaults() {
        this.operator = TypedParameter.of("AND");
        this.inputs = TypedParameter.of(2);
        this.useBitMask = TypedParameter.of("off");
        this.bitMask = TypedParameter.of("0xFF");
        this.sampleTime = TypedParameter.of(-1.0); // Inherited
        this.outDataTypeStr = TypedParameter.of("uint32");
    }

    /**
     * Factory method for creating BitwiseOperatorDto from parameter map.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param parameters Typed parameter map
     * @return Configured BitwiseOperatorDto instance
     */
    public static BitwiseOperatorDto fromParameters(String blockName, String blockPath,
                                                    BlockPositionDto position, BlockDimensionDto dimension,
                                                    TypedParameterMap parameters) {
        BitwiseOperatorDto dto = new BitwiseOperatorDto(blockName, blockPath, position, dimension);

        dto.operator = parameters.getTypedParameter("Operator", String.class)
                .orElse(TypedParameter.of("AND"));
        dto.inputs = parameters.getTypedParameter("Inputs", Integer.class)
                .orElse(TypedParameter.of(2));
        dto.useBitMask = parameters.getTypedParameter("UseBitMask", String.class)
                .orElse(TypedParameter.of("off"));
        dto.bitMask = parameters.getTypedParameter("BitMask", String.class)
                .orElse(TypedParameter.of("0xFF"));
        dto.sampleTime = parameters.getTypedParameter("SampleTime", Double.class)
                .orElse(TypedParameter.of(-1.0));
        dto.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class)
                .orElse(TypedParameter.of("uint32"));

        return dto;
    }

    // === Validation Methods ===

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();

        // Validate operator
        if (operator != null && operator.getAsString() != null) {
            String op = operator.getAsString().toUpperCase();
            if (!Arrays.asList("AND", "OR", "XOR", "NOT", "NAND", "NOR").contains(op)) {
                errors.add("Operator must be one of: AND, OR, XOR, NOT, NAND, NOR");
            }
        }

        // Validate inputs count
        if (inputs != null && inputs.getAsInteger() != null) {
            int inputCount = inputs.getAsInteger();
            if (inputCount < 1) {
                errors.add("Number of inputs must be at least 1");
            }

            // Special validation for NOT operation
            if (operator != null && "NOT".equalsIgnoreCase(operator.getAsString()) && inputCount > 1) {
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

        // Validate bit mask format if enabled
        if (isUseBitMaskEnabled() && bitMask != null && bitMask.getAsString() != null) {
            String maskStr = bitMask.getAsString();
            try {
                if (maskStr.startsWith("0x") || maskStr.startsWith("0X")) {
                    Long.parseLong(maskStr.substring(2), 16);
                } else {
                    Long.parseLong(maskStr);
                }
            } catch (NumberFormatException e) {
                errors.add("BitMask must be valid hexadecimal (e.g., '0xFF') or decimal number");
            }
        }

        // Validate output data type is integer type
        if (outDataTypeStr != null && outDataTypeStr.getAsString() != null) {
            String dtype = outDataTypeStr.getAsString().toLowerCase();
            if (!Arrays.asList("uint8", "uint16", "uint32", "uint64", "int8", "int16", "int32", "int64").contains(dtype)) {
                errors.add("Output data type must be an integer type (uint8, uint16, uint32, int8, int16, int32)");
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
     * Gets the bitwise operator type.
     *
     * @return Bitwise operation (AND, OR, XOR, NOT, NAND, NOR)
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
     * Checks if bit mask is enabled.
     *
     * @return true if bit mask should be applied
     */
    public boolean isUseBitMaskEnabled() {
        return useBitMask != null && "on".equalsIgnoreCase(useBitMask.getAsString());
    }

    /**
     * Gets the bit mask value as string.
     *
     * @return Bit mask value (e.g., "0xFF")
     */
    public String getBitMaskValue() {
        return bitMask != null ? bitMask.getAsString() : "0xFF";
    }

    /**
     * Gets the output data type specification.
     *
     * @return Output data type string
     */
    public String getOutDataTypeValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "uint32";
    }

    /**
     * Checks if this is a NOT operation.
     *
     * @return true if operator is NOT
     */
    public boolean isNotOperation() {
        return "NOT".equalsIgnoreCase(getOperatorValue());
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
    public BitwiseOperatorDto copy() {
        return BitwiseOperatorDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .position(getPosition())
                .dimension(getDimension())
                .operator(operator != null ? operator.copy() : null)
                .inputs(inputs != null ? inputs.copy() : null)
                .useBitMask(useBitMask != null ? useBitMask.copy() : null)
                .bitMask(bitMask != null ? bitMask.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
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
                .put("UseBitMask", useBitMask)
                .put("BitMask", bitMask)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
                "Operator", "Bitwise operation to perform (AND, OR, XOR, NOT, NAND, NOR)",
                "Inputs", "Number of input ports (1 for NOT, 2+ for others)",
                "UseBitMask", "Enable bit mask for selective bit operations (on/off)",
                "BitMask", "Hexadecimal mask value (e.g., '0xFF', '0xFFFF')",
                "SampleTime", "Sample time for discrete operation (-1 for inherited)",
                "OutDataTypeStr", "Output data type (uint8, uint16, uint32, int8, int16, int32)"
        );
    }

    @Override
    public String toString() {
        return String.format("BitwiseOperatorDto{blockName='%s', operator='%s', inputs=%d, bitMask='%s', sampleTime=%.3f}",
                getBlockName(), getOperatorValue(), getInputsValue(), getBitMaskValue(), getSampleTimeValue());
    }
}
