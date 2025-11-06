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
 * DTO for Enable block - Conditional subsystem execution control.
 *
 * <p>This block enables or disables execution of its containing subsystem
 * based on the value of an input signal. When the enable signal is greater than
 * zero, the subsystem executes normally. When the enable signal is zero or negative,
 * the subsystem execution is controlled by the StatesWhenEnabling parameter.</p>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>StatesWhenEnabling</b>: State handling mode - "held" (default) or "reset"</li>
 *   <li><b>ShowOutputPort</b>: Enable signal passthrough - "on" or "off" (default)</li>
 *   <li><b>ZeroCross</b>: Zero-crossing detection - "on" or "off" (default)</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 * </ul>
 *
 * <p><b>State Handling Modes:</b></p>
 * <ul>
 *   <li><b>held</b>: States maintain their values when disabled (default behavior)</li>
 *   <li><b>reset</b>: States reset to initial conditions when re-enabled</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 input port (enable signal - scalar double)</li>
 *   <li>0 or 1 output port (optional enable signal passthrough if ShowOutputPort="on")</li>
 * </ul>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>StatesWhenEnabling must be "held" or "reset"</li>
 *   <li>ShowOutputPort must be "on" or "off"</li>
 *   <li>ZeroCross must be "on" or "off"</li>
 *   <li>SampleTime must be >= -1.0 and finite</li>
 *   <li>Must be contained within a subsystem block</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Enable")
public class EnableDto extends BlockDto {

    /**
     * State handling mode when subsystem is being enabled.
     * "held" - States maintain their values when disabled (default)
     * "reset" - States reset to initial conditions when re-enabled
     */
    private TypedParameter statesWhenEnabling = TypedParameter.of("held");

    /**
     * Enable signal passthrough control.
     * "on" - Creates output port that passes through the enable signal
     * "off" - No output port (default)
     */
    private TypedParameter showOutputPort = TypedParameter.of("off");

    /**
     * Zero-crossing detection for enable signal transitions.
     * "on" - Enable zero-crossing detection for accurate transition timing
     * "off" - No zero-crossing detection (default)
     */
    private TypedParameter zeroCross = TypedParameter.of("off");

    /**
     * Constructs EnableDto with individual parameters.
     *
     * @param blockName          Name of the enable block
     * @param blockPath          Path of the block in the model hierarchy
     * @param statesWhenEnabling State handling mode ("held" or "reset")
     * @param showOutputPort     Output port visibility ("on" or "off")
     * @param zeroCross          Zero-crossing detection ("on" or "off")
     * @param sampleTime         Sample time parameter
     */
    public EnableDto(String blockName, String blockPath,
                     TypedParameter statesWhenEnabling,
                     TypedParameter showOutputPort,
                     TypedParameter zeroCross,
                     TypedParameter sampleTime) {
        super(blockName, blockPath);
        this.statesWhenEnabling = statesWhenEnabling;
        this.showOutputPort = showOutputPort;
        this.zeroCross = zeroCross;
        this.sampleTime = sampleTime;
    }

    /**
     * Constructs EnableDto with typed parameter map.
     *
     * @param blockName  Name of the enable block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public EnableDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.statesWhenEnabling = parameters.getTypedParameter("StatesWhenEnabling", String.class, "held");
        this.showOutputPort = parameters.getTypedParameter("ShowOutputPort", String.class, "off");
        this.zeroCross = parameters.getTypedParameter("ZeroCross", String.class, "off");
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
    }

    /**
     * Creates EnableDto with specified block metadata.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position  Block position in diagram
     * @param dimension Block visual dimensions
     */
    public EnableDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
        this.statesWhenEnabling = TypedParameter.of("held");
        this.showOutputPort = TypedParameter.of("off");
        this.zeroCross = TypedParameter.of("off");
        this.sampleTime = TypedParameter.of(-1.0);
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate StatesWhenEnabling
        String statesMode = getStatesWhenEnablingValue();
        if (!isValidStatesMode(statesMode)) {
            addValidationError("StatesWhenEnabling must be 'held' or 'reset'");
            return false;
        }

        // Validate ShowOutputPort
        String showOutput = getShowOutputPortValue();
        if (!isValidOnOffParameter(showOutput)) {
            addValidationError("ShowOutputPort must be 'on' or 'off'");
            return false;
        }

        // Validate ZeroCross
        String zeroCrossVal = getZeroCrossValue();
        if (!isValidOnOffParameter(zeroCrossVal)) {
            addValidationError("ZeroCross must be 'on' or 'off'");
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

        // Validate StatesWhenEnabling
        String statesMode = getStatesWhenEnablingValue();
        if (!isValidStatesMode(statesMode)) {
            errors.add("StatesWhenEnabling must be 'held' or 'reset'");
        }

        // Validate ShowOutputPort
        String showOutput = getShowOutputPortValue();
        if (!isValidOnOffParameter(showOutput)) {
            errors.add("ShowOutputPort must be 'on' or 'off'");
        }

        // Validate ZeroCross
        String zeroCrossVal = getZeroCrossValue();
        if (!isValidOnOffParameter(zeroCrossVal)) {
            errors.add("ZeroCross must be 'on' or 'off'");
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

    // === Parameter Access Methods ===

    /**
     * Gets the state handling mode value.
     *
     * @return State handling mode ("held" or "reset")
     */
    public String getStatesWhenEnablingValue() {
        if (statesWhenEnabling != null && statesWhenEnabling.getAsString() != null) {
            return statesWhenEnabling.getAsString();
        }
        return "held"; // Default mode
    }

    /**
     * Gets the output port visibility value.
     *
     * @return Output port visibility ("on" or "off")
     */
    public String getShowOutputPortValue() {
        if (showOutputPort != null && showOutputPort.getAsString() != null) {
            return showOutputPort.getAsString();
        }
        return "off"; // Default off
    }

    /**
     * Gets the zero-crossing detection value.
     *
     * @return Zero-crossing detection setting ("on" or "off")
     */
    public String getZeroCrossValue() {
        if (zeroCross != null && zeroCross.getAsString() != null) {
            return zeroCross.getAsString();
        }
        return "off"; // Default off
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
     * Checks if states are held when disabled.
     *
     * @return true if states maintain their values when disabled
     */
    public boolean isHeldMode() {
        return "held".equalsIgnoreCase(getStatesWhenEnablingValue());
    }

    /**
     * Checks if states reset when re-enabled.
     *
     * @return true if states reset to initial conditions when re-enabled
     */
    public boolean isResetMode() {
        return "reset".equalsIgnoreCase(getStatesWhenEnablingValue());
    }

    /**
     * Checks if output port is shown.
     *
     * @return true if enable signal is passed through output port
     */
    public boolean hasOutputPort() {
        return "on".equalsIgnoreCase(getShowOutputPortValue());
    }

    /**
     * Checks if zero-crossing detection is enabled.
     *
     * @return true if zero-crossing detection is active
     */
    public boolean isZeroCrossEnabled() {
        return "on".equalsIgnoreCase(getZeroCrossValue());
    }

    /**
     * Checks if the enable block is configured for continuous time operation.
     *
     * @return true if sample time is 0 (continuous)
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if the enable block inherits its sample time.
     *
     * @return true if sample time is -1 (inherited)
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Checks if the enable block is configured for discrete time operation.
     *
     * @return true if sample time is positive (discrete)
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }

    // === Validation Helpers ===

    /**
     * Validates states mode parameter.
     *
     * @param mode State handling mode string
     * @return true if mode is valid ("held" or "reset")
     */
    private boolean isValidStatesMode(String mode) {
        return mode != null && (mode.equalsIgnoreCase("held") || mode.equalsIgnoreCase("reset"));
    }

    /**
     * Validates on/off parameter.
     *
     * @param value Parameter value string
     * @return true if value is valid ("on" or "off")
     */
    private boolean isValidOnOffParameter(String value) {
        return value != null && (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("off"));
    }

    // === Factory Methods ===

    @Override
    public EnableDto copy() {
        EnableDto copy = new EnableDto();

        // Copy base fields
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());

        // Copy DTO-specific fields
        copy.statesWhenEnabling = statesWhenEnabling != null ? statesWhenEnabling.copy() : null;
        copy.showOutputPort = showOutputPort != null ? showOutputPort.copy() : null;
        copy.zeroCross = zeroCross != null ? zeroCross.copy() : null;

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
                .put("StatesWhenEnabling", statesWhenEnabling)
                .put("ShowOutputPort", showOutputPort)
                .put("ZeroCross", zeroCross)
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
            "StatesWhenEnabling", "State handling mode when subsystem is enabled ('held' or 'reset')",
            "ShowOutputPort", "Enable signal passthrough control ('on' or 'off')",
            "ZeroCross", "Zero-crossing detection for enable signal transitions ('on' or 'off')",
            "SampleTime", "Sample time for discrete operation (-1 for inherited, 0 for continuous)"
        );
    }

    @Override
    public String toString() {
        return String.format("EnableDto{blockName='%s', statesWhenEnabling='%s', showOutputPort='%s', zeroCross='%s', sampleTime=%.3f}",
                           getBlockName(), getStatesWhenEnablingValue(), getShowOutputPortValue(),
                           getZeroCrossValue(), getSampleTimeValue());
    }
}
