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
 * DTO for EnabledAndTriggeredSubsystem block - Combined enable and trigger control.
 *
 * <p>This subsystem combines both Enable and Trigger port functionality, providing
 * conditional execution based on BOTH an enable signal AND trigger events. The subsystem
 * only executes when the enable signal is active AND a trigger event has occurred.</p>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>StatesWhenEnabling</b>: State handling mode - "held" (default) or "reset"</li>
 *   <li><b>TriggerType</b>: Type of trigger detection ("rising", "falling", "either", "function-call")</li>
 *   <li><b>ShowEnablePort</b>: Enable port visibility - "on" (default) or "off"</li>
 *   <li><b>ShowTriggerPort</b>: Trigger port visibility - "on" (default) or "off"</li>
 *   <li><b>ZeroCross</b>: Zero-crossing detection - "on" or "off" (default)</li>
 * </ul>
 *
 * <p><b>Execution Logic:</b></p>
 * <ul>
 *   <li>Subsystem executes ONLY when BOTH conditions are met:</li>
 *   <li>1. Enable signal is active (> 0)</li>
 *   <li>2. Trigger event occurred (edge detected)</li>
 *   <li>Combined logic: if(isEnabled() && isTriggered()) { execute(); }</li>
 * </ul>
 *
 * <p><b>State Handling:</b></p>
 * <ul>
 *   <li><b>held</b>: States maintain their values when disabled (default)</li>
 *   <li><b>reset</b>: States reset to initial conditions when re-enabled</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 enable input port (on top edge)</li>
 *   <li>1 trigger input port (on top edge)</li>
 *   <li>Regular In/Out blocks for data flow (inherited from Subsystem)</li>
 * </ul>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>StatesWhenEnabling must be "held" or "reset"</li>
 *   <li>TriggerType must be "rising", "falling", "either", or "function-call"</li>
 *   <li>ShowEnablePort must be "on" or "off"</li>
 *   <li>ShowTriggerPort must be "on" or "off"</li>
 *   <li>ZeroCross must be "on" or "off"</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("EnabledAndTriggeredSubsystem")
public class EnabledAndTriggeredSubsystemDto extends SubsystemDto {

    /**
     * State handling mode when subsystem is being enabled.
     * "held" - States maintain their values when disabled (default)
     * "reset" - States reset to initial conditions when re-enabled
     */
    private TypedParameter statesWhenEnabling = TypedParameter.of("held");

    /**
     * Trigger type for edge detection.
     * Controls which type of signal transition triggers subsystem execution.
     */
    private TypedParameter triggerType = TypedParameter.of("rising");

    /**
     * Enable port visibility control.
     * "on" - Show enable port (default)
     * "off" - Hide enable port
     */
    private TypedParameter showEnablePort = TypedParameter.of("on");

    /**
     * Trigger port visibility control.
     * "on" - Show trigger port (default)
     * "off" - Hide trigger port
     */
    private TypedParameter showTriggerPort = TypedParameter.of("on");

    /**
     * Zero-crossing detection for enable and trigger signal transitions.
     * "on" - Enable zero-crossing detection for accurate transition timing
     * "off" - No zero-crossing detection (default)
     */
    private TypedParameter zeroCross = TypedParameter.of("off");

    /**
     * Constructs EnabledAndTriggeredSubsystemDto with individual parameters.
     *
     * @param blockName          Name of the subsystem block
     * @param blockPath          Path of the block in the model hierarchy
     * @param statesWhenEnabling State handling mode ("held" or "reset")
     * @param triggerType        Trigger type ("rising", "falling", "either", "function-call")
     * @param showEnablePort     Enable port visibility ("on" or "off")
     * @param showTriggerPort    Trigger port visibility ("on" or "off")
     * @param zeroCross          Zero-crossing detection ("on" or "off")
     */
    public EnabledAndTriggeredSubsystemDto(String blockName, String blockPath,
                                          TypedParameter statesWhenEnabling,
                                          TypedParameter triggerType,
                                          TypedParameter showEnablePort,
                                          TypedParameter showTriggerPort,
                                          TypedParameter zeroCross) {
        super(blockName, blockPath);
        this.statesWhenEnabling = statesWhenEnabling;
        this.triggerType = triggerType;
        this.showEnablePort = showEnablePort;
        this.showTriggerPort = showTriggerPort;
        this.zeroCross = zeroCross;
    }

    /**
     * Constructs EnabledAndTriggeredSubsystemDto with typed parameter map.
     *
     * @param blockName  Name of the subsystem block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public EnabledAndTriggeredSubsystemDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.statesWhenEnabling = parameters.getTypedParameter("StatesWhenEnabling", String.class, "held");
        this.triggerType = parameters.getTypedParameter("TriggerType", String.class, "rising");
        this.showEnablePort = parameters.getTypedParameter("ShowEnablePort", String.class, "on");
        this.showTriggerPort = parameters.getTypedParameter("ShowTriggerPort", String.class, "on");
        this.zeroCross = parameters.getTypedParameter("ZeroCross", String.class, "off");
    }

    /**
     * Creates EnabledAndTriggeredSubsystemDto with specified block metadata.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position  Block position in diagram
     * @param dimension Block visual dimensions
     */
    public EnabledAndTriggeredSubsystemDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
        this.statesWhenEnabling = TypedParameter.of("held");
        this.triggerType = TypedParameter.of("rising");
        this.showEnablePort = TypedParameter.of("on");
        this.showTriggerPort = TypedParameter.of("on");
        this.zeroCross = TypedParameter.of("off");
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

        // Validate TriggerType
        String triggerTypeValue = getTriggerTypeValue();
        if (!isValidTriggerType(triggerTypeValue)) {
            addValidationError("TriggerType must be 'rising', 'falling', 'either', or 'function-call'");
            return false;
        }

        // Validate ShowEnablePort
        String showEnableValue = getShowEnablePortValue();
        if (!isValidOnOffParameter(showEnableValue)) {
            addValidationError("ShowEnablePort must be 'on' or 'off'");
            return false;
        }

        // Validate ShowTriggerPort
        String showTriggerValue = getShowTriggerPortValue();
        if (!isValidOnOffParameter(showTriggerValue)) {
            addValidationError("ShowTriggerPort must be 'on' or 'off'");
            return false;
        }

        // Validate ZeroCross
        String zeroCrossVal = getZeroCrossValue();
        if (!isValidOnOffParameter(zeroCrossVal)) {
            addValidationError("ZeroCross must be 'on' or 'off'");
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

        // Validate TriggerType
        String triggerTypeValue = getTriggerTypeValue();
        if (!isValidTriggerType(triggerTypeValue)) {
            errors.add("TriggerType must be 'rising', 'falling', 'either', or 'function-call'");
        }

        // Validate ShowEnablePort
        String showEnableValue = getShowEnablePortValue();
        if (!isValidOnOffParameter(showEnableValue)) {
            errors.add("ShowEnablePort must be 'on' or 'off'");
        }

        // Validate ShowTriggerPort
        String showTriggerValue = getShowTriggerPortValue();
        if (!isValidOnOffParameter(showTriggerValue)) {
            errors.add("ShowTriggerPort must be 'on' or 'off'");
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
     * Gets the trigger type value.
     *
     * @return Trigger type string ("rising", "falling", "either", "function-call")
     */
    public String getTriggerTypeValue() {
        if (triggerType != null && triggerType.getAsString() != null) {
            return triggerType.getAsString();
        }
        return "rising"; // Default trigger type
    }

    /**
     * Gets the enable port visibility value.
     *
     * @return Enable port visibility ("on" or "off")
     */
    public String getShowEnablePortValue() {
        if (showEnablePort != null && showEnablePort.getAsString() != null) {
            return showEnablePort.getAsString();
        }
        return "on"; // Default on
    }

    /**
     * Gets the trigger port visibility value.
     *
     * @return Trigger port visibility ("on" or "off")
     */
    public String getShowTriggerPortValue() {
        if (showTriggerPort != null && showTriggerPort.getAsString() != null) {
            return showTriggerPort.getAsString();
        }
        return "on"; // Default on
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
     * @return true if mode is "held"
     */
    public boolean isHeldMode() {
        return "held".equalsIgnoreCase(getStatesWhenEnablingValue());
    }

    /**
     * Checks if states reset when re-enabled.
     *
     * @return true if mode is "reset"
     */
    public boolean isResetMode() {
        return "reset".equalsIgnoreCase(getStatesWhenEnablingValue());
    }

    /**
     * Checks if enable port is shown.
     *
     * @return true if ShowEnablePort="on"
     */
    public boolean isEnablePortShown() {
        return "on".equalsIgnoreCase(getShowEnablePortValue());
    }

    /**
     * Checks if trigger port is shown.
     *
     * @return true if ShowTriggerPort="on"
     */
    public boolean isTriggerPortShown() {
        return "on".equalsIgnoreCase(getShowTriggerPortValue());
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
     * Checks if trigger detects rising edges.
     *
     * @return true if trigger type is "rising" or "either"
     */
    public boolean detectsRisingEdge() {
        String type = getTriggerTypeValue();
        return "rising".equals(type) || "either".equals(type);
    }

    /**
     * Checks if trigger detects falling edges.
     *
     * @return true if trigger type is "falling" or "either"
     */
    public boolean detectsFallingEdge() {
        String type = getTriggerTypeValue();
        return "falling".equals(type) || "either".equals(type);
    }

    /**
     * Checks if trigger is function-call based.
     *
     * @return true if trigger type is "function-call"
     */
    public boolean isFunctionCall() {
        return "function-call".equals(getTriggerTypeValue());
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
     * Validates trigger type parameter.
     *
     * @param type Trigger type string
     * @return true if type is valid
     */
    private boolean isValidTriggerType(String type) {
        return type != null &&
               (type.equals("rising") || type.equals("falling") ||
                type.equals("either") || type.equals("function-call"));
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
    public EnabledAndTriggeredSubsystemDto copy() {
        EnabledAndTriggeredSubsystemDto copy = new EnabledAndTriggeredSubsystemDto();

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
        copy.triggerType = triggerType != null ? triggerType.copy() : null;
        copy.showEnablePort = showEnablePort != null ? showEnablePort.copy() : null;
        copy.showTriggerPort = showTriggerPort != null ? showTriggerPort.copy() : null;
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
        TypedParameterMap baseParams = super.toParameterMap();
        return TypedParameterMap.builder()
                .putAll(baseParams)
                .put("StatesWhenEnabling", statesWhenEnabling)
                .put("TriggerType", triggerType)
                .put("ShowEnablePort", showEnablePort)
                .put("ShowTriggerPort", showTriggerPort)
                .put("ZeroCross", zeroCross)
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
            "TriggerType", "Type of trigger detection (rising, falling, either, function-call)",
            "ShowEnablePort", "Enable port visibility control ('on' or 'off')",
            "ShowTriggerPort", "Trigger port visibility control ('on' or 'off')",
            "ZeroCross", "Zero-crossing detection for signal transitions ('on' or 'off')"
        );
    }

    @Override
    public String toString() {
        return String.format("EnabledAndTriggeredSubsystemDto{blockName='%s', statesWhenEnabling='%s', " +
                           "triggerType='%s', showEnablePort='%s', showTriggerPort='%s', zeroCross='%s'}",
                           getBlockName(), getStatesWhenEnablingValue(), getTriggerTypeValue(),
                           getShowEnablePortValue(), getShowTriggerPortValue(), getZeroCrossValue());
    }
}
