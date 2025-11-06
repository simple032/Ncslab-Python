package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import com.ncslab.dto.block.specialized.subsystem.ActionPortDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Action Port block for If/Switch Action Subsystems with SIMULINK-compatible parameters.
 *
 * <p>This block serves as the control interface for Action Subsystems, receiving
 * function-call signals from If or Switch blocks to conditionally trigger
 * subsystem execution. Action ports enable event-driven subsystem activation
 * based on logical conditions or switch cases.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Receives function-call signals from If/Switch block action outputs</li>
 *   <li>Controls execution timing of internal subsystem blocks</li>
 *   <li>Manages state initialization and reset based on action activity</li>
 *   <li>Supports error handling and message passing action types</li>
 * </ul>
 *
 * <p><b>SIMULINK Parameters:</b></p>
 * <ul>
 *   <li><b>ActionPortType</b>: Type of action port ("default", "error", "message")</li>
 *   <li><b>InitializeStates</b>: State handling mode ("held", "reset")</li>
 *   <li><b>PropagateVarSize</b>: Variable size propagation ("on", "off")</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 * </ul>
 *
 * <p><b>Action Port Types:</b></p>
 * <ul>
 *   <li><b>default</b>: Standard action port - executes when action signal is active</li>
 *   <li><b>error</b>: Error handling port - catches execution errors from other actions</li>
 *   <li><b>message</b>: Message port - receives message data from parent block</li>
 * </ul>
 *
 * <p><b>State Handling:</b></p>
 * <ul>
 *   <li><b>held</b>: State values are held between executions (preserves memory)</li>
 *   <li><b>reset</b>: State values are reset each time action becomes inactive</li>
 * </ul>
 *
 * <p><b>Integration with If/Switch Blocks:</b></p>
 * <pre>
 * If Block → action output → ActionPort → Action Subsystem
 * Switch Block → case action output → ActionPort → Action Subsystem
 * </pre>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 input port (function-call type from If/Switch action output)</li>
 *   <li>No output ports (action port is a special control port)</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-22
 */
public class ActionPort extends Block {

    // === SIMULINK-Compatible Parameters ===

    /**
     * Type of action port ("default", "error", or "message").
     * Determines the behavior and purpose of this action port.
     */
    @Getter
    private final Parameter actionPortType;

    /**
     * State handling mode when action becomes inactive ("held" or "reset").
     * Controls whether subsystem states are held or reset between executions.
     */
    private final Parameter initializeStates;

    /**
     * Variable size propagation flag ("on" or "off").
     * Controls whether variable-size signals can propagate through the subsystem.
     */
    private final Parameter propagateVarSize;

    /**
     * Sample time for discrete operation (-1 for inherited).
     */
    private final Parameter sampleTime;

    /**
     * Reference to parent Action Subsystem.
     * Set by the subsystem when this action port is added.
     */
    @Setter
    @Getter
    private Subsystem subsystem;

    /**
     * Action active state flag.
     * Tracks whether the action is currently active (receiving function-call signal).
     */
    @Getter
    private boolean actionActive = false;

    /**
     * Previous action active state.
     * Used to detect activation/deactivation transitions for state reset logic.
     */
    private boolean previousActionActive = false;

    // === Static Parameter Definitions ===

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("ActionPortType", "default");
        PARAMETER_DEFAULTS.put("InitializeStates", "held");
        PARAMETER_DEFAULTS.put("PropagateVarSize", "off");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
    }

    public static final List<String> inputNames = new ArrayList<>();
    public static final List<String> outputNames = new ArrayList<>();

    static {
        // Action port receives function-call signal from If/Switch block
        inputNames.add("action");
    }

    // === Private Constructor with Typed Parameters ===

    private ActionPort(Parameter actionPortType, Parameter initializeStates,
                      Parameter propagateVarSize, Parameter sampleTime,
                      String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.actionPortType = Objects.requireNonNull(actionPortType, "ActionPortType parameter cannot be null");
        this.initializeStates = Objects.requireNonNull(initializeStates, "InitializeStates parameter cannot be null");
        this.propagateVarSize = Objects.requireNonNull(propagateVarSize, "PropagateVarSize parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.actionPortType);
        parameterList.add(this.initializeStates);
        parameterList.add(this.propagateVarSize);
        parameterList.add(this.sampleTime);

        // Initialize ports - action port receives function-call signal, no outputs
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===

    /**
     * Legacy JSON constructor for backward compatibility.
     *
     * @param blockJSON JSON object containing block configuration
     * @param model Parent model reference
     * @deprecated Use DTO-native constructor for new development
     */
    @Deprecated
    public ActionPort(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create SIMULINK parameters with defaults
        this.actionPortType = getParameterByName("ActionPortType");
        this.initializeStates = getParameterByName("InitializeStates");
        this.propagateVarSize = getParameterByName("PropagateVarSize");
        this.sampleTime = getParameterByName("SampleTime");

        initializePorts();
    }

    // === DTO-NATIVE Constructor ===

    /**
     * DTO-NATIVE Constructor - Creates ActionPort block directly from ActionPortDto DTO.
     *
     * @param blockDto DTO containing action port configuration data
     * @param model Parent model reference
     */
    public ActionPort(ActionPortDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.actionPortType = getParameterByName("ActionPortType");
        this.initializeStates = getParameterByName("InitializeStates");
        this.propagateVarSize = getParameterByName("PropagateVarSize");
        this.sampleTime = getParameterByName("SampleTime");

        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===

    /**
     * Creates ActionPort block from JSON object.
     *
     * @param blockJSON JSON object containing block configuration
     * @param model Parent model reference
     * @return ActionPort block instance
     * @throws BlockCreationException if block creation fails
     */
    public static ActionPort fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter actionPortType = createActionPortTypeFromJSON(paramValues);
            Parameter initializeStates = createInitializeStatesFromJSON(paramValues);
            Parameter propagateVarSize = createPropagateVarSizeFromJSON(paramValues);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues);

            ActionPort block = new ActionPort(actionPortType, initializeStates, propagateVarSize, sampleTime,
                                             blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, actionPortType, initializeStates, propagateVarSize, sampleTime);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create ActionPort block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===

    /**
     * Creates an ActionPort block with default parameters (DTO-based approach).
     *
     * @param name Block name
     * @param path Block path
     * @param model Parent model
     * @return ActionPort block instance
     */
    public static ActionPort create(String name, String path, NCSLabModel model) {
        return create(name, path, "default", "held", "off", -1.0, model);
    }

    /**
     * Creates an ActionPort block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param actionPortType Type of action port ("default", "error", "message")
     * @param initializeStates State handling mode ("held", "reset")
     * @param propagateVarSize Variable size propagation ("on", "off")
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param model Parent model
     * @return ActionPort block instance
     */
    public static ActionPort create(String name, String path, String actionPortType,
                                   String initializeStates, String propagateVarSize,
                                   double sampleTime, NCSLabModel model) {
        // Build DTO using constructor with individual parameters
        ActionPortDto dto = new ActionPortDto(
            name,
            path,
            com.ncslab.dto.common.TypedParameter.of(actionPortType),
            com.ncslab.dto.common.TypedParameter.of(initializeStates),
            com.ncslab.dto.common.TypedParameter.of(propagateVarSize),
            com.ncslab.dto.common.TypedParameter.of(sampleTime)
        );

        // Set blockUUID
        dto.setBlockUUID("null");

        // Validate DTO (automatic validation)
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid ActionPort parameters: " + dto.getValidationErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new ActionPort(dto, model);
    }

    // === Code Generation Methods ===

    /**
     * Generates C code for action port execution control.
     * Creates code that checks action activation state and controls subsystem execution.
     *
     * @param code Code structure to append generated code to
     */
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add ActionPort-specific context
        context.put("actionPortType", getActionPortTypeValue());
        context.put("initializeStates", getInitializeStatesValue());
        context.put("propagateVarSize", getPropagateVarSizeValue());
        context.put("subsystem", subsystem);
        context.put("isStatesReset", isStatesReset());
        context.put("isStatesHeld", isStatesHeld());

        // Safely handle signal connection with null checks
        InputPort inputPort = inputPortList.get(0);
        if (inputPort != null && inputPort.getLinkedLine() != null &&
            inputPort.getLinkedLine().getLinkedOutputPort() != null) {
            context.put("actionSignal", inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
        } else {
            context.put("actionSignal", "0"); // Default - action not active
        }

        String outputCode = TemplateManager.renderTemplate("c/subsystem/ActionPort/output.vm", context);
        code.addOutputCode(outputCode);
    }

    /**
     * Updates dimension information for action port.
     * Action ports don't propagate dimensions directly since they handle function-call signals.
     *
     * @throws MatDimException if dimension update fails
     */
    public void updateDimension() throws MatDimException {
        // Action port receives function-call signal (not data signal)
        // No dimension propagation needed
    }

    /**
     * Checks dimension compatibility for action port.
     * Action ports don't have dimension constraints.
     *
     * @throws MatDimException if dimension check fails
     */
    public void checkDimension() throws MatDimException {
        // Action port validation - no dimension constraints
    }

    /**
     * Calculates output for action port at the specified time.
     * Checks if action signal is active and triggers subsystem execution accordingly.
     *
     * @param t Current simulation time
     */
    @Override
    public void calculateOutput(double t) {
        // Check if action is currently active (receiving function-call signal)
        InputPort in = inputPortList.get(0);

        if (in.getLinkedLine() != null && in.getLinkedLine().getLinkedOutputPort() != null) {
            // Action is active if input signal is non-zero (function-call active)
            double actionSignal = in.getData().getInitValue();
            actionActive = (actionSignal != 0.0);

            // Detect deactivation transition for state reset
            if (isStatesReset() && previousActionActive && !actionActive) {
                resetSubsystemStates();
            }

            previousActionActive = actionActive;
        } else {
            actionActive = false;
        }
    }

    /**
     * Initializes action port state.
     * Sets initial action active state to false.
     */
    @Override
    public void calculateInit() {
        actionActive = false;
        previousActionActive = false;
    }

    // === Action Control Methods ===

    /**
     * Resets all states in the parent subsystem.
     * Called when action becomes inactive and InitializeStates is "reset".
     */
    private void resetSubsystemStates() {
        if (subsystem != null) {
            // Reset all state variables in subsystem blocks to their initial values
            for (Block block : subsystem.getContainedBlocks()) {
                for (com.ncslab.block.io.State state : block.getStateList()) {
                    state.setData(state.getData()); // Reset to current data value
                }
            }
        }
    }

    // === Parameter Access Methods ===

    /**
     * Gets the action port type value.
     *
     * @return Action port type ("default", "error", or "message")
     */
    public String getActionPortTypeValue() {
        return actionPortType.getData().getInitString();
    }

    /**
     * Gets the initialize states mode value.
     *
     * @return Initialize states mode ("held" or "reset")
     */
    public String getInitializeStatesValue() {
        return initializeStates.getData().getInitString();
    }

    /**
     * Gets the propagate var size flag value.
     *
     * @return Propagate var size flag ("on" or "off")
     */
    public String getPropagateVarSizeValue() {
        return propagateVarSize.getData().getInitString();
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
     * Checks if this is a default action port.
     *
     * @return true if action port type is "default"
     */
    public boolean isDefault() {
        return "default".equals(getActionPortTypeValue());
    }

    /**
     * Checks if this is an error action port.
     *
     * @return true if action port type is "error"
     */
    public boolean isError() {
        return "error".equals(getActionPortTypeValue());
    }

    /**
     * Checks if this is a message action port.
     *
     * @return true if action port type is "message"
     */
    public boolean isMessage() {
        return "message".equals(getActionPortTypeValue());
    }

    // === Helper Methods for JSON Parameter Creation ===

    private static Parameter createActionPortTypeFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("ActionPortType", "default");
        return new Parameter(null, 1, "ActionPortType", value);
    }

    private static Parameter createInitializeStatesFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("InitializeStates", "held");
        return new Parameter(null, 2, "InitializeStates", value);
    }

    private static Parameter createPropagateVarSizeFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("PropagateVarSize", "off");
        return new Parameter(null, 3, "PropagateVarSize", value);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 4, "SampleTime", value);
    }

    // === Utility Methods ===

    private static String requireNonEmptyString(JSONObject json, String key) {
        if (!json.has(key)) {
            throw new IllegalArgumentException("Required field '" + key + "' is missing");
        }
        String value = json.getString(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Field '" + key + "' cannot be empty");
        }
        return value;
    }

    private static void setParameterBlockReference(ActionPort block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
            } catch (Exception e) {
                // Fallback: parameter block reference will be null, but should work for basic operations
            }
        }
    }

    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "ActionPort");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Port Initialization ===

    /**
     * Initializes action port input port.
     * Action port receives function-call signal from If/Switch block.
     */
    private void initializePorts() {
        // Action port receives function-call signal (no data propagation)
        inputPortList.add(new InputPort(this, 1));

        // Action port has no output ports - it's a control port only
    }
}
