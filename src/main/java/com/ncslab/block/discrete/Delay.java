package com.ncslab.block.discrete;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import org.json.JSONObject;
import Jama.Matrix;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discrete.DelayDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;

/**
 * Delay block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * Implements a discrete-time delay (z^-n) by buffering input samples and outputting
 * them after a specified number of time steps. Essential for discrete control systems.
 *
 * SIMULINK Parameters:
 * - DelayLength: Number of samples to delay (n in z^-n)
 * - InitialCondition: Initial condition for the delay buffer
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * @author NCSLab Team
 * @version 2025
 */
public class Delay extends DiscreteBlock {

    // === Internal State ===
    /** Delay buffer storing historical input samples */
    private List<Data> buffer;
    
    // === SIMULINK-Compatible Parameters ===
    /** Delay length parameter (number of samples) */
    private final Parameter delayLength;
    
    /** Initial condition parameter for delay buffer */
    private final Parameter initialCondition;
    
    /** Sample time parameter */
    private final Parameter sampleTimeParam;
    
    /** Output data type specification parameter */
    private final Parameter outDataType;
    
    /** Integer overflow handling parameter */
    private final Parameter saturateOnIntegerOverflow;

    // === Port References ===
    private OutputPort output;
    private InputPort input;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("DelayLength", "1");
        PARAMETER_DEFAULTS.put("InitialCondition", "0");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // SIMULINK parameter names

        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
        
        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);
        
        // Output port defaults (delay has no feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", false);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }
    // === Private Constructor with Typed Parameters ===
    private Delay(Parameter delayLength, Parameter initialCondition, Parameter sampleTime,
                 Parameter outDataType, Parameter saturateOnIntegerOverflow,
                 String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(delayLength, sampleTime);

        // Assign parameters
        this.delayLength = Objects.requireNonNull(delayLength, "Delay length parameter cannot be null");
        this.initialCondition = Objects.requireNonNull(initialCondition, "Initial condition parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.delayLength);
        parameterList.add(this.initialCondition);
        parameterList.add(this.sampleTimeParam);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);
        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        // Port initialization is now handled by the centralized parseInputOutputPorts() method in parent constructor
        
        // Call post-construction initialization to ensure ports are properly set up
        postConstructionInitialization();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Delay(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.initialCondition = getParameterByName("InitialCondition");
        this.delayLength = getParameterByName("DelayLength");

        if(delayLength.getData().getDataType()==DataType.REAL && delayLength.getData().getInitValue() == 0.0){
            feedthrough = true;
        }

        // Create missing SIMULINK parameters with defaults
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        // Set discrete sample time
        setSampleTime(sampleTimeParam);

        // Initialize ports
        // Port initialization is now handled by the centralized parseInputOutputPorts() method in parent constructor
        
        // Call post-construction initialization to ensure ports are properly set up
        postConstructionInitialization();
    }    /**
     * DTO-NATIVE Constructor - Creates Delay block directly from DelayDto DTO
     */
    public Delay(DelayDto delayDto, NCSLabModel model) {
        super(delayDto, model);

        // Initialize final parameters from DelayDto
        this.delayLength = getParameterByName("DelayLength");
        this.initialCondition = getParameterByName("InitialCondition");
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Set discrete sample time
        setSampleTime(this.sampleTimeParam);

        // Initialize ports
        // Port initialization is now handled by the centralized parseInputOutputPorts() method in parent constructor
        
        // Call post-construction initialization to ensure ports are properly set up
        postConstructionInitialization();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + delayDto.getBlockName());
    }

    
    // === Static Factory Method for JSON Deserialization ===
    public static Delay fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter delayLength = createDelayLengthFromJSON(paramValues, blockName);
            Parameter initialCondition = createInitialConditionFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            Delay block = new Delay(delayLength, initialCondition, sampleTime, outDataType, saturateParam,
                                  blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, delayLength, initialCondition, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Delay block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Delay create(String name, String path, int delayLength, double initialCondition, double sampleTime, NCSLabModel model) {
        return create(name, path, delayLength, initialCondition, sampleTime, "Inherit: Same as input", false, model);
    }

    /**
     * Create a Delay block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param delayLength Number of samples to delay (n in z^-n)
     * @param initialCondition Initial condition for delay buffer
     * @param sampleTime Sample time for discrete operation
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return Delay block instance
     */
    public static Delay create(String name, String path, int delayLength, double initialCondition, double sampleTime,
                              String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        DelayDto dto = DelayDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .delayLength(com.ncslab.dto.common.TypedParameter.of(delayLength))
            .initialCondition(com.ncslab.dto.common.TypedParameter.of(initialCondition))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Delay parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Delay(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter delayLength, Parameter sampleTime) {
        int delayLengthValue = (int) delayLength.getDouble();
        if (delayLengthValue <= 0) {
            throw new IllegalArgumentException("Delay length must be positive");
        }

        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue <= 0.0 || sampleTimeValue == Double.NaN || sampleTimeValue == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Sample time must be positive and finite");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createDelayLengthFromJSON(JSONObject paramValues, String blockName) {
        String delayLengthValue = paramValues.optString("DelayLength", "1");
        return new Parameter(null, 1, "DelayLength", delayLengthValue);
    }

    private static Parameter createInitialConditionFromJSON(JSONObject paramValues, String blockName) {
        String initialConditionValue = paramValues.optString("InitialCondition", "0.0");
        return new Parameter(null, 2, "InitialCondition", initialConditionValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "1.0");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 4, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateValue);
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

    private static void setParameterBlockReference(Delay block, Parameter... parameters) {
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
        identity.put("blockType", "Delay");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    // === Port Initialization ===
    protected void postConstructionInitialization() {
        // Assign ports from centrally-created defaults
        if (inputPortList != null && !inputPortList.isEmpty()) {
            input = inputPortList.get(0);
        }
        if (outputPortList != null && !outputPortList.isEmpty()) {
            output = outputPortList.get(0);
        }

        // For standalone delay blocks (created for testing), initialize ports if they don't exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            inputPortList = new ArrayList<>();
            input = new InputPort(this, 1);
            inputPortList.add(input);
        }
        if (outputPortList == null || outputPortList.isEmpty()) {
            outputPortList = new ArrayList<>();
            output = new OutputPort(this, 1, feedthrough); // No feedthrough for delay
            outputPortList.add(output);
        }

        // CRITICAL FIX: Set initial output dimensions from IC if it's a matrix
        // This must happen BEFORE updateBlock() is called, so the OutputSignal
        // gets created with the correct dimensions
        System.out.println("DEBUG Delay " + blockName + ": postConstructionInitialization - checking IC");
        System.out.println("  initialCondition = " + initialCondition);
        System.out.println("  output = " + output);

        if (initialCondition != null && output != null) {
            Data icData = initialCondition.getData();
            System.out.println("  IC dataType = " + icData.getDataType());

            if (icData.getDataType() == DataType.MATRIX) {
                Matrix icMatrix = icData.getMatrix();
                int icHeight = icMatrix.getRowDimension();
                int icWidth = icMatrix.getColumnDimension();

                System.out.println("  Setting initial output dimensions from IC: [" + icHeight + "×" + icWidth + "]");

                output.setWidth(icWidth);
                output.setHeight(icHeight);
            }
        }
    }

    @Override
    public void updateDimension() throws MatDimException {
        super.updateDimension();

        // SIMULINK-compatible dimension feedthrough: Output dimensions = Input dimensions
        // IC expansion happens later in expandICToMatchDimensions() after dimensions converge

        if (inputPortList == null || inputPortList.isEmpty() || outputPortList == null || outputPortList.isEmpty()) {
            return;
        }

        InputPort inputPort = inputPortList.get(0);
        OutputPort outputPort = outputPortList.get(0);
        OutputSignal outputSignal = outputPort.getOutputSignalC();

        System.out.println("DEBUG Delay " + blockName + ": updateDimension called");

        // Get input dimensions
        if (inputPort.getLinkedLine() != null) {
            OutputSignal inputSignal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            if (inputSignal != null) {
                int inputWidth = inputSignal.getWidth();
                int inputHeight = inputSignal.getHeight();
                DataType inputDataType = inputSignal.getDataType();

                System.out.println("  Input signal: [" + inputHeight + "×" + inputWidth + "] type=" + inputDataType);

                // Check if input has valid dimensions (not default [1×1] from uninitialized feedback loop)
                boolean hasValidInput = !(inputWidth == 1 && inputHeight == 1 && inputDataType == DataType.REAL);

                if (hasValidInput) {
                    // Dimension feedthrough: output = input
                    // CRITICAL: Must set dimensions on BOTH outputPort AND outputSignalC
                    // because other blocks read from outputSignalC, not outputPort!
                    outputPort.setWidth(inputWidth);
                    outputPort.setHeight(inputHeight);

                    outputSignal.setWidth(inputWidth);
                    outputSignal.setHeight(inputHeight);
                    outputSignal.setDataType(inputDataType);
                    System.out.println("  Set output from input: [" + inputHeight + "×" + inputWidth + "]");
                } else if (initialCondition != null) {
                    // No valid input (feedback loop first iteration) - use IC dimensions if available
                    Data icData = initialCondition.getData();
                    System.out.println("  IC dataType: " + icData.getDataType());
                    if (icData.getDataType() == DataType.MATRIX) {
                        // Matrix IC - use its dimensions
                        Matrix icMatrix = icData.getMatrix();
                        int icHeight = icMatrix.getRowDimension();
                        int icWidth = icMatrix.getColumnDimension();

                        System.out.println("  IC matrix dimensions: [" + icHeight + "×" + icWidth + "]");

                        outputPort.setWidth(icWidth);
                        outputPort.setHeight(icHeight);

                        outputSignal.setWidth(icWidth);
                        outputSignal.setHeight(icHeight);
                        outputSignal.setDataType(DataType.MATRIX);
                        System.out.println("  Set output from IC: [" + icHeight + "×" + icWidth + "]");
                    } else {
                        System.out.println("  IC is scalar, keeping default [1×1]");
                    }
                    // else: scalar IC - keep default [1×1] dimensions
                }
            }
        } else if (initialCondition != null) {
            // Input not connected yet - use IC dimensions if available
            Data icData = initialCondition.getData();
            System.out.println("  Input not connected, using IC");
            System.out.println("  IC dataType: " + icData.getDataType());
            if (icData.getDataType() == DataType.MATRIX) {
                // Matrix IC - use its dimensions
                Matrix icMatrix = icData.getMatrix();
                int icHeight = icMatrix.getRowDimension();
                int icWidth = icMatrix.getColumnDimension();

                System.out.println("  IC matrix dimensions: [" + icHeight + "×" + icWidth + "]");

                outputPort.setWidth(icWidth);
                outputPort.setHeight(icHeight);

                outputSignal.setWidth(icWidth);
                outputSignal.setHeight(icHeight);
                outputSignal.setDataType(DataType.MATRIX);
                System.out.println("  Set output from IC (unconnected): [" + icHeight + "×" + icWidth + "]");
            } else {
                System.out.println("  IC is scalar, keeping default [1×1]");
            }
        }
    }

    /**
     * Expand IC to match final dimensions after dimension propagation converges
     * SIMULINK-compatible: Scalar IC="0" expands to zero matrix matching output dimensions
     */
    public void expandICToMatchDimensions() {
        if (initialCondition == null || outputPortList == null || outputPortList.isEmpty()) {
            return;
        }

        OutputPort outputPort = outputPortList.get(0);
        int outputWidth = outputPort.getWidth();
        int outputHeight = outputPort.getHeight();

        Data icData = initialCondition.getData();

        // SIMULINK SCALAR ZERO EXPANSION (done AFTER dimension propagation)
        if (icData.getDataType() == DataType.REAL && (outputWidth > 1 || outputHeight > 1)) {
            double icValue = icData.getInitValue();
            if (Math.abs(icValue) < 1e-10) { // Scalar zero
                Matrix zeroMatrix = new Matrix(outputHeight, outputWidth);
                icData.setMatrix(zeroMatrix);

                System.out.println(blockName + ": Expanded IC=0 to zero matrix [" +
                                 outputHeight + "×" + outputWidth + "] to match output dimensions");
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // Delay block supports both scalar and matrix inputs
        // Validate IC matrix dimensions if IC is a matrix
        // SIMULINK behavior for IC dimension matching:
        // 1. Scalar input [1×1]: IC can be any size, uses IC(0,0)
        // 2. Column vector input [n×1]: IC can be [n×1] (exact match) or [n×m] (uses first column)
        // 3. Row vector input [1×n]: IC can be [1×n] (exact match) or [m×n] (uses first row)
        // 4. Matrix input [m×n]: IC must be [m×n] (exact match)
        InputPort inputPort = inputPortList.get(0);
        if (inputPort == null || inputPort.getLinkedLine() == null) {
            return;
        }
        OutputSignal inputSignal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (inputSignal == null) {
            return;
        }
        if (initialCondition != null && initialCondition.getDataType() != DataType.REAL) {
            Matrix icMatrix = initialCondition.getMatrix();
            if (icMatrix != null) {
                int inputWidth = inputSignal.getWidth();
                int inputHeight = inputSignal.getHeight();
                int icHeight = icMatrix.getRowDimension();
                int icWidth = icMatrix.getColumnDimension();

                boolean isScalarInput = (inputWidth == 1 && inputHeight == 1);
                boolean isColumnVector = (inputWidth == 1 && inputHeight > 1);
                boolean isRowVector = (inputWidth > 1 && inputHeight == 1);

                // Validate based on input type
                if (isScalarInput) {
                    if(icWidth != this.delayLength.getData().getIntValue()+1){
                        throw new MatDimException(
                            String.format("Delay block '%s': IC matrix width [%d] does not match input vector length [%d]",
                                blockName, icWidth, this.delayLength.getData().getIntValue()));
                    }
                }
                else if (isColumnVector) {
                    // Column vector: IC must have matching height (width can differ, uses first column)
                    if (icHeight != inputHeight) {
                        throw new MatDimException(
                            String.format("Delay block '%s': IC matrix height [%d] does not match input vector length [%d]",
                                blockName, icHeight, inputHeight));
                    }
                }
                else if (isRowVector) {
                    // Row vector input [1×n]: IC can be [1×n] (exact match) or [m×n] (uses first row)
                    if (icWidth != inputWidth) {
                        throw new MatDimException(
                            String.format("Delay block '%s': IC matrix width [%d] does not match input row vector length [%d]",
                                blockName, icWidth, inputWidth));
                    }
                }
                else {
                    // Matrix input: IC dimensions must match exactly, or IC can be scalar and will expand
                    if (icHeight != inputHeight || icWidth != inputWidth) {
                        // Allow scalar IC with any input dimensions (scalar expansion)
                        if (!(icHeight == 1 && icWidth == 1)) {
                            throw new MatDimException(
                                String.format("Delay block '%s': IC matrix dimensions [%d×%d] do not match input dimensions [%d×%d]",
                                    blockName, icHeight, icWidth, inputHeight, inputWidth));
                        }
                    }
                }
            }
        }
    }

    // Define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Fail fast - validate required connections exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new BlockCreationException("Delay block requires input port for code generation");
        }

        InputPort inputPort = inputPortList.get(0);
        if (inputPort == null) {
            throw new BlockCreationException("Delay block input port cannot be null");
        }

        if (inputPort.getLinkedLine() == null || inputPort.getLinkedLine().getLinkedOutputPort() == null) {
            throw new BlockCreationException("Delay block requires valid input signal connection for code generation");
        }

        OutputSignal signal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (signal == null) {
            throw new BlockCreationException("Delay block requires valid output signal for code generation");
        }

        // Fail fast - validate delay length parameter exists
        if (delayLength == null || delayLength.getData() == null) {
            throw new BlockCreationException("Delay block requires valid delay length parameter");
        }

        String codeStr = TemplateManager.renderTemplate("c/discrete/Delay/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/discrete/Delay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/discrete/Delay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/discrete/Delay/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void calculateOutput(double t) {
        // Discrete delay: y[k] = u[k-n] where n is the delay length        
        OutputPort output = outputPortList.get(0);        
        
        int delayLengthValue = (int) delayLength.getData().getInitValue();

        if(feedthrough){
            output.setData(inputPortList.get(0).getData());
            return;
        }
        
        // Output the delayed sample from buffer
        // Buffer stores samples in chronological order: [oldest, ..., newest]
        // For delay of n, we want the sample from n steps ago
        if (buffer.size() >= delayLengthValue) {
            // Get the delayed sample (from n steps ago)
            int delayedIndex = buffer.size() - delayLengthValue;
            if (delayedIndex >= 0 && delayedIndex < buffer.size()) {
                Data delayedData = buffer.get(delayedIndex);
                output.setData(delayedData);
            } else {
                // Index out of bounds, use initial condition
                double ic = initialCondition.getDouble();
                output.setData(new Data(ic));
            }
        } else {
            // Not enough samples in buffer yet, use initial condition
            double ic = initialCondition.getDouble();
            output.setData(new Data(ic));
        }
    }

    @Override
    public void calculateInit() {
        // Initialize delay block
        OutputPort output = outputPortList.get(0);

        int delayLengthValue = (int) delayLength.getData().getInitValue();

        // Initialize buffer
        buffer = new ArrayList<>();

        // SIMULINK edge case: delayLength = 0 means direct feedthrough (no delay)
        if (delayLengthValue == 0) {
            // No buffer needed for zero delay - output will directly copy input in calculateOutput
            return;
        }

        // Determine input type for proper IC handling
        int inputWidth = input != null ? input.getWidth() : 1;
        int inputHeight = input != null ? input.getHeight() : 1;
        boolean isScalarInput = (inputWidth == 1 && inputHeight == 1);
        boolean isColumnVector = (inputWidth == 1 && inputHeight > 1);
        boolean isRowVector = (inputWidth > 1 && inputHeight == 1);

        if(initialCondition.getDataType() == DataType.REAL){
            double ic = initialCondition.getDouble();

            // Pre-fill buffer with scalar initial conditions for the delay length
            for (int i = 0; i < delayLengthValue; i++) {
                buffer.add(new Data(ic));
            }

            // Initial output is the initial condition
            output.setData(new Data(ic));
        } else {
            // Matrix/Vector initial condition
            Matrix icMatrix = initialCondition.getMatrix();

            if (icMatrix == null) {
                throw new IllegalStateException("Delay block cannot initialize: initial condition matrix is null");
            }

            if (isScalarInput) {
                // Scalar input with matrix IC: use IC(0,0)
                double ic = icMatrix.get(0, 0);

                // Pre-fill buffer with scalar initial conditions
                for (int i = 0; i < delayLengthValue; i++) {
                    buffer.add(new Data(ic));
                }

                // Initial output is the first element of IC matrix
                output.setData(new Data(ic));
            } else if (isColumnVector) {
                // Column vector input [n×1]: extract first column from IC matrix [n×m]
                Matrix icVector = new Matrix(inputHeight, 1);
                for (int i = 0; i < inputHeight; i++) {
                    icVector.set(i, 0, icMatrix.get(i, 0));
                }

                // Pre-fill buffer with IC column vector
                for (int i = 0; i < delayLengthValue; i++) {
                    buffer.add(new Data(icVector.copy()));
                }

                // Initial output is the IC column vector
                output.setData(new Data(icVector.copy()));
            } else if (isRowVector) {
                // Row vector input [1×n]: extract first row from IC matrix [m×n]
                Matrix icVector = new Matrix(1, inputWidth);
                for (int j = 0; j < inputWidth; j++) {
                    icVector.set(0, j, icMatrix.get(0, j));
                }

                // Pre-fill buffer with IC row vector
                for (int i = 0; i < delayLengthValue; i++) {
                    buffer.add(new Data(icVector.copy()));
                }

                // Initial output is the IC row vector
                output.setData(new Data(icVector.copy()));
            } else {
                // Matrix input [m×n]: IC must be [m×n] (validated in updateDimension)
                // Pre-fill buffer with full IC matrix
                for (int i = 0; i < delayLengthValue; i++) {
                    buffer.add(new Data(icMatrix.copy()));
                }

                // Initial output is the full IC matrix
                output.setData(new Data(icMatrix.copy()));
            }
        }
        
        
    }
    
    @Override
    public void calculateUpdate(double t) {
        // Update delay buffer with new input sample        
        InputPort input = inputPortList.get(0);       
        
        Data inputData = input.getData();
        int delayLengthValue = (int) delayLength.getData().getInitValue();
        
        // Add new sample to buffer
        buffer.add(inputData);
        
        // Keep buffer size manageable - only keep what we need for delay
        // We need delayLength + 1 samples to output the properly delayed value
        int maxBufferSize = delayLengthValue + 10; // Keep a few extra for stability
        while (buffer.size() > maxBufferSize) {
            buffer.remove(0); // Remove oldest sample
        }
    }
}
