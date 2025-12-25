package com.ncslab.dto.block.specialized.subsystem;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.block.BlockPositionDto;
import com.ncslab.dto.block.BlockDimensionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for Switch Case block - Multi-way conditional branching control.
 *
 * <p>This block provides multi-way branching based on an integer input signal,
 * triggering different Action Subsystems based on the input value. Each case
 * connects to a SwitchCaseActionSubsystem for conditional execution.</p>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>CaseConditions</b>: Case values in MATLAB array format like "{1, 2, [3 4], 5}"</li>
 *   <li><b>ShowDefaultCase</b>: Whether to show default output (on/off)</li>
 *   <li><b>CaseShowStatus</b>: Visibility of each case output (on/off or array)</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 * </ul>
 *
 * <p><b>Case Conditions Format:</b></p>
 * <ul>
 *   <li>Single values: {1, 2, 3} - creates 3 cases</li>
 *   <li>Ranges: {1, [3 4], 5} - case 2 matches 3 or 4</li>
 *   <li>Mixed: {1, [2 3 4], 5} - supports multiple values per case</li>
 * </ul>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>CaseConditions must be valid MATLAB array format</li>
 *   <li>ShowDefaultCase must be "on" or "off"</li>
 *   <li>SampleTime must be >= -1.0 and finite</li>
 *   <li>Case values must be integers</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-12-03
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("SwitchCase")
public class SwitchCaseDto extends BlockDto {

    /**
     * Case conditions in MATLAB array format.
     * Example: "{1, 2, [3 4], 5}" means 4 cases, case 2 matches 3 or 4.
     */
    private TypedParameter caseConditions = TypedParameter.of("{1}");

    /**
     * Show default case flag.
     * Controls whether a default output is provided for unmatched values.
     */
    private TypedParameter showDefaultCase = TypedParameter.of("on");

    /**
     * Case show status.
     * Controls visibility of each case output port.
     */
    private TypedParameter caseShowStatus = TypedParameter.of("on");

    /**
     * Constructs SwitchCaseDto with individual parameters.
     *
     * @param blockName       Name of the switch case block
     * @param blockPath       Path of the block in the model hierarchy
     * @param caseConditions  Case values in MATLAB array format
     * @param showDefaultCase Show default case flag
     * @param caseShowStatus  Case show status
     * @param sampleTime      Sample time parameter
     */
    public SwitchCaseDto(String blockName, String blockPath,
                        TypedParameter caseConditions,
                        TypedParameter showDefaultCase,
                        TypedParameter caseShowStatus,
                        TypedParameter sampleTime) {
        super(blockName, blockPath);
        this.caseConditions = caseConditions;
        this.showDefaultCase = showDefaultCase;
        this.caseShowStatus = caseShowStatus;
        this.sampleTime = sampleTime;
    }

    /**
     * Constructs SwitchCaseDto with typed parameter map.
     *
     * @param blockName  Name of the switch case block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public SwitchCaseDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.caseConditions = parameters.getTypedParameter("CaseConditions", String.class, "{1}");
        this.showDefaultCase = parameters.getTypedParameter("ShowDefaultCase", String.class, "on");
        this.caseShowStatus = parameters.getTypedParameter("CaseShowStatus", String.class, "on");
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
    }

    /**
     * Creates SwitchCaseDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position  Block position in diagram
     * @param dimension Block visual dimensions
     */
    public SwitchCaseDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
        this.caseConditions = TypedParameter.of("{1}");
        this.showDefaultCase = TypedParameter.of("on");
        this.caseShowStatus = TypedParameter.of("on");
        this.sampleTime = TypedParameter.of(-1.0);
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate case conditions
        if (caseConditions == null || caseConditions.getAsString() == null) {
            addValidationError("CaseConditions cannot be null");
            return false;
        }

        String condStr = getCaseConditionsValue();
        if (condStr.trim().isEmpty()) {
            addValidationError("CaseConditions cannot be empty");
            return false;
        }

        // Basic format validation
        if (!condStr.contains("{") || !condStr.contains("}")) {
            addValidationError("CaseConditions must be in MATLAB array format like {1, 2, 3}");
            return false;
        }

        // Validate show default case
        if (showDefaultCase == null || showDefaultCase.getAsString() == null) {
            addValidationError("ShowDefaultCase cannot be null");
            return false;
        }

        String showDefault = getShowDefaultCaseValue();
        if (!isValidOnOff(showDefault)) {
            addValidationError("ShowDefaultCase must be 'on' or 'off'");
            return false;
        }

        // Validate sample time
        if (sampleTime == null || sampleTime.getAsDouble() == null) {
            addValidationError("Sample time cannot be null");
            return false;
        }

        double sampleTimeValue = getSampleTimeValue();
        if (sampleTimeValue < -1.0 || Double.isNaN(sampleTimeValue) || Double.isInfinite(sampleTimeValue)) {
            addValidationError("Sample time must be >= -1.0 and finite");
            return false;
        }

        return true;
    }

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();

        // Validate case conditions
        if (caseConditions != null && caseConditions.getAsString() != null) {
            String condStr = getCaseConditionsValue();
            if (condStr.trim().isEmpty()) {
                errors.add("CaseConditions cannot be empty");
            } else if (!condStr.contains("{") || !condStr.contains("}")) {
                errors.add("CaseConditions must be in MATLAB array format like {1, 2, 3}");
            }
        }

        // Validate show default case
        if (showDefaultCase != null && showDefaultCase.getAsString() != null) {
            String showDefault = getShowDefaultCaseValue();
            if (!isValidOnOff(showDefault)) {
                errors.add("ShowDefaultCase must be 'on' or 'off'");
            }
        }

        // Validate sample time
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            double stValue = getSampleTimeValue();
            if (stValue < -1.0 || Double.isNaN(stValue) || Double.isInfinite(stValue)) {
                errors.add("Sample time must be >= -1.0 and finite");
            }
        }

        return errors;
    }

    // === Validation Helper Methods ===

    /**
     * Validates if the given string is a valid on/off value.
     *
     * @param value Value to validate
     * @return true if valid
     */
    private boolean isValidOnOff(String value) {
        return value != null && (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("off") ||
                                value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"));
    }

    // === Parameter Access Methods ===

    /**
     * Gets the case conditions value.
     *
     * @return Case conditions in MATLAB array format
     */
    public String getCaseConditionsValue() {
        if (caseConditions != null && caseConditions.getAsString() != null) {
            return caseConditions.getAsString();
        }
        return "{1}";
    }

    /**
     * Gets the show default case flag value.
     *
     * @return "on" or "off"
     */
    public String getShowDefaultCaseValue() {
        if (showDefaultCase != null && showDefaultCase.getAsString() != null) {
            return showDefaultCase.getAsString();
        }
        return "on";
    }

    /**
     * Gets the case show status value.
     *
     * @return Case show status string
     */
    public String getCaseShowStatusValue() {
        if (caseShowStatus != null && caseShowStatus.getAsString() != null) {
            return caseShowStatus.getAsString();
        }
        return "on";
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time for discrete operation
     */
    public double getSampleTimeValue() {
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            return sampleTime.getAsDouble();
        }
        return -1.0; // Default inherited
    }

    // === Helper Methods ===

    /**
     * Checks if the default case is shown.
     *
     * @return true if default case should be shown
     */
    public boolean isShowDefaultCase() {
        String value = getShowDefaultCaseValue();
        return "on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    /**
     * Checks if the switch case inherits its sample time.
     *
     * @return true if sample time is -1 (inherited)
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    // === Setter Methods ===

    /**
     * Sets the case conditions.
     *
     * @param conditions Case conditions in MATLAB array format
     */
    public void setCaseConditionsValue(String conditions) {
        this.caseConditions = TypedParameter.of(conditions);
    }

    /**
     * Sets the show default case flag.
     *
     * @param show Show default case flag ("on" or "off")
     */
    public void setShowDefaultCaseValue(String show) {
        if (isValidOnOff(show)) {
            this.showDefaultCase = TypedParameter.of(show);
        }
    }

    // === Factory Methods ===

    @Override
    public SwitchCaseDto copy() {
        SwitchCaseDto copy = new SwitchCaseDto();

        // Copy base fields
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());

        // Copy DTO-specific fields
        copy.caseConditions = caseConditions != null ? caseConditions.copy() : null;
        copy.showDefaultCase = showDefaultCase != null ? showDefaultCase.copy() : null;
        copy.caseShowStatus = caseShowStatus != null ? caseShowStatus.copy() : null;

        return copy;
    }

    /**
     * Creates a TypedParameterMap from this DTO's parameters.
     *
     * @return TypedParameterMap containing all block parameters
     */
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("CaseConditions", caseConditions)
                .put("ShowDefaultCase", showDefaultCase)
                .put("CaseShowStatus", caseShowStatus)
                .put("SampleTime", sampleTime)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "CaseConditions", "Case values in MATLAB array format like {1, 2, [3 4], 5}",
            "ShowDefaultCase", "Show default output for unmatched values (on/off)",
            "CaseShowStatus", "Visibility of each case output port",
            "SampleTime", "Sample time for discrete operation (-1 for inherited)"
        );
    }

    @Override
    public String toString() {
        return String.format("SwitchCaseDto{blockName='%s', caseConditions='%s', showDefaultCase='%s', sampleTime=%.3f}",
                           getBlockName(), getCaseConditionsValue(), getShowDefaultCaseValue(), getSampleTimeValue());
    }
}
