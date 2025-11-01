package com.ncslab.dto.block.specialized.string;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO representation of String To ASCII block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the String To ASCII conversion block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * The String To ASCII block converts a string signal to a uint8 vector of ASCII values.
 * Each character in the input string is converted to its corresponding ASCII decimal value.
 *
 * Example: "Hello" → [72, 101, 108, 108, 111]
 *
 * SIMULINK Parameters:
 * - No parameters required (simple conversion block)
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("StringToASCII")
@MigrationCompatible(originalClass = "com.ncslab.block.string.StringToASCII")
public class StringToASCIIDto extends BlockDto {

    // ===== STRING TO ASCII SPECIFIC PARAMETERS =====
    // No block-specific parameters required for simple string-to-ASCII conversion

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate block structure
        // String To ASCII should have exactly 1 input (string) and 1 output (uint8 vector)
        // These checks are performed by the block constructor/initialization

        return result;
    }

    // ===== UTILITY METHODS =====

    @Override
    public StringToASCIIDto copy() {
        return StringToASCIIDto.builder()
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

        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("StringToASCIIDto{id=%d, name='%s', type='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType());
    }
}
