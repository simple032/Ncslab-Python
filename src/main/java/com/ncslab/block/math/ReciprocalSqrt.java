package com.ncslab.block.math;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.block.math.MathBlock;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Reciprocal Sqrt block (rsqrt) with SIMULINK-compatible parameters.
 *
 * Computes reciprocal square root (inverse square root): output = 1 / sqrt(input)
 * Also known as rsqrt, this operation is commonly used in graphics and normalization.
 *
 * SIMULINK Parameters:
 * - Function: Operation type (rsqrt, sqrt)
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * Mathematical Behavior:
 * - rsqrt(x) = 1 / sqrt(|x|)
 * - Division by zero returns Infinity
 * - Negative inputs use absolute value
 * - Supports both scalar and matrix inputs (element-wise for matrices)
 *
 * @author NCSLab Team
 * @version 2025
 */
public class ReciprocalSqrt extends MathBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter function;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Function", "rsqrt");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");

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

        // Output port defaults (rsqrt has feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private ReciprocalSqrt(Parameter function, Parameter sampleTime, Parameter outDataType,
                Parameter saturateOnIntegerOverflow, String blockName, String blockPath,
                String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(function, sampleTime);

        // Assign parameters
        this.function = Objects.requireNonNull(function, "Function parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.function);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);

        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public ReciprocalSqrt(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create parameters from JSON with defaults
        this.function = getParameterByName("Function");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Initialize ports
        initializePorts();
    }

    // === Static Factory Method for JSON Deserialization ===
    public static ReciprocalSqrt fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter function = createFunctionFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            ReciprocalSqrt block = new ReciprocalSqrt(function, sampleTime, outDataType, saturateParam,
                                 blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, function, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create ReciprocalSqrt block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static ReciprocalSqrt create(String name, String path, NCSLabModel model) {
        return create(name, path, "rsqrt", -1.0, "Inherit: Same as input", false, model);
    }

    public static ReciprocalSqrt create(String name, String path, String function, double sampleTime,
                             String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter functionParam = new Parameter(null, 1, "Function", function);
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 3, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 4, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));

        ReciprocalSqrt block = new ReciprocalSqrt(functionParam, sampleTimeParam, outDataTypeParam, saturateParam,
                             name, path, "null", model);

        setParameterBlockReference(block, functionParam, sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter function, Parameter sampleTime) {
        String functionValue = function.getInitString();
        if (functionValue == null || functionValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Function parameter cannot be empty");
        }

        // Validate function type
        if (!functionValue.equals("rsqrt") && !functionValue.equals("sqrt")) {
            throw new IllegalArgumentException("Function must be 'rsqrt' or 'sqrt'");
        }

        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createFunctionFromJSON(JSONObject paramValues, String blockName) {
        String functionValue = paramValues.optString("Function", "rsqrt");
        return new Parameter(null, 1, "Function", functionValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 2, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 3, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 4, "SaturateOnIntegerOverflow", saturateValue);
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

    private static void setParameterBlockReference(ReciprocalSqrt block, Parameter... parameters) {
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
        identity.put("blockType", "ReciprocalSqrt");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Code Generation Methods ===
    public void generateOutputCodeC(CodeStructC code) {
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("function", function);

        String codeStr = TemplateManager.renderTemplate("c/math/ReciprocalSqrt/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateInit() {
        // Initialization logic for ReciprocalSqrt block
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK ReciprocalSqrt block: computes reciprocal square root
        Data inputData = inputPortList.get(0).getData();
        String functionValue = function.getInitString();
        Data outputData;

        if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix input - apply rsqrt function element-wise
            Jama.Matrix inputMatrix = inputData.getMatrix();
            Jama.Matrix outputMatrix = new Jama.Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());

            for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                    double value = inputMatrix.get(i, j);
                    double rsqrtValue;

                    if (functionValue.equals("rsqrt")) {
                        // Reciprocal square root: 1 / sqrt(|x|)
                        double absValue = Math.abs(value);
                        rsqrtValue = absValue == 0 ? Double.POSITIVE_INFINITY : 1.0 / Math.sqrt(absValue);
                    } else {
                        // Forward square root (for compatibility)
                        rsqrtValue = Math.sqrt(Math.abs(value));
                    }

                    outputMatrix.set(i, j, rsqrtValue);
                }
            }
            outputData = new Data(outputMatrix);
        } else {
            // Scalar input
            double inputValue = inputData.getInitValue();
            double rsqrtValue;

            if (functionValue.equals("rsqrt")) {
                // Reciprocal square root: 1 / sqrt(|x|)
                double absValue = Math.abs(inputValue);
                rsqrtValue = absValue == 0 ? Double.POSITIVE_INFINITY : 1.0 / Math.sqrt(absValue);
            } else {
                // Forward square root (for compatibility)
                rsqrtValue = Math.sqrt(Math.abs(inputValue));
            }

            outputData = new Data(1, 1);
            outputData.setInitValue(rsqrtValue);
        }

        outputPortList.get(0).setData(outputData);
    }

    /**
     * Get static input names for block definition
     */
    public static List<String> getInputNames() {
        return inputNames;
    }

    /**
     * Get static output names for block definition
     */
    public static List<String> getOutputNames() {
        return outputNames;
    }
}
