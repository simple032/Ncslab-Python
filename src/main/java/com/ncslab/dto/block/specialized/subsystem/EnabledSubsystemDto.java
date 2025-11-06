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
 * DTO for Enabled Subsystem block - Pre-configured subsystem with Enable port.
 *
 * <p>This block represents a specialized subsystem that automatically includes
 * an Enable block for conditional execution control. It combines the hierarchical
 * modeling capabilities of a subsystem with built-in enable/disable functionality.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Automatic Enable block integration</li>
 *   <li>Conditional subsystem execution based on enable signal</li>
 *   <li>Configurable state handling (held/reset modes)</li>
 *   <li>Enable port exposed on subsystem boundary</li>
 * </ul>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>StatesWhenEnabling</b>: "held" (default) - states maintain values when disabled,
 *                                   "reset" - states reset to initial conditions when re-enabled</li>
 *   <li><b>ShowOutputPort</b>: "on" - creates output port for enable signal passthrough,
 *                              "off" (default) - no output port</li>
 *   <li><b>ZeroCross</b>: "on" - enable zero-crossing detection for accurate transitions,
 *                         "off" (default) - no zero-crossing detection</li>
 *   <li><b>SampleTime</b>: -1 (inherited, default), 0 (continuous), >0 (discrete)</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 enable input port (scalar double, on top edge of subsystem)</li>
 *   <li>Variable data input ports (from internal Inport blocks)</li>
 *   <li>Variable data output ports (from internal Outport blocks)</li>
 *   <li>Optional enable output port (if ShowOutputPort="on")</li>
 * </ul>
 *
 * <p><b>State Handling:</b></p>
 * <ul>
 *   <li><b>held mode</b>: Subsystem states freeze at current values when disabled,
 *                         resume from frozen values when re-enabled</li>
 *   <li><b>reset mode</b>: Subsystem states reset to initial conditions when re-enabled,
 *                          previous state values are discarded</li>
 * </ul>
 *
 * <p><b>Internal Structure:</b></p>
 * <ul>
 *   <li>Automatically creates internal Enable block</li>
 *   <li>Enable block parameters match subsystem parameters</li>
 *   <li>Enable port exposed on subsystem boundary (top edge)</li>
 *   <li>Regular In/Out blocks for data flow</li>
 * </ul>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>StatesWhenEnabling must be "held" or "reset"</li>
 *   <li>ShowOutputPort must be "on" or "off"</li>
 *   <li>ZeroCross must be "on" or "off"</li>
 *   <li>SampleTime must be >= -1.0 and finite</li>
 *   <li>Internal Enable block must exist and be properly configured</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-11-03
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("EnabledSubsystem")
public class EnabledSubsystemDto extends SubsystemDto {

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
     * Constructs EnabledSubsystemDto with individual parameters.
     *
     * @param blockName           Name of the enabled subsystem
     * @param blockPath           Path of the block in the model hierarchy
     * @param statesWhenEnabling  State handling mode ("held" or "reset")
     * @param showOutputPort      Output port visibility ("on" or "off")
     * @param zeroCross           Zero-crossing detection ("on" or "off")
     * @param sampleTime          Sample time parameter
     */
    public EnabledSubsystemDto(String blockName, String blockPath,
                               TypedParameter statesWhenEnabling,
                               TypedParameter showOutputPort,
                               TypedParameter zeroCross,
                               TypedParameter sampleTime) {
        super(blockName, blockPath,
              TypedParameter.of(""),      // subsystemDescription
              TypedParameter.of(true),    // showPortLabels
              TypedParameter.of(false),   // readOnly
              sampleTime);                // sampleTime
        this.statesWhenEnabling = statesWhenEnabling;
        this.showOutputPort = showOutputPort;
        this.zeroCross = zeroCross;
    }

    /**
     * Constructs EnabledSubsystemDto with typed parameter map.
     *
     * @param blockName  Name of the enabled subsystem
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public EnabledSubsystemDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath, parameters);
        this.statesWhenEnabling = parameters.getTypedParameter("StatesWhenEnabling", String.class, "held");
        this.showOutputPort = parameters.getTypedParameter("ShowOutputPort", String.class, "off");
        this.zeroCross = parameters.getTypedParameter("ZeroCross", String.class, "off");
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
    }

    /**
     * Creates EnabledSubsystemDto with specified block metadata.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position  Block position in diagram
     * @param dimension Block visual dimensions
     */
    public EnabledSubsystemDto(String blockName, String blockPath,
                               BlockPositionDto position, BlockDimensionDto dimension) {
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

        // Sample time validation is already handled by parent SubsystemDto

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
    public boolean hasEnableOutputPort() {
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
    public EnabledSubsystemDto copy() {
        EnabledSubsystemDto copy = new EnabledSubsystemDto();

        // Copy base fields from SubsystemDto
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());

        // Copy SubsystemDto-specific fields
        copy.setSubsystemDescription(getSubsystemDescription());
        copy.setShowPortLabels(getShowPortLabels());
        copy.setReadOnly(getReadOnly());
        copy.setMaskType(getMaskType());
        copy.setNumInputPorts(getNumInputPorts());
        copy.setNumOutputPorts(getNumOutputPorts());

        // Copy EnabledSubsystemDto-specific fields
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
        // Start with parent parameter map
        TypedParameterMap params = super.toParameterMap();

        // Add EnabledSubsystem-specific parameters
        params.put("StatesWhenEnabling", statesWhenEnabling);
        params.put("ShowOutputPort", showOutputPort);
        params.put("ZeroCross", zeroCross);

        return params;
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
            "SampleTime", "Sample time for discrete operation (-1 for inherited, 0 for continuous)",
            "Description", "Optional description of subsystem functionality",
            "ShowPortLabels", "Display port labels on subsystem block",
            "ReadOnly", "Subsystem contents cannot be modified when true"
        );
    }

    @Override
    public String toString() {
        return String.format("EnabledSubsystemDto{blockName='%s', inputs=%d, outputs=%d, " +
                           "statesWhenEnabling='%s', showOutputPort='%s', zeroCross='%s', sampleTime=%.3f}",
                           getBlockName(), getNumInputPortsValue(), getNumOutputPortsValue(),
                           getStatesWhenEnablingValue(), getShowOutputPortValue(),
                           getZeroCrossValue(), getSampleTimeValue());
    }
}
