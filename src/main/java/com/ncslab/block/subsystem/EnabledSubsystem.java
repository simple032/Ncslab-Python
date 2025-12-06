package com.ncslab.block.subsystem;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.block.specialized.subsystem.EnabledSubsystemDto;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.dto.core.BlockDto;

/**
 * Enabled Subsystem block - Pre-configured subsystem with automatic Enable port integration.
 *
 * <p>This block extends the standard Subsystem block by automatically including an Enable
 * block for conditional execution control. The subsystem executes only when the enable
 * signal is greater than zero. It combines the hierarchical modeling capabilities of
 * a subsystem with built-in enable/disable functionality.</p>
 *
 * <p><b>SIMULINK-Compatible Parameters:</b></p>
 * <ul>
 *   <li><b>StatesWhenEnabling</b>: "held" (default) - states maintain values when disabled,
 *                                   "reset" - states reset to initial conditions when re-enabled</li>
 *   <li><b>ShowOutputPort</b>: "on" - creates output port for enable signal passthrough,
 *                              "off" (default) - no output port on Enable block</li>
 *   <li><b>ZeroCross</b>: "on" - enable zero-crossing detection for accurate transitions,
 *                         "off" (default) - no zero-crossing detection</li>
 *   <li><b>SampleTime</b>: -1 (inherited, default), 0 (continuous), >0 (discrete)</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 enable input port (scalar double, on top edge of subsystem block)</li>
 *   <li>Variable data input ports (from internal Inport blocks)</li>
 *   <li>Variable data output ports (from internal Outport blocks)</li>
 *   <li>Optional enable output port (if ShowOutputPort="on")</li>
 * </ul>
 *
 * <p><b>Internal Structure:</b></p>
 * <ul>
 *   <li>Automatically creates internal Enable block with matching parameters</li>
 *   <li>Enable block is created during subsystem initialization</li>
 *   <li>Enable port exposed on subsystem boundary (top edge)</li>
 *   <li>Regular In/Out blocks for data flow (inherited from Subsystem)</li>
 *   <li>State management (held/reset) handled by internal Enable block</li>
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
 * <p><b>Execution Behavior:</b></p>
 * <ul>
 *   <li>Enable signal > 0: Subsystem executes normally</li>
 *   <li>Enable signal <= 0: Subsystem execution controlled by StatesWhenEnabling parameter</li>
 *   <li>State transitions handled automatically by internal Enable block</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-11-03
 */
public class EnabledSubsystem extends Subsystem {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter statesWhenEnabling;

    @Getter
    private final Parameter showOutputPort;

    @Getter
    private final Parameter zeroCross;

    @Getter
    private final Parameter sampleTimeParam;

    // === Internal Enable Block ===
    /**
     * Internal Enable block for conditional execution control.
     * Created automatically during subsystem initialization.
     */
    @Getter
    @Setter
    private Enable enableBlock;

    // === Enable Port Reference ===
    /**
     * Enable input port on the subsystem boundary (top edge).
     * Index 0 in inputPortList is reserved for the enable port.
     */
    @Getter
    private InputPort enablePort;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("StatesWhenEnabling", "held");
        PARAMETER_DEFAULTS.put("ShowOutputPort", "off");
        PARAMETER_DEFAULTS.put("ZeroCross", "off");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Enable port is always present at port index 0 (top edge)
        inputNames.add("enable");
        // Data ports added dynamically based on In/Out blocks
    }

    // === Private Constructor with Typed Parameters ===
    private EnabledSubsystem(Parameter statesWhenEnabling, Parameter showOutputPort,
                            Parameter zeroCross, Parameter sampleTimeParam,
                            String blockName, String blockPath, String blockUUID,
                            NCSLabModel model) {
        super(createBlockIdentityJSON(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.statesWhenEnabling = Objects.requireNonNull(statesWhenEnabling,
            "StatesWhenEnabling parameter cannot be null");
        this.showOutputPort = Objects.requireNonNull(showOutputPort,
            "ShowOutputPort parameter cannot be null");
        this.zeroCross = Objects.requireNonNull(zeroCross,
            "ZeroCross parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTimeParam,
            "SampleTime parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.statesWhenEnabling);
        parameterList.add(this.showOutputPort);
        parameterList.add(this.zeroCross);
        parameterList.add(this.sampleTimeParam);

        // Initialize enable port (top edge of subsystem)
        initializeEnablePort();

        // Internal Enable block will be created after subsystem structure is set up
    }

    // === Legacy Constructor (Deprecated) ===
    /**
     * Legacy constructor for backward compatibility with JSONObject-based initialization.
     *
     * @deprecated Use DTO-based constructor {@link #EnabledSubsystem(EnabledSubsystemDto, NCSLabModel)} instead.
     * @param blockJSON JSON object containing block configuration
     * @param model Parent model
     */
    @Deprecated
    public EnabledSubsystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create SIMULINK parameters with defaults
        this.statesWhenEnabling = getParameterByName("StatesWhenEnabling");
        this.showOutputPort = getParameterByName("ShowOutputPort");
        this.zeroCross = getParameterByName("ZeroCross");
        this.sampleTimeParam = getParameterByName("SampleTime");

        initializeEnablePort();
    }

    /**
     * DTO-NATIVE Constructor - Creates EnabledSubsystem block directly from EnabledSubsystemDto.
     * This is the preferred constructor following the DTO-first approach.
     *
     * @param blockDto EnabledSubsystem block DTO with validated parameters
     * @param model Parent model
     */
    public EnabledSubsystem(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.statesWhenEnabling = getParameterByName("StatesWhenEnabling");
        this.showOutputPort = getParameterByName("ShowOutputPort");
        this.zeroCross = getParameterByName("ZeroCross");
        this.sampleTimeParam = getParameterByName("SampleTime");

        initializeEnablePort();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() +
            " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    /**
     * Creates an EnabledSubsystem block from JSON configuration.
     *
     * @param blockJSON JSON object containing block parameters
     * @param model Parent model
     * @return EnabledSubsystem block instance
     * @throws BlockCreationException if block creation fails
     */
    public static EnabledSubsystem fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter statesWhenEnabling = createStatesWhenEnablingFromJSON(paramValues);
            Parameter showOutputPort = createShowOutputPortFromJSON(paramValues);
            Parameter zeroCross = createZeroCrossFromJSON(paramValues);
            Parameter sampleTimeParam = createSampleTimeFromJSON(paramValues);

            EnabledSubsystem block = new EnabledSubsystem(statesWhenEnabling, showOutputPort,
                                                         zeroCross, sampleTimeParam,
                                                         blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, statesWhenEnabling, showOutputPort,
                                      zeroCross, sampleTimeParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create EnabledSubsystem block from JSON: " +
                e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Creates an EnabledSubsystem block with default parameters.
     *
     * @param name Block name
     * @param path Block path
     * @param model Parent model
     * @return EnabledSubsystem block instance
     */
    public static EnabledSubsystem create(String name, String path, NCSLabModel model) {
        return create(name, path, "held", "off", "off", -1.0, model);
    }

    /**
     * Creates an EnabledSubsystem block with full parameters using DTO-based approach.
     *
     * @param name Block name
     * @param path Block path
     * @param statesWhenEnabling State handling mode ("held" or "reset")
     * @param showOutputPort Output port visibility ("on" or "off")
     * @param zeroCross Zero-crossing detection ("on" or "off")
     * @param sampleTime Sample time (-1 for inherited, 0 for continuous, >0 for discrete)
     * @param model Parent model
     * @return EnabledSubsystem block instance
     */
    public static EnabledSubsystem create(String name, String path, String statesWhenEnabling,
                                         String showOutputPort, String zeroCross, double sampleTime,
                                         NCSLabModel model) {
        // Build DTO using constructor with individual parameters
        EnabledSubsystemDto dto = new EnabledSubsystemDto(
            name,
            path,
            com.ncslab.dto.common.TypedParameter.of(statesWhenEnabling),
            com.ncslab.dto.common.TypedParameter.of(showOutputPort),
            com.ncslab.dto.common.TypedParameter.of(zeroCross),
            com.ncslab.dto.common.TypedParameter.of(sampleTime)
        );

        // Set blockUUID
        dto.setBlockUUID("null");

        // Validate DTO (automatic validation)
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid EnabledSubsystem parameters: " +
                dto.getValidationErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new EnabledSubsystem(dto, model);
    }

    /**
     * Factory method to create EnabledSubsystem from EnabledSubsystemDto.
     *
     * @param dto   EnabledSubsystemDto containing block configuration
     * @param model NCSLabModel containing the block diagram
     * @return Created EnabledSubsystem block
     */
    public static EnabledSubsystem createFromDto(EnabledSubsystemDto dto, NCSLabModel model) {
        if (dto == null) {
            throw new IllegalArgumentException("EnabledSubsystemDto cannot be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("NCSLabModel cannot be null");
        }

        // Validate DTO before creating block
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid EnabledSubsystemDto: " + dto.getValidationErrors());
        }

        return new EnabledSubsystem(dto, model);
    }

    // === Port Initialization ===
    /**
     * Initializes the enable input port on the subsystem boundary (top edge).
     * The enable port is always at port index 0.
     */
    private void initializeEnablePort() {
        // Enable input port at index 0 (top edge of subsystem)
        this.enablePort = new InputPort(this, 0);  // Port 0 reserved for enable
        // Note: Don't add to inputPortList yet - it will be added when Enable block is created
    }

    // === Enable Block Management ===
    /**
     * Creates and configures the internal Enable block.
     * This method should be called after the subsystem structure is initialized.
     *
     * @return The created Enable block
     */
    public Enable createEnableBlock() {
        if (enableBlock != null) {
            System.err.println("Warning: Enable block already exists for subsystem " + getBlockName());
            return enableBlock;
        }

        try {
            // Create Enable block with matching parameters
            String enableBlockName = "Enable_" + getBlockName();
            String enableBlockPath = getFullPath();

            enableBlock = Enable.create(
                enableBlockName,
                enableBlockPath,
                getStatesWhenEnablingValue(),
                getShowOutputPortValue(),
                getZeroCrossValue(),
                getSampleTimeValue(),
                model
            );

            // Set subsystem reference in Enable block
            enableBlock.setSubsystem(this);

            // Add Enable block to inner system
            getInnerSystem().addBlock(enableBlock);

            System.out.println("Created internal Enable block for subsystem " + getBlockName());

            return enableBlock;

        } catch (Exception e) {
            System.err.println("Failed to create Enable block for subsystem " + getBlockName() +
                ": " + e.getMessage());
            throw new BlockCreationException("Failed to create internal Enable block", e);
        }
    }

    // === Code Generation Methods ===
    /**
     * Generates C code for the EnabledSubsystem block using Velocity templates.
     *
     * @param code Code structure for C code generation
     */
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        // Call parent subsystem code generation
        super.generateOutputCodeC(code);

        // Populate template context with EnabledSubsystem-specific data
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add EnabledSubsystem-specific context
        context.put("statesWhenEnabling", getStatesWhenEnablingValue());
        context.put("showOutputPort", hasEnableOutputPort());
        context.put("zeroCross", isZeroCrossEnabled());
        context.put("enableBlock", enableBlock);
        context.put("isHeldMode", isHeldMode());
        context.put("isResetMode", isResetMode());

        // Add enable signal context
        if (enablePort != null && enablePort.getLinkedLine() != null &&
            enablePort.getLinkedLine().getLinkedOutputPort() != null) {
            context.put("enableSignal", enablePort.getLinkedLine()
                .getLinkedOutputPort().getOutputSignalC().getName());
        } else {
            context.put("enableSignal", "0.0"); // Default: disabled
        }

        // Generate EnabledSubsystem wrapper code using template
        String outputCode = TemplateManager.renderTemplate(
            "c/subsystem/EnabledSubsystem/output.vm", context);
        code.addOutputCode(outputCode);
    }

    // === Execution Methods ===
    /**
     * Calculates output and delegates to Enable block for conditional execution.
     *
     * @param t Current simulation time
     */
    @Override
    public void calculateOutput(double t) {
        // Delegate to Enable block, which handles conditional subsystem execution
        if (enableBlock != null) {
            enableBlock.calculateOutput(t);
        } else {
            // Fallback: execute subsystem unconditionally if Enable block not set up
            System.err.println("Warning: Enable block not configured for " + getBlockName() +
                ", executing unconditionally");
            super.calculateOutput(t);
        }
    }

    /**
     * Initializes the enabled subsystem and its internal Enable block.
     */
    @Override
    public void calculateInit() {
        // Initialize Enable block first
        if (enableBlock != null) {
            enableBlock.calculateInit();
        }

        // Initialize subsystem (will only execute if enabled)
        if (isEnabled()) {
            super.calculateInit();
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createStatesWhenEnablingFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("StatesWhenEnabling", "held");
        return new Parameter(null, 1, "StatesWhenEnabling", value);
    }

    private static Parameter createShowOutputPortFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("ShowOutputPort", "off");
        return new Parameter(null, 2, "ShowOutputPort", value);
    }

    private static Parameter createZeroCrossFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("ZeroCross", "off");
        return new Parameter(null, 3, "ZeroCross", value);
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

    private static void setParameterBlockReference(EnabledSubsystem block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
            } catch (Exception e) {
                // Fallback: parameter block reference will be null,
                // but should work for basic operations
            }
        }
    }

    private static JSONObject createBlockIdentityJSON(String blockName, String blockPath,
                                                     String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "EnabledSubsystem");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Accessor Methods ===
    /**
     * Gets the state handling mode value.
     *
     * @return "held" or "reset"
     */
    public String getStatesWhenEnablingValue() {
        return statesWhenEnabling.getData().getInitString();
    }

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
     * Gets the output port visibility value.
     *
     * @return "on" or "off"
     */
    public String getShowOutputPortValue() {
        return showOutputPort.getData().getInitString();
    }

    /**
     * Gets the zero-crossing detection value.
     *
     * @return "on" or "off"
     */
    public String getZeroCrossValue() {
        return zeroCross.getData().getInitString();
    }

    /**
     * Checks if enable output port is configured.
     *
     * @return true if ShowOutputPort="on"
     */
    public boolean hasEnableOutputPort() {
        String value = getShowOutputPortValue();
        return "on".equalsIgnoreCase(value);
    }

    /**
     * Checks if zero-crossing detection is enabled.
     *
     * @return true if ZeroCross="on"
     */
    public boolean isZeroCrossEnabled() {
        String value = getZeroCrossValue();
        return "on".equalsIgnoreCase(value);
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time
     */
    public double getSampleTimeValue() {
        return sampleTimeParam.getData().getInitValue();
    }

    /**
     * Checks if the subsystem is currently enabled.
     * Delegates to the internal Enable block.
     *
     * @return true if subsystem is currently enabled
     */
    public boolean isEnabled() {
        if (enableBlock != null) {
            return enableBlock.isCurrentlyEnabled();
        }
        return false; // Not enabled if Enable block not configured
    }
}
