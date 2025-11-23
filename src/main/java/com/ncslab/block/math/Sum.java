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
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.SumDto;

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
 * Sum block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * Performs element-wise summation and subtraction operations based on input sequence.
 * Similar to Add block but with additional icon configuration support.
 * 
 * SIMULINK Parameters:
 * - Inputs: String sequence defining input signs (e.g., "++", "+-", "++--")
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - InputSameDT: Require inputs to have same data type
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * - Icon: Icon shape representation
 * 
 * @author NCSLab Team
 * @version 2025
 */
public class Sum extends MathBlock {
    
    // === SIMULINK-Compatible Parameters ===
    /** Input sequence parameter defining operation signs */
    private final Parameter inputs;
    
    /** Sample time parameter for discrete operation */
    private final Parameter sampleTime;
    
    /** Input data type consistency requirement parameter */
    private final Parameter inputSameDT;
    
    /** Output data type specification parameter */
    private final Parameter outDataType;
    
    /** Integer overflow handling parameter */
    private final Parameter saturateOnIntegerOverflow;
    
    /** Icon shape representation parameter */
    private final Parameter icon;
    
    // === Operational Settings ===
    /** Input sequence string defining operation signs ('+' or '-' for each input) */
    @Getter
    private final String inputSequence;
    
    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Inputs", "++");  // Two positive inputs by default
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("InputSameDT", "on");  // Require same data type
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as first input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
        PARAMETER_DEFAULTS.put("Icon", "round");  // Icon shape
    }
    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();
    
    static {
        // SIMULINK parameter names
        
        // Port names
        outputNames.add("out1");
        // Input names are dynamic based on sequence length
    }

    // === Private Constructor with Typed Parameters ===
    private Sum(Parameter inputs, Parameter sampleTime, Parameter inputSameDT,
               Parameter outDataType, Parameter saturateOnIntegerOverflow, Parameter icon,
               String blockName, String blockPath, String blockUUID, 
               NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Extract input sequence from inputs parameter
        this.inputSequence = inputs.getInitString();
        
        // Validate parameters
        validateParameters(inputs, sampleTime);
        
        // Assign parameters
        this.inputs = Objects.requireNonNull(inputs, "Inputs parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.inputSameDT = Objects.requireNonNull(inputSameDT, "InputSameDT parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        this.icon = Objects.requireNonNull(icon, "Icon parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.inputs);
        parameterList.add(this.sampleTime);
        parameterList.add(this.inputSameDT);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);
        parameterList.add(this.icon);

        //  based on input sequence
        initializePorts();
    }
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Sum(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        
        // Use name-based parameter access instead of index-based
        this.inputs = getParameterByName("Inputs");
        this.inputSequence = this.inputs.getInitString();
        this.sampleTime = getParameterByName("SampleTime");
        this.inputSameDT = getParameterByName("InputSameDT");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        this.icon = getParameterByName("Icon");
        
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates Sum block directly from BlockDto DTO
     */
    public Sum(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.inputs = getParameterByName("Inputs");
        this.inputSequence = this.inputs.getInitString(); // Initialize final field from parameter
        this.sampleTime = getParameterByName("SampleTime");
        this.inputSameDT = getParameterByName("InputSameDT");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        this.icon = getParameterByName("Icon");
        
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    
    
    
    // === Static Factory Method for JSON Deserialization ===
    public static Sum fromJSON(JSONObject blockJSON, NCSLabModel model) {
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
            Parameter icon = createIconFromJSON(paramValues, blockName);
            
            Sum block = new Sum(inputs, sampleTime, inputSameDT, outDataType, saturateParam, icon,
                               blockName, blockPath, blockUUID, model);
            
            // Set block reference in parameters (required for Parameter constructor compatibility)
            setParameterBlockReference(block, inputs, sampleTime, inputSameDT, outDataType, saturateParam, icon);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Sum block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Sum create(String name, String path, String inputSequence, NCSLabModel model) {
        return create(name, path, inputSequence, -1.0, true, "Inherit: Same as input", false, "rectangular", model);
    }

    /**
     * Create a Sum block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of JSONObject manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param inputSequence Input sequence (e.g., "++", "+-", "+--")
     * @param sampleTime Sample time (-1 for inherited)
     * @param inputSameDT Require same data type for all inputs
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param iconShape Icon shape ("round" or "rectangular")
     * @param model Parent model
     * @return Sum block instance
     */
    public static Sum create(String name, String path, String inputSequence,
                            double sampleTime, boolean inputSameDT, String outDataType,
                            boolean saturateOnOverflow, String iconShape, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        com.ncslab.dto.block.specialized.math.SumDto dto =
            com.ncslab.dto.block.specialized.math.SumDto.builder()
                .blockName(name)
                .blockPath(path)
                .blockUUID("null")
                .inputs(com.ncslab.dto.common.TypedParameter.of(inputSequence))
                .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
                .inputSameDT(com.ncslab.dto.common.TypedParameter.of(inputSameDT))
                .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
                .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
                .icon(com.ncslab.dto.common.TypedParameter.of(iconShape))
                .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Sum parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Sum(dto, model);
    }
    // === Parameter Validation ===
    private static void validateParameters(Parameter inputs, Parameter sampleTime) {
        // Validate input sequence
        String sequence = inputs.getInitString();
        if (sequence == null || sequence.trim().isEmpty()) {
            throw new IllegalArgumentException("Input sequence cannot be empty");
        }
        // Validate sequence contains only + and - characters
        for (char c : sequence.toCharArray()) {
            if (c != '+' && c != '-') {
                throw new IllegalArgumentException("Input sequence must contain only '+' and '-' characters");
            }
        }
        
        if (sequence.length() < 1) {
            throw new IllegalArgumentException("Input sequence must have at least one input");
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
    private static Parameter createIconFromJSON(JSONObject paramValues, String blockName) {
        String iconValue = paramValues.optString("Icon", "rectangular");
        return new Parameter(null, 6, "Icon", iconValue);
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
    private static void setParameterBlockReference(Sum block, Parameter... parameters) {
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
        identity.put("blockType", "Sum");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }
    // === Port Initialization ===
    private void initializePorts() {
        // Create output port with feedthrough (sum is instantaneous)
        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);
        
        // Create input ports based on sequence length
        for (int i = 0; i < inputSequence.length(); i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }
        // Update input names for static reference
        inputNames.clear();
        for (int i = 0; i < inputSequence.length(); i++) {
            inputNames.add("in" + (i + 1));
        }
    }

    // === Legacy Compatibility Methods ===
    @Deprecated
    private String getSequence() {
        return inputSequence;
    }
    // === Code Generation Methods (preserved from original) ===
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("sequence", getInputSequence());

        String codeStr = TemplateManager.renderTemplate("c/math/Sum/output.vm", context);
        code.addOutputCode(codeStr);
    }
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal[] signal = new OutputSignal[inputSequence.length()];

        // Collect all input signals
        for (int i = 0; i < inputSequence.length(); i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        }

        // SIMULINK-compatible scalar expansion support
        // Supported operations:
        //   Scalar + Scalar = Scalar
        //   Scalar + Matrix = Matrix (scalar expanded)
        //   Matrix + Scalar = Matrix (scalar expanded)
        //   Matrix + Matrix (same dimensions) = Matrix (element-wise)

        // Find the maximum dimensions (non-scalar dimension if present)
        int maxHeight = 1;
        int maxWidth = 1;
        boolean hasMatrix = false;

        for (int i = 0; i < signal.length; i++) {
            int height = signal[i].getHeight();
            int width = signal[i].getWidth();
            boolean isScalar = (height == 1 && width == 1);

            if (!isScalar) {
                hasMatrix = true;
                if (maxHeight == 1 && maxWidth == 1) {
                    // First non-scalar sets the reference dimensions
                    maxHeight = height;
                    maxWidth = width;
                } else {
                    // Verify all non-scalar inputs have the same dimensions
                    if (height != maxHeight || width != maxWidth) {
                        throw new MatDimException(
                            String.format("Block %s: Non-scalar input dimensions must match. " +
                                "Found [%d×%d] and [%d×%d]",
                                blockName, maxHeight, maxWidth, height, width));
                    }
                }
            }
        }

        // Set output dimensions
        out.setHeight(maxHeight);
        out.setWidth(maxWidth);
        out.getOutputSignalC().setHeight(maxHeight);
        out.getOutputSignalC().setWidth(maxWidth);
        out.getOutputSignalC().setDataType(hasMatrix ? DataType.MATRIX : DataType.REAL);
    }

    public void checkDimension() throws MatDimException {
        // Validate all input dimensions are non-zero
        for (int i = 0; i < inputPortList.size(); i++) {
            OutputSignal signal = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            int height = signal.getHeight();
            int width = signal.getWidth();

            if (height == 0 || width == 0) {
                throw new MatDimException(
                    String.format("Sum block '%s': Input %d has invalid dimensions [%d×%d]. " +
                        "All input dimensions must be at least [1×1].",
                        blockName, i+1, height, width));
            }
        }
    }

    // === Runtime Simulation API (restored) ===
    @Override
    public void calculateInit() {
        // Initialization logic for Sum block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data resultData = new Data(out.getHeight(), out.getWidth());

        // Check if inputSequence is numeric (count) or operator string
        boolean isNumericInput = inputSequence.length() < inputPortList.size();

        for (int i = 0; i < inputPortList.size(); i++) {
            char operation;

            if (isNumericInput) {
                // Numeric input: use default '+' operation
                operation = '+';
            } else {
                // String input: get operation from sequence
                operation = inputSequence.charAt(i);
            }

            if (operation == '+') {
                resultData = resultData.plus(inputPortList.get(i).getData());
            } else if (operation == '-') {
                resultData = resultData.minus(inputPortList.get(i).getData());
            }
        }

        out.setData(resultData);
    }
}
