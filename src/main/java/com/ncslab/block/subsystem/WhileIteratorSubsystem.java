package com.ncslab.block.subsystem;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.data.Data;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.block.specialized.subsystem.WhileIteratorSubsystemDto;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;
import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * While Iterator Subsystem block - Executes while a condition is true.
 *
 * <p>This subsystem implements while-loop semantics in the block diagram,
 * executing its contents repeatedly while a condition signal is true
 * and the iteration count is below MaxIterations.</p>
 *
 * <p><b>SIMULINK-Compatible Parameters:</b></p>
 * <ul>
 *   <li><b>MaxIterations</b>: Maximum iterations to prevent infinite loops (default: 10)</li>
 *   <li><b>ShowIterationNumber</b>: "on" or "off" - creates output port for iteration index</li>
 *   <li><b>ResetState</b>: "each time step" or "at start of simulation" - when to reset state</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 input port: condition signal (must be connected to internal condition output)</li>
 *   <li>0 or 1 output port: iteration number (only if ShowIterationNumber="on")</li>
 *   <li>Regular In/Out blocks for data flow (inherited from Subsystem)</li>
 * </ul>
 *
 * <p><b>While Loop Logic:</b></p>
 * <pre>
 * iteration = 1;
 * while (condition != 0 && iteration <= MaxIterations) {
 *     executeSubsystem();
 *     iteration++;
 * }
 * </pre>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-22
 */
public class WhileIteratorSubsystem extends Subsystem {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter maxIterations;
    @Getter
    private final Parameter showIterationNumber;
    @Getter
    private final Parameter resetState;

    // === Runtime State ===
    /** Current iteration number (starts at 1) */
    @Getter
    private int currentIteration = 1;

    /** Condition value from internal condition block */
    private double conditionValue = 0.0;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("MaxIterations", "10");
        PARAMETER_DEFAULTS.put("ShowIterationNumber", "off");
        PARAMETER_DEFAULTS.put("ResetState", "each time step");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names
        inputNames.add("condition"); // Condition input from internal condition block
        // Output port added dynamically if ShowIterationNumber is "on"
    }

    // === DTO-Native Constructor ===
    /**
     * Creates WhileIteratorSubsystem from WhileIteratorSubsystemDto.
     *
     * @param blockDto DTO containing block configuration
     * @param model Parent model reference
     */
    public WhileIteratorSubsystem(WhileIteratorSubsystemDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize parameters from DTO
        this.maxIterations = getParameterByName("MaxIterations");
        this.showIterationNumber = getParameterByName("ShowIterationNumber");
        this.resetState = getParameterByName("ResetState");

        // Initialize ports based on ShowIterationNumber
        initializeIteratorPorts();

        System.out.println("DTO-NATIVE: WhileIteratorSubsystem block created successfully - " + blockDto.getBlockName());
    }

    // === Legacy JSON Constructor ===
    /**
     * Creates WhileIteratorSubsystem from JSONObject (legacy).
     *
     * @param blockJSON JSON object containing block configuration
     * @param model Parent model reference
     * @deprecated Use DTO-native constructor for new development
     */
    @Deprecated
    public WhileIteratorSubsystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Initialize parameters with defaults
        this.maxIterations = getParameterByName("MaxIterations");
        this.showIterationNumber = getParameterByName("ShowIterationNumber");
        this.resetState = getParameterByName("ResetState");

        // Initialize ports based on ShowIterationNumber
        initializeIteratorPorts();
    }

    // === Port Initialization ===
    /**
     * Initializes iterator-specific ports based on ShowIterationNumber parameter.
     */
    private void initializeIteratorPorts() {
        // Add iteration number output port if ShowIterationNumber is "on"
        if (shouldShowIterationNumber()) {
            outputPortList.add(new OutputPort(this, "iteration", outputPortList.size() + 1, true));
            outputNames.add("iteration");
        }
    }

    // === Initialization and Execution Methods ===
    @Override
    public void calculateInit() {
        // Reset iteration counter
        resetIterationState();

        // Initialize subsystem contents
        super.calculateInit();
    }

    @Override
    public void calculateOutput(double t) {
        // Reset state if configured to reset each time step
        if (shouldResetEachTimeStep()) {
            resetIterationState();
        }

        // Execute while loop
        currentIteration = 1;
        while (checkCondition() && currentIteration <= getMaxIterationsValue()) {
            executeIteration(currentIteration);
            currentIteration++;
        }

        // Update iteration number output port if enabled
        if (shouldShowIterationNumber() && !outputPortList.isEmpty()) {
            OutputPort iterationPort = outputPortList.get(outputPortList.size() - 1);
            Data iterationData = new Data();
            iterationData.setInitValue(currentIteration - 1); // Output final iteration count
            iterationPort.setData(iterationData);
        }
    }

    // === While Loop Logic Methods ===

    /**
     * Checks the condition for while loop continuation.
     * The condition is evaluated from a special internal condition output block.
     *
     * @return true if condition is non-zero (continue looping)
     */
    private boolean checkCondition() {
        // In a real implementation, this would check a special condition output block
        // For now, we'll use a placeholder that checks if there's a designated condition block

        // Look for a condition output block (e.g., a special Out block named "condition")
        for (Out outBlock : getOutBlockList()) {
            if ("condition".equalsIgnoreCase(outBlock.getBlockName())) {
                // Get the condition value from the Out block's input
                if (!outBlock.getInputPortList().isEmpty()) {
                    Data conditionData = outBlock.getInputPortList().get(0).getData();
                    if (conditionData != null) {
                        conditionValue = conditionData.getInitValue();
                        return Math.abs(conditionValue) > 1e-10; // Non-zero is true
                    }
                }
            }
        }

        // Default: assume condition is false if no condition block found
        return false;
    }

    /**
     * Executes one iteration of the while loop.
     *
     * @param iteration Current iteration number (1-based)
     */
    private void executeIteration(int iteration) {
        // Execute all blocks in the subsystem
        try {
            getInnerSystem().calculateOutput(0.0); // Time is managed externally
        } catch (Exception e) {
            System.err.println("Error executing iteration " + iteration + " in WhileIteratorSubsystem " +
                             getBlockName() + ": " + e.getMessage());
        }
    }

    /**
     * Resets the iteration state to initial values.
     */
    private void resetIterationState() {
        currentIteration = 1;
        conditionValue = 0.0;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the maximum iterations value.
     *
     * @return Maximum number of iterations
     */
    public int getMaxIterationsValue() {
        if (maxIterations != null) {
            try {
                return maxIterations.getData().getIntValue();
            } catch (Exception e) {
                return 10; // Default
            }
        }
        return 10;
    }

    /**
     * Gets the show iteration number setting.
     *
     * @return true if iteration number should be shown
     */
    public boolean shouldShowIterationNumber() {
        if (showIterationNumber != null) {
            String value = showIterationNumber.getData().getInitString();
            if (value == null || value.isEmpty()) {
                value = String.valueOf(showIterationNumber.getData().getInitValue());
            }
            return "on".equalsIgnoreCase(value.trim());
        }
        return false;
    }

    /**
     * Gets the reset state setting.
     *
     * @return true if state should be reset each time step
     */
    public boolean shouldResetEachTimeStep() {
        if (resetState != null) {
            String value = resetState.getData().getInitString();
            if (value == null || value.isEmpty()) {
                value = String.valueOf(resetState.getData().getInitValue());
            }
            return "each time step".equalsIgnoreCase(value.trim());
        }
        return true; // Default
    }

    /**
     * Gets the reset state setting.
     *
     * @return true if state should be reset only at simulation start
     */
    public boolean shouldResetAtSimulationStart() {
        return !shouldResetEachTimeStep();
    }

    // === Code Generation Methods ===
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        // Don't call super - we handle code generation entirely here

        // Populate template context with all standard fields
        TemplateUtils.populateAllContext(context, this);

        // Add WhileIteratorSubsystem-specific context
        context.put("maxIterations", getMaxIterationsValue());
        context.put("showIterationNumber", shouldShowIterationNumber());
        context.put("resetState", resetState.getData().getInitValue());
        context.put("currentIteration", currentIteration);
        context.put("containedBlocks", getInnerSystem().getBlocks());
        context.put("containedLines", getInnerSystem().getLines());
        context.put("inBlockList", getInBlockList());
        context.put("outBlockList", getOutBlockList());
        context.put("boundaryLines", getBoundaryLines());
        context.put("pureInternalLines", getPureInternalLines());

        // Generate while loop wrapper code using template
        String outputCode = TemplateManager.renderTemplate("c/subsystem/WhileIteratorSubsystem/output.vm", context);
        code.addOutputCode(outputCode);

        // Generate code for all contained blocks (except In/Out which are handled by boundary)
        for (com.ncslab.block.Block block : getInnerSystem().getBlocks()) {
            try {
                if (!(block instanceof In || block instanceof Out)) {
                    block.generateOutputCodeC(code);
                }
            } catch (Exception e) {
                System.err.println("Error generating code for block " + block.getBlockName() +
                                 " in WhileIteratorSubsystem: " + e.getMessage());
            }
        }
    }

    @Override
    public void updateDimension() throws MatDimException {
        // Update dimensions for all internal blocks
        super.updateDimension();

        // Update iteration number output port dimension if enabled
        if (shouldShowIterationNumber() && !outputPortList.isEmpty()) {
            OutputPort iterationPort = outputPortList.get(outputPortList.size() - 1);
            iterationPort.setWidth(1);
            iterationPort.setHeight(1);
            if (iterationPort.getOutputSignalC() != null) {
                iterationPort.getOutputSignalC().setWidth(1);
                iterationPort.getOutputSignalC().setHeight(1);
            }
        }
    }

    // === Static Factory Method (DTO-Based) ===
    /**
     * Creates a WhileIteratorSubsystem with default parameters.
     *
     * @param name Block name
     * @param path Block path
     * @param model Parent model
     * @return WhileIteratorSubsystem instance
     */
    public static WhileIteratorSubsystem create(String name, String path, NCSLabModel model) {
        return create(name, path, 10, "off", "each time step", model);
    }

    /**
     * Creates a WhileIteratorSubsystem with full parameters (DTO-based approach).
     *
     * @param name Block name
     * @param path Block path
     * @param maxIterations Maximum iterations (1-10000)
     * @param showIterationNumber "on" or "off"
     * @param resetState "each time step" or "at start of simulation"
     * @param model Parent model
     * @return WhileIteratorSubsystem instance
     */
    public static WhileIteratorSubsystem create(String name, String path, int maxIterations,
                                               String showIterationNumber, String resetState,
                                               NCSLabModel model) {
        // Build DTO using constructor with individual parameters
        WhileIteratorSubsystemDto dto = new WhileIteratorSubsystemDto(
            name,
            path,
            com.ncslab.dto.common.TypedParameter.of(maxIterations),
            com.ncslab.dto.common.TypedParameter.of(showIterationNumber),
            com.ncslab.dto.common.TypedParameter.of(resetState),
            com.ncslab.dto.common.TypedParameter.of(-1.0) // Inherited sample time
        );

        // Set blockUUID
        dto.setBlockUUID("null");

        // Validate DTO
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid WhileIteratorSubsystem parameters: " +
                                             dto.getValidationErrors());
        }

        // Use DTO constructor
        return new WhileIteratorSubsystem(dto, model);
    }

    // === Override toString for debugging ===
    @Override
    public String toString() {
        return String.format("WhileIteratorSubsystem{name='%s', maxIterations=%d, currentIteration=%d, " +
                           "showIterationNumber=%s, resetState=%s}",
                           getBlockName(), getMaxIterationsValue(), currentIteration,
                           shouldShowIterationNumber(),
                           shouldResetEachTimeStep() ? "each time step" : "at start of simulation");
    }
}
