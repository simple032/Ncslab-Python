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
 * DTO representation of StringToEnum block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the StringToEnum block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * The StringToEnum block converts a string to an enumeration value (0-based index).
 *
 * SIMULINK Parameters:
 * - EnumValues: Comma-separated list of enum values (e.g., "Red,Green,Blue")
 *
 * Behavior:
 * - Input: String signal
 * - Output: Integer index (0-based) of matching enumeration value
 * - Case-sensitive string matching
 * - Returns 0 if no match found (defaults to first enum value)
 *
 * Examples:
 * - Input="Red", EnumValues="Red,Green,Blue" → Output=0
 * - Input="Green", EnumValues="Red,Green,Blue" → Output=1
 * - Input="Blue", EnumValues="Red,Green,Blue" → Output=2
 * - Input="Yellow", EnumValues="Red,Green,Blue" → Output=0 (no match)
 *
 * Character Set: ISO/IEC 8859-1 (first 256 Unicode code points)
 *
 * @author NCSLab Team
 * @version 1.0
 * @since Phase 3 String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("StringToEnum")
@MigrationCompatible(originalClass = "com.ncslab.block.string.StringToEnum")
public class StringToEnumDto extends BlockDto {

    // ===== STRING TO ENUM SPECIFIC PARAMETERS =====

    /**
     * Comma-separated list of enumeration values.
     * Example: "Red,Green,Blue"
     * Default: "Value1,Value2,Value3"
     */
    private String enumValues;

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate enumValues
        if (enumValues == null || enumValues.trim().isEmpty()) {
            result.addWarning("enumValues", "Enum values is null or empty - using default 'Value1,Value2,Value3'");
        } else {
            // Verify at least one value
            String[] values = enumValues.split(",");
            if (values.length == 0) {
                result.addError("enumValues", "EnumValues must contain at least one value");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    @Override
    public StringToEnumDto copy() {
        return StringToEnumDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .position(getPosition())
                .dimension(getDimension())
                .enumValues(this.enumValues)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public java.util.Map<String, String> toLegacyParameters() {
        java.util.Map<String, String> params = new java.util.HashMap<>();

        if (enumValues != null) {
            params.put("EnumValues", enumValues);
        }

        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("StringToEnumDto{id=%d, name='%s', type='%s', enumValues='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           enumValues);
    }

    // ===== HELPER METHODS =====

    /**
     * Get the enum values with a default value if null.
     *
     * @return enumValues or "Value1,Value2,Value3" if null
     */
    public String getEnumValuesOrDefault() {
        return enumValues != null ? enumValues : "Value1,Value2,Value3";
    }

    /**
     * Get the enum values as a list (split by comma and trimmed).
     *
     * @return list of enum values
     */
    public java.util.List<String> getEnumValuesAsList() {
        String values = getEnumValuesOrDefault();
        String[] valuesArray = values.split(",");
        java.util.List<String> result = new java.util.ArrayList<>();
        for (String value : valuesArray) {
            result.add(value.trim());
        }
        return result;
    }

    /**
     * Get the number of enum values.
     *
     * @return count of enum values
     */
    public int getEnumValuesCount() {
        return getEnumValuesAsList().size();
    }

    @Override
    public String getBlockType() {
        return "StringToEnum";
    }
}
