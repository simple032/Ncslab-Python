package com.ncslab.dto.block.specialized.string;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Data Transfer Object for ComposeString block.
 *
 * Formats multiple inputs into a string using printf-style format.
 *
 * Block Behavior:
 * - Input 1..N: Numeric or string signals
 * - Output: String signal (formatted result)
 * - Parameters: Format (e.g., "Value: %d"), NumberOfInputs
 * - Example: Format="Pi: %f", Input=3.14 → Output="Pi: 3.14"
 *
 * Format Specifiers:
 * - %d: Integer (int32)
 * - %f: Floating-point (double)
 * - %s: String
 * - %%: Literal % character
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ComposeString")
@MigrationCompatible(originalClass = "com.ncslab.block.string.ComposeString")
public class ComposeStringDto extends BlockDto {

    // ===== COMPOSESTRING BLOCK SPECIFIC PARAMETERS =====

    /**
     * Format string with printf-style placeholders
     * Default: "%d"
     * Examples: "Value: %d", "%s: %f", "Temperature: %.2f°C"
     * Validation: Must not be null or empty
     */
    @Builder.Default
    private TypedParameter format = TypedParameter.of("%d");

    /**
     * Number of input ports
     * Default: 1
     * Range: 1-32
     * Validation: Must match the number of format specifiers
     */
    @Builder.Default
    private TypedParameter numberOfInputs = TypedParameter.of(1);

    // ===== PARAMETER ACCESS HELPERS =====

    public String getFormatValue() {
        return format != null ? format.getAsString() : "%d";
    }

    public Integer getNumberOfInputsValue() {
        return numberOfInputs != null ? numberOfInputs.getAsInteger() : 1;
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate format parameter
        if (format == null) {
            result.addError("Format", "Format parameter is required");
        } else {
            String formatStr = format.getAsString();
            if (formatStr == null || formatStr.trim().isEmpty()) {
                result.addError("Format", "Format string cannot be empty");
            } else {
                // Validate format string syntax
                try {
                    // Count format specifiers (excluding %%)
                    int specifierCount = countFormatSpecifiers(formatStr);

                    // Validate number of inputs matches format specifiers
                    Integer numInputs = getNumberOfInputsValue();
                    if (numInputs != null && specifierCount != numInputs) {
                        result.addWarning("Format", String.format(
                            "Number of format specifiers (%d) does not match NumberOfInputs (%d)",
                            specifierCount, numInputs));
                    }
                } catch (Exception e) {
                    result.addError("Format", "Invalid format string: " + e.getMessage());
                }
            }
        }

        // Validate numberOfInputs parameter
        if (numberOfInputs == null) {
            result.addError("NumberOfInputs", "NumberOfInputs parameter is required");
        } else {
            Integer numInputs = numberOfInputs.getAsInteger();
            if (numInputs == null) {
                result.addError("NumberOfInputs", "NumberOfInputs must be an integer");
            } else if (numInputs < 1) {
                result.addError("NumberOfInputs", "NumberOfInputs must be at least 1");
            } else if (numInputs > 32) {
                result.addError("NumberOfInputs", "NumberOfInputs cannot exceed 32 (got: " + numInputs + ")");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Count the number of format specifiers in the format string
     * Handles %%  (escaped percent) correctly
     */
    private int countFormatSpecifiers(String formatStr) {
        if (formatStr == null) {
            return 0;
        }

        // Pattern to match format specifiers: %[flags][width][.precision]specifier
        // Handles: %d, %f, %s, %e, %g, etc.
        // Excludes: %% (escaped percent)
        Pattern pattern = Pattern.compile("%(?!%)[-+0 #]?\\d*(?:\\.\\d+)?[diouxXeEfFgGaAcspn]");
        Matcher matcher = pattern.matcher(formatStr);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }

    /**
     * Check if format string contains integer specifiers (%d, %i, etc.)
     */
    public boolean hasIntegerSpecifiers() {
        String formatStr = getFormatValue();
        return formatStr != null && Pattern.compile("%[-+0 #]?\\d*[di]").matcher(formatStr).find();
    }

    /**
     * Check if format string contains floating-point specifiers (%f, %e, %g)
     */
    public boolean hasFloatingPointSpecifiers() {
        String formatStr = getFormatValue();
        return formatStr != null && Pattern.compile("%[-+0 #]?\\d*(?:\\.\\d+)?[fFeEgGaA]").matcher(formatStr).find();
    }

    /**
     * Check if format string contains string specifiers (%s)
     */
    public boolean hasStringSpecifiers() {
        String formatStr = getFormatValue();
        return formatStr != null && Pattern.compile("%[-+0 #]?\\d*s").matcher(formatStr).find();
    }

    /**
     * Get the actual number of format specifiers (calculated from format string)
     */
    public int getActualSpecifierCount() {
        return countFormatSpecifiers(getFormatValue());
    }

    /**
     * Check if the format string and number of inputs are consistent
     */
    public boolean isConsistent() {
        return getActualSpecifierCount() == getNumberOfInputsValue();
    }

    @Override
    public ComposeStringDto copy() {
        return ComposeStringDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .format(format != null ? format.copy() : null)
                .numberOfInputs(numberOfInputs != null ? numberOfInputs.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Format", format)
                .put("NumberOfInputs", numberOfInputs)
                .build();
    }

    @Override
    public String getBlockType() {
        return "ComposeString";
    }

    @Override
    public String toString() {
        return String.format("ComposeStringDto{id=%d, name='%s', type='%s', format='%s', numInputs=%d, specifiers=%d}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getFormatValue(),
                           getNumberOfInputsValue(),
                           getActualSpecifierCount());
    }
}
