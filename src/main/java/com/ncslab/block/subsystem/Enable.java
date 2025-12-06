package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import com.ncslab.dto.block.specialized.subsystem.EnableDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.dto.core.BlockDto;

/**
 * Enable block for conditional subsystem execution with SIMULINK-compatible parameters.
 *
 * <p>This block controls the execution of its containing subsystem based on the value
 * of an input enable signal. When the enable signal is greater than zero, the subsystem
 * executes normally. When the enable signal is zero or negative, subsystem execution
 * is controlled by the StatesWhenEnabling parameter.</p>
 *
 * <p><b>SIMULINK Parameters:</b></p>
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
 *   <li>1 input port: Enable signal (scalar double)</li>
 *   <li>0 or 1 output port: Optional enable signal passthrough (if ShowOutputPort="on")</li>
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
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
public class Enable extends Block {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter statesWhenEnabling;

    @Getter
    private final Parameter showOutputPort;

    @Getter
    private final Parameter zeroCross;

    @Getter
    private final Parameter sampleTimeParam;

    // === Subsystem Reference ===
    /**
     * Reference to the parent subsystem that this Enable block controls.
     * Set during subsystem initialization.
     */
    @Setter
    @Getter
    private EnabledSubsystem subsystem;

    // === Enable State Management ===
    /**
     * Current enable state: true if subsystem is enabled, false otherwise.
     */
    @Getter
    private boolean enabled = false;

    /**
     * Previous enable signal value for detecting state transitions.
     */
    private double previousEnableSignal = 0.0;

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
        // Port names
        inputNames.add("enable");
        // Output port added conditionally based on ShowOutputPort parameter
    }

    // === Private Constructor with Typed Parameters ===
    private Enable(Parameter statesWhenEnabling, Parameter showOutputPort, Parameter zeroCross,
                   Parameter sampleTimeParam, String blockName, String blockPath,
                   String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

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

        // Initialize ports based on configuration
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    /**
     * Legacy constructor for backward compatibility with JSONObject-based initialization.
     *
     * @deprecated Use DTO-based constructor {@link #Enable(EnableDto, NCSLabModel)} instead.
     * @param blockJSON JSON object containing block configuration
     * @param model Parent model
     */
    @Deprecated
    public Enable(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create SIMULINK parameters with defaults
        this.statesWhenEnabling = getParameterByName("StatesWhenEnabling");
        this.showOutputPort = getParameterByName("ShowOutputPort");
        this.zeroCross = getParameterByName("ZeroCross");
        this.sampleTimeParam = getParameterByName("SampleTime");

        initializePorts();
    }

    /**
     * DTO-NATIVE Constructor - Creates Enable block directly from EnableDto.
     * This is the preferred constructor following the DTO-first approach.
     *
     * @param blockDto Enable block DTO with validated parameters
     * @param model Parent model
     */
    public Enable(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.statesWhenEnabling = getParameterByName("StatesWhenEnabling");
        this.showOutputPort = getParameterByName("ShowOutputPort");
        this.zeroCross = getParameterByName("ZeroCross");
        this.sampleTimeParam = getParameterByName("SampleTime");

        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() +
            " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    /**
     * Creates an Enable block from JSON configuration.
     *
     * @param blockJSON JSON object containing block parameters
     * @param model Parent model
     * @return Enable block instance
     * @throws BlockCreationException if block creation fails
     */
    public static Enable fromJSON(JSONObject blockJSON, NCSLabModel model) {
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

            Enable block = new Enable(statesWhenEnabling, showOutputPort, zeroCross,
                                     sampleTimeParam, blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, statesWhenEnabling, showOutputPort,
                                      zeroCross, sampleTimeParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Enable block from JSON: " +
                e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Creates an Enable block with default parameters.
     *
     * @param name Block name
     * @param path Block path
     * @param model Parent model
     * @return Enable block instance
     */
    public static Enable create(String name, String path, NCSLabModel model) {
        return create(name, path, "held", "off", "off", -1.0, model);
    }

    /**
     * Creates an Enable block with full parameters using DTO-based approach.
     *
     * @param name Block name
     * @param path Block path
     * @param statesWhenEnabling State handling mode ("held" or "reset")
     * @param showOutputPort Output port visibility ("on" or "off")
     * @param zeroCross Zero-crossing detection ("on" or "off")
     * @param sampleTime Sample time (-1 for inherited, 0 for continuous, >0 for discrete)
     * @param model Parent model
     * @return Enable block instance
     */
    public static Enable create(String name, String path, String statesWhenEnabling,
                               String showOutputPort, String zeroCross, double sampleTime,
                               NCSLabModel model) {
        // Build DTO using constructor with individual parameters
        EnableDto dto = new EnableDto(
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
            throw new IllegalArgumentException("Invalid Enable parameters: " +
                dto.getValidationErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Enable(dto, model);
    }

    /**
     * Factory method to create Enable block from EnableDto.
     *
     * @param dto   EnableDto containing block configuration
     * @param model NCSLabModel containing the block diagram
     * @return Created Enable block
     */
    public static Enable createFromDto(EnableDto dto, NCSLabModel model) {
        if (dto == null) {
            throw new IllegalArgumentException("EnableDto cannot be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("NCSLabModel cannot be null");
        }

        // Validate DTO before creating block
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid EnableDto: " + dto.getValidationErrors());
        }

        return new Enable(dto, model);
    }

    // === Port Initialization ===
    /**
     * Initializes input and output ports based on block configuration.
     * Creates enable input port and optional output port if ShowOutputPort="on".
     */
    private void initializePorts() {
        // Enable input port (always present)
        inputPortList.add(new InputPort(this, 1));

        // Optional output port for enable signal passthrough
        if (hasOutputPort()) {
            outputPortList.add(new OutputPort(this, 1, true));
            if (!outputNames.contains("enable_out")) {
                outputNames.add("enable_out");
            }
        }
    }

    // === Code Generation Methods ===
    /**
     * Generates C code for the Enable block using Velocity templates.
     *
     * @param code Code structure for C code generation
     */
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add Enable-specific context
        context.put("statesWhenEnabling", getStatesWhenEnablingValue());
        context.put("showOutputPort", hasOutputPort());
        context.put("zeroCross", isZeroCrossEnabled());
        context.put("subsystem", subsystem);
        context.put("isHeldMode", isHeldMode());
        context.put("isResetMode", isResetMode());

        // Input enable signal
        InputPort enableInput = inputPortList.get(0);
        if (enableInput != null && enableInput.getLinkedLine() != null &&
            enableInput.getLinkedLine().getLinkedOutputPort() != null) {
            context.put("enableSignal", enableInput.getLinkedLine()
                .getLinkedOutputPort().getOutputSignalC().getName());
        } else {
            context.put("enableSignal", "0.0"); // Default: disabled
        }

        // Optional output signal
        if (hasOutputPort() && outputPortList.size() > 0) {
            context.put("outputSignal", outputPortList.get(0).getOutputSignalC().getName());
        } else {
            context.put("outputSignal", null);
        }

        String outputCode = TemplateManager.renderTemplate("c/subsystem/Enable/output.vm", context);
        code.addOutputCode(outputCode);
    }

    // === Dimension Handling ===
    /**
     * Updates port dimensions. Enable signal is always scalar.
     */
    @Override
    public void updateDimension() throws MatDimException {
        // Enable signal is always scalar (1x1)
        if (hasOutputPort() && outputPortList.size() > 0) {
            OutputPort out = outputPortList.get(0);
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
        }
    }

    /**
     * Validates dimensions. Enable signal must be scalar.
     */
    @Override
    public void checkDimension() throws MatDimException {
        InputPort enableInput = inputPortList.get(0);
        if (enableInput.getLinkedLine() != null) {
            int height = enableInput.getLinkedLine().getLinkedOutputPort()
                .getOutputSignalC().getHeight();
            int width = enableInput.getLinkedLine().getLinkedOutputPort()
                .getOutputSignalC().getWidth();

            if (height != 1 || width != 1) {
                throw new MatDimException("Enable signal must be scalar (1x1), got " +
                    height + "x" + width);
            }
        }
    }

    // === Execution Methods ===
    /**
     * Calculates output and updates enable state based on enable signal.
     *
     * @param t Current simulation time
     */
    @Override
    public void calculateOutput(double t) {
        // Get enable signal value
        InputPort enableInput = inputPortList.get(0);
        double enableSignal = 0.0;

        if (enableInput.getLinkedLine() != null) {
            enableSignal = enableInput.getData().getInitValue();
        }

        // Detect enable state transitions
        boolean wasEnabled = enabled;
        enabled = enableSignal > 0.0;

        // Handle state transitions
        if (!wasEnabled && enabled) {
            // Transition from disabled to enabled
            if (isResetMode() && subsystem != null) {
                // Reset subsystem states to initial conditions
                subsystem.calculateInit();
            }
        } else if (wasEnabled && !enabled) {
            // Transition from enabled to disabled
            if (isHeldMode()) {
                // States are held (no action needed, they remain frozen)
            }
        }

        // Update previous signal for next iteration
        previousEnableSignal = enableSignal;

        // Pass enable signal to output port if present
        if (hasOutputPort() && outputPortList.size() > 0) {
            OutputPort out = outputPortList.get(0);
            out.getOutputSignalC().setValue(enableSignal);
        }

        // Execute subsystem if enabled
        if (enabled && subsystem != null) {
            subsystem.calculateOutput(t);
        }
    }

    /**
     * Initializes the enable block and sets initial enable state.
     */
    @Override
    public void calculateInit() {
        // Get initial enable signal value
        InputPort enableInput = inputPortList.get(0);
        double enableSignal = 0.0;

        if (enableInput.getLinkedLine() != null) {
            enableSignal = enableInput.getData().getInitValue();
        }

        // Set initial enable state
        enabled = enableSignal > 0.0;
        previousEnableSignal = enableSignal;

        // Pass enable signal to output port if present
        if (hasOutputPort() && outputPortList.size() > 0) {
            OutputPort out = outputPortList.get(0);
            out.getOutputSignalC().setValue(enableSignal);
        }

        // Initialize subsystem if initially enabled
        if (enabled && subsystem != null) {
            subsystem.calculateInit();
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

    private static void setParameterBlockReference(Enable block, Parameter... parameters) {
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

    private static JSONObject createBlockIdentity(String blockName, String blockPath,
                                                  String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Enable");
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
     * Checks if output port is configured.
     *
     * @return true if ShowOutputPort="on"
     */
    public boolean hasOutputPort() {
        String value = showOutputPort.getData().getInitString();
        return "on".equalsIgnoreCase(value);
    }

    /**
     * Checks if zero-crossing detection is enabled.
     *
     * @return true if ZeroCross="on"
     */
    public boolean isZeroCrossEnabled() {
        String value = zeroCross.getData().getInitString();
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
     * Checks if the enable signal is currently active (> 0).
     *
     * @return true if subsystem is currently enabled
     */
    public boolean isCurrentlyEnabled() {
        return enabled;
    }
}
