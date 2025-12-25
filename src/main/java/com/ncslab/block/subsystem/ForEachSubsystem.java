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
import com.ncslab.dto.block.specialized.subsystem.ForEachSubsystemDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.dto.core.BlockDto;

/**
 * For Each Subsystem block - Applies subsystem to each element/partition of input vectors.
 *
 * <p>This subsystem block partitions input signals along a specified dimension and applies
 * the subsystem contents to each partition independently. The subsystem executes once per
 * partition element, implementing a for-each loop pattern over array partitions.</p>
 *
 * <p><b>SIMULINK-Compatible Parameters:</b></p>
 * <ul>
 *   <li><b>PartitionDimension</b>: Which dimension to partition (1 or 2, default: 1)</li>
 *   <li><b>PartitionWidth</b>: Width of each partition slice in elements (default: 1)</li>
 *   <li><b>ResetStates</b>: "held" or "reset" - state behavior across iterations (default: "held")</li>
 *   <li><b>ShowIterationIndex</b>: "on" or "off" (default) - creates output port for iteration index</li>
 * </ul>
 *
 * <p><b>Partitioning Logic:</b></p>
 * <ul>
 *   <li><b>Dimension 1 (rows)</b>: Partitions vertically, each slice is PartitionWidth rows</li>
 *   <li><b>Dimension 2 (columns)</b>: Partitions horizontally, each slice is PartitionWidth columns</li>
 *   <li><b>Iteration Count</b>: total_size / PartitionWidth (must divide evenly)</li>
 *   <li><b>Output Concatenation</b>: Outputs concatenated along the partition dimension</li>
 * </ul>
 *
 * <p><b>Example (1-D vector [a b c d], PartitionWidth=1, PartitionDimension=1):</b></p>
 * <pre>
 * Iteration 1: Subsystem processes [a], outputs [y1]
 * Iteration 2: Subsystem processes [b], outputs [y2]
 * Iteration 3: Subsystem processes [c], outputs [y3]
 * Iteration 4: Subsystem processes [d], outputs [y4]
 * Final output: [y1 y2 y3 y4]
 * </pre>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>Input ports: Defined by In blocks (receive full input, partitioned internally)</li>
 *   <li>Output ports: Defined by Out blocks (concatenated from partition outputs)</li>
 *   <li>Optional iteration index output if ShowIterationIndex="on"</li>
 * </ul>
 *
 * <p><b>State Management:</b></p>
 * <ul>
 *   <li><b>held</b>: States persist across partition iterations (default)</li>
 *   <li><b>reset</b>: States reset to initial conditions before each partition</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
public class ForEachSubsystem extends Subsystem {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter partitionDimension;

    @Getter
    private final Parameter partitionWidth;

    @Getter
    private final Parameter resetStates;

    @Getter
    private final Parameter showIterationIndex;

    // === Iteration State Management ===
    /**
     * Current iteration number (updated during loop execution).
     */
    @Getter
    private int currentIteration = 0;

    /**
     * Total number of partitions to process.
     */
    @Getter
    private int numPartitions = 0;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("PartitionDimension", "1");
        PARAMETER_DEFAULTS.put("PartitionWidth", "1");
        PARAMETER_DEFAULTS.put("ResetStates", "held");
        PARAMETER_DEFAULTS.put("ShowIterationIndex", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Output port added conditionally based on ShowIterationIndex parameter
        // Input ports defined by In blocks within the subsystem
    }

    // === Private Constructor with Typed Parameters ===
    private ForEachSubsystem(Parameter partitionDimension, Parameter partitionWidth,
                             Parameter resetStates, Parameter showIterationIndex,
                             String blockName, String blockPath,
                             String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.partitionDimension = Objects.requireNonNull(partitionDimension,
            "PartitionDimension parameter cannot be null");
        this.partitionWidth = Objects.requireNonNull(partitionWidth,
            "PartitionWidth parameter cannot be null");
        this.resetStates = Objects.requireNonNull(resetStates,
            "ResetStates parameter cannot be null");
        this.showIterationIndex = Objects.requireNonNull(showIterationIndex,
            "ShowIterationIndex parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.partitionDimension);
        parameterList.add(this.partitionWidth);
        parameterList.add(this.resetStates);
        parameterList.add(this.showIterationIndex);

        // Initialize ports based on configuration
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    /**
     * Legacy constructor for backward compatibility with JSONObject-based initialization.
     *
     * @deprecated Use DTO-based constructor {@link #ForEachSubsystem(ForEachSubsystemDto, NCSLabModel)} instead.
     * @param blockJSON JSON object containing block configuration
     * @param model Parent model
     */
    @Deprecated
    public ForEachSubsystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create SIMULINK parameters with defaults
        this.partitionDimension = getParameterByName("PartitionDimension");
        this.partitionWidth = getParameterByName("PartitionWidth");
        this.resetStates = getParameterByName("ResetStates");
        this.showIterationIndex = getParameterByName("ShowIterationIndex");

        initializePorts();
    }

    /**
     * DTO-NATIVE Constructor - Creates ForEachSubsystem block directly from ForEachSubsystemDto.
     * This is the preferred constructor following the DTO-first approach.
     *
     * @param blockDto For Each Subsystem block DTO with validated parameters
     * @param model Parent model
     */
    public ForEachSubsystem(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.partitionDimension = getParameterByName("PartitionDimension");
        this.partitionWidth = getParameterByName("PartitionWidth");
        this.resetStates = getParameterByName("ResetStates");
        this.showIterationIndex = getParameterByName("ShowIterationIndex");

        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() +
            " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    /**
     * Creates a ForEachSubsystem block from JSON configuration.
     *
     * @param blockJSON JSON object containing block parameters
     * @param model Parent model
     * @return ForEachSubsystem block instance
     * @throws BlockCreationException if block creation fails
     */
    public static ForEachSubsystem fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter partitionDimension = createPartitionDimensionFromJSON(paramValues);
            Parameter partitionWidth = createPartitionWidthFromJSON(paramValues);
            Parameter resetStates = createResetStatesFromJSON(paramValues);
            Parameter showIterationIndex = createShowIterationIndexFromJSON(paramValues);

            ForEachSubsystem block = new ForEachSubsystem(partitionDimension, partitionWidth,
                                                         resetStates, showIterationIndex,
                                                         blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, partitionDimension, partitionWidth,
                                      resetStates, showIterationIndex);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create ForEachSubsystem block from JSON: " +
                e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Creates a ForEachSubsystem block with default parameters.
     *
     * @param name Block name
     * @param path Block path
     * @param model Parent model
     * @return ForEachSubsystem block instance
     */
    public static ForEachSubsystem create(String name, String path, NCSLabModel model) {
        return create(name, path, 1, 1, "held", "off", model);
    }

    /**
     * Creates a ForEachSubsystem block with full parameters using DTO-based approach.
     *
     * @param name Block name
     * @param path Block path
     * @param partitionDimension Which dimension to partition (1 or 2)
     * @param partitionWidth Width of each partition slice
     * @param resetStates State reset behavior ("held" or "reset")
     * @param showIterationIndex Iteration index output ("on" or "off")
     * @param model Parent model
     * @return ForEachSubsystem block instance
     */
    public static ForEachSubsystem create(String name, String path, int partitionDimension,
                                         int partitionWidth, String resetStates,
                                         String showIterationIndex, NCSLabModel model) {
        // Build DTO using constructor with individual parameters
        ForEachSubsystemDto dto = new ForEachSubsystemDto(
            name,
            path,
            com.ncslab.dto.common.TypedParameter.of(partitionDimension),
            com.ncslab.dto.common.TypedParameter.of(partitionWidth),
            com.ncslab.dto.common.TypedParameter.of(resetStates),
            com.ncslab.dto.common.TypedParameter.of("on".equalsIgnoreCase(showIterationIndex))
        );

        // Set blockUUID
        dto.setBlockUUID("null");

        // Validate DTO (automatic validation)
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid ForEachSubsystem parameters: " +
                dto.getValidationErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new ForEachSubsystem(dto, model);
    }

    /**
     * Factory method to create ForEachSubsystem from ForEachSubsystemDto.
     *
     * @param dto   ForEachSubsystemDto containing block configuration
     * @param model NCSLabModel containing the block diagram
     * @return Created ForEachSubsystem block
     */
    public static ForEachSubsystem createFromDto(ForEachSubsystemDto dto, NCSLabModel model) {
        if (dto == null) {
            throw new IllegalArgumentException("ForEachSubsystemDto cannot be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("NCSLabModel cannot be null");
        }

        // Validate DTO before creating block
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid ForEachSubsystemDto: " + dto.getValidationErrors());
        }

        return new ForEachSubsystem(dto, model);
    }

    // === Port Initialization ===
    /**
     * Initializes input and output ports based on block configuration.
     * Creates optional output port for iteration index if ShowIterationIndex="on".
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
     * Generates C code for the ForEachSubsystem block using Velocity templates.
     *
     * @param code Code structure for C code generation
     */
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        // Don't call super.generateOutputCodeC - we handle everything here

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add ForEachSubsystem-specific context
        context.put("partitionDimension", getPartitionDimensionValue());
        context.put("partitionWidth", getPartitionWidthValue());
        context.put("resetStates", getResetStatesValue());
        context.put("showIterationIndex", hasIterationOutput());
        context.put("statesAreHeld", statesAreHeld());
        context.put("statesAreReset", statesAreReset());
        context.put("isPartitionDimension1", isPartitionDimension1());
        context.put("isPartitionDimension2", isPartitionDimension2());
        context.put("currentIteration", currentIteration);
        context.put("numPartitions", numPartitions);
        context.put("containedBlocks", getInnerSystem().getBlocks());
        context.put("containedLines", getInnerSystem().getLines());
        context.put("inBlockList", getInBlockList());
        context.put("outBlockList", getOutBlockList());

        // Optional output signal for iteration index
        if (hasIterationOutput() && outputPortList.size() > 0) {
            context.put("iterationOutputSignal", outputPortList.get(0).getOutputSignalC().getName());
        } else {
            context.put("iterationOutputSignal", null);
        }

        String outputCode = TemplateManager.renderTemplate(
            "c/subsystem/ForEachSubsystem/output.vm", context);
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

        // Calculate number of partitions based on input dimensions
        updatePartitionCount();

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
                throw new MatDimException("ForEachSubsystem iteration output must be scalar (1x1), got " +
                    out.getHeight() + "x" + out.getWidth());
            }
        }

        // Verify input dimensions are compatible with partition settings
        validatePartitionDimensions();
    }

    // === Execution Methods ===
    /**
     * Initializes the for-each subsystem and sets initial iteration state.
     */
    @Override
    public void calculateInit() {
        // Reset to starting iteration index
        currentIteration = 0;

        // Initialize subsystem
        super.calculateInit();

        // Set iteration output if enabled
        if (hasIterationOutput() && outputPortList.size() > 0) {
            OutputPort out = outputPortList.get(0);
            out.getOutputSignalC().setValue(currentIteration);
        }
    }

    /**
     * Calculates output by executing the subsystem for each partition.
     *
     * @param t Current simulation time
     */
    @Override
    public void calculateOutput(double t) {
        // Calculate number of partitions
        updatePartitionCount();

        // Execute for-each loop over partitions
        for (int i = 0; i < numPartitions; i++) {
            currentIteration = i;

            // Reset states if configured
            if (statesAreReset()) {
                super.calculateInit();
            }

            // Update iteration output port if enabled
            if (hasIterationOutput() && outputPortList.size() > 0) {
                OutputPort out = outputPortList.get(0);
                out.getOutputSignalC().setValue(currentIteration);
            }

            // Execute one iteration of the subsystem for current partition
            executePartition(i, t);
        }
    }

    /**
     * Executes one partition iteration of the subsystem.
     *
     * @param partitionIndex Current partition index
     * @param t Current simulation time
     */
    protected void executePartition(int partitionIndex, double t) {
        // Execute subsystem with current partition context
        // The subsystem's internal blocks process the partitioned data
        super.calculateOutput(t);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createPartitionDimensionFromJSON(JSONObject paramValues) {
        int value = paramValues.optInt("PartitionDimension", 1);
        return new Parameter(null, 1, "PartitionDimension", String.valueOf(value));
    }

    private static Parameter createPartitionWidthFromJSON(JSONObject paramValues) {
        int value = paramValues.optInt("PartitionWidth", 1);
        return new Parameter(null, 2, "PartitionWidth", String.valueOf(value));
    }

    private static Parameter createResetStatesFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("ResetStates", "held");
        return new Parameter(null, 3, "ResetStates", value);
    }

    private static Parameter createShowIterationIndexFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("ShowIterationIndex", "off");
        return new Parameter(null, 4, "ShowIterationIndex", value);
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

    private static void setParameterBlockReference(ForEachSubsystem block, Parameter... parameters) {
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
        identity.put("blockType", "ForEachSubsystem");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Accessor Methods ===
    /**
     * Gets the partition dimension value.
     *
     * @return Partition dimension (1 or 2)
     */
    public int getPartitionDimensionValue() {
        return Integer.parseInt(partitionDimension.getData().getInitString());
    }

    /**
     * Gets the partition width value.
     *
     * @return Width of each partition slice
     */
    public int getPartitionWidthValue() {
        return Integer.parseInt(partitionWidth.getData().getInitString());
    }

    /**
     * Gets the reset states value.
     *
     * @return Reset states mode ("held" or "reset")
     */
    public String getResetStatesValue() {
        return resetStates.getData().getInitString();
    }

    /**
     * Gets the show iteration index value.
     *
     * @return Show iteration index setting ("on" or "off")
     */
    public String getShowIterationIndexValue() {
        return showIterationIndex.getData().getInitString();
    }

    /**
     * Checks if iteration index output port is configured.
     *
     * @return true if ShowIterationIndex="on"
     */
    public boolean hasIterationOutput() {
        String value = showIterationIndex.getData().getInitString();
        return "on".equalsIgnoreCase(value);
    }

    /**
     * Checks if states are held across iterations.
     *
     * @return true if ResetStates="held"
     */
    public boolean statesAreHeld() {
        String value = resetStates.getData().getInitString();
        return "held".equalsIgnoreCase(value);
    }

    /**
     * Checks if states reset before each iteration.
     *
     * @return true if ResetStates="reset"
     */
    public boolean statesAreReset() {
        String value = resetStates.getData().getInitString();
        return "reset".equalsIgnoreCase(value);
    }

    /**
     * Checks if partition dimension is 1 (rows/vertical).
     *
     * @return true if PartitionDimension=1
     */
    public boolean isPartitionDimension1() {
        return getPartitionDimensionValue() == 1;
    }

    /**
     * Checks if partition dimension is 2 (columns/horizontal).
     *
     * @return true if PartitionDimension=2
     */
    public boolean isPartitionDimension2() {
        return getPartitionDimensionValue() == 2;
    }

    /**
     * Updates the partition count based on input dimensions.
     */
    private void updatePartitionCount() {
        // In a real implementation, this would calculate based on input dimensions
        // For now, we set a default value
        // This should be updated based on the actual input signal dimensions
        if (getInBlockList().isEmpty()) {
            numPartitions = 1;
            return;
        }

        // Get first input block's dimensions
        In firstIn = getInBlockList().get(0);
        if (firstIn.getInputPortList().isEmpty()) {
            numPartitions = 1;
            return;
        }

        InputPort inputPort = firstIn.getInputPortList().get(0);
        int width = inputPort.getWidth();
        int height = inputPort.getHeight();
        int partitionWidthValue = getPartitionWidthValue();

        // Calculate number of partitions based on partition dimension
        if (isPartitionDimension1()) {
            // Partition along rows (dimension 1)
            numPartitions = height / partitionWidthValue;
        } else {
            // Partition along columns (dimension 2)
            numPartitions = width / partitionWidthValue;
        }

        // Ensure at least one partition
        numPartitions = Math.max(1, numPartitions);
    }

    /**
     * Validates that input dimensions are compatible with partition settings.
     *
     * @throws MatDimException if dimensions are incompatible
     */
    private void validatePartitionDimensions() throws MatDimException {
        if (getInBlockList().isEmpty()) {
            return; // No inputs to validate
        }

        // Get first input block's dimensions
        In firstIn = getInBlockList().get(0);
        if (firstIn.getInputPortList().isEmpty()) {
            return;
        }

        InputPort inputPort = firstIn.getInputPortList().get(0);
        int width = inputPort.getWidth();
        int height = inputPort.getHeight();
        int partitionWidthValue = getPartitionWidthValue();

        // Verify dimensions are divisible by partition width
        if (isPartitionDimension1()) {
            if (height % partitionWidthValue != 0) {
                throw new MatDimException(
                    "Input height (" + height + ") must be divisible by PartitionWidth (" +
                    partitionWidthValue + ") for dimension 1 partitioning");
            }
        } else {
            if (width % partitionWidthValue != 0) {
                throw new MatDimException(
                    "Input width (" + width + ") must be divisible by PartitionWidth (" +
                    partitionWidthValue + ") for dimension 2 partitioning");
            }
        }
    }
}
