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
 * DTO for Switch Case Action Subsystem block - Conditional subsystem for SwitchCase blocks.
 *
 * <p>This block extends the standard Subsystem to work with SwitchCase blocks,
 * providing conditional execution based on which case is matched. The subsystem
 * executes only when its corresponding case in the parent SwitchCase block is active.</p>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>ActionPortType</b>: Type of action port ("default", "error", "message")</li>
 *   <li><b>InitializeStates</b>: State handling mode ("held", "reset")</li>
 *   <li><b>PropagateVarSize</b>: Variable size propagation ("on", "off")</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 * </ul>
 *
 * <p><b>Action Port Types:</b></p>
 * <ul>
 *   <li><b>default</b>: Standard action port - executes when case is matched</li>
 *   <li><b>error</b>: Error handling port - catches execution errors</li>
 *   <li><b>message</b>: Message port - receives message data</li>
 * </ul>
 *
 * <p><b>State Handling Modes:</b></p>
 * <ul>
 *   <li><b>held</b>: State values are held when case is inactive</li>
 *   <li><b>reset</b>: State values are reset when case becomes active again</li>
 * </ul>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>ActionPortType must be one of: "default", "error", "message"</li>
 *   <li>InitializeStates must be one of: "held", "reset"</li>
 *   <li>PropagateVarSize must be one of: "on", "off"</li>
 *   <li>SampleTime must be >= -1.0 and finite</li>
 *   <li>Must be connected to SwitchCase block output</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-12-03
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("SwitchCaseActionSubsystem")
public class SwitchCaseActionSubsystemDto extends SubsystemDto {

    /**
     * Type of action port.
     * Determines the behavior and purpose of this action subsystem.
     * Valid values: "default", "error", "message"
     */
    private TypedParameter actionPortType = TypedParameter.of("default");

    /**
     * State handling mode when action becomes inactive.
     * Controls whether subsystem states are held or reset between executions.
     * Valid values: "held", "reset"
     */
    private TypedParameter initializeStates = TypedParameter.of("held");

    /**
     * Variable size propagation flag.
     * Controls whether variable-size signals are allowed to propagate through the subsystem.
     * Valid values: "on", "off"
     */
    private TypedParameter propagateVarSize = TypedParameter.of("off");

    /**
     * Constructs SwitchCaseActionSubsystemDto with individual parameters.
     *
     * @param blockName         Name of the action subsystem block
     * @param blockPath         Path of the block in the model hierarchy
     * @param actionPortType    Type of action port
     * @param initializeStates  State handling mode
     * @param propagateVarSize  Variable size propagation flag
     * @param sampleTime        Sample time parameter
     */
    public SwitchCaseActionSubsystemDto(String blockName, String blockPath,
                                       TypedParameter actionPortType,
                                       TypedParameter initializeStates,
                                       TypedParameter propagateVarSize,
                                       TypedParameter sampleTime) {
        super(blockName, blockPath);
        this.actionPortType = actionPortType;
        this.initializeStates = initializeStates;
        this.propagateVarSize = propagateVarSize;
        this.sampleTime = sampleTime;
    }

    /**
     * Constructs SwitchCaseActionSubsystemDto with typed parameter map.
     *
     * @param blockName  Name of the action subsystem block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public SwitchCaseActionSubsystemDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.actionPortType = parameters.getTypedParameter("ActionPortType", String.class, "default");
        this.initializeStates = parameters.getTypedParameter("InitializeStates", String.class, "held");
        this.propagateVarSize = parameters.getTypedParameter("PropagateVarSize", String.class, "off");
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
    }

    /**
     * Creates SwitchCaseActionSubsystemDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position  Block position in diagram
     * @param dimension Block visual dimensions
     */
    public SwitchCaseActionSubsystemDto(String blockName, String blockPath,
                                       BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
        this.actionPortType = TypedParameter.of("default");
        this.initializeStates = TypedParameter.of("held");
        this.propagateVarSize = TypedParameter.of("off");
        this.sampleTime = TypedParameter.of(-1.0);
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate action port type
        if (actionPortType == null || actionPortType.getAsString() == null) {
            addValidationError("ActionPortType cannot be null");
            return false;
        }

        String portType = getActionPortTypeValue();
        if (!isValidActionPortType(portType)) {
            addValidationError("ActionPortType must be one of: default, error, message");
            return false;
        }

        // Validate initialize states mode
        if (initializeStates == null || initializeStates.getAsString() == null) {
            addValidationError("InitializeStates cannot be null");
            return false;
        }

        String statesMode = getInitializeStatesValue();
        if (!isValidInitializeStates(statesMode)) {
            addValidationError("InitializeStates must be one of: held, reset");
            return false;
        }

        // Validate propagate var size
        if (propagateVarSize == null || propagateVarSize.getAsString() == null) {
            addValidationError("PropagateVarSize cannot be null");
            return false;
        }

        String varSizeProp = getPropagateVarSizeValue();
        if (!isValidOnOff(varSizeProp)) {
            addValidationError("PropagateVarSize must be one of: on, off");
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

        // Validate action port type
        if (actionPortType != null && actionPortType.getAsString() != null) {
            String portType = getActionPortTypeValue();
            if (!isValidActionPortType(portType)) {
                errors.add("ActionPortType must be one of: default, error, message");
            }
        }

        // Validate initialize states mode
        if (initializeStates != null && initializeStates.getAsString() != null) {
            String statesMode = getInitializeStatesValue();
            if (!isValidInitializeStates(statesMode)) {
                errors.add("InitializeStates must be one of: held, reset");
            }
        }

        // Validate propagate var size
        if (propagateVarSize != null && propagateVarSize.getAsString() != null) {
            String varSizeProp = getPropagateVarSizeValue();
            if (!isValidOnOff(varSizeProp)) {
                errors.add("PropagateVarSize must be one of: on, off");
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
     * Validates if the given string is a valid action port type.
     *
     * @param type Action port type to validate
     * @return true if valid
     */
    private boolean isValidActionPortType(String type) {
        return type != null && (type.equals("default") || type.equals("error") || type.equals("message"));
    }

    /**
     * Validates if the given string is a valid initialize states mode.
     *
     * @param mode Initialize states mode to validate
     * @return true if valid
     */
    private boolean isValidInitializeStates(String mode) {
        return mode != null && (mode.equals("held") || mode.equals("reset"));
    }

    /**
     * Validates if the given string is a valid on/off value.
     *
     * @param value Value to validate
     * @return true if valid
     */
    private boolean isValidOnOff(String value) {
        return value != null && (value.equals("on") || value.equals("off"));
    }

    // === Parameter Access Methods ===

    /**
     * Gets the action port type value.
     *
     * @return Action port type ("default", "error", or "message")
     */
    public String getActionPortTypeValue() {
        if (actionPortType != null && actionPortType.getAsString() != null) {
            return actionPortType.getAsString();
        }
        return "default";
    }

    /**
     * Gets the initialize states mode value.
     *
     * @return Initialize states mode ("held" or "reset")
     */
    public String getInitializeStatesValue() {
        if (initializeStates != null && initializeStates.getAsString() != null) {
            return initializeStates.getAsString();
        }
        return "held";
    }

    /**
     * Gets the propagate var size flag value.
     *
     * @return Propagate var size flag ("on" or "off")
     */
    public String getPropagateVarSizeValue() {
        if (propagateVarSize != null && propagateVarSize.getAsString() != null) {
            return propagateVarSize.getAsString();
        }
        return "off";
    }

    // === Helper Methods ===

    /**
     * Checks if this is a default action subsystem.
     *
     * @return true if action port type is "default"
     */
    public boolean isDefault() {
        return "default".equals(getActionPortTypeValue());
    }

    /**
     * Checks if this is an error action subsystem.
     *
     * @return true if action port type is "error"
     */
    public boolean isError() {
        return "error".equals(getActionPortTypeValue());
    }

    /**
     * Checks if this is a message action subsystem.
     *
     * @return true if action port type is "message"
     */
    public boolean isMessage() {
        return "message".equals(getActionPortTypeValue());
    }

    /**
     * Checks if states are held between executions.
     *
     * @return true if initialize states mode is "held"
     */
    public boolean isStatesHeld() {
        return "held".equals(getInitializeStatesValue());
    }

    /**
     * Checks if states are reset when action becomes inactive.
     *
     * @return true if initialize states mode is "reset"
     */
    public boolean isStatesReset() {
        return "reset".equals(getInitializeStatesValue());
    }

    /**
     * Checks if variable size propagation is enabled.
     *
     * @return true if propagate var size is "on"
     */
    public boolean isPropagateVarSizeEnabled() {
        return "on".equals(getPropagateVarSizeValue());
    }

    // === Setter Methods ===

    /**
     * Sets the action port type.
     *
     * @param portType Action port type ("default", "error", or "message")
     */
    public void setActionPortTypeValue(String portType) {
        if (isValidActionPortType(portType)) {
            this.actionPortType = TypedParameter.of(portType);
        }
    }

    /**
     * Sets the initialize states mode.
     *
     * @param mode Initialize states mode ("held" or "reset")
     */
    public void setInitializeStatesValue(String mode) {
        if (isValidInitializeStates(mode)) {
            this.initializeStates = TypedParameter.of(mode);
        }
    }

    /**
     * Sets the propagate var size flag.
     *
     * @param enabled Propagate var size flag ("on" or "off")
     */
    public void setPropagateVarSizeValue(String enabled) {
        if (isValidOnOff(enabled)) {
            this.propagateVarSize = TypedParameter.of(enabled);
        }
    }

    // === Factory Methods ===

    @Override
    public SwitchCaseActionSubsystemDto copy() {
        SwitchCaseActionSubsystemDto copy = new SwitchCaseActionSubsystemDto();

        // Copy base fields from SubsystemDto
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());

        // Copy SubsystemDto fields
        copy.setSubsystemDescription(getSubsystemDescription());
        copy.setShowPortLabels(getShowPortLabels());
        copy.setReadOnly(getReadOnly());
        copy.setMaskType(getMaskType());
        copy.setNumInputPorts(getNumInputPorts());
        copy.setNumOutputPorts(getNumOutputPorts());

        // Copy DTO-specific fields
        copy.actionPortType = actionPortType != null ? actionPortType.copy() : null;
        copy.initializeStates = initializeStates != null ? initializeStates.copy() : null;
        copy.propagateVarSize = propagateVarSize != null ? propagateVarSize.copy() : null;

        return copy;
    }

    /**
     * Creates a TypedParameterMap from this DTO's parameters.
     *
     * @return TypedParameterMap containing all block parameters
     */
    @Override
    public TypedParameterMap toParameterMap() {
        TypedParameterMap map = super.toParameterMap();
        map.put("ActionPortType", actionPortType);
        map.put("InitializeStates", initializeStates);
        map.put("PropagateVarSize", propagateVarSize);
        return map;
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "ActionPortType", "Type of action port: default (standard execution), error (error handling), message (message data)",
            "InitializeStates", "State handling mode: held (preserve between executions), reset (clear when inactive)",
            "PropagateVarSize", "Variable size propagation: on (allow), off (disable)",
            "SampleTime", "Sample time for discrete operation (-1 for inherited)"
        );
    }

    @Override
    public String toString() {
        return String.format("SwitchCaseActionSubsystemDto{blockName='%s', actionPortType='%s', initializeStates='%s', sampleTime=%.3f}",
                           getBlockName(), getActionPortTypeValue(), getInitializeStatesValue(), getSampleTimeValue());
    }
}
