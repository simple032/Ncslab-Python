package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.block.specialized.subsystem.IfActionSubsystemDto;
import com.ncslab.dto.block.specialized.subsystem.ActionPortDto;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * IfActionSubsystem block - Action subsystem for If block conditional execution.
 *
 * <p>The IfActionSubsystem is a specialized subsystem that executes only when its
 * associated action port receives a function-call signal from an If block. It enables
 * conditional execution of subsystem logic based on If block branch conditions.</p>
 *
 * <p><b>SIMULINK Parameters:</b></p>
 * <ul>
 *   <li><b>ActionPortType</b>: Type of action port ("default", "error", "message")</li>
 *   <li><b>InitializeStates</b>: State handling mode ("held", "reset")</li>
 *   <li><b>PropagateVarSize</b>: Variable size propagation ("on", "off")</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 * </ul>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li><b>Automatic ActionPort Block</b>: Internal ActionPort block automatically created and configured</li>
 *   <li><b>Conditional Execution</b>: Subsystem executes only when action signal is active</li>
 *   <li><b>State Management</b>: Supports held or reset state handling between activations</li>
 *   <li><b>Hierarchical Structure</b>: Full subsystem functionality with Inport/Outport blocks</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 action input port (function-call from If block)</li>
 *   <li>N data input ports (from Inport blocks)</li>
 *   <li>M data output ports (from Outport blocks)</li>
 * </ul>
 *
 * <p><b>Internal Structure:</b></p>
 * <ul>
 *   <li>Inherits all subsystem functionality from Subsystem base class</li>
 *   <li>Automatically creates internal ActionPort block on initialization</li>
 *   <li>ActionPort parameters configured from subsystem parameters</li>
 *   <li>ActionPort exposed on subsystem boundary</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-22
 */
public class IfActionSubsystem extends Subsystem {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter actionPortType;
    private final Parameter initializeStates;
    private final Parameter propagateVarSize;

    // === Internal ActionPort Block ===
    @Getter
    private ActionPort actionPort;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("ActionPortType", "default");
        PARAMETER_DEFAULTS.put("InitializeStates", "held");
        PARAMETER_DEFAULTS.put("PropagateVarSize", "off");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names - IfActionSubsystem has one action input port
        inputNames.add("action");
        // Data ports added dynamically from In/Out blocks
    }

    // === Private Constructor with Typed Parameters ===
    private IfActionSubsystem(Parameter actionPortType, Parameter initializeStates,
                              Parameter propagateVarSize, Parameter sampleTime,
                              String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.actionPortType = Objects.requireNonNull(actionPortType, "ActionPortType parameter cannot be null");
        this.initializeStates = Objects.requireNonNull(initializeStates, "InitializeStates parameter cannot be null");
        this.propagateVarSize = Objects.requireNonNull(propagateVarSize, "PropagateVarSize parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.actionPortType);
        parameterList.add(this.initializeStates);
        parameterList.add(this.propagateVarSize);
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public IfActionSubsystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create SIMULINK parameters with defaults
        this.actionPortType = getParameterByName("ActionPortType");
        this.initializeStates = getParameterByName("InitializeStates");
        this.propagateVarSize = getParameterByName("PropagateVarSize");
    }

    /**
     * DTO-NATIVE Constructor - Creates IfActionSubsystem block directly from IfActionSubsystemDto DTO.
     */
    public IfActionSubsystem(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.actionPortType = getParameterByName("ActionPortType");
        this.initializeStates = getParameterByName("InitializeStates");
        this.propagateVarSize = getParameterByName("PropagateVarSize");

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static IfActionSubsystem fromJSON(JSONObject blockJSON, NCSLabModel model) {
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

            IfActionSubsystem block = new IfActionSubsystem(actionPortType, initializeStates,
                                                           propagateVarSize, sampleTime,
                                                           blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, actionPortType, initializeStates, propagateVarSize, sampleTime);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create IfActionSubsystem block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static IfActionSubsystem create(String name, String path, NCSLabModel model) {
        return create(name, path, "default", "held", "off", -1.0, model);
    }

    /**
     * Create an IfActionSubsystem with full parameters (DTO-based approach).
     *
     * @param name Block name
     * @param path Block path
     * @param actionPortType Type of action port ("default", "error", "message")
     * @param initializeStates State handling mode ("held", "reset")
     * @param propagateVarSize Variable size propagation ("on", "off")
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param model Parent model
     * @return IfActionSubsystem instance
     */
    public static IfActionSubsystem create(String name, String path, String actionPortType,
                                          String initializeStates, String propagateVarSize,
                                          double sampleTime, NCSLabModel model) {
        // Build DTO using constructor with individual parameters
        IfActionSubsystemDto dto = new IfActionSubsystemDto(
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
            throw new IllegalArgumentException("Invalid IfActionSubsystem parameters: " + dto.getValidationErrors());
        }

        // Use DTO constructor
        return new IfActionSubsystem(dto, model);
    }

    /**
     * Factory method to create IfActionSubsystem from IfActionSubsystemDto.
     *
     * @param dto   IfActionSubsystemDto containing block configuration
     * @param model NCSLabModel containing the block diagram
     * @return Created IfActionSubsystem block
     */
    public static IfActionSubsystem createFromDto(IfActionSubsystemDto dto, NCSLabModel model) {
        if (dto == null) {
            throw new IllegalArgumentException("IfActionSubsystemDto cannot be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("NCSLabModel cannot be null");
        }

        // Validate DTO before creating block
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid IfActionSubsystemDto: " + dto.getValidationErrors());
        }

        return new IfActionSubsystem(dto, model);
    }

    // === ActionPort Block Management ===

    /**
     * Creates and configures the internal ActionPort block.
     * This method is called automatically during subsystem initialization.
     */
    public void createActionPort() {
        if (actionPort != null) {
            return; // Already created
        }

        try {
            // Create ActionPort block DTO with parameters matching subsystem configuration
            ActionPortDto actionPortDto = new ActionPortDto(
                "ActionPort",
                getFullPath(),
                com.ncslab.dto.common.TypedParameter.of(getActionPortTypeValue()),
                com.ncslab.dto.common.TypedParameter.of(getInitializeStatesValue()),
                com.ncslab.dto.common.TypedParameter.of(getPropagateVarSizeValue()),
                com.ncslab.dto.common.TypedParameter.of(-1.0)
            );

            actionPortDto.setBlockUUID("actionport_" + getBlockUUID());

            // Create ActionPort block instance
            actionPort = new ActionPort(actionPortDto, getModel());
            actionPort.setSubsystem(this);

            // Add to inner system
            innerSystem.addBlock(actionPort);

            System.out.println("Created internal ActionPort block for IfActionSubsystem: " + getBlockName());

        } catch (Exception e) {
            System.err.println("Error creating ActionPort block for IfActionSubsystem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if the subsystem's action is currently active.
     *
     * @return true if action signal is active
     */
    public boolean isActionActive() {
        if (actionPort == null) {
            return false;
        }
        return actionPort.isActionActive();
    }

    // === Code Generation Methods ===

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        // Generate action port code first
        if (actionPort != null) {
            actionPort.generateOutputCodeC(code);
        }

        // Populate template context with subsystem-specific data
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add IfActionSubsystem-specific context
        context.put("actionPortType", getActionPortTypeValue());
        context.put("initializeStates", getInitializeStatesValue());
        context.put("propagateVarSize", getPropagateVarSizeValue());
        context.put("isStatesReset", isStatesReset());
        context.put("isStatesHeld", isStatesHeld());
        context.put("actionPort", actionPort);
        context.put("containedBlocks", innerSystem.getBlocks());
        context.put("containedLines", innerSystem.getLines());
        context.put("inBlockList", inBlockList);
        context.put("outBlockList", outBlockList);
        context.put("boundaryLines", getBoundaryLines());
        context.put("pureInternalLines", getPureInternalLines());

        // Generate IfActionSubsystem wrapper code using template
        String outputCode = TemplateManager.renderTemplate("c/subsystem/IfActionSubsystem/output.vm", context);
        code.addOutputCode(outputCode);

        // Generate code for all contained blocks (except ActionPort and In/Out which are handled by boundary)
        for (Block block : innerSystem.getBlocks()) {
            try {
                if (!(block instanceof ActionPort || block instanceof In || block instanceof Out)) {
                    block.generateOutputCodeC(code);
                }
            } catch (Exception e) {
                System.err.println("Error generating code for block " + block.getBlockName() + ": " + e.getMessage());
            }
        }
    }

    // === Initialization and Execution Methods ===

    @Override
    public void calculateInit() {
        // Initialize action port first
        if (actionPort != null) {
            actionPort.calculateInit();
        }

        // Initialize all blocks in subsystem
        super.calculateInit();
    }

    @Override
    public void calculateOutput(double t) {
        // Update action port first
        if (actionPort != null) {
            actionPort.calculateOutput(t);
        }

        // Execute subsystem only if action is active
        if (isActionActive()) {
            super.calculateOutput(t);
        }
    }

    @Override
    public void updateDimension() throws MatDimException {
        // Update action port dimensions
        if (actionPort != null) {
            actionPort.updateDimension();
        }

        // Update subsystem dimensions
        super.updateDimension();
    }

    @Override
    public void checkDimension() throws MatDimException {
        // Check action port dimensions
        if (actionPort != null) {
            actionPort.checkDimension();
        }

        // Check subsystem dimensions
        super.checkDimension();
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

    private static void setParameterBlockReference(IfActionSubsystem block, Parameter... parameters) {
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
        identity.put("blockType", "IfActionSubsystem");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Accessor Methods ===

    /**
     * Gets the action port type parameter.
     *
     * @return Action port type parameter
     */
    public Parameter getActionPortTypeParameter() {
        return actionPortType;
    }

    /**
     * Gets the action port type value.
     *
     * @return Action port type string ("default", "error", or "message")
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
}
