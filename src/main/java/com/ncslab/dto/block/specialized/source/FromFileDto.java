package com.ncslab.dto.block.specialized.source;

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
 * DTO representation of From File block with SIMULINK-compatible parameters.
 *
 * Reads signal data from a MAT file and outputs it during simulation.
 * Supports interpolation, extrapolation, and various data formats.
 *
 * SIMULINK Parameters:
 * - FileName: Name of MAT file to read from (default: "data.mat")
 * - SampleTime: Sample time for output (default: 0 for continuous)
 * - Interpolate: Whether to interpolate between data points (default: true)
 * - ExtrapolationBeforeFirstDataPoint: Behavior before first data point
 *   Values: "Hold first value", "Set to zero", "Extrapolation"
 * - ExtrapolationAfterLastDataPoint: Behavior after last data point
 *   Values: "Hold last value", "Set to zero", "Extrapolation"
 * - ZeroCross: Enable zero-crossing detection (default: true)
 * - OutDataTypeStr: Output data type specification
 *
 * @author NCSLab Team
 * @version 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("From File")
@MigrationCompatible(originalClass = "com.ncslab.block.source.FromFile")
public class FromFileDto extends BlockDto {

    // ===== FROM FILE BLOCK SPECIFIC PARAMETERS =====

    /**
     * Name of MAT file to read from
     * Default: "data.mat"
     * Validation: Must be non-empty string, preferably with .mat extension
     */
    private TypedParameter fileName;

    /**
     * Sample time for output
     * Default: 0 (continuous)
     * Validation: 0 (continuous), -1 (inherited), or > 0 (discrete)
     */
    private TypedParameter sampleTime;

    /**
     * Whether to interpolate between data points
     * Default: true
     */
    private TypedParameter interpolate;

    /**
     * Behavior before first data point is reached
     * Default: "Hold first value"
     * Values: "Hold first value", "Set to zero", "Extrapolation"
     */
    private TypedParameter extrapolationBeforeFirstDataPoint;

    /**
     * Behavior after last data point is reached
     * Default: "Hold last value"
     * Values: "Hold last value", "Set to zero", "Extrapolation"
     */
    private TypedParameter extrapolationAfterLastDataPoint;

    /**
     * Enable zero-crossing detection
     * Default: true
     */
    private TypedParameter zeroCross;

    /**
     * Output data type specification
     * Default: "Inherit: auto"
     */
    private TypedParameter outDataTypeStr;

    // ===== PARAMETER ACCESS HELPERS =====

    public String getFileNameValue() {
        return fileName != null ? fileName.getAsString() : "data.mat";
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : 0.0;
    }

    public Boolean getInterpolateValue() {
        return interpolate != null ? interpolate.getAsBoolean() : true;
    }

    public String getExtrapolationBeforeFirstDataPointValue() {
        return extrapolationBeforeFirstDataPoint != null ?
            extrapolationBeforeFirstDataPoint.getAsString() : "Hold first value";
    }

    public String getExtrapolationAfterLastDataPointValue() {
        return extrapolationAfterLastDataPoint != null ?
            extrapolationAfterLastDataPoint.getAsString() : "Hold last value";
    }

    public Boolean getZeroCrossValue() {
        return zeroCross != null ? zeroCross.getAsBoolean() : true;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: auto";
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

        // Validate sample time
        if (sampleTime != null) {
            Double stValue = sampleTime.getAsDouble();
            if (stValue != null && stValue < -1.0) {
                result.addError("SampleTime must be >= -1");
            }
        }

        // Validate ExtrapolationBeforeFirstDataPoint
        if (extrapolationBeforeFirstDataPoint != null) {
            String behavior = extrapolationBeforeFirstDataPoint.getAsString();
            if (behavior != null && !behavior.equals("Hold first value") &&
                !behavior.equals("Set to zero") && !behavior.equals("Extrapolation")) {
                result.addError("ExtrapolationBeforeFirstDataPoint must be 'Hold first value', 'Set to zero', or 'Extrapolation'");
            }
        }

        // Validate ExtrapolationAfterLastDataPoint
        if (extrapolationAfterLastDataPoint != null) {
            String behavior = extrapolationAfterLastDataPoint.getAsString();
            if (behavior != null && !behavior.equals("Hold last value") &&
                !behavior.equals("Set to zero") && !behavior.equals("Extrapolation")) {
                result.addError("ExtrapolationAfterLastDataPoint must be 'Hold last value', 'Set to zero', or 'Extrapolation'");
            }
        }

        return result;
    }

    // ===== TYPE IDENTIFICATION =====

    @Override
    public String getBlockType() {
        return "From File";
    }
}
