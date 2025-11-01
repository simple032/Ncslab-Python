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
 * DTO representation of String Constant block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the String Constant block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * The String Constant block outputs a constant string signal specified by the String parameter.
 *
 * SIMULINK Parameters:
 * - String: The constant string to output (default: "string")
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 *
 * Character Set: ISO/IEC 8859-1 (first 256 Unicode code points)
 * Restriction: Does not support char(0) "NULL" character
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("StringConstant")
@MigrationCompatible(originalClass = "com.ncslab.block.string.StringConstant")
public class StringConstantDto extends BlockDto {

    // ===== STRING CONSTANT SPECIFIC PARAMETERS =====

    /**
     * The constant string value to output.
     * Default: "string"
     */
    private String stringValue;

    /**
     * Output data type.
     * Default: "string"
     */
    private String outDataType;

    /**
     * Whether to saturate the output on overflow.
     * Default: false
     */
    private Boolean saturate;

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate string value
        if (stringValue == null || stringValue.isEmpty()) {
            result.addWarning("stringValue", "String value is empty - using default 'string'");
        }

        // Validate ISO 8859-1 compatibility
        if (stringValue != null) {
            for (int i = 0; i < stringValue.length(); i++) {
                char c = stringValue.charAt(i);
                if (c > 255) {
                    result.addError("stringValue", "String contains non-ISO 8859-1 characters: '" + c + "' at position " + i);
                }
                if (c == 0) {
                    result.addError("stringValue", "String contains NULL character (char(0)) which is not supported");
                }
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    @Override
    public StringConstantDto copy() {
        return StringConstantDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .position(getPosition())
                .dimension(getDimension())
                .stringValue(this.stringValue)
                .outDataType(this.outDataType)
                .saturate(this.saturate)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public java.util.Map<String, String> toLegacyParameters() {
        java.util.Map<String, String> params = new java.util.HashMap<>();

        if (stringValue != null) {
            params.put("String", stringValue);
        }

        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }

        if (outDataType != null) {
            params.put("OutDataType", outDataType);
        }

        if (saturate != null) {
            params.put("Saturate", saturate.toString());
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("StringConstantDto{id=%d, name='%s', type='%s', stringValue='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           stringValue);
    }
}
