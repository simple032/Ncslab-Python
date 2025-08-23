package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Rounding math block.
 * Rounding blocks perform various rounding operations (floor, ceil, round, fix).
 *
 * @author NCSLab
 * @version 2.0
 * @since 2025
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonTypeName("Rounding")
public class RoundingDto extends BlockDto {

    /**
     * Type of rounding operation.
     * Valid values: "floor", "ceil", "round", "fix" (truncate towards zero)
     */
    private TypedParameter operator;

    /**
     * Sample time for the rounding block.
     * -1 for inherited, 0 for continuous, >0 for discrete
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification.
     * Common values: "Inherit: Same as input", "double", "single", "int32", "int16"
     */
    private TypedParameter outDataTypeStr;

    /**
     * Constructs RoundingDto with individual parameters.
     *
     * @param blockName      Name of the block
     * @param blockPath      Path of the block in the model hierarchy
     * @param operator       Rounding operator parameter
     * @param sampleTime     Sample time parameter
     * @param outDataTypeStr Output data type parameter
     */
    public RoundingDto(String blockName, String blockPath,
                       TypedParameter operator,
                       TypedParameter sampleTime,
                       TypedParameter outDataTypeStr) {
        super("Rounding", blockName, blockPath);
        this.operator = operator;
        this.sampleTime = sampleTime;
        this.outDataTypeStr = outDataTypeStr;
    }

    /**
     * Constructs RoundingDto with typed parameter map.
     *
     * @param blockName  Name of the block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public RoundingDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super("Rounding", blockName, blockPath);
        this.operator = parameters.getTypedParameter("Operator", String.class, "floor");
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
        this.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class, "Inherit: Same as input");
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate operator
        if (operator == null || operator.getAsString() == null || operator.getAsString().trim().isEmpty()) {
            addValidationError("Operator cannot be null or empty");
            return false;
        }

        String operatorValue = operator.getAsString().trim();
        if (!isValidOperator(operatorValue)) {
            addValidationError("Operator must be one of: floor, ceil, round, fix");
            return false;
        }

        // Validate sample time
        if (sampleTime == null || sampleTime.getAsDouble() == null) {
            addValidationError("Sample time cannot be null");
            return false;
        }

        Double sampleTimeValue = sampleTime.getAsDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue.isNaN() || sampleTimeValue.isInfinite()) {
            addValidationError("Sample time must be >= -1.0 and finite");
            return false;
        }

        // Validate output data type
        if (outDataTypeStr == null || outDataTypeStr.getAsString() == null || outDataTypeStr.getAsString().trim().isEmpty()) {
            addValidationError("Output data type cannot be null or empty");
            return false;
        }

        return true;
    }

    private boolean isValidOperator(String op) {
        return "floor".equals(op) || "ceil".equals(op) || "round".equals(op) || "fix".equals(op);
    }

    // === Helper Methods ===

    /**
     * Gets the operator with validation.
     *
     * @return Rounding operator
     * @throws IllegalStateException if operator is invalid
     */
    public String getOperatorValue() {
        if (operator == null || operator.getAsString() == null) {
            throw new IllegalStateException("Operator is not properly initialized");
        }
        return operator.getAsString().trim();
    }

    /**
     * Gets the sample time value with validation.
     *
     * @return Sample time value
     * @throws IllegalStateException if sample time is invalid
     */
    public double getSampleTimeValue() {
        if (sampleTime == null || sampleTime.getAsDouble() == null) {
            throw new IllegalStateException("Sample time is not properly initialized");
        }
        return sampleTime.getAsDouble();
    }

    /**
     * Gets the output data type string with validation.
     *
     * @return Output data type string
     * @throws IllegalStateException if output data type is invalid
     */
    public String getOutDataTypeString() {
        if (outDataTypeStr == null || outDataTypeStr.getAsString() == null) {
            throw new IllegalStateException("Output data type is not properly initialized");
        }
        return outDataTypeStr.getAsString();
    }

    /**
     * Checks if this is a floor operation (round towards negative infinity).
     *
     * @return true if operator is "floor", false otherwise
     */
    public boolean isFloor() {
        return "floor".equals(getOperatorValue());
    }

    /**
     * Checks if this is a ceiling operation (round towards positive infinity).
     *
     * @return true if operator is "ceil", false otherwise
     */
    public boolean isCeil() {
        return "ceil".equals(getOperatorValue());
    }

    /**
     * Checks if this is a round operation (round to nearest integer).
     *
     * @return true if operator is "round", false otherwise
     */
    public boolean isRound() {
        return "round".equals(getOperatorValue());
    }

    /**
     * Checks if this is a fix operation (truncate towards zero).
     *
     * @return true if operator is "fix", false otherwise
     */
    public boolean isFix() {
        return "fix".equals(getOperatorValue());
    }

    /**
     * Gets the C/C++ function name for this rounding operation.
     *
     * @return C/C++ function name
     */
    public String getCFunctionName() {
        String op = getOperatorValue();
        switch (op) {
            case "floor":
                return "floor";
            case "ceil":
                return "ceil";
            case "round":
                return "round";
            case "fix":
                return "trunc"; // fix is equivalent to trunc in C
            default:
                throw new IllegalStateException("Unknown operator: " + op);
        }
    }

    /**
     * Gets a description of the rounding operation.
     *
     * @return Human-readable description
     */
    public String getOperatorDescription() {
        String op = getOperatorValue();
        switch (op) {
            case "floor":
                return "Round towards negative infinity";
            case "ceil":
                return "Round towards positive infinity";
            case "round":
                return "Round to nearest integer";
            case "fix":
                return "Truncate towards zero";
            default:
                return "Unknown rounding operation";
        }
    }

    // === Factory Methods ===
    private static String getDescriptionForOperator(String operator) {
        switch (operator) {
            case "floor":
                return "Floor operation (round towards negative infinity)";
            case "ceil":
                return "Ceiling operation (round towards positive infinity)";
            case "round":
                return "Round operation (round to nearest integer)";
            case "fix":
                return "Fix operation (truncate towards zero)";
            default:
                return "Rounding operation";
        }
    }

    @Override
    public String toString() {
        return String.format("RoundingDto{blockName='%s', blockPath='%s', operator='%s', sampleTime=%s, outDataType='%s'}",
                getBlockName(), getBlockPath(),
                operator != null ? operator.getAsString() : "null",
                sampleTime != null ? sampleTime.getAsDouble() : "null",
                outDataTypeStr != null ? outDataTypeStr.getAsString() : "null");
    }
}