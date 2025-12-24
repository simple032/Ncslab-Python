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
import com.ncslab.dto.block.specialized.subsystem.ForIteratorSubsystemDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.dto.core.BlockDto;

/**
 * For Iterator Subsystem block for fixed-count iteration with SIMULINK-compatible parameters.
 *
 * <p>This subsystem block executes its contents a fixed number of times per simulation
 * time step, implementing a for-loop pattern. The subsystem iterates from a starting
 * index to the iteration limit, optionally providing the current iteration number
 * to internal blocks.</p>
 *
 * <p><b>SIMULINK Parameters:</b></p>
 * <ul>
 *   <li><b>IterationLimit</b>: Number of iterations per time step (default: 10)</li>
 *   <li><b>ShowIterationNumber</b>: "on" or "off" (default) - creates output port for iteration index</li>
 *   <li><b>ResetState</b>: "each time step" (default) or "at start of simulation" - state reset behavior</li>
 *   <li><b>IndexMode</b>: "Zero-based" or "One-based" (default) - iteration index starting value</li>
 * </ul>
 *
 * <p><b>Iteration Logic:</b></p>
 * <ul>
 *   <li><b>Fixed Iterations</b>: Executes exactly IterationLimit times per time step</li>
 *   <li><b>Loop Structure</b>: for(i=startIndex; i<=endIndex; i++) { executeIteration(i); }</li>
 *   <li><b>Iteration Index</b>: Current iteration number provided to internal blocks if enabled</li>
 *   <li><b>State Reset</b>: Optionally reset subsystem states before each time step</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>0 or 1 output port (iteration index if ShowIterationNumber="on")</li>
 *   <li>Regular In/Out blocks for subsystem data flow (inherited from Subsystem)</li>
 * </ul>
 *
 * <p><b>Execution Pattern:</b></p>
 * <pre>
 * for each time step t:
 *   if (ResetState == "each time step"):
 *     reset subsystem states
 *   for i in [startIndex..endIndex]:
 *     if (ShowIterationNumber == "on"):
 *       output iteration number i
 *     execute subsystem with current iteration i
 * </pre>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
public class ForIteratorSubsystem extends Subsystem {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter iterationLimit;

    @Getter
    private final Parameter showIterationNumber;

    @Getter
    private final Parameter resetState;

    @Getter
    private final Parameter indexMode;

    // === Iteration State Management ===
    /**
     * Current iteration number (updated during loop execution).
     */
    @Getter
    private int currentIteration = 1;

    /**
     * Flag indicating if this is the first time step (for simulation start reset).
     */
    private boolean isFirstTimeStep = true;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("IterationLimit", "10");
        PARAMETER_DEFAULTS.put("ShowIterationNumber", "off");
        PARAMETER_DEFAULTS.put("ResetState", "each time step");
        PARAMETER_DEFAULTS.put("IndexMode", "One-based");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Output port added conditionally based on ShowIterationNumber parameter
        // No standard input ports for For Iterator Subsystem
    }

    // === Private Constructor with Typed Parameters ===
    private ForIteratorSubsystem(Parameter iterationLimit, Parameter showIterationNumber,
                                 Parameter resetState, Parameter indexMode,
                                 String blockName, String blockPath,
                                 String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.iterationLimit = Objects.requireNonNull(iterationLimit,
            "IterationLimit parameter cannot be null");
        this.showIterationNumber = Objects.requireNonNull(showIterationNumber,
            "ShowIterationNumber parameter cannot be null");
        this.resetState = Objects.requireNonNull(resetState,
            "ResetState parameter cannot be null");
        this.indexMode = Objects.requireNonNull(indexMode,
            "IndexMode parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.iterationLimit);
        parameterList.add(this.showIterationNumber);
        parameterList.add(this.resetState);
        parameterList.add(this.indexMode);

        // Initialize ports based on configuration
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    /**
     * Legacy constructor for backward compatibility with JSONObject-based initialization.
     *
     * @deprecated Use DTO-based constructor {@link #ForIteratorSubsystem(ForIteratorSubsystemDto, NCSLabModel)} instead.
     * @param blockJSON JSON object containing block configuration
     * @param model Parent model
     */
    @Deprecated
    public ForIteratorSubsystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create SIMULINK parameters with defaults
        this.iterationLimit = getParameterByName("IterationLimit");
        this.showIterationNumber = getParameterByName("ShowIterationNumber");
        this.resetState = getParameterByName("ResetState");
        this.indexMode = getParameterByName("IndexMode");

        initializePorts();
    }

    /**
     * DTO-NATIVE Constructor - Creates ForIteratorSubsystem block directly from ForIteratorSubsystemDto.
     * This is the preferred constructor following the DTO-first approach.
     *
     * @param blockDto For Iterator Subsystem block DTO with validated parameters
     * @param model Parent model
     */
    public ForIteratorSubsystem(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.iterationLimit = getParameterByName("IterationLimit");
        this.showIterationNumber = getParameterByName("ShowIterationNumber");
        this.resetState = getParameterByName("ResetState");
        this.indexMode = getParameterByName("IndexMode");

        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() +
            " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    /**
     * Creates a ForIteratorSubsystem block from JSON configuration.
     *
     * @param blockJSON JSON object containing block parameters
     * @param model Parent model
     * @return ForIteratorSubsystem block instance
     * @throws BlockCreationException if block creation fails
     */
    public static ForIteratorSubsystem fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter iterationLimit = createIterationLimitFromJSON(paramValues);
            Parameter showIterationNumber = createShowIterationNumberFromJSON(paramValues);
            Parameter resetState = createResetStateFromJSON(paramValues);
            Parameter indexMode = createIndexModeFromJSON(paramValues);

            ForIteratorSubsystem block = new ForIteratorSubsystem(iterationLimit, showIterationNumber,
                                                                 resetState, indexMode,
                                                                 blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, iterationLimit, showIterationNumber,
                                      resetState, indexMode);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create ForIteratorSubsystem block from JSON: " +
                e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Creates a ForIteratorSubsystem block with default parameters.
     *
     * @param name Block name
     * @param path Block path
     * @param model Parent model
     * @return ForIteratorSubsystem block instance
     */
    public static ForIteratorSubsystem create(String name, String path, NCSLabModel model) {
        return create(name, path, 10, "off", "each time step", "One-based", model);
    }

    /**
     * Creates a ForIteratorSubsystem block with full parameters using DTO-based approach.
     *
     * @param name Block name
     * @param path Block path
     * @param iterationLimit Number of iterations per time step
     * @param showIterationNumber Output port visibility ("on" or "off")
     * @param resetState State reset behavior
     * @param indexMode Iteration index mode
     * @param model Parent model
     * @return ForIteratorSubsystem block instance
     */
    public static ForIteratorSubsystem create(String name, String path, int iterationLimit,
                                             String showIterationNumber, String resetState,
                                             String indexMode, NCSLabModel model) {
        // Build DTO using constructor with individual parameters
        ForIteratorSubsystemDto dto = new ForIteratorSubsystemDto(
            name,
            path,
            com.ncslab.dto.common.TypedParameter.of(iterationLimit),
            com.ncslab.dto.common.TypedParameter.of(showIterationNumber),
            com.ncslab.dto.common.TypedParameter.of(resetState),
            com.ncslab.dto.common.TypedParameter.of(indexMode)
        );

        // Set blockUUID
        dto.setBlockUUID("null");

        // Validate DTO (automatic validation)
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid ForIteratorSubsystem parameters: " +
                dto.getValidationErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new ForIteratorSubsystem(dto, model);
    }

    /**
     * Factory method to create ForIteratorSubsystem from ForIteratorSubsystemDto.
     *
     * @param dto   ForIteratorSubsystemDto containing block configuration
     * @param model NCSLabModel containing the block diagram
     * @return Created ForIteratorSubsystem block
     */
    public static ForIteratorSubsystem createFromDto(ForIteratorSubsystemDto dto, NCSLabModel model) {
        if (dto == null) {
            throw new IllegalArgumentException("ForIteratorSubsystemDto cannot be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("NCSLabModel cannot be null");
        }

        // Validate DTO before creating block
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid ForIteratorSubsystemDto: " + dto.getValidationErrors());
        }

        return new ForIteratorSubsystem(dto, model);
    }

    // === Port Initialization ===
    /**
     * Initializes input and output ports based on block configuration.
     * Creates optional output port for iteration index if ShowIterationNumber="on".
     */
    private void initializePorts() {
        // Optional output port for iteration index
        if (hasIterationOutput()) {
            outputPortList.add(new OutputPort(this, 1, true));
            if (!outputNames.contains("iteration")) {
                outputNames.add("iteration");
            }
        }
    }

    // === Code Generation Methods ===
    /**
     * Generates C code for the ForIteratorSubsystem block using Velocity templates.
     *
     * @param code Code structure for C code generation
     */
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add ForIteratorSubsystem-specific context
        context.put("iterationLimit", getIterationLimitValue());
        context.put("showIterationNumber", hasIterationOutput());
        context.put("resetState", getResetStateValue());
        context.put("indexMode", getIndexModeValue());
        context.put("isZeroBasedIndex", isZeroBasedIndex());
        context.put("isOneBasedIndex", isOneBasedIndex());
        context.put("startIndex", getStartIndex());
        context.put("endIndex", getEndIndex());
        context.put("resetsEachTimeStep", resetsEachTimeStep());
        context.put("resetsAtSimulationStart", resetsAtSimulationStart());

        // Optional output signal for iteration index
        if (hasIterationOutput() && outputPortList.size() > 0) {
            context.put("iterationOutputSignal", outputPortList.get(0).getOutputSignalC().getName());
        } else {
            context.put("iterationOutputSignal", null);
        }

        String outputCode = TemplateManager.renderTemplate(
            "c/subsystem/ForIteratorSubsystem/output.vm", context);
        code.addOutputCode(outputCode);
    }

    // === Dimension Handling ===
    /**
     * Updates port dimensions. Iteration index output is always scalar.
     */
    @Override
    public void updateDimension() throws MatDimException {
        // Call parent to update subsystem ports
        super.updateDimension();

        // Iteration index output is always scalar (1x1)
        if (hasIterationOutput() && outputPortList.size() > 0) {
            OutputPort out = outputPortList.get(0);
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
        }
    }

    /**
     * Validates dimensions. Iteration index must be scalar.
     */
    @Override
    public void checkDimension() throws MatDimException {
        // Call parent to check subsystem dimensions
        super.checkDimension();

        // Verify iteration output is scalar if present
        if (hasIterationOutput() && outputPortList.size() > 0) {
            OutputPort out = outputPortList.get(0);
            if (out.getHeight() != 1 || out.getWidth() != 1) {
                throw new MatDimException("ForIteratorSubsystem iteration output must be scalar (1x1), got " +
                    out.getHeight() + "x" + out.getWidth());
            }
        }
    }

    // === Execution Methods ===
    /**
     * Initializes the for iterator subsystem and sets initial iteration state.
     */
    @Override
    public void calculateInit() {
        // Mark that this is the first time step
        isFirstTimeStep = true;

        // Reset to starting iteration index
        currentIteration = getStartIndex();

        // Initialize subsystem if reset at simulation start
        if (resetsAtSimulationStart()) {
            super.calculateInit();
        }

        // Set iteration output if enabled
        if (hasIterationOutput() && outputPortList.size() > 0) {
            OutputPort out = outputPortList.get(0);
            out.getOutputSignalC().setValue(currentIteration);
        }
    }

    /**
     * Calculates output by executing the subsystem for each iteration.
     *
     * @param t Current simulation time
     */
    @Override
    public void calculateOutput(double t) {
        // Reset states at each time step if configured
        if (resetsEachTimeStep() && !isFirstTimeStep) {
            super.calculateInit();
        }

        // Execute for-loop iterations
        int startIdx = getStartIndex();
        int limit = getIterationLimitValue();
        int endIdx = isZeroBasedIndex() ? (startIdx + limit - 1) : (startIdx + limit - 1);

        for (int i = startIdx; i <= endIdx; i++) {
            currentIteration = i;

            // Update iteration output port if enabled
            if (hasIterationOutput() && outputPortList.size() > 0) {
                OutputPort out = outputPortList.get(0);
                out.getOutputSignalC().setValue(currentIteration);
            }

            // Execute one iteration of the subsystem
            executeIteration(i, t);
        }

        // Mark that first time step is complete
        isFirstTimeStep = false;
    }

    /**
     * Executes one iteration of the subsystem.
     *
     * @param iterationIndex Current iteration index
     * @param t Current simulation time
     */
    protected void executeIteration(int iterationIndex, double t) {
        // Execute subsystem with current iteration context
        // The subsystem's internal blocks can access the current iteration
        // through the iteration output port if ShowIterationNumber="on"
        super.calculateOutput(t);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createIterationLimitFromJSON(JSONObject paramValues) {
        int value = paramValues.optInt("IterationLimit", 10);
        return new Parameter(null, 1, "IterationLimit", String.valueOf(value));
    }

    private static Parameter createShowIterationNumberFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("ShowIterationNumber", "off");
        return new Parameter(null, 2, "ShowIterationNumber", value);
    }

    private static Parameter createResetStateFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("ResetState", "each time step");
        return new Parameter(null, 3, "ResetState", value);
    }

    private static Parameter createIndexModeFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("IndexMode", "One-based");
        return new Parameter(null, 4, "IndexMode", value);
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

    private static void setParameterBlockReference(ForIteratorSubsystem block, Parameter... parameters) {
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
        identity.put("blockType", "ForIteratorSubsystem");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Accessor Methods ===
    /**
     * Gets the iteration limit value.
     *
     * @return Number of iterations per time step
     */
    public int getIterationLimitValue() {
        return Integer.parseInt(iterationLimit.getData().getInitString());
    }

    /**
     * Gets the show iteration number value.
     *
     * @return Show iteration number setting ("on" or "off")
     */
    public String getShowIterationNumberValue() {
        return showIterationNumber.getData().getInitString();
    }

    /**
     * Gets the reset state value.
     *
     * @return Reset state mode
     */
    public String getResetStateValue() {
        return resetState.getData().getInitString();
    }

    /**
     * Gets the index mode value.
     *
     * @return Index mode ("Zero-based" or "One-based")
     */
    public String getIndexModeValue() {
        return indexMode.getData().getInitString();
    }

    /**
     * Checks if iteration number output port is configured.
     *
     * @return true if ShowIterationNumber="on"
     */
    public boolean hasIterationOutput() {
        String value = showIterationNumber.getData().getInitString();
        return "on".equalsIgnoreCase(value);
    }

    /**
     * Checks if states reset at each time step.
     *
     * @return true if ResetState="each time step"
     */
    public boolean resetsEachTimeStep() {
        String value = resetState.getData().getInitString();
        return "each time step".equalsIgnoreCase(value);
    }

    /**
     * Checks if states reset only at simulation start.
     *
     * @return true if ResetState="at start of simulation"
     */
    public boolean resetsAtSimulationStart() {
        String value = resetState.getData().getInitString();
        return "at start of simulation".equalsIgnoreCase(value);
    }

    /**
     * Checks if iteration index is zero-based.
     *
     * @return true if IndexMode="Zero-based"
     */
    public boolean isZeroBasedIndex() {
        String value = indexMode.getData().getInitString();
        return "Zero-based".equalsIgnoreCase(value);
    }

    /**
     * Checks if iteration index is one-based.
     *
     * @return true if IndexMode="One-based"
     */
    public boolean isOneBasedIndex() {
        String value = indexMode.getData().getInitString();
        return "One-based".equalsIgnoreCase(value);
    }

    /**
     * Gets the starting index value based on index mode.
     *
     * @return Starting index (0 for zero-based, 1 for one-based)
     */
    public int getStartIndex() {
        return isZeroBasedIndex() ? 0 : 1;
    }

    /**
     * Gets the ending index value for loop termination.
     *
     * @return Ending index (inclusive)
     */
    public int getEndIndex() {
        int startIdx = getStartIndex();
        int limit = getIterationLimitValue();
        return startIdx + limit - 1;
    }
}
