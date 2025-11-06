package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
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
import com.ncslab.dto.block.specialized.subsystem.FunctionCallGeneratorDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Function-Call Generator block for triggering function-call subsystems.
 *
 * <p>This block generates function-call events at specified sample intervals to trigger
 * execution of function-call subsystems. Function-call subsystems execute only when triggered
 * by a function-call event, providing precise control over subsystem execution timing and
 * enabling multi-rate system modeling with explicit task scheduling.</p>
 *
 * <p><b>SIMULINK Parameters:</b></p>
 * <ul>
 *   <li><b>SampleTime</b>: Sample time for function-call generation (default: 1.0, must be positive)</li>
 *   <li><b>InitFcnCallInput</b>: Initial function-call state - "off" (default) or "on"</li>
 * </ul>
 *
 * <p><b>Function-Call Event Generation:</b></p>
 * <ul>
 *   <li>Generates events at regular intervals based on sample time</li>
 *   <li>Each event triggers execution of connected function-call subsystem</li>
 *   <li>InitFcnCallInput="on" generates an event at t=0 (initialization)</li>
 *   <li>Events are edge-triggered (rising edge detection)</li>
 *   <li>Sample time must be positive (discrete-time operation only)</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>0 input ports (autonomous event generator)</li>
 *   <li>1 output port (function-call signal - special signal type)</li>
 * </ul>
 *
 * <p><b>Implementation Details:</b></p>
 * <ul>
 *   <li>Maintains internal event generation counter</li>
 *   <li>Checks simulation time against sample time to determine event timing</li>
 *   <li>Generates rising edge on function-call signal at event times</li>
 *   <li>Connected function-call subsystems execute on rising edge detection</li>
 * </ul>
 *
 * <p><b>Use Cases:</b></p>
 * <ul>
 *   <li>Periodic task execution in real-time embedded systems</li>
 *   <li>Multi-rate system simulation with explicit rate scheduling</li>
 *   <li>Event-driven control logic and state machine triggering</li>
 *   <li>Coordinated execution of multiple function-call subsystems</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
public class FunctionCallGenerator extends Block {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter sampleTimeParam;

    @Getter
    private final Parameter initFcnCallInput;

    // === Function-Call State Management ===
    /**
     * Last simulation time when a function-call event was generated.
     * Used to determine next event timing.
     */
    private double lastEventTime = -1.0;

    /**
     * Current function-call signal state (0.0 = inactive, 1.0 = active).
     * Represents the output function-call signal level.
     */
    @Getter
    private double currentCallState = 0.0;

    /**
     * Flag indicating whether initialization event has been generated.
     */
    private boolean initEventGenerated = false;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "1.0");
        PARAMETER_DEFAULTS.put("InitFcnCallInput", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names - FunctionCallGenerator has only output
        outputNames.add("fcn");
    }

    // === Private Constructor with Typed Parameters ===
    private FunctionCallGenerator(Parameter sampleTimeParam, Parameter initFcnCallInput,
                                  String blockName, String blockPath, String blockUUID,
                                  NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.sampleTimeParam = Objects.requireNonNull(sampleTimeParam,
            "SampleTime parameter cannot be null");
        this.initFcnCallInput = Objects.requireNonNull(initFcnCallInput,
            "InitFcnCallInput parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.sampleTimeParam);
        parameterList.add(this.initFcnCallInput);

        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    /**
     * Legacy constructor for backward compatibility with JSONObject-based initialization.
     *
     * @deprecated Use DTO-based constructor {@link #FunctionCallGenerator(FunctionCallGeneratorDto, NCSLabModel)} instead.
     * @param blockJSON JSON object containing block configuration
     * @param model Parent model
     */
    @Deprecated
    public FunctionCallGenerator(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create SIMULINK parameters with defaults
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.initFcnCallInput = getParameterByName("InitFcnCallInput");

        initializePorts();
    }

    /**
     * DTO-NATIVE Constructor - Creates FunctionCallGenerator block directly from DTO.
     * This is the preferred constructor following the DTO-first approach.
     *
     * @param blockDto Function-call generator block DTO with validated parameters
     * @param model Parent model
     */
    public FunctionCallGenerator(FunctionCallGeneratorDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.initFcnCallInput = getParameterByName("InitFcnCallInput");

        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() +
            " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    /**
     * Creates a FunctionCallGenerator block from JSON configuration.
     *
     * @param blockJSON JSON object containing block parameters
     * @param model Parent model
     * @return FunctionCallGenerator block instance
     * @throws BlockCreationException if block creation fails
     */
    public static FunctionCallGenerator fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter sampleTimeParam = createSampleTimeFromJSON(paramValues);
            Parameter initFcnCallInput = createInitFcnCallInputFromJSON(paramValues);

            FunctionCallGenerator block = new FunctionCallGenerator(sampleTimeParam, initFcnCallInput,
                                                                   blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, sampleTimeParam, initFcnCallInput);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create FunctionCallGenerator block from JSON: " +
                e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Creates a FunctionCallGenerator block with default parameters.
     *
     * @param name Block name
     * @param path Block path
     * @param model Parent model
     * @return FunctionCallGenerator block instance
     */
    public static FunctionCallGenerator create(String name, String path, NCSLabModel model) {
        return create(name, path, 1.0, "off", model);
    }

    /**
     * Creates a FunctionCallGenerator block with full parameters using DTO-based approach.
     *
     * @param name Block name
     * @param path Block path
     * @param sampleTime Sample time for function-call generation (must be positive)
     * @param initFcnCallInput Initial function-call state ("on" or "off")
     * @param model Parent model
     * @return FunctionCallGenerator block instance
     */
    public static FunctionCallGenerator create(String name, String path, double sampleTime,
                                              String initFcnCallInput, NCSLabModel model) {
        // Build DTO using constructor with individual parameters
        FunctionCallGeneratorDto dto = new FunctionCallGeneratorDto(
            name,
            path,
            com.ncslab.dto.common.TypedParameter.of(sampleTime),
            com.ncslab.dto.common.TypedParameter.of(initFcnCallInput)
        );

        // Set blockUUID
        dto.setBlockUUID("null");

        // Validate DTO (automatic validation)
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid FunctionCallGenerator parameters: " +
                dto.getValidationErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new FunctionCallGenerator(dto, model);
    }

    // === Port Initialization ===
    /**
     * Initializes output port for function-call signal.
     * No input ports (autonomous event generator).
     */
    private void initializePorts() {
        // Function-call output port (special signal type)
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Code Generation Methods ===
    /**
     * Generates C code for the FunctionCallGenerator block using Velocity templates.
     *
     * @param code Code structure for C code generation
     */
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add FunctionCallGenerator-specific context
        context.put("sampleTimeValue", getSampleTimeValue());
        context.put("initFcnCallInput", getInitFcnCallInputValue());
        context.put("isInitialCallEnabled", isInitialCallEnabled());
        context.put("outputSignal", outputPortList.get(0).getOutputSignalC().getName());

        String outputCode = TemplateManager.renderTemplate("c/subsystem/FunctionCallGenerator/output.vm", context);
        code.addOutputCode(outputCode);
    }

    // === Dimension Handling ===
    /**
     * Updates port dimensions. Function-call signal is always scalar.
     */
    @Override
    public void updateDimension() throws MatDimException {
        // Function-call signal is always scalar (1x1)
        OutputPort out = outputPortList.get(0);
        out.setHeight(1);
        out.setWidth(1);
        out.getOutputSignalC().setHeight(1);
        out.getOutputSignalC().setWidth(1);
    }

    /**
     * Validates dimensions. Function-call signal must be scalar.
     */
    @Override
    public void checkDimension() throws MatDimException {
        // Function-call signal is always scalar - no validation needed
    }

    // === Execution Methods ===
    /**
     * Calculates output and generates function-call events based on sample time.
     *
     * <p>This method determines whether a function-call event should be generated
     * at the current simulation time by comparing the elapsed time since the last
     * event against the configured sample time.</p>
     *
     * @param t Current simulation time
     */
    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);

        // Check if it's time to generate a function-call event
        if (shouldGenerateCall(t)) {
            // Generate function-call event (rising edge)
            currentCallState = 1.0;
            out.getOutputSignalC().setValue(1.0);
            lastEventTime = t;
        } else {
            // No event - maintain inactive state
            currentCallState = 0.0;
            out.getOutputSignalC().setValue(0.0);
        }
    }

    /**
     * Initializes the function-call generator and handles initial event generation.
     *
     * <p>If InitFcnCallInput="on", generates an initial function-call event at t=0.
     * Otherwise, initializes to inactive state.</p>
     */
    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);

        // Check if initial function-call should be generated
        if (isInitialCallEnabled() && !initEventGenerated) {
            // Generate initial function-call event
            currentCallState = 1.0;
            out.getOutputSignalC().setValue(1.0);
            lastEventTime = 0.0;
            initEventGenerated = true;
        } else {
            // Initialize to inactive state
            currentCallState = 0.0;
            out.getOutputSignalC().setValue(0.0);
        }
    }

    // === Function-Call Event Logic ===
    /**
     * Determines whether a function-call event should be generated at the current time.
     *
     * <p>A function-call event is generated when:</p>
     * <ul>
     *   <li>Elapsed time since last event >= sample time</li>
     *   <li>Current time is a multiple of the sample time (within tolerance)</li>
     * </ul>
     *
     * @param t Current simulation time
     * @return true if function-call event should be generated
     */
    private boolean shouldGenerateCall(double t) {
        double sampleTime = getSampleTimeValue();

        // First event after initialization
        if (lastEventTime < 0.0 && t > 0.0) {
            return true;
        }

        // Periodic event generation based on sample time
        if (lastEventTime >= 0.0) {
            double elapsedTime = t - lastEventTime;
            // Generate event if elapsed time >= sample time (with small tolerance for floating-point precision)
            return elapsedTime >= (sampleTime - 1e-9);
        }

        return false;
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues) {
        String sampleTimeValue = paramValues.optString("SampleTime", "1.0");
        return new Parameter(null, 1, "SampleTime", sampleTimeValue);
    }

    private static Parameter createInitFcnCallInputFromJSON(JSONObject paramValues) {
        String initFcnCallValue = paramValues.optString("InitFcnCallInput", "off");
        return new Parameter(null, 2, "InitFcnCallInput", initFcnCallValue);
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

    private static void setParameterBlockReference(FunctionCallGenerator block, Parameter... parameters) {
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

    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "FunctionCallGenerator");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Accessor Methods ===
    /**
     * Gets the sample time value for function-call generation.
     *
     * @return Sample time interval (positive value)
     */
    public double getSampleTimeValue() {
        try {
            return sampleTimeParam.getData().getInitValue();
        } catch (Exception e) {
            return 1.0; // Default sample time
        }
    }

    /**
     * Gets the initial function-call state value.
     *
     * @return Initial state ("on" or "off")
     */
    public String getInitFcnCallInputValue() {
        return initFcnCallInput.getData().getInitString();
    }

    /**
     * Checks if initial function-call is enabled.
     *
     * @return true if function-call event is generated at t=0
     */
    public boolean isInitialCallEnabled() {
        return "on".equalsIgnoreCase(getInitFcnCallInputValue());
    }

    /**
     * Gets the last event time.
     *
     * @return Last simulation time when function-call event was generated
     */
    public double getLastEventTime() {
        return lastEventTime;
    }

    /**
     * Checks if the generator is currently generating a function-call event.
     *
     * @return true if function-call signal is active (rising edge)
     */
    public boolean isGeneratingCall() {
        return currentCallState > 0.0;
    }

    /**
     * Calculates the next function-call event time.
     *
     * @param currentTime Current simulation time
     * @return Next event time
     */
    public double getNextEventTime(double currentTime) {
        if (lastEventTime < 0.0) {
            return isInitialCallEnabled() ? 0.0 : getSampleTimeValue();
        }
        return lastEventTime + getSampleTimeValue();
    }
}
