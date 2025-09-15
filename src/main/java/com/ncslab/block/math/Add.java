package com.ncslab.block.math;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import org.json.JSONObject;

// Internal imports - DTO
import com.ncslab.dto.block.specialized.math.AddDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
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
 * Add block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * Performs element-wise addition and subtraction based on input sequence specification.
 * The input sequence string defines the operation for each input port ('+' for addition, '-' for subtraction).
 *
 * SIMULINK Parameters:
 * - Inputs: String sequence defining input signs (e.g., "++", "+-", "++--")
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - InputSameDT: Require inputs to have same data type
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * @author NCSLab Team
 * @version 2025
 */
public class Add extends MathBlock {

    // === SIMULINK-Compatible Parameters ===
    private Parameter inputs;
    private Parameter sampleTime;
    private Parameter inputSameDT;
    private Parameter outDataType;
    private Parameter saturateOnIntegerOverflow;
    
    // === Operational Settings ===
    /** Input sequence string defining operation signs ('+' or '-' for each input) */
    @Getter
    private String inputSequence;

    // === Static Configuration ===
    
    /** Parameter defaults matching SIMULINK database format */
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Inputs", "++");  // Two positive inputs by default
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("InputSameDT", "on");  // Require same data type
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as first input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    
    /** Output port names for template generation */
    public static final List<String> outputNames = new ArrayList<>();
    
    /** Input port names for template generation (dynamic based on sequence length) */
    public static final List<String> inputNames = new ArrayList<>();
    
    static {
        outputNames.add("out1");
        // Input names are populated dynamically based on input sequence length
    }

    // === Constructors ===
    
    /**
     * Private constructor with typed parameters for factory methods.
     * 
     * @param inputs Input sequence parameter
     * @param sampleTime Sample time parameter
     * @param inputSameDT Input same data type parameter
     * @param outDataType Output data type parameter
     * @param saturateOnIntegerOverflow Integer overflow handling parameter
     * @param blockName Block name
     * @param blockPath Block path
     * @param blockUUID Block UUID
     * @param model Parent model
     */
    private Add(Parameter inputs, Parameter sampleTime, Parameter inputSameDT,
               Parameter outDataType, Parameter saturateOnIntegerOverflow,
               String blockName, String blockPath, String blockUUID,
               NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Extract and validate input sequence
        this.inputSequence = inputs.getInitString();
        validateParameters(inputs, sampleTime);
        
        // Assign parameters with null checks
        this.inputs = Objects.requireNonNull(inputs, "Inputs parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.inputSameDT = Objects.requireNonNull(inputSameDT, "InputSameDT parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.inputs);
        parameterList.add(this.sampleTime);
        parameterList.add(this.inputSameDT);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);

        // based on input sequence        
        initializePorts();
    }

    /**
     * Legacy JSON Constructor - Creates Add block from JSONObject.
     * Maintained for backward compatibility with existing JSON-based workflows.
     * 
     * @param blockJSON JSON object containing block configuration
     * @param model Parent model reference
     * @deprecated Use DTO-native constructor for new development
     */
    @Deprecated
    public Add(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        initializeBlock();
    }
    
    /**
     * DTO-Native Constructor - Creates Add block directly from AddDto.
     * Provides type-safe construction with comprehensive validation.
     * 
     * @param dto AddDto containing block configuration
     * @param model Parent model reference
     */
    public Add(AddDto dto, NCSLabModel model) {
        super(dto, model);
        
        // Validate DTO before processing
        dto.validate();
        
        // Extract validated parameters from DTO
        String inputsValue = dto.getInputsValue();
        String sampleTimeValue = dto.getSampleTimeValue().toString();
        String inputSameDTValue = "on"; // Fixed parameter for Add block
        String outDataTypeValue = dto.getOutDataTypeStrValue();
        String saturateValue = dto.getSaturateOnIntegerOverflowValue() ? "on" : "off";
        
        // Initialize parameters from parsed data
        this.inputs = getParameterByName("Inputs");
        this.sampleTime = getParameterByName("SampleTime");
        this.inputSameDT = getParameterByName("InputSameDT");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        this.inputSequence = this.inputs.getInitString();
        
        // Validate parameters and initialize ports
        validateParameters(this.inputs, this.sampleTime);
        initializePorts();
        
        System.out.println("DTO-NATIVE: Add block created successfully - " + dto.getBlockName());
    }

    /**
     * Factory method for creating Add blocks from DTO.
     * 
     * @param dto AddDto containing block configuration
     * @param model Parent model reference
     * @return Configured Add block instance
     */
    public static Add fromDto(AddDto dto, NCSLabModel model) {
        return new Add(dto, model);
    }
    
    /**
     * Initializes block from legacy JSONObject format.
     * Handles parameter extraction with fallback defaults.
     */
    private void initializeBlock() {
        // Extract parameters with safe fallbacks
        this.inputs = getParameterByName("Inputs");
        if (this.inputs != null) {
            this.inputSequence = this.inputs.getInitString();
        } else {
            this.inputSequence = "++"; // Default: two positive inputs
        }
        
        this.sampleTime = getParameterByName("SampleTime");
        this.inputSameDT = getParameterByName("InputSameDT");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Add all parameters to parameter list if they exist
        if (this.inputs != null) parameterList.add(this.inputs);
        if (this.sampleTime != null) parameterList.add(this.sampleTime);
        if (this.inputSameDT != null) parameterList.add(this.inputSameDT);
        if (this.outDataType != null) parameterList.add(this.outDataType);
        if (this.saturateOnIntegerOverflow != null) parameterList.add(this.saturateOnIntegerOverflow);

        //  based on sequence
        initializePorts();
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Add fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            // Extract and validate JSON fields
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            // Create typed parameters from JSON with defaults
            Parameter inputs = createInputsFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter inputSameDT = createInputSameDTFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            Add block = new Add(inputs, sampleTime, inputSameDT, outDataType, saturateParam,
                               blockName, blockPath, blockUUID, model);

            // Set block reference in parameters (required for Parameter constructor compatibility)
            setParameterBlockReference(block, inputs, sampleTime, inputSameDT, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Add block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static Add create(String name, String path, String inputSequence, NCSLabModel model) {
        return create(name, path, inputSequence, -1.0, true, "Inherit: Same as input", false, model);
    }

    public static Add create(String name, String path, String inputSequence,
                            double sampleTime, boolean inputSameDT, String outDataType,
                            boolean saturateOnOverflow, NCSLabModel model) {
        // Create parameters
        Parameter inputs = new Parameter(null, 1, "Inputs", inputSequence);
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter inputSameDTParam = new Parameter(null, 3, "InputSameDT", inputSameDT ? "on" : "off");
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));

        Add block = new Add(inputs, sampleTimeParam, inputSameDTParam, outDataTypeParam, saturateParam,
                           name, path, "null", model);

        // Set block reference in parameters
        setParameterBlockReference(block, inputs, sampleTimeParam, inputSameDTParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Parameter Validation ===
    
    /**
     * Validates Add block parameters for consistency and correctness.
     * 
     * @param inputs Input sequence parameter
     * @param sampleTime Sample time parameter
     * @throws IllegalArgumentException if parameters are invalid
     */
    private static void validateParameters(Parameter inputs, Parameter sampleTime) {
        // Validate input sequence format
        String sequence = inputs.getInitString();
        if (sequence == null || sequence.trim().isEmpty()) {
            throw new IllegalArgumentException("Input sequence cannot be empty");
        }
        
        if (sequence.length() < 1) {
            throw new IllegalArgumentException("Input sequence must have at least one input");
        }
        
        // Validate sequence contains only valid operation characters
        for (char c : sequence.toCharArray()) {
            if (c != '+' && c != '-') {
                throw new IllegalArgumentException(
                    "Input sequence must contain only '+' and '-' characters, found: '" + c + "'");
            }
        }
        
        // Validate sample time
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createInputsFromJSON(JSONObject paramValues, String blockName) {
        String inputsValue = paramValues.optString("Inputs", "++");
        return new Parameter(null, 1, "Inputs", inputsValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 2, "SampleTime", sampleTimeValue);
    }

    private static Parameter createInputSameDTFromJSON(JSONObject paramValues, String blockName) {
        String inputSameDTValue = paramValues.optString("InputSameDT", "on");
        return new Parameter(null, 3, "InputSameDT", inputSameDTValue);
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

    private static void setParameterBlockReference(Add block, Parameter... parameters) {
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

    /**
     * Creates a block identity JSONObject for constructor compatibility.
     * 
     * @param blockName Block name
     * @param blockPath Block path
     * @param blockUUID Block UUID
     * @return JSONObject containing block identity
     */
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Add");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Empty paramValues for base constructor
        return identity;
    }

    // === Port Initialization ===
    
    /**
     * Initializes input and output ports based on input sequence length.
     * Creates dynamic number of input ports matching the sequence specification.
     */
    private void initializePorts() {
        // Create output port with feedthrough (addition is instantaneous)
        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);
        
        // Create input ports based on input sequence length
        for (int i = 0; i < inputSequence.length(); i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }
        
        // Update input names for template generation
        inputNames.clear();
        for (int i = 0; i < inputSequence.length(); i++) {
            inputNames.add("in" + (i + 1));
        }
    }

    // === Public Interface Methods ===
    
    /**
     * Gets the input sequence string.
     * 
     * @return Input sequence string
     * @deprecated Use getInputSequence() for consistent naming
     */
    @Deprecated
    public String getSeq() {
        return inputSequence;
    }
    
    /**
     * Checks if the specified input has a positive sign.
     * 
     * @param n Input index (0-based)
     * @return True if the input has '+' sign, false if '-'
     */
    public boolean getSign(int n) {
        if (n < 0 || n >= inputSequence.length()) {
            throw new IndexOutOfBoundsException("Input index " + n + " out of bounds for sequence length " + inputSequence.length());
        }
        return inputSequence.charAt(n) == '+';
    }

    // === Code Generation Methods ===
    
    /**
     * Generates MATLAB/Octave output code for the Add block.
     * 
     * @param code MATLAB code structure to append to
     */
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add Add-specific template context
        OutputPort out = outputPortList.get(0);
        OutputPort firstInput = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        context.put("sequence", inputSequence);
        // For C templates - use string variable names, not objects
        context.put("outputSignal", out.getOutputSignalC().getName());
        context.put("firstInputSignal", firstInput.getOutputSignalC().getName());
        // For MATLAB templates that need objects - provide separate object references
        context.put("outputSignalObject", out.getOutputSignalC());
        context.put("firstInputSignalObject", firstInput.getOutputSignalC());
        
        String codeStr = TemplateManager.renderTemplate("m/math/Add/output.vm", context);
        code.addOutputCode(codeStr);
    }
    
    /**
     * Generates C/C++ output code for the Add block.
     * 
     * @param code C code structure to append to
     */
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add Add-specific template context
        context.put("sequence", getInputSequence());
        context.put("inputs", this.inputs);
        
        String codeStr = TemplateManager.renderTemplate("c/math/Add/output.vm", context);
        code.addOutputCode(codeStr);
    }

    // === Dimension Management ===
    
    /**
     * Updates output dimensions based on input signal dimensions.
     * All inputs must have matching dimensions for element-wise operations.
     * 
     * @throws MatDimException if input dimensions don't match
     */
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal[] inputSignals = new OutputSignal[inputSequence.length()];
        
        // Collect input signals
        for (int i = 0; i < inputSequence.length(); i++) {
            inputSignals[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        }
        
        // Validate all inputs have the same dimensions
        int referenceHeight = inputSignals[0].getHeight();
        int referenceWidth = inputSignals[0].getWidth();
        
        for (int i = 1; i < inputSignals.length; i++) {
            OutputSignal signal = inputSignals[i];
            if (signal.getHeight() != referenceHeight || signal.getWidth() != referenceWidth) {
                throw new MatDimException(
                    String.format("Block %s input dimensions don't match! Input 1: [%dx%d], Input %d: [%dx%d]",
                        this.blockName, referenceHeight, referenceWidth, i + 1, signal.getHeight(), signal.getWidth()));
            }
        }
        
        // Set output dimensions to match inputs
        out.setHeight(referenceHeight);
        out.setWidth(referenceWidth);
        out.getOutputSignalC().setHeight(referenceHeight);
        out.getOutputSignalC().setWidth(referenceWidth);
        out.getOutputSignalC().setDataType(inputSignals[0].getDataType());
    }
    
    /**
     * Performs additional dimension validation if needed.
     * 
     * @throws MatDimException if dimensions are invalid
     */
    public void checkDimension() throws MatDimException {
        // Additional dimension checks can be added here if needed
    }

    // === Runtime Simulation Interface ===
    
    /**
     * Calculates the Add block output by performing element-wise operations.
     * Processes each input according to its sign in the input sequence.
     * 
     * @param t Current simulation time
     */
    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data result = new Data(out.getHeight(), out.getWidth());
        
        // Process each input according to its operation sign
        for (int i = 0; i < inputSequence.length(); i++) {
            Data inputData = inputPortList.get(i).getData();
            char operation = inputSequence.charAt(i);
            
            if (operation == '+') {
                result = result.plus(inputData);
            } else if (operation == '-') {
                result = result.minus(inputData);
            }
        }
        
        out.setData(result);
    }
}

