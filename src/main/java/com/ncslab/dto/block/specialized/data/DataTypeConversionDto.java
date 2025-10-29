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
 * DTO representation of Data Type Conversion block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the Data Type Conversion block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * The Data Type Conversion block converts input signals from one data type to another,
 * with optional rounding methods for numeric conversions.
 *
 * SIMULINK Parameters:
 * - OutDataTypeStr: Output data type specification (e.g., "int16", "double")
 * - RndMeth: Rounding method for conversion (e.g., "Round", "Floor", "Ceiling")
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
@JsonTypeName("DataTypeConversion")
@MigrationCompatible(originalClass = "com.ncslab.block.data.DataTypeConversion")
public class DataTypeConversionDto extends BlockDto {

    // ===== DATA TYPE CONVERSION SPECIFIC PARAMETERS =====

    /**
     * Output data type specification
     * Specifies the target data type for conversion
     * Examples: "double", "single", "int8", "int16", "int32", "uint8", "uint16", "uint32", "boolean"
     * Default: "double"
     * Validation: Must be a valid SIMULINK data type
     */
    private TypedParameter outDataTypeStr;

    /**
     * Rounding method for numeric conversions
     * Controls how values are rounded when converting between numeric types
     * Valid values: "Round", "Floor", "Ceiling", "Convergent", "Simplest"
     * Default: "Round"
     * Validation: Must be a valid rounding method
     */
    private TypedParameter rndMeth;

    // ===== PARAMETER ACCESS HELPERS =====

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "double";
    }

    public String getRndMethValue() {
        return rndMeth != null ? rndMeth.getAsString() : "Round";
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate OutDataTypeStr parameter
        if (outDataTypeStr == null) {
            result.addError("OutDataTypeStr", "Output data type parameter is required");
        } else {
            String dataType = outDataTypeStr.getAsString();
            if (dataType == null || dataType.trim().isEmpty()) {
                result.addError("OutDataTypeStr", "Output data type cannot be empty");
            } else if (!isValidDataType(dataType)) {
                result.addError("OutDataTypeStr", "Invalid data type: " + dataType);
            }
        }

        // Validate RndMeth parameter
        if (rndMeth != null) {
            String roundingMethod = rndMeth.getAsString();
            if (roundingMethod != null && !roundingMethod.trim().isEmpty() && !isValidRoundingMethod(roundingMethod)) {
                result.addError("RndMeth", "Invalid rounding method: " + roundingMethod);
            }
        }

        return result;
    }

    /**
     * Validate if the given string is a valid SIMULINK data type
     */
    private boolean isValidDataType(String dataType) {
        String[] validTypes = {
            "double", "single", "half",
            "int8", "int16", "int32", "int64",
            "uint8", "uint16", "uint32", "uint64",
            "boolean", "fixdt", "sfix", "ufix",
            "Inherit: Same as input", "Inherit: auto"
        };

        for (String validType : validTypes) {
            if (validType.equalsIgnoreCase(dataType)) {
                return true;
            }
        }

        // Allow fixdt expressions
        return dataType.startsWith("fixdt(") || dataType.startsWith("sfix(") || dataType.startsWith("ufix(");
    }

    /**
     * Validate if the given string is a valid rounding method
     */
    private boolean isValidRoundingMethod(String method) {
        String[] validMethods = {
            "Round", "Floor", "Ceiling", "Convergent", "Simplest", "Zero"
        };

        for (String validMethod : validMethods) {
            if (validMethod.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    // ===== UTILITY METHODS =====

    @Override
    public DataTypeConversionDto copy() {
        return DataTypeConversionDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .position(getPosition())
                .dimension(getDimension())
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .rndMeth(rndMeth != null ? rndMeth.copy() : null)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public java.util.Map<String, String> toLegacyParameters() {
        java.util.Map<String, String> params = new java.util.HashMap<>();

        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", outDataTypeStr.getAsString());
        }
        if (rndMeth != null) {
            params.put("RndMeth", rndMeth.getAsString());
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("DataTypeConversionDto{id=%d, name='%s', type='%s', outDataType=%s, rndMeth=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getOutDataTypeStrValue(),
                           getRndMethValue());
    }
}