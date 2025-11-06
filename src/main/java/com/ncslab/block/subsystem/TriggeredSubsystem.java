package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.block.specialized.subsystem.TriggeredSubsystemDto;
import com.ncslab.dto.block.specialized.subsystem.TriggerDto;
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
 * Triggered Subsystem block - Event-driven subsystem with automatic trigger port.
 *
 * <p>The Triggered Subsystem is a pre-configured subsystem that contains an integrated
 * Trigger block and executes only when a trigger event occurs. This provides event-driven
 * execution control for complex subsystem functionality.</p>
 *
 * <p><b>SIMULINK Parameters:</b></p>
 * <ul>
 *   <li><b>TriggerType</b>: Type of edge detection ("rising", "falling", "either", "function-call")</li>
 *   <li><b>ShowOutputPort</b>: Create optional output port from trigger block ("on", "off")</li>
 *   <li><b>ZeroCross</b>: Enable zero-crossing detection for precise edges ("on", "off")</li>
 *   <li><b>InitialCondition</b>: Initial output value before first trigger (default: 0.0)</li>
 * </ul>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li><b>Automatic Trigger Block</b>: Internal Trigger block automatically created and configured</li>
 *   <li><b>Event-Driven Execution</b>: Subsystem executes only when trigger event occurs</li>
 *   <li><b>Edge Detection</b>: Rising, falling, either edge, or function-call triggering</li>
 *   <li><b>Zero-Crossing</b>: Optional precise edge detection using zero-crossing algorithm</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 trigger input port (on top edge of subsystem boundary)</li>
 *   <li>N data input ports (from Inport blocks)</li>
 *   <li>M data output ports (from Outport blocks)</li>
 *   <li>Optional trigger output port (if ShowOutputPort="on")</li>
 * </ul>
 *
 * <p><b>Internal Structure:</b></p>
 * <ul>
 *   <li>Inherits all subsystem functionality from Subsystem base class</li>
 *   <li>Automatically creates internal Trigger block on initialization</li>
 *   <li>Trigger block parameters configured from subsystem parameters</li>
 *   <li>Trigger port exposed on subsystem boundary</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
public class TriggeredSubsystem extends Subsystem {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter triggerType;
    private final Parameter showOutputPort;
    private final Parameter zeroCross;
    private final Parameter initialCondition;

    // === Internal Trigger Block ===
    @Getter
    private Trigger triggerBlock;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("TriggerType", "rising");
        PARAMETER_DEFAULTS.put("ShowOutputPort", "off");
        PARAMETER_DEFAULTS.put("ZeroCross", "on");
        PARAMETER_DEFAULTS.put("InitialCondition", "0.0");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names - Triggered subsystem has one trigger input port
        inputNames.add("trigger");
        // Data ports added dynamically from In/Out blocks
    }

    // === Private Constructor with Typed Parameters ===
    private TriggeredSubsystem(Parameter triggerType, Parameter showOutputPort, Parameter zeroCross,
                              Parameter initialCondition,
                              String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.triggerType = Objects.requireNonNull(triggerType, "TriggerType parameter cannot be null");
        this.showOutputPort = Objects.requireNonNull(showOutputPort, "ShowOutputPort parameter cannot be null");
        this.zeroCross = Objects.requireNonNull(zeroCross, "ZeroCross parameter cannot be null");
        this.initialCondition = Objects.requireNonNull(initialCondition, "InitialCondition parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.triggerType);
        parameterList.add(this.showOutputPort);
        parameterList.add(this.zeroCross);
        parameterList.add(this.initialCondition);

        // Initialize trigger port
        initializeTriggerPort();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public TriggeredSubsystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create SIMULINK parameters with defaults
        this.triggerType = getParameterByName("TriggerType");
        this.showOutputPort = getParameterByName("ShowOutputPort");
        this.zeroCross = getParameterByName("ZeroCross");
        this.initialCondition = getParameterByName("InitialCondition");

        // Initialize trigger port
        initializeTriggerPort();
    }

    /**
     * DTO-NATIVE Constructor - Creates TriggeredSubsystem block directly from TriggeredSubsystemDto DTO
     */
    public TriggeredSubsystem(TriggeredSubsystemDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.triggerType = getParameterByName("TriggerType");
        this.showOutputPort = getParameterByName("ShowOutputPort");
        this.zeroCross = getParameterByName("ZeroCross");
        this.initialCondition = getParameterByName("InitialCondition");

        // Initialize trigger port
        initializeTriggerPort();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static TriggeredSubsystem fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter triggerType = createTriggerTypeFromJSON(paramValues);
            Parameter showOutputPort = createShowOutputPortFromJSON(paramValues);
            Parameter zeroCross = createZeroCrossFromJSON(paramValues);
            Parameter initialCondition = createInitialConditionFromJSON(paramValues);

            TriggeredSubsystem block = new TriggeredSubsystem(triggerType, showOutputPort, zeroCross,
                                                             initialCondition,
                                                             blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, triggerType, showOutputPort, zeroCross, initialCondition);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create TriggeredSubsystem block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static TriggeredSubsystem create(String name, String path, NCSLabModel model) {
        return create(name, path, "rising", false, true, 0.0, model);
    }

    /**
     * Create a TriggeredSubsystem with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param triggerType Type of trigger detection ("rising", "falling", "either", "function-call")
     * @param showOutputPort Whether to create output port for trigger signal passthrough
     * @param zeroCross Enable zero-crossing detection
     * @param initialCondition Initial output value before first trigger
     * @param model Parent model
     * @return TriggeredSubsystem instance
     */
    public static TriggeredSubsystem create(String name, String path, String triggerType, boolean showOutputPort,
                                           boolean zeroCross, double initialCondition, NCSLabModel model) {
        // Build DTO using constructor with individual parameters
        TriggeredSubsystemDto dto = new TriggeredSubsystemDto(
            name,
            path,
            com.ncslab.dto.common.TypedParameter.of(triggerType),
            com.ncslab.dto.common.TypedParameter.of(showOutputPort ? "on" : "off"),
            com.ncslab.dto.common.TypedParameter.of(zeroCross ? "on" : "off"),
            com.ncslab.dto.common.TypedParameter.of(initialCondition),
            com.ncslab.dto.common.TypedParameter.of(-1.0)
        );

        // Set blockUUID
        dto.setBlockUUID("null");

        // Validate DTO (automatic validation)
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid TriggeredSubsystem parameters: " + dto.getValidationErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new TriggeredSubsystem(dto, model);
    }

    // === Trigger Block Management ===

    /**
     * Creates and configures the internal Trigger block.
     * This method is called automatically during subsystem initialization.
     */
    public void createTriggerBlock() {
        if (triggerBlock != null) {
            return; // Already created
        }

        try {
            // Create Trigger block DTO with parameters matching subsystem configuration
            TriggerDto triggerDto = new TriggerDto(
                "Trigger",
                getFullPath(),
                com.ncslab.dto.common.TypedParameter.of(getTriggerTypeValue()),
                com.ncslab.dto.common.TypedParameter.of(getShowOutputPortValue()),
                com.ncslab.dto.common.TypedParameter.of(getZeroCrossValue()),
                com.ncslab.dto.common.TypedParameter.of(-1.0),
                com.ncslab.dto.common.TypedParameter.of("auto")
            );

            triggerDto.setBlockUUID("trigger_" + getBlockUUID());

            // Create Trigger block instance
            triggerBlock = new Trigger(triggerDto, getModel());
            triggerBlock.setSubsystem(this);

            // Add to inner system
            innerSystem.addBlock(triggerBlock);

            System.out.println("Created internal Trigger block for TriggeredSubsystem: " + getBlockName());

        } catch (Exception e) {
            System.err.println("Error creating Trigger block for TriggeredSubsystem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if the subsystem has been triggered in the current time step.
     *
     * @return true if trigger event detected
     */
    public boolean isTriggered() {
        if (triggerBlock == null) {
            return false;
        }
        return triggerBlock.isTriggered();
    }

    // === Code Generation Methods ===

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        // Generate trigger block code first
        if (triggerBlock != null) {
            triggerBlock.generateOutputCodeC(code);
        }

        // Populate template context with subsystem-specific data
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add triggered subsystem-specific context
        context.put("triggerType", getTriggerTypeValue());
        context.put("hasOutputPort", hasOutputPort());
        context.put("zeroCrossingEnabled", isZeroCrossingEnabled());
        context.put("initialCondition", getInitialConditionValue());
        context.put("triggerBlock", triggerBlock);
        context.put("containedBlocks", innerSystem.getBlocks());
        context.put("containedLines", innerSystem.getLines());
        context.put("inBlockList", inBlockList);
        context.put("outBlockList", outBlockList);
        context.put("boundaryLines", getBoundaryLines());
        context.put("pureInternalLines", getPureInternalLines());

        // Generate triggered subsystem wrapper code using template
        String outputCode = TemplateManager.renderTemplate("c/subsystem/TriggeredSubsystem/output.vm", context);
        code.addOutputCode(outputCode);

        // Generate code for all contained blocks (except Trigger and In/Out which are handled by boundary)
        for (Block block : innerSystem.getBlocks()) {
            try {
                if (!(block instanceof Trigger || block instanceof In || block instanceof Out)) {
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
        // Initialize trigger block first
        if (triggerBlock != null) {
            triggerBlock.calculateInit();
        }

        // Initialize all blocks in subsystem
        super.calculateInit();
    }

    @Override
    public void calculateOutput(double t) {
        // Update trigger block first
        if (triggerBlock != null) {
            triggerBlock.calculateOutput(t);
        }

        // Execute subsystem only if triggered
        if (isTriggered()) {
            super.calculateOutput(t);
        }
    }

    @Override
    public void updateDimension() throws MatDimException {
        // Update trigger block dimensions
        if (triggerBlock != null) {
            triggerBlock.updateDimension();
        }

        // Update subsystem dimensions
        super.updateDimension();
    }

    @Override
    public void checkDimension() throws MatDimException {
        // Check trigger block dimensions
        if (triggerBlock != null) {
            triggerBlock.checkDimension();
        }

        // Check subsystem dimensions
        super.checkDimension();
    }

    // === Port Initialization ===
    private void initializeTriggerPort() {
        // Add trigger input port (port 1 is reserved for trigger signal)
        inputPortList.add(new InputPort(this, 1));
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createTriggerTypeFromJSON(JSONObject paramValues) {
        String triggerTypeValue = paramValues.optString("TriggerType", "rising");
        return new Parameter(null, 1, "TriggerType", triggerTypeValue);
    }

    private static Parameter createShowOutputPortFromJSON(JSONObject paramValues) {
        String showOutputValue = paramValues.optString("ShowOutputPort", "off");
        return new Parameter(null, 2, "ShowOutputPort", showOutputValue);
    }

    private static Parameter createZeroCrossFromJSON(JSONObject paramValues) {
        String zeroCrossValue = paramValues.optString("ZeroCross", "on");
        return new Parameter(null, 3, "ZeroCross", zeroCrossValue);
    }

    private static Parameter createInitialConditionFromJSON(JSONObject paramValues) {
        String initialConditionValue = paramValues.optString("InitialCondition", "0.0");
        return new Parameter(null, 4, "InitialCondition", initialConditionValue);
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

    private static void setParameterBlockReference(TriggeredSubsystem block, Parameter... parameters) {
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
        identity.put("blockType", "TriggeredSubsystem");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Accessor Methods ===

    /**
     * Gets the trigger type parameter.
     *
     * @return Trigger type parameter
     */
    public Parameter getTriggerTypeParameter() {
        return triggerType;
    }

    /**
     * Gets the trigger type value.
     *
     * @return Trigger type string ("rising", "falling", "either", "function-call")
     */
    public String getTriggerTypeValue() {
        return triggerType.getInitString();
    }

    /**
     * Gets the show output port flag value.
     *
     * @return Show output port flag ("on" or "off")
     */
    public String getShowOutputPortValue() {
        return showOutputPort.getInitString();
    }

    /**
     * Gets the zero-crossing detection flag value.
     *
     * @return Zero-crossing detection flag ("on" or "off")
     */
    public String getZeroCrossValue() {
        return zeroCross.getInitString();
    }

    /**
     * Gets the initial condition value.
     *
     * @return Initial output value before first trigger
     */
    public double getInitialConditionValue() {
        try {
            return initialCondition.getData().getInitValue();
        } catch (Exception e) {
            return 0.0; // Default initial condition
        }
    }

    /**
     * Checks if output port is enabled.
     *
     * @return true if ShowOutputPort is "on"
     */
    public boolean hasOutputPort() {
        return "on".equals(showOutputPort.getInitString());
    }

    /**
     * Checks if zero-crossing detection is enabled.
     *
     * @return true if ZeroCross is "on"
     */
    public boolean isZeroCrossingEnabled() {
        return "on".equals(zeroCross.getInitString());
    }
}
