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
 * DTO representation of Byte Unpack block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the Byte Unpack block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * The Byte Unpack block unpacks bytes into multiple data type values,
 * the reverse operation of the Byte Pack block.
 *
 * SIMULINK Parameters:
 * - datatypes: Data types of outputs to unpack (e.g., "{'int16'}")
 * - byteAlign: Byte alignment for unpacked data (default: "1")
 * - dimensions: Dimensions of output signals (e.g., "{[1]}")
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
@JsonTypeName("Byte Unpack")
@MigrationCompatible(originalClass = "com.ncslab.block.data.ByteUnpack")
public class ByteUnpackDto extends BlockDto {

    // ===== BYTE UNPACK SPECIFIC PARAMETERS =====

    /**
     * Data types of outputs to unpack
     * Format: cell array like "{'int16'}", "{'double', 'int8'}"
     * Default: "{'double'}"
     * Validation: Must be valid MATLAB cell array format
     */
    private TypedParameter datatypes;

    /**
     * Byte alignment for unpacked data
     * Controls memory alignment of unpacked bytes
     * Default: "1"
     * Validation: Must be positive integer as string
     */
    private TypedParameter byteAlign;

    /**
     * Dimensions of output signals
     * Format: cell array like "{[1]}", "{[2 3], [1]}"
     * Default: "{[1]}"
     * Validation: Must be valid MATLAB cell array of dimensions
     */
    private TypedParameter dimensions;

    // ===== PARAMETER ACCESS HELPERS =====

    public String getDatatypesValue() {
        return datatypes != null ? datatypes.getAsString() : "{'double'}";
    }

    public String getByteAlignValue() {
        return byteAlign != null ? byteAlign.getAsString() : "1";
    }

    public String getDimensionsValue() {
        return dimensions != null ? dimensions.getAsString() : "{[1]}";
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

        // Validate dimensions parameter
        if (dimensions == null) {
            result.addError("dimensions", "Dimensions parameter is required");
        } else {
            String dimValue = dimensions.getAsString();
            if (dimValue == null || dimValue.trim().isEmpty()) {
                result.addError("dimensions", "Dimensions cannot be empty");
            } else if (!dimValue.startsWith("{") || !dimValue.endsWith("}")) {
                result.addError("dimensions", "Dimensions must be in cell array format like {[1]}");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    @Override
    public ByteUnpackDto copy() {
        return ByteUnpackDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .position(getPosition())
                .dimension(getDimension())
                .datatypes(datatypes != null ? datatypes.copy() : null)
                .byteAlign(byteAlign != null ? byteAlign.copy() : null)
                .dimensions(dimensions != null ? dimensions.copy() : null)
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
        if (dimensions != null) {
            params.put("dimensions", dimensions.getAsString());
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("ByteUnpackDto{id=%d, name='%s', type='%s', datatypes=%s, byteAlign=%s, dimensions=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getDatatypesValue(),
                           getByteAlignValue(),
                           getDimensionsValue());
    }
}