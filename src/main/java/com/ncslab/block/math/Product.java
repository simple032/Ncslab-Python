package com.ncslab.block.math;

import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.ProductDto;
import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import com.ncslab.util.TemplateManager;

import Jama.Matrix;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Product block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - Inputs: String sequence defining input operations (e.g., "**", "", "/")
 * - Multiplication: Element-wise or Matrix multiplication mode
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - InputSameDT: Require inputs to have same data type
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Product extends MathBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter inputs;
    private final Parameter multiplication;
    private final Parameter sampleTime;
    private final Parameter inputSameDT;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Operational Settings ===
    @Getter
    private final String inputSequence;
    private final boolean matrixMultiplication;

    // === Static Parameter Definitions ===

    // Parameter defaults
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Inputs", "**");  // Two multiplication inputs
        PARAMETER_DEFAULTS.put("Multiplication", "Element-wise(.*)");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("InputSameDT", "on");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as first input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
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
    private Product(Parameter inputs, Parameter multiplication, Parameter sampleTime,
                   Parameter inputSameDT, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                   String blockName, String blockPath, String blockUUID,
                   NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Extract input sequence and multiplication mode from parameters
        this.inputSequence = inputs.getInitString();
        this.matrixMultiplication = "Matrix(*)".equals(multiplication.getInitString());

        // Validate parameters
        validateParameters(inputs, sampleTime);

        // Assign parameters
        this.inputs = Objects.requireNonNull(inputs, "Inputs parameter cannot be null");
        this.multiplication = Objects.requireNonNull(multiplication, "Multiplication parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.inputSameDT = Objects.requireNonNull(inputSameDT, "InputSameDT parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.inputs);
        parameterList.add(this.multiplication);
        parameterList.add(this.sampleTime);
        parameterList.add(this.inputSameDT);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);

        //based on input sequence
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Product(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Extract input sequence and multiplication mode for backward compatibility
        this.inputSequence = paramValues.getString("Inputs");
        this.matrixMultiplication = "Matrix(*)".equals(paramValues.getString("Multiplication"));

        // Create legacy parameters
        this.inputs = getParameterByName("Inputs");
        this.multiplication = getParameterByName("Multiplication");

        // Create missing SIMULINK parameters with defaults
        this.sampleTime = getParameterByName("SampleTime");
        this.inputSameDT = getParameterByName("InputSameDT");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates Product block directly from BlockDto DTO
     */
    public Product(ProductDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        
        // Use centralized parameter management via getParameterByName
        this.inputs = getParameterByName("Inputs");
        this.inputSequence = this.inputs.getInitString(); // Initialize final field from parameter
        this.multiplication = getParameterByName("Multiplication");
        this.matrixMultiplication = "Matrix(*)".equals(this.multiplication.getInitString());
        this.sampleTime = getParameterByName("SampleTime");
        this.inputSameDT = getParameterByName("InputSameDT");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }
    // === Static Factory Method for JSON Deserialization ===
    public static Product fromJSON(JSONObject blockJSON, NCSLabModel model) {
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
            Parameter multiplication = createMultiplicationFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter inputSameDT = createInputSameDTFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            Product block = new Product(inputs, multiplication, sampleTime, inputSameDT,
                                       outDataType, saturateParam, blockName, blockPath, blockUUID, model);

            // Set block reference in parameters (required for Parameter constructor compatibility)
            setParameterBlockReference(block, inputs, multiplication, sampleTime, inputSameDT, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Product block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Create a Product block with default parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param inputSequence Input sequence defining operations
     * @param model Parent model
     * @return Configured Product block instance
     */
    public static Product create(String name, String path, String inputSequence, NCSLabModel model) {
        return create(name, path, inputSequence, "Element-wise(*)", -1.0, true,
                     "Inherit: Same as input", false, model);
    }

    /**
     * Create a Product block with full parameters using DTO-based construction.
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param inputSequence Input sequence defining operations
     * @param multiplicationMode Element-wise or Matrix multiplication mode
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, greater than 0 for discrete)
     * @param inputSameDT Require inputs to have same data type
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return Configured Product block instance
     */
    public static Product create(String name, String path, String inputSequence, String multiplicationMode,
                                double sampleTime, boolean inputSameDT, String outDataType,
                                boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        ProductDto dto = ProductDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .inputs(com.ncslab.dto.common.TypedParameter.of(inputSequence))
            .multiplication(com.ncslab.dto.common.TypedParameter.of(multiplicationMode))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .inputSameDT(com.ncslab.dto.common.TypedParameter.of(inputSameDT))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Product parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Product(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter inputs, Parameter sampleTime) {
        // Validate input sequence
        String sequence = inputs.getInitString();
        if (sequence == null || sequence.trim().isEmpty()) {
            throw new IllegalArgumentException("Input sequence cannot be empty");
        }

        // Validate sequence contains only * and / characters
        for (char c : sequence.toCharArray()) {
            if (c != '*' && c != '/') {
                throw new IllegalArgumentException("Input sequence must contain only '*' and '/' characters");
            }
        }

        if (sequence.isEmpty()) {
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
        String inputsValue = paramValues.optString("Inputs", "**");
        return new Parameter(null, 1, "Inputs", inputsValue);
    }

    private static Parameter createMultiplicationFromJSON(JSONObject paramValues, String blockName) {
        String multiplicationValue = paramValues.optString("Multiplication", "Element-wise(*)");
        return new Parameter(null, 2, "Multiplication", multiplicationValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }

    private static Parameter createInputSameDTFromJSON(JSONObject paramValues, String blockName) {
        String inputSameDTValue = paramValues.optString("InputSameDT", "on");
        return new Parameter(null, 4, "InputSameDT", inputSameDTValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 5, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateValue);
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

    private static void setParameterBlockReference(Product block, Parameter... parameters) {
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
        identity.put("blockType", "Product");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Create output port with feedthrough (product is instantaneous)
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

    @Deprecated
    private boolean isMultiplication() {
        return matrixMultiplication;
    }

    // === Code Generation Methods (preserved from original) ===
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/math/Product/output.vm", context);
        code.addOutputCode(codeStr);
    }

    private boolean isMatrixMultiplication() {
        return matrixMultiplication;
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal[] signal = new OutputSignal[inputSequence.length()];
        int[] m = new int[inputSequence.length()];
        int[] n = new int[inputSequence.length()];

        for (int i = 0; i < inputSequence.length(); i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            m[i] = signal[i].getHeight();
            n[i] = signal[i].getWidth();
        }
        int v = 1;

        if (!isMatrixMultiplication()) {
            for (OutputSignal x : signal) {
                if ((x.getHeight() != m[0]) || (x.getWidth() != n[0])) {
                    v = 0;
                    MatDimException e = new MatDimException("Block " + this.blockName + " " + inputSequence.length() + " input dimensions doesn't match !\n \n");
                    throw(e);
                }
            }
            if (v == 1) {
                out.setHeight(signal[0].getHeight());
                out.setWidth(signal[0].getWidth());
                out.getOutputSignalC().setHeight(signal[0].getHeight());
                out.getOutputSignalC().setWidth(signal[0].getWidth());
                out.getOutputSignalC().setDataType(signal[0].getDataType());
            }
        } else {
            for (int i = 0; i < inputSequence.length() - 1; i++) {
                if (n[i] != m[i + 1]) {
                    v = 0;
                    MatDimException e = new MatDimException("Block " + this.blockName + " " + inputSequence.length() + " input dimensions doesn't match !\n \n");
                    throw(e);
                }
            }
            out.setHeight(signal[0].getHeight());
            out.setWidth(signal[inputSequence.length() - 1].getWidth());
            out.getOutputSignalC().setHeight(signal[0].getHeight());
            out.getOutputSignalC().setWidth(signal[inputSequence.length() - 1].getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        }
    }

    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateInit() {
        // Initialization logic for Product block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data resultData = new Data(out.getHeight(), out.getWidth());
        if(resultData.getDataType()==DataType.MATRIX) {
            Matrix matrix = new Matrix(out.getHeight(), out.getWidth());
            for (int i = 0; i < out.getHeight(); i++) {
                for (int j = 0; j < out.getWidth(); j++) {
                    matrix.set(i, j, 1);
                }
            }
            resultData.setMatrix(matrix);
        }else {
            resultData.setInitValue(1);
        }

        // Check if inputSequence is numeric (count) or operator string
        boolean isNumericInput = inputSequence.length() < inputPortList.size();

        if (!isMatrixMultiplication()) {
            // Element-wise operations
            for (int i = 0; i < inputPortList.size(); i++) {
                char operation;

                if (isNumericInput) {
                    // Numeric input: use default '*' operation
                    operation = '*';
                } else {
                    // String input: get operation from sequence
                    operation = inputSequence.charAt(i);
                }

                if (operation == '*') {
                    resultData = resultData.times(inputPortList.get(i).getData());
                } else if (operation == '/') {
                    resultData = resultData.divide(inputPortList.get(i).getData());
                }
            }
        } else {
            // Matrix multiplication mode
            for (int i = 0; i < inputPortList.size(); i++) {
                char operation;

                if (isNumericInput) {
                    // Numeric input: use default '*' operation
                    operation = '*';
                } else {
                    // String input: get operation from sequence
                    operation = inputSequence.charAt(i);
                }

                if (operation == '*') {
                    resultData = resultData.arrayTimes(inputPortList.get(i).getData());
                } else if (operation == '/')  {
                    resultData = resultData.divide(inputPortList.get(i).getData());
                }
            }
        }

        out.setData(resultData);
    }
}

