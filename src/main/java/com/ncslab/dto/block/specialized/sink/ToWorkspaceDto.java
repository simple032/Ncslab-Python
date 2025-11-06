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
 * DTO representation of To Workspace block with SIMULINK-compatible parameters.
 *
 * Logs signal data to a workspace variable during simulation for analysis and visualization.
 * Data can be saved in multiple formats (Array, Structure, StructureWithTime).
 *
 * SIMULINK Parameters:
 * - VariableName: Name of workspace variable to save to
 * - MaxDataPoints: Maximum number of data points to save (default: inf)
 * - Decimation: Decimation factor for saving data (default: 1)
 * - SampleTime: Sample time (-1 for inherited)
 * - SaveFormat: "Array", "Structure", or "StructureWithTime"
 * - FixptAsFi: Save fixed-point data as fi object (default: false)
 *
 * @author NCSLab Team
 * @version 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("To Workspace")
@MigrationCompatible(originalClass = "com.ncslab.block.sink.ToWorkspace")
public class ToWorkspaceDto extends SinkDto {

    // ===== TO WORKSPACE BLOCK SPECIFIC PARAMETERS =====

    /**
     * Name of workspace variable to save to
     * Default: "simout"
     * Validation: Must be valid MATLAB variable name
     */
    private TypedParameter variableName;

    /**
     * Maximum number of data points to save
     * Default: "inf" (unlimited)
     * Validation: Must be positive or "inf"
     */
    private TypedParameter maxDataPoints;

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

    /**
     * Data save format
     * Default: "Array"
     * Values: "Array", "Structure", "StructureWithTime"
     */
    private TypedParameter saveFormat;

    /**
     * Save fixed-point data as fi object
     * Default: false
     */
    private TypedParameter fixptAsFi;

    // ===== PARAMETER ACCESS HELPERS =====

    public String getVariableNameValue() {
        return variableName != null ? variableName.getAsString() : "simout";
    }

    public String getMaxDataPointsValue() {
        return maxDataPoints != null ? maxDataPoints.getAsString() : "inf";
    }

    public Integer getDecimationValue() {
        return decimation != null ? decimation.getAsInteger() : 1;
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getSaveFormatValue() {
        return saveFormat != null ? saveFormat.getAsString() : "Array";
    }

    public Boolean getFixptAsFiValue() {
        return fixptAsFi != null ? fixptAsFi.getAsBoolean() : false;
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate variable name (must be non-empty)
        if (variableName != null && (variableName.getAsString() == null || variableName.getAsString().trim().isEmpty())) {
            result.addError("VariableName cannot be empty");
        }

        // Validate decimation (must be positive integer)
        if (decimation != null) {
            Integer decValue = decimation.getAsInteger();
            if (decValue != null && decValue <= 0) {
                result.addError("Decimation must be positive");
            }
        }

        // Validate save format
        if (saveFormat != null) {
            String format = saveFormat.getAsString();
            if (format != null && !format.equals("Array") && !format.equals("Structure") && !format.equals("StructureWithTime")) {
                result.addError("SaveFormat must be 'Array', 'Structure', or 'StructureWithTime'");
            }
        }

        return result;
    }

    // ===== TYPE IDENTIFICATION =====

    @Override
    public String getBlockType() {
        return "To Workspace";
    }
}
