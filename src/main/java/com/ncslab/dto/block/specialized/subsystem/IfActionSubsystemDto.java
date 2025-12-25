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
 * DTO for IfActionSubsystem block - Action subsystem for If block conditional execution.
 *
 * <p>The IfActionSubsystem is a specialized subsystem that executes only when its
 * associated action port receives a function-call signal from an If block. It enables
 * conditional execution of subsystem logic based on If block branch conditions.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Contains an ActionPort block that receives signals from If block outputs</li>
 *   <li>Executes only when its condition branch is active</li>
 *   <li>Supports state initialization options (held/reset)</li>
 *   <li>Inherits all subsystem functionality (hierarchy, Inport/Outport blocks)</li>
 * </ul>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>ActionPortType</b>: Type of action port ("default", "error", "message")</li>
 *   <li><b>InitializeStates</b>: State handling mode ("held", "reset")</li>
 *   <li><b>PropagateVarSize</b>: Variable size propagation ("on", "off")</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 * </ul>
 *
 * <p><b>Integration with If Block:</b></p>
 * <pre>
 * If Block → action output port → IfActionSubsystem ActionPort → Subsystem execution
 * </pre>
 *
 * <p><b>Internal Structure:</b></p>
 * <ul>
 *   <li>Automatically creates internal ActionPort block on initialization</li>
 *   <li>ActionPort configured from subsystem parameters</li>
 *   <li>Contains Inport/Outport blocks for data interface</li>
 *   <li>Can contain arbitrary blocks and subsystems</li>
 * </ul>
 *
 * <p><b>State Handling:</b></p>
 * <ul>
 *   <li><b>held</b>: State values preserved between action activations</li>
 *   <li><b>reset</b>: State values reset when action becomes inactive</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 action input port (function-call from If block)</li>
 *   <li>N data input ports (from Inport blocks)</li>
 *   <li>M data output ports (from Outport blocks)</li>
 * </ul>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Must contain an ActionPort block</li>
 *   <li>ActionPort must be connected to If block output</li>
 *   <li>Internal blocks follow standard subsystem validation rules</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-22
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("IfActionSubsystem")
public class IfActionSubsystemDto extends SubsystemDto {

    /**
     * Type of action port ("default", "error", or "message").
     * Determines the behavior and purpose of the action port.
     */
    private TypedParameter actionPortType = TypedParameter.of("default");

    /**
     * State handling mode when action becomes inactive ("held" or "reset").
     * Controls whether subsystem states are held or reset between executions.
     */
    private TypedParameter initializeStates = TypedParameter.of("held");

    /**
     * Variable size propagation flag ("on" or "off").
     * Controls whether variable-size signals can propagate through the subsystem.
     */
    private TypedParameter propagateVarSize = TypedParameter.of("off");

    /**
     * Constructs IfActionSubsystemDto with basic parameters.
     */
    public IfActionSubsystemDto(String blockName, String blockPath) {
        super(blockName, blockPath);
        this.actionPortType = TypedParameter.of("default");
        this.initializeStates = TypedParameter.of("held");
        this.propagateVarSize = TypedParameter.of("off");
        this.sampleTime = TypedParameter.of(-1.0);
    }

    /**
     * Constructs IfActionSubsystemDto with individual parameters.
     *
     * @param blockName          Name of the IfActionSubsystem
     * @param blockPath          Path of the IfActionSubsystem in the model hierarchy
     * @param actionPortType     Type of action port
     * @param initializeStates   State handling mode
     * @param propagateVarSize   Variable size propagation flag
     * @param sampleTime         Sample time parameter
     */
    public IfActionSubsystemDto(String blockName, String blockPath,
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
     * Constructs IfActionSubsystemDto with typed parameter map.
     *
     * @param blockName  Name of the IfActionSubsystem
     * @param blockPath  Path of the IfActionSubsystem in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public IfActionSubsystemDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath, parameters);
        this.actionPortType = parameters.getTypedParameter("ActionPortType", String.class, "default");
        this.initializeStates = parameters.getTypedParameter("InitializeStates", String.class, "held");
        this.propagateVarSize = parameters.getTypedParameter("PropagateVarSize", String.class, "off");
    }

    /**
     * Creates IfActionSubsystemDto with specified block metadata and default parameters.
     *
     * @param blockName  Block instance name
     * @param blockPath  Hierarchical path in model
     * @param position   Block position in diagram
     * @param dimension  Block visual dimensions
     */
    public IfActionSubsystemDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
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

        String actionType = getActionPortTypeValue();
        if (!actionType.equals("default") && !actionType.equals("error") && !actionType.equals("message")) {
            addValidationError("ActionPortType must be 'default', 'error', or 'message'");
            return false;
        }

        // Validate initialize states
        if (initializeStates == null || initializeStates.getAsString() == null) {
            addValidationError("InitializeStates cannot be null");
            return false;
        }

        String initStates = getInitializeStatesValue();
        if (!initStates.equals("held") && !initStates.equals("reset")) {
            addValidationError("InitializeStates must be 'held' or 'reset'");
            return false;
        }

        // Validate propagate var size
        if (propagateVarSize != null && propagateVarSize.getAsString() != null) {
            String propagate = getPropagateVarSizeValue();
            if (!propagate.equals("on") && !propagate.equals("off")) {
                addValidationError("PropagateVarSize must be 'on' or 'off'");
                return false;
            }
        }

        return true;
    }

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();

        // Validate action port type
        if (actionPortType != null && actionPortType.getAsString() != null) {
            String actionType = getActionPortTypeValue();
            if (!actionType.equals("default") && !actionType.equals("error") && !actionType.equals("message")) {
                errors.add("ActionPortType must be 'default', 'error', or 'message'");
            }
        }

        // Validate initialize states
        if (initializeStates != null && initializeStates.getAsString() != null) {
            String initStates = getInitializeStatesValue();
            if (!initStates.equals("held") && !initStates.equals("reset")) {
                errors.add("InitializeStates must be 'held' or 'reset'");
            }
        }

        // Validate propagate var size
        if (propagateVarSize != null && propagateVarSize.getAsString() != null) {
            String propagate = getPropagateVarSizeValue();
            if (!propagate.equals("on") && !propagate.equals("off")) {
                errors.add("PropagateVarSize must be 'on' or 'off'");
            }
        }

        return errors;
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
     * Checks if this is a default action subsystem.
     *
     * @return true if action port type is "default"
     */
    public boolean isDefaultAction() {
        return "default".equals(getActionPortTypeValue());
    }

    /**
     * Checks if this is an error action subsystem.
     *
     * @return true if action port type is "error"
     */
    public boolean isErrorAction() {
        return "error".equals(getActionPortTypeValue());
    }

    /**
     * Checks if this is a message action subsystem.
     *
     * @return true if action port type is "message"
     */
    public boolean isMessageAction() {
        return "message".equals(getActionPortTypeValue());
    }

    /**
     * Checks if variable size propagation is enabled.
     *
     * @return true if PropagateVarSize is "on"
     */
    public boolean isPropagateVarSizeEnabled() {
        return "on".equals(getPropagateVarSizeValue());
    }

    // === Factory Methods ===

    @Override
    public IfActionSubsystemDto copy() {
        IfActionSubsystemDto copy = new IfActionSubsystemDto();

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

        // Copy IfActionSubsystemDto-specific fields
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
        TypedParameterMap paramMap = super.toParameterMap();
        paramMap.put("ActionPortType", actionPortType);
        paramMap.put("InitializeStates", initializeStates);
        paramMap.put("PropagateVarSize", propagateVarSize);
        return paramMap;
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        Map<String, String> descriptions = SubsystemDto.getParameterDescriptions();
        descriptions.put("ActionPortType", "Type of action port (default/error/message)");
        descriptions.put("InitializeStates", "State handling mode (held/reset)");
        descriptions.put("PropagateVarSize", "Variable size propagation (on/off)");
        return descriptions;
    }

    @Override
    public String toString() {
        return String.format("IfActionSubsystemDto{blockName='%s', actionType='%s', initStates='%s', sampleTime=%.3f}",
                           getBlockName(), getActionPortTypeValue(), getInitializeStatesValue(), getSampleTimeValue());
    }
}
