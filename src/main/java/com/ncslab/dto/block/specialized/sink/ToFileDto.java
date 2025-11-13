package com.ncslab.dto.block.specialized.sink;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.block.sink.SinkDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO representation of To File block with SIMULINK-compatible parameters.
 *
 * Writes signal data to a MAT file during simulation for post-processing and analysis.
 * Supports multiple save formats (Timeseries, Array) and data decimation.
 *
 * SIMULINK Parameters:
 * - FileName: Name of MAT file to write to (default: "output.mat")
 * - MatrixName: Variable name in MAT file (default: "data")
 * - SaveFormat: Data save format ("Timeseries" or "Array")
 * - Decimation: Decimation factor for saving data (default: 1)
 * - SampleTime: Sample time (-1 for inherited, 0 for continuous)
 *
 * @author NCSLab Team
 * @version 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("To File")
@MigrationCompatible(originalClass = "com.ncslab.block.sink.ToFile")
public class ToFileDto extends SinkDto {

    // ===== TO FILE BLOCK SPECIFIC PARAMETERS =====

    /**
     * Name of MAT file to write to
     * Default: "output.mat"
     * Validation: Must be non-empty string, preferably with .mat extension
     */
    private TypedParameter fileName;

    /**
     * Variable name in MAT file
     * Default: "data"
     * Validation: Must be valid MATLAB variable name
     */
    private TypedParameter matrixName;

    /**
     * Data save format
     * Default: "Timeseries"
     * Values: "Timeseries", "Array"
     */
    private TypedParameter saveFormat;

    /**
     * Decimation factor for saving data
     * Default: 1 (save every sample)
     * Validation: Must be positive integer
     */
    private TypedParameter decimation;

    /**
     * Sample time for data collection
     * Default: -1 (inherited)
     * Validation: -1 (inherited), 0 (continuous), or > 0 (discrete)
     */
    private TypedParameter sampleTime;

    // ===== PARAMETER ACCESS HELPERS =====

    public String getFileNameValue() {
        return fileName != null ? fileName.getAsString() : "output.mat";
    }

    public String getMatrixNameValue() {
        return matrixName != null ? matrixName.getAsString() : "data";
    }

    public String getSaveFormatValue() {
        return saveFormat != null ? saveFormat.getAsString() : "Timeseries";
    }

    public Integer getDecimationValue() {
        return decimation != null ? decimation.getAsInteger() : 1;
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate file name (must be non-empty)
        if (fileName != null) {
            String fileNameStr = fileName.getAsString();
            if (fileNameStr == null || fileNameStr.trim().isEmpty()) {
                result.addError("FileName cannot be empty");
            }
        }

        // Validate matrix name (must be non-empty)
        if (matrixName != null) {
            String matrixNameStr = matrixName.getAsString();
            if (matrixNameStr == null || matrixNameStr.trim().isEmpty()) {
                result.addError("MatrixName cannot be empty");
            }
        }

        // Validate decimation (must be positive integer)
        if (decimation != null) {
            Integer decValue = decimation.getAsInteger();
            if (decValue != null && decValue <= 0) {
                result.addError("Decimation must be positive");
            }
        }

        // Validate sample time
        if (sampleTime != null) {
            Double stValue = sampleTime.getAsDouble();
            if (stValue != null && stValue < -1.0) {
                result.addError("SampleTime must be >= -1");
            }
        }

        // Validate save format
        if (saveFormat != null) {
            String format = saveFormat.getAsString();
            if (format != null && !format.equals("Timeseries") && !format.equals("Array")) {
                result.addError("SaveFormat must be 'Timeseries' or 'Array'");
            }
        }

        return result;
    }

    // ===== TYPE IDENTIFICATION =====

    @Override
    public String getBlockType() {
        return "To File";
    }
}
