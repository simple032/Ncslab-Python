package com.ncslab.block.math;

import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
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
import java.util.Vector;

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
public class Product extends Block {

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
    @Getter
    private final boolean matrixMultiplication;

    // === Static Parameter Definitions ===

    // Parameter defaults
    @Getter
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

    @Getter
    public static final Vector<String> outputNames = new Vector<>();

    @Getter
    public static final Vector<String> inputNames = new Vector<>();

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
        // Initialize ports based on input sequence
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
        this.inputs = new Parameter(this, 1, "Inputs", this.inputSequence);
        this.multiplication = new Parameter(this, 2, "Multiplication", paramValues.getString("Multiplication"));

        // Create missing SIMULINK parameters with defaults
        this.sampleTime = new Parameter(this, 3, "SampleTime", "-1");
        this.inputSameDT = new Parameter(this, 4, "InputSameDT", "on");
        this.outDataType = new Parameter(this, 5, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 6, "SaturateOnIntegerOverflow", "off");

        // Add all parameters to parameter list

        // Initialize ports
        initializePorts();
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

    // === Static Factory Method for Programmatic Creation ===
    public static Product create(String name, String path, String inputSequence, NCSLabModel model) {
        return create(name, path, inputSequence, "Element-wise(*)", -1.0, true,
                     "Inherit: Same as input", false, model);
    }

    public static Product create(String name, String path, String inputSequence, String multiplicationMode,
                                double sampleTime, boolean inputSameDT, String outDataType,
                                boolean saturateOnOverflow, NCSLabModel model) {
        // Create parameters
        Parameter inputs = new Parameter(null, 1, "Inputs", inputSequence);
        Parameter multiplication = new Parameter(null, 2, "Multiplication", multiplicationMode);
        Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", String.valueOf(sampleTime));
        Parameter inputSameDTParam = new Parameter(null, 4, "InputSameDT", inputSameDT ? "on" : "off");
        Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));

        Product block = new Product(inputs, multiplication, sampleTimeParam, inputSameDTParam,
                                   outDataTypeParam, saturateParam, name, path, "null", model);

        // Set block reference in parameters
        setParameterBlockReference(block, inputs, multiplication, sampleTimeParam, inputSameDTParam,
                                  outDataTypeParam, saturateParam);

        return block;
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

        context.put("block", this);
        context.put("sequence", getInputSequence());
        context.put("inputs", this.inputs);
        context.put("multiplication", this.multiplication);
        context.put("sampleTime", this.sampleTime);
        context.put("inputSameDT", this.inputSameDT);
        context.put("outDataType", this.outDataType);
        context.put("saturateOnIntegerOverflow", this.saturateOnIntegerOverflow);
        context.put("matrixMultiplication", this.matrixMultiplication);

        String codeStr = TemplateManager.renderTemplate("c/math/Product/output.vm", context);
        code.addOutputCode(codeStr);
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
        if (!isMatrixMultiplication()) {
            for (int i = 0; i < inputSequence.length(); i++) {
                if (inputSequence.charAt(i) == '*') {
                    resultData = resultData.times(inputPortList.get(i).getData());
                } else if (inputSequence.charAt(i) == '/') {
                    resultData = resultData.divide(inputPortList.get(i).getData());
                }
            }
        } else {
            for (int i = 0; i < inputSequence.length(); i++) {
                if (inputSequence.charAt(i) == '*') {
                    resultData = resultData.arrayTimes(inputPortList.get(i).getData());
                } else if (inputSequence.charAt(i) == '/')  {
                    resultData = resultData.divide(inputPortList.get(i).getData());
                }
            }
        }

        out.setData(resultData);
    }
}

