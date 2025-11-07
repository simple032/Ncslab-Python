package com.ncslab.block.continuous;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.FifoBufferExtended;
import lombok.Getter;
import com.ncslab.util.TemplateManager;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.continuous.TransportDelayDto;

import Jama.Matrix;

import com.ncslab.block.continuous.ContinuousBlock;
import com.ncslab.block.data.DataType;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.io.OutputSignal;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * TransportDelay block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - DelayTime: Delay time (scalar or matrix)
 * - InitialOutput: Initial output value before delay takes effect
 * - BufferSize: Size of the delay buffer for variable step solvers
 * - PadeOrder: Order of Pade approximation (for approximation methods)
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class TransportDelay extends ContinuousBlock {

    // === Internal Implementation ===
    private Matrix delayTimeMatrix;
    private FifoBufferExtended<Data> buffer;

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter delayTime;
    // Note: No @Getter on initialOutput to avoid conflict with ContinuousBlock.getInitialOutput()
    private final Parameter initialOutput;
    @Getter
    private final Parameter bufferSize;
    @Getter
    private final Parameter padeOrder;
    @Getter
    private final Parameter sampleTime;
    @Getter
    private final Parameter outDataType;
    @Getter
    private final Parameter saturateOnIntegerOverflow;

    // === Port References ===
    private OutputPort output;
    private InputPort input;

    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");

        // Parameter defaults for transport delay
        PARAMETER_DEFAULTS.put("DelayTime", "1");
        PARAMETER_DEFAULTS.put("InitialOutput", "0");
        PARAMETER_DEFAULTS.put("BufferSize", "1024");
        PARAMETER_DEFAULTS.put("PadeOrder", "0");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    // === Private Constructor with Typed Parameters ===
    private TransportDelay(Parameter delayTime, Parameter initialOutput, Parameter bufferSize,
                          Parameter padeOrder, Parameter sampleTime, Parameter outDataType,
                          Parameter saturateOnIntegerOverflow, String blockName, String blockPath,
                          String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(delayTime, initialOutput, bufferSize, sampleTime);

        // Assign parameters
        this.delayTime = Objects.requireNonNull(delayTime, "Delay time parameter cannot be null");
        this.initialOutput = Objects.requireNonNull(initialOutput, "Initial output parameter cannot be null");
        this.bufferSize = Objects.requireNonNull(bufferSize, "Buffer size parameter cannot be null");
        this.padeOrder = Objects.requireNonNull(padeOrder, "Pade order parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Cache delay time matrix
        this.delayTimeMatrix = delayTime.getMatrix();

        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public TransportDelay(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.delayTime = getParameterByName("DelayTime");
        this.initialOutput = getParameterByName("InitialOutput");
        this.bufferSize = getParameterByName("BufferSize");

        // Create missing SIMULINK parameters with defaults
        this.padeOrder = getParameterByName("PadeOrder");
        this.sampleTime = getParameterByName("SampleTime"); // 0 for continuous delay
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Add all parameters to parameter list

        // Cache delay time matrix for legacy compatibility
        if (this.delayTime != null) {
            this.delayTimeMatrix = this.delayTime.getMatrix();
        }

        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates TransportDelay block directly from BlockDto DTO
     */
    public TransportDelay(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.delayTime = getParameterByName("DelayTime");
        this.initialOutput = getParameterByName("InitialOutput");
        this.bufferSize = getParameterByName("BufferSize");
        this.padeOrder = getParameterByName("PadeOrder");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Cache delay time matrix
        if (this.delayTime != null) {
            this.delayTimeMatrix = this.delayTime.getMatrix();
        }

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }// === Static Factory Method for JSON Deserialization ===
    public static TransportDelay fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter delayTime = createDelayTimeFromJSON(paramValues, blockName);
            Parameter initialOutput = createInitialOutputFromJSON(paramValues, blockName);
            Parameter bufferSize = createBufferSizeFromJSON(paramValues, blockName);
            Parameter padeOrder = createPadeOrderFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            TransportDelay block = new TransportDelay(delayTime, initialOutput, bufferSize,
                                                     padeOrder, sampleTime, outDataType,
                                                     saturateParam, blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, delayTime, initialOutput, bufferSize,
                                     padeOrder, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create TransportDelay block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static TransportDelay create(String name, String path, double delayTime, double initialOutput, NCSLabModel model) {
        return create(name, path, delayTime, initialOutput, 1024, 0, 0.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a TransportDelay block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param delayTime Delay time (scalar or matrix)
     * @param initialOutput Initial output value before delay takes effect
     * @param bufferSize Size of the delay buffer for variable step solvers
     * @param padeOrder Order of Pade approximation (for approximation methods)
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return TransportDelay block instance
     */
    public static TransportDelay create(String name, String path, double delayTime, double initialOutput,
                                       int bufferSize, int padeOrder, double sampleTime, String outDataType,
                                       boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        TransportDelayDto dto = TransportDelayDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .delayTime(com.ncslab.dto.common.TypedParameter.of(delayTime))
            .initialOutput(com.ncslab.dto.common.TypedParameter.of(initialOutput))
            .bufferSize(com.ncslab.dto.common.TypedParameter.of(bufferSize))
            .padeOrder(com.ncslab.dto.common.TypedParameter.of(padeOrder))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow ? "on" : "off"))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid TransportDelay parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new TransportDelay(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter delayTime, Parameter initialOutput, Parameter bufferSize, Parameter sampleTime) {
        double delayTimeValue = delayTime.getDouble();
        if (delayTimeValue < 0.0 || delayTimeValue == Double.NaN || delayTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Delay time must be non-negative and finite");
        }

        int bufferSizeValue = (int) bufferSize.getDouble();
        if (bufferSizeValue <= 0) {
            throw new IllegalArgumentException("Buffer size must be positive");
        }

        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be >= 0 or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createDelayTimeFromJSON(JSONObject paramValues, String blockName) {
        String delayTimeValue = paramValues.optString("DelayTime", "1.0");
        return new Parameter(null, 1, "DelayTime", delayTimeValue);
    }

    private static Parameter createInitialOutputFromJSON(JSONObject paramValues, String blockName) {
        String initialOutputValue = paramValues.optString("InitialOutput", "0.0");
        return new Parameter(null, 2, "InitialOutput", initialOutputValue);
    }

    private static Parameter createBufferSizeFromJSON(JSONObject paramValues, String blockName) {
        String bufferSizeValue = paramValues.optString("BufferSize", "1024");
        return new Parameter(null, 3, "BufferSize", bufferSizeValue);
    }

    private static Parameter createPadeOrderFromJSON(JSONObject paramValues, String blockName) {
        String padeOrderValue = paramValues.optString("PadeOrder", "0");
        return new Parameter(null, 4, "PadeOrder", padeOrderValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "0");
        return new Parameter(null, 5, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 6, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 7, "SaturateOnIntegerOverflow", saturateValue);
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

    private static void setParameterBlockReference(TransportDelay block, Parameter... parameters) {
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
        identity.put("blockType", "TransportDelay");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Main input port
        input = new InputPort(this, 1);
        inputPortList.add(input);

        // Main output port (no feedthrough for transport delay)
        output = new OutputPort(this, 1, false);
        outputPortList.add(output);
    }

    /**
     * Check if the given solver is a fixed-step solver.
     * Made public for Velocity template access.
     * @param solver The solver name
     * @return true if fixed-step solver, false if variable-step
     */
    public boolean isFixedStepSolver(String solver) {
        return solver.equals("ode1") || solver.equals("ode2") || solver.equals("ode3") ||
            solver.equals("ode4") || solver.equals("ode5") || solver.equals("ode6");
    }

    /**
     * Calculate buffer length based on delay time.
     * Made public for Velocity template access.
     * @param delay The delay time
     * @return The required buffer length
     */
    public int calculateBufferLength(double delay) {
        // Ensure buffer has enough space with a generous safety margin
        return Math.max(20, (int)(delay / model.getConfig().getFixedStep()) + 5);
    }
    
    @Override
    protected String getBufferName() {
        return "buffer" + getBlockId();
    }

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);


		context.put("realDataType", DataType.REAL);
		context.put("matrixDataType", DataType.MATRIX);
		context.put("blockId", getBlockId());
		context.put("blockName", getBlockName());
		context.put("delayTime", delayTime);
		context.put("initialOutput", initialOutput);
		context.put("bufferSize", bufferSize);
		context.put("bufferName", getBufferName());

		// Fail fast - validate required input signal connection exists
		if (inputPortList == null || inputPortList.isEmpty()) {
			throw new BlockCreationException("TransportDelay block cannot generate init code: no input ports configured");
		}
		
		InputPort inputPort = inputPortList.get(0);
		if (inputPort == null || inputPort.getLinkedLine() == null || 
			inputPort.getLinkedLine().getLinkedOutputPort() == null) {
			throw new BlockCreationException("TransportDelay block cannot generate init code: input port not properly connected");
		}
		
		OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if (signal == null) {
			throw new BlockCreationException("TransportDelay block cannot generate init code: input signal is null");
		}
		
		context.put("inputSignal", signal);
		context.put("isFixedStepSolver", isFixedStepSolver(model.getConfig().getSolver()));

		if (isFixedStepSolver(model.getConfig().getSolver())) {
			int bufferLength = calculateBufferLength(delayTime.getDouble());
			context.put("bufferLength", bufferLength);

			// Create buffer lengths matrix for template
			java.util.List<java.util.List<Integer>> bufferLengths = new java.util.ArrayList<>();
			for (int i = 0; i < Math.max(1, delayTime.getHeight()); i++) {
				java.util.List<Integer> row = new java.util.ArrayList<>();
				for (int j = 0; j < Math.max(1, delayTime.getWidth()); j++) {
					if (delayTime.getDataType() == DataType.MATRIX) {
						row.add(calculateBufferLength(delayTimeMatrix.get(i, j)));
					} else {
						row.add(calculateBufferLength(delayTime.getDouble()));
					}
				}
				bufferLengths.add(row);
			}
			context.put("bufferLengths", bufferLengths);
		}

		String initCode = TemplateManager.renderTemplate("c/continuous/TransportDelay/init.vm", context);
		code.addInitCode(initCode);
	}

    public void generateArraysCodeC(CodeStructC code) {

        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("delayTime", delayTime);
        context.put("bufferName", getBufferName());

        // Fail fast - validate required input signal connection exists
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new BlockCreationException("TransportDelay block cannot generate arrays code: no input ports configured");
        }
        
        InputPort inputPort = inputPortList.get(0);
        if (inputPort == null || inputPort.getLinkedLine() == null || 
            inputPort.getLinkedLine().getLinkedOutputPort() == null) {
            throw new BlockCreationException("TransportDelay block cannot generate arrays code: input port not properly connected");
        }
        
        OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (signal == null) {
            throw new BlockCreationException("TransportDelay block cannot generate arrays code: input signal is null");
        }
        
        context.put("inputSignal", signal);
        context.put("isFixedStepSolver", isFixedStepSolver(model.getConfig().getSolver()));

        if (isFixedStepSolver(model.getConfig().getSolver())) {
            int bufferLength = calculateBufferLength(delayTime.getDouble());
            context.put("bufferLength", bufferLength);

            // Create buffer lengths matrix for template
            java.util.List<java.util.List<Integer>> bufferLengths = new java.util.ArrayList<>();
            for (int i = 0; i < Math.max(1, delayTime.getHeight()); i++) {
                java.util.List<Integer> row = new java.util.ArrayList<>();
                for (int j = 0; j < Math.max(1, delayTime.getWidth()); j++) {
                    if (delayTime.getDataType() == DataType.MATRIX) {
                        row.add(calculateBufferLength(delayTimeMatrix.get(i, j)));
                    } else {
                        row.add(calculateBufferLength(delayTime.getDouble()));
                    }
                }
                bufferLengths.add(row);
            }
            context.put("bufferLengths", bufferLengths);
        }

        String arraysCode = TemplateManager.renderTemplate("c/continuous/TransportDelay/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    /**
     * Generates derivative code for C code generation.
     * For Transport Delay, this function updates the delay buffer with new input values.
     *
     * @param code The CodeStructC object to add the derivative code to
     */
    public void generateDerivativeCodeC(CodeStructC code) {
        super.generateDerivativeCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        context.put("block", this);
        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("bufferSize", bufferSize);
        context.put("bufferName", getBufferName());

        // Pass delayTime as delaytime to match template expectations
        context.put("delaytime", delayTime);
        context.put("delayTime", delayTime);
        context.put("initialOutput", initialOutput);
        
        // Add solver type for template
        context.put("solverType", model.getConfig().getSolver());

        // Pass model for solver check
        context.put("model", model);
        context.put("isFixedStepSolver", isFixedStepSolver(model.getConfig().getSolver()));
        
        // Fail fast - validate required input signal connection exists
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new BlockCreationException("TransportDelay block cannot generate derivative code: no input ports configured");
        }
        
        InputPort inputPort = inputPortList.get(0);
        if (inputPort == null) {
            throw new BlockCreationException("TransportDelay block cannot generate derivative code: input port is null");
        }
        
        if (inputPort.getLinkedLine() == null) {
            throw new BlockCreationException("TransportDelay block cannot generate derivative code: input port not connected");
        }
        
        OutputPort linkedOutputPort = inputPort.getLinkedLine().getLinkedOutputPort();
        if (linkedOutputPort == null) {
            throw new BlockCreationException("TransportDelay block cannot generate derivative code: input signal source is null");
        }
        
        OutputSignal inputSignal = linkedOutputPort.getOutputSignalC();
        if (inputSignal == null) {
            throw new BlockCreationException("TransportDelay block cannot generate derivative code: input signal is null");
        }
        
        // Add validated input signal information to context
        context.put("inputSignal", inputSignal);
        context.put("inputSignalName", inputSignal.getName()); // Signal name already includes full block prefix // C variable name
        context.put("signalHeight", inputSignal.getHeight());
        context.put("signalWidth", inputSignal.getWidth());
        context.put("signalDataType", inputSignal.getDataType());
        
        // Add delay time value for template usage
        context.put("delayTimeValue", delayTime.getDouble());
        
        // Add the validated signal to context for template access
        context.put("signal", inputSignal);

        // For matrix delay times, provide proper matrix access
        if (delayTime.getDataType() == DataType.MATRIX && delayTimeMatrix != null) {
            context.put("delayTimeMatrix", delayTimeMatrix);
            context.put("delayTimeHeight", delayTime.getHeight());
            context.put("delayTimeWidth", delayTime.getWidth());
            
            // Create buffer length calculations for matrix delay times
            java.util.List<java.util.List<Integer>> bufferLengths = new java.util.ArrayList<>();
            for (int i = 0; i < delayTime.getHeight(); i++) {
                java.util.List<Integer> row = new java.util.ArrayList<>();
                for (int j = 0; j < delayTime.getWidth(); j++) {
                    row.add(calculateBufferLength(delayTimeMatrix.get(i, j)));
                }
                bufferLengths.add(row);
            }
            context.put("bufferLengths", bufferLengths);
            
            // Create a test object that provides matrix access for templates
            context.put("test", new Object() {
                public double get(int i, int j) {
                    if (delayTimeMatrix != null && i >= 0 && i < delayTimeMatrix.getRowDimension() && 
                        j >= 0 && j < delayTimeMatrix.getColumnDimension()) {
                        return delayTimeMatrix.get(i, j);
                    }
                    return delayTime.getDouble(); // Fallback to scalar value
                }
            });
        } else {
            context.put("delayTimeMatrix", null);
            context.put("delayTimeHeight", 1);
            context.put("delayTimeWidth", 1);
            context.put("bufferLengths", new java.util.ArrayList<>());
        }

        String derivativeCode = TemplateManager.renderTemplate("c/continuous/TransportDelay/derivative.vm", context);
        code.addDerivativeCode(derivativeCode);
    }

    /**
     * Generates output code for C code generation.
     * For Transport Delay, this function reads from the delay buffer to produce output.
     *
     * @param code The CodeStructC object to add the output code to
     */
    public void generateOutputCodeC(CodeStructC code) {

        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("delayTime", delayTime);
        context.put("initialOutput", initialOutput);
        context.put("bufferSize", bufferSize);
        context.put("bufferName", getBufferName());

        // Fail fast - validate required signal connections exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new BlockCreationException("TransportDelay block cannot generate output code: no input ports configured");
        }
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new BlockCreationException("TransportDelay block cannot generate output code: no output ports configured");
        }
        
        InputPort inputPort = inputPortList.get(0);
        if (inputPort == null || inputPort.getLinkedLine() == null || 
            inputPort.getLinkedLine().getLinkedOutputPort() == null) {
            throw new BlockCreationException("TransportDelay block cannot generate output code: input port not properly connected");
        }
        
        OutputPort outputPort = outputPortList.get(0);
        if (outputPort == null) {
            throw new BlockCreationException("TransportDelay block cannot generate output code: output port is null");
        }
        
        OutputSignal inputSignal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal outputSignal = outputPort.getOutputSignalC();
        
        if (inputSignal == null) {
            throw new BlockCreationException("TransportDelay block cannot generate output code: input signal is null");
        }
        if (outputSignal == null) {
            throw new BlockCreationException("TransportDelay block cannot generate output code: output signal is null");
        }
        
        context.put("inputSignal", inputSignal);
        context.put("outputSignal", outputSignal);
        context.put("isFixedStepSolver", isFixedStepSolver(model.getConfig().getSolver()));

        // For fixed step solver with matrix delay time, we need the delay time value
        if (isFixedStepSolver(model.getConfig().getSolver()) &&
            inputSignal.getDataType() == DataType.MATRIX &&
            delayTime.getDataType() == DataType.REAL) {
            context.put("delayTimeValue", delayTime.getDouble());
        }

        String outputCode = TemplateManager.renderTemplate("c/continuous/TransportDelay/output.vm", context);
        code.addOutputCode(outputCode);
    }

	public void updateDimension() throws MatDimException {
		// Fail fast - validate required ports and connections exist
		if (outputPortList == null || outputPortList.isEmpty()) {
			throw new MatDimException("TransportDelay block cannot update dimensions: no output ports configured");
		}
		if (inputPortList == null || inputPortList.isEmpty()) {
			throw new MatDimException("TransportDelay block cannot update dimensions: no input ports configured");
		}
		
		OutputPort out = outputPortList.get(0);
		InputPort in = inputPortList.get(0);
		
		if (out == null) {
			throw new MatDimException("TransportDelay block cannot update dimensions: output port is null");
		}
		if (in == null || in.getLinkedLine() == null || in.getLinkedLine().getLinkedOutputPort() == null) {
			throw new MatDimException("TransportDelay block cannot update dimensions: input port not properly connected");
		}
		
		OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if (signal == null) {
			throw new MatDimException("TransportDelay block cannot update dimensions: input signal is null");
		}
		switch (signal.getDataType()) {
		case REAL:
			switch (delayTime.getDataType()) {
			case REAL:
				out.setHeight(1);
				out.setWidth(1);
				out.getOutputSignalC().setHeight(1);
				out.getOutputSignalC().setWidth(1);
				out.getOutputSignalC().setDataType(DataType.REAL);
				break;
			case MATRIX:
				out.setHeight(delayTime.getHeight());
				out.setWidth(delayTime.getWidth());
				out.getOutputSignalC().setHeight(delayTime.getHeight());
				out.getOutputSignalC().setWidth(delayTime.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
				break;
			}
			break;
		case MATRIX:
			switch (delayTime.getDataType()) {
			case REAL:
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
				break;
			case MATRIX:
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
				break;
			}
			break;
		}
	}

	public void checkDimension() throws MatDimException {
		// Fail fast - validate required ports and connections exist
		if (inputPortList == null || inputPortList.isEmpty()) {
			throw new MatDimException("TransportDelay block cannot check dimensions: no input ports configured");
		}
		
		InputPort inputPort = inputPortList.get(0);
		if (inputPort == null || inputPort.getLinkedLine() == null || 
			inputPort.getLinkedLine().getLinkedOutputPort() == null) {
			throw new MatDimException("TransportDelay block cannot check dimensions: input port not properly connected");
		}
		
		OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if (signal == null) {
			throw new MatDimException("TransportDelay block cannot check dimensions: input signal is null");
		}
		switch (initialOutput.getDataType()) {
		case REAL:
			switch (signal.getDataType()) {
			case REAL:

				break;
			case MATRIX:
				if (initialOutput.getHeight() != delayTime.getHeight()
						|| initialOutput.getWidth() != delayTime.getWidth()) {
					MatDimException e = new MatDimException(
							"Block " + this.blockName + " input dimensions don't match!");
					throw (e);
				}
				break;
			}
			break;
		case MATRIX:
			switch (signal.getDataType()) {
			case REAL:
				if (initialOutput.getHeight() != delayTime.getHeight()
						|| initialOutput.getWidth() != delayTime.getWidth()) {
					MatDimException e = new MatDimException(
							"Block " + this.blockName + " input dimensions don't match1!");
					throw (e);
				}
				break;
			case MATRIX:
				if (signal.getHeight() != delayTime.getHeight() || signal.getWidth() != delayTime.getWidth()
						|| signal.getHeight() != initialOutput.getHeight()
						|| signal.getWidth() != initialOutput.getWidth()
						|| initialOutput.getHeight() != delayTime.getHeight()
						|| initialOutput.getWidth() != delayTime.getWidth()) {
					MatDimException e = new MatDimException(
							"Block " + this.blockName + " input dimensions don't match!");
					throw (e);
				}
				break;
			}
			break;
		}

	}

    @Override
    public void calculateInit() {
        // Fail fast - validate required ports exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new IllegalStateException("TransportDelay block cannot initialize: no output ports configured");
        }
        
        OutputPort out = outputPortList.get(0);
        if (out == null) {
            throw new IllegalStateException("TransportDelay block cannot initialize: output port is null");
        }
        if (bufferSize == null || bufferSize.getData() == null) {
            throw new IllegalStateException("TransportDelay block cannot initialize: buffer size parameter not configured");
        }
        
        buffer = new FifoBufferExtended<>(bufferSize.getData().getIntValue());
        buffer.setInterpolationStrategy(new FifoBufferExtended.LinearDataInterpolationStrategy());
        out.setData(new Data(out.getHeight(), out.getWidth()));
    }

    @Override
    public void calculateOutput(double t) {
        // Fail fast - validate required components exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new IllegalStateException("TransportDelay block cannot calculate output: no output ports configured");
        }
        if (buffer == null) {
            throw new IllegalStateException("TransportDelay block cannot calculate output: buffer not initialized");
        }
        if (delayTime == null) {
            throw new IllegalStateException("TransportDelay block cannot calculate output: delay time parameter not configured");
        }
        
        OutputPort out = outputPortList.get(0);
        if (out == null) {
            throw new IllegalStateException("TransportDelay block cannot calculate output: output port is null");
        }
        
        Data linearResult = buffer.getValueByDelay(t, delayTime.getDouble());
        if (linearResult != null) {
            out.setData(linearResult);
        }
    }

    @Override
    public void calculateDerivative(double t) {
        // Fail fast - validate required components exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new IllegalStateException("TransportDelay block cannot calculate derivative: no input ports configured");
        }
        if (buffer == null) {
            throw new IllegalStateException("TransportDelay block cannot calculate derivative: buffer not initialized");
        }
        
        InputPort in = inputPortList.get(0);
        if (in == null) {
            throw new IllegalStateException("TransportDelay block cannot calculate derivative: input port is null");
        }
        if (in.getData() == null) {
            throw new IllegalStateException("TransportDelay block cannot calculate derivative: input data is null");
        }
        
        buffer.add(t, in.getData());
    }
}
