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
 * Dot Product block with SIMULINK-compatible parameters.
 *
 * Computes the dot product of two vectors: output = sum(A[i] * B[i])
 *
 * SIMULINK Parameters:
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * Mathematical Behavior:
 * - dot(A, B) = A[0]*B[0] + A[1]*B[1] + ... + A[n]*B[n]
 * - Both inputs must have the same dimensions
 * - Output is always a scalar (1x1)
 * - Supports row vectors (1xN), column vectors (Nx1), and matrices (treated as flattened)
 *
 * @author NCSLab Team
 * @version 2025
 */
public class DotProduct extends MathBlock {

    // === SIMULINK-Compatible Parameters ===
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
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as first input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");

        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2");

        // Input port defaults (two inputs)
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);

        Map<String, Object> input2 = new HashMap<>();
        input2.put("name", "in2");
        input2.put("width", 1);
        input2.put("height", 1);
        input2.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input2);

        // Output port defaults (scalar output with feedthrough)
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
    private DotProduct(Parameter sampleTime, Parameter outDataType,
                Parameter saturateOnIntegerOverflow, String blockName, String blockPath,
                String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(sampleTime);

        // Assign parameters
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);

        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public DotProduct(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create parameters from JSON with defaults
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Initialize ports
        initializePorts();
    }

    // === Static Factory Method for JSON Deserialization ===
    public static DotProduct fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            DotProduct block = new DotProduct(sampleTime, outDataType, saturateParam,
                                 blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create DotProduct block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static DotProduct create(String name, String path, NCSLabModel model) {
        return create(name, path, -1.0, "Inherit: Same as first input", false, model);
    }

    public static DotProduct create(String name, String path, double sampleTime,
                             String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter sampleTimeParam = new Parameter(null, 1, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 2, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 3, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));

        DotProduct block = new DotProduct(sampleTimeParam, outDataTypeParam, saturateParam,
                             name, path, "null", model);

        setParameterBlockReference(block, sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter sampleTime) {
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 1, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as first input");
        return new Parameter(null, 2, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 3, "SaturateOnIntegerOverflow", saturateValue);
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

    private static void setParameterBlockReference(DotProduct block, Parameter... parameters) {
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
        identity.put("blockType", "DotProduct");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Two input ports
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));

        // One scalar output port with feedthrough
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Code Generation Methods ===
    public void generateOutputCodeC(CodeStructC code) {
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());

        // Add input/output variables for template
        if (inputPortList != null && inputPortList.size() >= 2 &&
            inputPortList.get(0).getLinkedLine() != null &&
            inputPortList.get(0).getLinkedLine().getLinkedOutputPort() != null &&
            inputPortList.get(1).getLinkedLine() != null &&
            inputPortList.get(1).getLinkedLine().getLinkedOutputPort() != null) {

            String input1Var = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
            String input2Var = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
            context.put("input1Var", input1Var);
            context.put("input2Var", input2Var);

            // Get dimensions from first input
            OutputSignal signal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            context.put("input1Height", signal1.getHeight());
            context.put("input1Width", signal1.getWidth());
        }

        String codeStr = TemplateManager.renderTemplate("c/math/DotProduct/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        // Validate that both inputs have the same dimensions
        OutputSignal signal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (signal1.getHeight() != signal2.getHeight() || signal1.getWidth() != signal2.getWidth()) {
            throw new MatDimException("DotProduct block " + this.blockName +
                ": Input dimensions must match! Input1 [" + signal1.getHeight() + "x" + signal1.getWidth() +
                "] vs Input2 [" + signal2.getHeight() + "x" + signal2.getWidth() + "]");
        }

        // Output is always scalar (1x1)
        OutputPort out = outputPortList.get(0);
        out.setHeight(1);
        out.setWidth(1);
        out.getOutputSignalC().setHeight(1);
        out.getOutputSignalC().setWidth(1);
        out.getOutputSignalC().setDataType(DataType.REAL);
    }

    public void checkDimension() throws MatDimException {
        // Additional dimension checking if needed
    }

    @Override
    public void calculateInit() {
        // Initialization logic for DotProduct block
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK DotProduct block: computes dot product of two vectors
        Data input1Data = inputPortList.get(0).getData();
        Data input2Data = inputPortList.get(1).getData();

        double dotProduct = 0.0;

        if (input1Data.getDataType() == DataType.MATRIX && input2Data.getDataType() == DataType.MATRIX) {
            // Matrix inputs - compute dot product element-wise and sum
            Jama.Matrix input1Matrix = input1Data.getMatrix();
            Jama.Matrix input2Matrix = input2Data.getMatrix();

            for (int i = 0; i < input1Matrix.getRowDimension(); i++) {
                for (int j = 0; j < input1Matrix.getColumnDimension(); j++) {
                    dotProduct += input1Matrix.get(i, j) * input2Matrix.get(i, j);
                }
            }
        } else if (input1Data.getDataType() == DataType.MATRIX) {
            // Input1 is matrix, Input2 is scalar - broadcast scalar
            Jama.Matrix input1Matrix = input1Data.getMatrix();
            double scalar2 = input2Data.getInitValue();

            for (int i = 0; i < input1Matrix.getRowDimension(); i++) {
                for (int j = 0; j < input1Matrix.getColumnDimension(); j++) {
                    dotProduct += input1Matrix.get(i, j) * scalar2;
                }
            }
        } else if (input2Data.getDataType() == DataType.MATRIX) {
            // Input1 is scalar, Input2 is matrix - broadcast scalar
            double scalar1 = input1Data.getInitValue();
            Jama.Matrix input2Matrix = input2Data.getMatrix();

            for (int i = 0; i < input2Matrix.getRowDimension(); i++) {
                for (int j = 0; j < input2Matrix.getColumnDimension(); j++) {
                    dotProduct += scalar1 * input2Matrix.get(i, j);
                }
            }
        } else {
            // Both scalars
            dotProduct = input1Data.getInitValue() * input2Data.getInitValue();
        }

        // Output is always scalar
        Data outputData = new Data(1, 1);
        outputData.setInitValue(dotProduct);
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
