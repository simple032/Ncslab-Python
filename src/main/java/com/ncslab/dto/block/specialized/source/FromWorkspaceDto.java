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
 * DTO representation of From Workspace block with SIMULINK-compatible parameters.
 *
 * Reads signal data from a workspace variable and outputs it during simulation.
 * Supports interpolation and various behaviors after final value.
 *
 * SIMULINK Parameters:
 * - VariableName: Name of workspace variable to read from
 * - SampleTime: Sample time for output (default: 0 for continuous)
 * - Interpolate: Whether to interpolate between data points (default: true)
 * - ZeroCross: Enable zero-crossing detection (default: true)
 * - OutputAfterFinalValue: Behavior after final value
 *   Values: "Extrapolation", "Holding final value", "Setting to zero", "Cyclic repetition"
 * - OutDataTypeStr: Output data type specification
 * - FormOutput: Form output (default: "Array")
 *
 * @author NCSLab Team
 * @version 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("From Workspace")
@MigrationCompatible(originalClass = "com.ncslab.block.source.FromWorkspace")
public class FromWorkspaceDto extends BlockDto {

    // ===== FROM WORKSPACE BLOCK SPECIFIC PARAMETERS =====

    /**
     * Name of workspace variable to read from
     * Default: "simin"
     * Validation: Must be valid MATLAB variable name
     */
    private TypedParameter variableName;

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
     * Enable zero-crossing detection
     * Default: true
     */
    private TypedParameter zeroCross;

    /**
     * Behavior after final value is reached
     * Default: "Holding final value"
     * Values: "Extrapolation", "Holding final value", "Setting to zero", "Cyclic repetition"
     */
    private TypedParameter outputAfterFinalValue;

    /**
     * Output data type specification
     * Default: "Inherit: auto"
     */
    private TypedParameter outDataTypeStr;

    /**
     * Form output
     * Default: "Array"
     */
    private TypedParameter formOutput;

    // ===== PARAMETER ACCESS HELPERS =====

    public String getVariableNameValue() {
        return variableName != null ? variableName.getAsString() : "simin";
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : 0.0;
    }

    public Boolean getInterpolateValue() {
        return interpolate != null ? interpolate.getAsBoolean() : true;
    }

    public Boolean getZeroCrossValue() {
        return zeroCross != null ? zeroCross.getAsBoolean() : true;
    }

    public String getOutputAfterFinalValueValue() {
        return outputAfterFinalValue != null ? outputAfterFinalValue.getAsString() : "Holding final value";
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: auto";
    }

    public String getFormOutputValue() {
        return formOutput != null ? formOutput.getAsString() : "Array";
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate variable name (must be non-empty)
        if (variableName != null && (variableName.getAsString() == null || variableName.getAsString().trim().isEmpty())) {
            result.addError("VariableName cannot be empty");
        }

        // Validate sample time
        if (sampleTime != null) {
            Double stValue = sampleTime.getAsDouble();
            if (stValue != null && stValue < -1.0) {
                result.addError("SampleTime must be >= -1");
            }
        }

        // Validate OutputAfterFinalValue
        if (outputAfterFinalValue != null) {
            String behavior = outputAfterFinalValue.getAsString();
            if (behavior != null && !behavior.equals("Extrapolation") &&
                !behavior.equals("Holding final value") &&
                !behavior.equals("Setting to zero") &&
                !behavior.equals("Cyclic repetition")) {
                result.addError("OutputAfterFinalValue must be 'Extrapolation', 'Holding final value', 'Setting to zero', or 'Cyclic repetition'");
            }
        }

        return result;
    }

    // ===== TYPE IDENTIFICATION =====

    @Override
    public String getBlockType() {
        return "From Workspace";
    }
}
