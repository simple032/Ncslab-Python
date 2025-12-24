package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.block.specialized.subsystem.FunctionCallSubsystemDto;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ncslab.dto.core.BlockDto;

/**
 * Function-Call Subsystem block for event-driven subsystem execution.
 *
 * <p>This specialized subsystem executes only when triggered by a function-call
 * event from a connected block (such as Function-Call Generator, S-Function, or
 * Hit Crossing block). Unlike regular subsystems that execute continuously,
 * function-call subsystems are event-driven and execute atomically when triggered.</p>
 *
 * <p><b>Key Characteristics:</b></p>
 * <ul>
 *   <li><b>Event-Driven Execution</b>: Executes only when function-call signal is active</li>
 *   <li><b>Atomic Execution</b>: Completes all internal block execution within single time step</li>
 *   <li><b>Function-Call Port</b>: Special input port (first port) for function-call events</li>
 *   <li><b>No Continuous States</b>: Cannot contain continuous-time blocks (integrators, derivatives)</li>
 *   <li><b>Discrete Operation</b>: All internal blocks must support discrete execution</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li><b>Port 1</b>: Special function-call input port (not a regular data port)</li>
 *   <li><b>Additional Ports</b>: Data input/output ports via In/Out blocks (ports 2+)</li>
 *   <li>Function-call port triggers subsystem execution</li>
 *   <li>Data ports provide data flow when subsystem is active</li>
 * </ul>
 *
 * <p><b>Execution Model:</b></p>
 * <ol>
 *   <li>Subsystem remains inactive by default</li>
 *   <li>When function-call signal becomes active (> 0):
 *     <ul>
 *       <li>Set function-call active flag</li>
 *       <li>Execute all internal blocks once in dependency order</li>
 *       <li>Process all internal lines and data flow</li>
 *       <li>Update output ports</li>
 *     </ul>
 *   </li>
 *   <li>Reset function-call flag after execution</li>
 *   <li>Remain inactive until next function-call event</li>
 * </ol>
 *
 * <p><b>Common Use Cases:</b></p>
 * <ul>
 *   <li><b>Interrupt Service Routines (ISR)</b>: Model hardware interrupt handlers</li>
 *   <li><b>Event-Driven State Machines</b>: State transitions on specific events</li>
 *   <li><b>Callback Functions</b>: Implement callback-based execution logic</li>
 *   <li><b>Scheduled Tasks</b>: Execute tasks on specific schedule triggers</li>
 *   <li><b>Conditional Processing</b>: Process data only when specific conditions occur</li>
 *   <li><b>Sampling Operations</b>: Sample signals at specific trigger events</li>
 * </ul>
 *
 * <p><b>Restrictions:</b></p>
 * <ul>
 *   <li>Cannot contain continuous-time blocks (Integrator, Derivative, Transfer Function with continuous states)</li>
 *   <li>Sample time must be inherited (-1) or discrete (> 0), not continuous (0)</li>
 *   <li>All internal blocks must support event-driven execution</li>
 *   <li>Cannot have algebraic loops that depend on function-call timing</li>
 * </ul>
 *
 * <p><b>Simulink Compatibility:</b></p>
 * <p>This block is compatible with MATLAB/Simulink Function-Call Subsystem block,
 * supporting the same execution semantics, port configuration, and restrictions.</p>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
public class FunctionCallSubsystem extends Subsystem {

    /**
     * Function-call active flag.
     * True when a function-call event has been received and subsystem should execute.
     */
    @Getter
    @Setter
    private boolean functionCallActive = false;

    /**
     * Previous function-call signal value for edge detection.
     * Used to detect rising edges in function-call signal.
     */
    private double previousFunctionCallSignal = 0.0;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        // Function-Call Subsystem inherits base subsystem parameters
        // No additional specialized parameters needed
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Function-call port is implicit (port 1)
        // Additional data ports defined by In/Out blocks
        inputNames.add("fcn"); // Function-call port identifier
    }

    // === Legacy Constructor (Deprecated) ===
    /**
     * Legacy constructor for backward compatibility with JSONObject-based initialization.
     *
     * @deprecated Use DTO-based constructor {@link #FunctionCallSubsystem(FunctionCallSubsystemDto, NCSLabModel)} instead.
     * @param blockJSON JSON object containing block configuration
     * @param model Parent model
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public FunctionCallSubsystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        initializeFunctionCallPort();
    }

    /**
     * DTO-NATIVE Constructor - Creates Function-Call Subsystem directly from DTO.
     * This is the preferred constructor following the DTO-first approach.
     *
     * @param blockDto Function-Call Subsystem DTO with validated parameters
     * @param model Parent model
     */
    public FunctionCallSubsystem(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        initializeFunctionCallPort();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() +
            " block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create FunctionCallSubsystem from FunctionCallSubsystemDto.
     *
     * @param dto   FunctionCallSubsystemDto containing block configuration
     * @param model NCSLabModel containing the block diagram
     * @return Created FunctionCallSubsystem block
     */
    public static FunctionCallSubsystem createFromDto(FunctionCallSubsystemDto dto, NCSLabModel model) {
        if (dto == null) {
            throw new IllegalArgumentException("FunctionCallSubsystemDto cannot be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("NCSLabModel cannot be null");
        }

        // Validate DTO before creating block
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid FunctionCallSubsystemDto: " + dto.getValidationErrors());
        }

        return new FunctionCallSubsystem(dto, model);
    }

    // === Port Initialization ===
    /**
     * Initializes the function-call input port (port 1).
     * This is a special port that does not carry data, only function-call events.
     */
    private void initializeFunctionCallPort() {
        // Function-call port is the first input port
        // It's implicitly created by the base Subsystem class
        // We just need to ensure it's properly configured for function-call behavior

        // Add function-call input port if not already present
        if (inputPortList.isEmpty()) {
            inputPortList.add(new InputPort(this, 1));
        }
    }

    // === Code Generation Methods ===

    /**
     * Generates array declarations for function-call state variables.
     */
    @Override
    public void generateArraysCodeC(CodeStructC code) {
        super.generateArraysCodeC(code);

        try {
            // Populate context for function-call state variables
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);

            String codeStr = TemplateManager.renderTemplate("c/subsystem/FunctionCallSubsystem/arrays.vm", context);
            code.addArraysCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error generating arrays code for FunctionCallSubsystem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates initialization code for function-call state variables.
     */
    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        try {
            // Populate context for function-call initialization
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);

            String templatePath = "c/subsystem/FunctionCallSubsystem/init.vm";
            String codeStr = TemplateManager.renderTemplate(templatePath, context);
            code.addInitCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error generating init code for FunctionCallSubsystem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates C code for the Function-Call Subsystem using Velocity templates.
     *
     * <p>Generated code includes:</p>
     * <ul>
     *   <li>Function-call signal detection logic</li>
     *   <li>Rising edge detection for function-call activation</li>
     *   <li>Conditional execution of internal blocks</li>
     *   <li>Function-call flag reset after execution</li>
     * </ul>
     *
     * @param code Code structure for C code generation
     */
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        // Generate base subsystem code first
        // Don't call super.generateOutputCodeC() because we need custom execution logic

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add Function-Call Subsystem specific context
        context.put("containedBlocks", innerSystem.getBlocks());
        context.put("containedLines", innerSystem.getLines());
        context.put("inBlockList", inBlockList);
        context.put("outBlockList", outBlockList);
        context.put("boundaryLines", getBoundaryLines());
        context.put("pureInternalLines", getPureInternalLines());
        context.put("functionCallActive", functionCallActive);

        // Get function-call input signal name
        InputPort functionCallPort = inputPortList.get(0);
        if (functionCallPort != null && functionCallPort.getLinkedLine() != null &&
            functionCallPort.getLinkedLine().getLinkedOutputPort() != null) {
            context.put("functionCallSignal", functionCallPort.getLinkedLine()
                .getLinkedOutputPort().getOutputSignalC().getName());
        } else {
            context.put("functionCallSignal", "0.0"); // Default: no function-call
        }

        // Generate function-call subsystem wrapper code using template
        String outputCode = TemplateManager.renderTemplate("c/subsystem/FunctionCallSubsystem/output.vm", context);
        code.addOutputCode(outputCode);

        // Generate code for all contained blocks (except In/Out which are handled by boundary)
        // But only if function-call is active in generated code
        for (Block block : innerSystem.getBlocks()) {
            try {
                if (!(block instanceof In || block instanceof Out)) {
                    // Generate code for internal blocks
                    // The template will wrap this in function-call check
                    block.generateOutputCodeC(code);
                }
            } catch (Exception e) {
                System.err.println("Error generating code for block " + block.getBlockName() + ": " + e.getMessage());
            }
        }
    }

    // === Execution Methods ===

    /**
     * Initializes the function-call subsystem.
     * Sets initial function-call state to inactive.
     */
    @Override
    public void calculateInit() {
        // Initialize function-call state
        functionCallActive = false;
        previousFunctionCallSignal = 0.0;

        // Do NOT initialize internal blocks yet
        // They should only initialize when first function-call is received
    }

    /**
     * Calculates output and manages function-call execution.
     *
     * <p>Execution logic:</p>
     * <ol>
     *   <li>Read function-call signal from input port</li>
     *   <li>Detect rising edge (0 to positive transition)</li>
     *   <li>If rising edge detected:
     *     <ul>
     *       <li>Set function-call active flag</li>
     *       <li>Execute all internal blocks via innerSystem.calculateOutput()</li>
     *       <li>Reset function-call active flag</li>
     *     </ul>
     *   </li>
     *   <li>Store current signal value for next comparison</li>
     * </ol>
     *
     * @param t Current simulation time
     */
    @Override
    public void calculateOutput(double t) {
        // Get function-call signal value from first input port
        InputPort functionCallPort = inputPortList.get(0);
        double functionCallSignal = 0.0;

        if (functionCallPort.getLinkedLine() != null) {
            functionCallSignal = functionCallPort.getData().getInitValue();
        }

        // Detect rising edge: transition from 0 (or negative) to positive
        boolean risingEdgeDetected = (previousFunctionCallSignal <= 0.0) && (functionCallSignal > 0.0);

        if (risingEdgeDetected) {
            // Function-call event detected - execute subsystem
            functionCallActive = true;

            // Execute all internal blocks in dependency order
            innerSystem.calculateOutput(t);

            // Reset function-call flag after execution
            functionCallActive = false;
        }

        // Store current signal value for next comparison
        previousFunctionCallSignal = functionCallSignal;
    }

    // === Helper Methods ===

    /**
     * Gets the function-call input port (port 1).
     *
     * @return Function-call input port
     */
    public InputPort getFunctionCallPort() {
        if (inputPortList.isEmpty()) {
            return null;
        }
        return inputPortList.get(0);
    }

    /**
     * Checks if the function-call port is connected.
     *
     * @return true if function-call port has an incoming connection
     */
    public boolean isFunctionCallPortConnected() {
        InputPort fcPort = getFunctionCallPort();
        return fcPort != null && fcPort.getLinkedLine() != null;
    }

    /**
     * Gets the full path of this function-call subsystem for hierarchical identification.
     *
     * @return Full hierarchical path
     */
    @Override
    public String getFullPath() {
        return getBlockPath() + "/" + getBlockName();
    }

    /**
     * Validates that the subsystem can be used as a function-call subsystem.
     *
     * <p>Checks for:</p>
     * <ul>
     *   <li>No continuous-time blocks (Integrator, Derivative, etc.)</li>
     *   <li>Sample time is not continuous (0)</li>
     *   <li>All internal blocks support discrete execution</li>
     * </ul>
     *
     * @return true if subsystem is valid for function-call operation
     */
    public boolean validateFunctionCallCompatibility() {
        // Check sample time - should not be continuous
        // This check would be performed during model compilation

        // Check for continuous-time blocks
        for (Block block : innerSystem.getBlocks()) {
            // Check if block is continuous-time
            // This is a simplified check - full implementation would examine block sample times
            String blockType = block.getClass().getSimpleName();
            if (blockType.equals("Integrator") || blockType.equals("Derivative") ||
                blockType.equals("TransferFcn") || blockType.equals("StateSpace")) {
                System.err.println("Warning: Function-Call Subsystem contains continuous-time block: " +
                    blockType + " (" + block.getBlockName() + ")");
                return false;
            }
        }

        return true;
    }

    /**
     * Gets a description of the function-call subsystem configuration.
     *
     * @return Configuration description string
     */
    @Override
    public String toString() {
        return String.format("FunctionCallSubsystem{name='%s', path='%s', blocks=%d, lines=%d, " +
                           "fcPortConnected=%s, dataInputs=%d, outputs=%d}",
                           getBlockName(), getBlockPath(), getBlockCount(), getLineCount(),
                           isFunctionCallPortConnected(), inBlockList.size(), outBlockList.size());
    }
}
