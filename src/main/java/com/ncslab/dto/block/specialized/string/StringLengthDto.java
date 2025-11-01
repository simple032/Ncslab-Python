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
 * DTO representation of String Length measurement block.
 *
 * This DTO provides a modern, type-safe interface for the String Length block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * The String Length block outputs the number of characters in the input string.
 * It does not count the null terminator.
 *
 * SIMULINK Behavior:
 * - Input: String signal (scalar)
 * - Output: uint32 scalar (character count)
 * - No parameters required (simple measurement)
 *
 * Example: Input "Hello" produces output 5
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("StringLength")
@MigrationCompatible(originalClass = "com.ncslab.block.string.StringLength")
public class StringLengthDto extends BlockDto {

    // ===== STRING LENGTH SPECIFIC PARAMETERS =====
    // This block has no parameters - it is a simple measurement block

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // String Length block has no specific parameters to validate
        // All validation is done by the parent BlockDto

        return result;
    }

    // ===== UTILITY METHODS =====

    @Override
    public StringLengthDto copy() {
        return StringLengthDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .position(getPosition())
                .dimension(getDimension())
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public java.util.Map<String, String> toLegacyParameters() {
        java.util.Map<String, String> params = new java.util.HashMap<>();

        // String Length has no parameters, so return empty map
        // Only include sample time if set
        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("StringLengthDto{id=%d, name='%s', type='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType());
    }
}
