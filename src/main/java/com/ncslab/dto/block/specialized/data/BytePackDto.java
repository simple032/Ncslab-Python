package com.ncslab.dto.block.specialized.data;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO representation of Byte Pack block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the Byte Pack block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * The Byte Pack block packs multiple data type values into bytes for efficient
 * data transmission or storage.
 *
 * SIMULINK Parameters:
 * - datatypes: Data types of inputs to pack (e.g., "{'int16'}")
 * - byteAlign: Byte alignment for packed data (default: "1")
 *
 * @author DTO Migration Framework
 * @version 1.0
 * @since DTO Migration Week 6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Byte pack")
@MigrationCompatible(originalClass = "com.ncslab.block.data.BytePack")
public class BytePackDto extends BlockDto {

    // ===== BYTE PACK SPECIFIC PARAMETERS =====

    /**
     * Data types of inputs to pack
     * Format: cell array like "{'int16'}", "{'double', 'int8'}"
     * Default: "{'double'}"
     * Validation: Must be valid MATLAB cell array format
     */
    private TypedParameter datatypes;

    /**
     * Byte alignment for packed data
     * Controls memory alignment of packed bytes
     * Default: "1"
     * Validation: Must be positive integer as string
     */
    private TypedParameter byteAlign;

    // ===== PARAMETER ACCESS HELPERS =====

    public String getDatatypesValue() {
        return datatypes != null ? datatypes.getAsString() : "{'double'}";
    }

    public String getByteAlignValue() {
        return byteAlign != null ? byteAlign.getAsString() : "1";
    }

    public Integer getByteAlignAsInteger() {
        try {
            return byteAlign != null ? Integer.parseInt(byteAlign.getAsString()) : 1;
        } catch (NumberFormatException e) {
            return 1; // Default fallback
        }
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate datatypes parameter
        if (datatypes == null) {
            result.addError("datatypes", "Data types parameter is required");
        } else {
            String dtValue = datatypes.getAsString();
            if (dtValue == null || dtValue.trim().isEmpty()) {
                result.addError("datatypes", "Data types cannot be empty");
            } else if (!dtValue.startsWith("{") || !dtValue.endsWith("}")) {
                result.addError("datatypes", "Data types must be in cell array format like {'int16'}");
            }
        }

        // Validate byteAlign parameter
        if (byteAlign != null) {
            String baValue = byteAlign.getAsString();
            if (baValue != null && !baValue.trim().isEmpty()) {
                try {
                    int alignment = Integer.parseInt(baValue);
                    if (alignment <= 0) {
                        result.addError("byteAlign", "Byte alignment must be positive");
                    }
                } catch (NumberFormatException e) {
                    result.addError("byteAlign", "Byte alignment must be a valid integer");
                }
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    @Override
    public BytePackDto copy() {
        return BytePackDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .position(getPosition())
                .dimension(getDimension())
                .datatypes(datatypes != null ? datatypes.copy() : null)
                .byteAlign(byteAlign != null ? byteAlign.copy() : null)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public java.util.Map<String, String> toLegacyParameters() {
        java.util.Map<String, String> params = new java.util.HashMap<>();

        if (datatypes != null) {
            params.put("datatypes", datatypes.getAsString());
        }
        if (byteAlign != null) {
            params.put("byteAlign", byteAlign.getAsString());
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("BytePackDto{id=%d, name='%s', type='%s', datatypes=%s, byteAlign=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getDatatypesValue(),
                           getByteAlignValue());
    }
}