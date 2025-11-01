package com.ncslab.dto.block.specialized.string;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of String Concatenate block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the String Concatenate block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * The String Concatenate block concatenates multiple input strings into one output string.
 *
 * SIMULINK Parameters:
 * - NumberOfInputs: Number of strings to concatenate (default: 2, range: 1-32)
 * - MaximumLength: Maximum length of output in bytes including null-terminator (default: 0 = sum of inputs, unlimited)
 * - OutputDimensionsMode: "Fixed-size" or "Variable-size" (default: "Fixed-size")
 *
 * Behavior:
 * - Concatenates inputs in port order (Input1 + Input2 + Input3 + ...)
 * - Does NOT insert separators (spaces, etc.) between strings
 * - If MaximumLength > 0 and result exceeds it, truncate to max length
 * - If MaximumLength = 0, output dimension = sum of input dimensions
 *
 * Examples:
 * - Input1="Hello", Input2=" ", Input3="World" → Output="Hello World"
 * - Input1="Test", Input2="123" with MaximumLength=5 → Output="Test1"
 *
 * Character Set: ISO/IEC 8859-1 (first 256 Unicode code points)
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("StringConcatenate")
@MigrationCompatible(originalClass = "com.ncslab.block.string.StringConcatenate")
public class StringConcatenateDto extends BlockDto {

    // ===== STRING CONCATENATE SPECIFIC PARAMETERS =====

    /**
     * Number of input strings to concatenate.
     * Valid range: 1-32
     * Default: 2
     */
    private Integer numberOfInputs;

    /**
     * Maximum length of output string in bytes (including null-terminator).
     * A value of 0 indicates unlimited length (sum of input lengths).
     * Default: 0
     */
    private Integer maximumLength;

    /**
     * Output dimensions mode.
     * Valid values: "Fixed-size", "Variable-size"
     * Default: "Fixed-size"
     */
    private String outputDimensionsMode;

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate numberOfInputs (1-32)
        if (numberOfInputs == null) {
            result.addWarning("numberOfInputs", "Number of inputs is null - using default value 2");
        } else if (numberOfInputs < 1 || numberOfInputs > 32) {
            result.addError("numberOfInputs", "Number of inputs must be between 1 and 32, got: " + numberOfInputs);
        }

        // Validate maximumLength (>= 0)
        if (maximumLength == null) {
            result.addWarning("maximumLength", "Maximum length is null - using default value 0 (unlimited)");
        } else if (maximumLength < 0) {
            result.addError("maximumLength", "Maximum length must be >= 0, got: " + maximumLength);
        }

        // Validate outputDimensionsMode
        if (outputDimensionsMode == null || outputDimensionsMode.isEmpty()) {
            result.addWarning("outputDimensionsMode", "Output dimensions mode is empty - using default 'Fixed-size'");
        } else if (!outputDimensionsMode.equals("Fixed-size") && !outputDimensionsMode.equals("Variable-size")) {
            result.addError("outputDimensionsMode",
                "Output dimensions mode must be 'Fixed-size' or 'Variable-size', got: " + outputDimensionsMode);
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    @Override
    public StringConcatenateDto copy() {
        return StringConcatenateDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .position(getPosition())
                .dimension(getDimension())
                .numberOfInputs(this.numberOfInputs)
                .maximumLength(this.maximumLength)
                .outputDimensionsMode(this.outputDimensionsMode)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public java.util.Map<String, String> toLegacyParameters() {
        java.util.Map<String, String> params = new java.util.HashMap<>();

        if (numberOfInputs != null) {
            params.put("NumberOfInputs", numberOfInputs.toString());
        }

        if (maximumLength != null) {
            params.put("MaximumLength", maximumLength.toString());
        }

        if (outputDimensionsMode != null) {
            params.put("OutputDimensionsMode", outputDimensionsMode);
        }

        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("StringConcatenateDto{id=%d, name='%s', type='%s', numberOfInputs=%d, maximumLength=%d, mode='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           numberOfInputs,
                           maximumLength,
                           outputDimensionsMode);
    }

    // ===== HELPER METHODS =====

    /**
     * Check if the output has a maximum length constraint.
     *
     * @return true if maximumLength > 0, false otherwise
     */
    public boolean hasMaximumLengthConstraint() {
        return maximumLength != null && maximumLength > 0;
    }

    /**
     * Check if the output dimensions are fixed-size.
     *
     * @return true if outputDimensionsMode is "Fixed-size", false otherwise
     */
    public boolean isFixedSizeOutput() {
        return "Fixed-size".equals(outputDimensionsMode);
    }

    /**
     * Get the number of inputs with a default value if null.
     *
     * @return numberOfInputs or 2 if null
     */
    public int getNumberOfInputsOrDefault() {
        return numberOfInputs != null ? numberOfInputs : 2;
    }

    /**
     * Get the maximum length with a default value if null.
     *
     * @return maximumLength or 0 if null
     */
    public int getMaximumLengthOrDefault() {
        return maximumLength != null ? maximumLength : 0;
    }

    /**
     * Get the output dimensions mode with a default value if null.
     *
     * @return outputDimensionsMode or "Fixed-size" if null
     */
    public String getOutputDimensionsModeOrDefault() {
        return outputDimensionsMode != null ? outputDimensionsMode : "Fixed-size";
    }
}
