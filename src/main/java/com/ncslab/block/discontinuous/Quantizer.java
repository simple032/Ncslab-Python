package com.ncslab.block.discontinuous;

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
import com.ncslab.dto.block.specialized.discontinuous.QuantizerDto;

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
 * Quantizer block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * Implements signal quantization by discretizing input signals to a specified quantization interval.
 * The output is computed using the formula: y = q * round(u / q) where q is the quantization interval.
 *
 * SIMULINK Parameters:
 * - QuantizationInterval: The step size for quantization (default 0.5)
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * @author NCSLab Team
 * @version 2025
 */
public class Quantizer extends DiscontinuousBlock {
    // === SIMULINK-Compatible Parameters ===
    /** Quantization interval parameter (step size) */
    private final Parameter quantizationInterval;

    /** Sample time parameter */
    private final Parameter sampleTime;

    /** Output data type specification parameter */
    private final Parameter outDataType;

    /** Integer overflow handling parameter */
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("QuantizationInterval", "0.5");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
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

        // Output port defaults (quantizer has feedthrough)
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
    private Quantizer(String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Get parameters by name from the automatically populated parameterList (via parseParameterList())
        this.quantizationInterval = getParameterByName("QuantizationInterval");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Quantizer(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // This calls parseParameterList() automatically

        // Get parameters by name from the automatically populated parameterList
        this.quantizationInterval = getParameterByName("QuantizationInterval");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    /**
     * DTO-NATIVE Constructor - Creates Quantizer block directly from BlockDto DTO
     */
    public Quantizer(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.quantizationInterval = getParameterByName("QuantizationInterval");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Quantizer fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");

            return new Quantizer(blockName, blockPath, blockUUID, model);

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Quantizer block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Quantizer create(String name, String path, String quantizationInterval, NCSLabModel model) {
        return create(name, path, quantizationInterval, -1.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a Quantizer block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param quantizationInterval Quantization step size (must be positive)
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return Quantizer block instance
     */
    public static Quantizer create(String name, String path, String quantizationInterval,
                                  double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        QuantizerDto dto = QuantizerDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .quantizationInterval(com.ncslab.dto.common.TypedParameter.of(quantizationInterval))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Quantizer parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Quantizer(dto, model);
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

    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Quantizer");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }

    // === Getter Methods ===
    public double getQuantizationInterval() {
        if (quantizationInterval == null || quantizationInterval.getData() == null) {
            throw new IllegalStateException("Quantizer block quantization interval parameter is not properly configured");
        }
        return quantizationInterval.getData().getInitValue();
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK Quantizer block: y = q * round(u / q)

        // Fail fast - validate required ports and parameters exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new IllegalStateException("Quantizer block cannot calculate output: no input ports configured");
        }
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new IllegalStateException("Quantizer block cannot calculate output: no output ports configured");
        }

        InputPort inputPort = inputPortList.get(0);
        if (inputPort == null || inputPort.getData() == null) {
            throw new IllegalStateException("Quantizer block cannot calculate output: input data is null");
        }

        OutputPort outputPort = outputPortList.get(0);
        if (outputPort == null) {
            throw new IllegalStateException("Quantizer block cannot calculate output: output port is null");
        }

        Data inputData = inputPort.getData();
        double q = getQuantizationInterval();
        Data outputData;

        if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix input - apply quantization element-wise
            Matrix inputMatrix = inputData.getMatrix();
            Matrix outputMatrix = new Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());

            for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                    double value = inputMatrix.get(i, j);
                    double quantizedValue = applyQuantization(value, q);
                    outputMatrix.set(i, j, quantizedValue);
                }
            }
            outputData = new Data(outputMatrix);
        } else {
            // Scalar input
            double inputValue = inputData.getInitValue();
            double quantizedValue = applyQuantization(inputValue, q);
            outputData = new Data(1, 1);
            outputData.setInitValue(quantizedValue);
        }

        outputPort.setData(outputData);
    }

    /**
     * Apply quantization to a value using formula: y = q * round(u / q)
     * @param value Input value to quantize
     * @param quantizationInterval Quantization step size
     * @return Quantized value
     */
    protected double applyQuantization(double value, double quantizationInterval) {
        return quantizationInterval * Math.round(value / quantizationInterval);
    }

    // === Code Generation Methods ===
    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Fail fast - validate required ports exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new BlockCreationException("Quantizer block requires input port for code generation");
        }

        // Fail fast - validate required parameters exist
        if (quantizationInterval == null) {
            throw new BlockCreationException("Quantizer block requires quantization interval parameter");
        }

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/Quantizer/output.vm", context);
        code.addOutputCode(codeStr);
    }

    // === Dimension Management ===
    public void updateDimension() throws MatDimException {
        // Fail fast - validate required ports and connections exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new MatDimException("Quantizer block cannot update dimensions: no output ports configured");
        }
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new MatDimException("Quantizer block cannot update dimensions: no input ports configured");
        }

        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);

        if (out == null) {
            throw new MatDimException("Quantizer block cannot update dimensions: output port is null");
        }
        if (in == null) {
            throw new MatDimException("Quantizer block cannot update dimensions: input port is null");
        }
        if (in.getLinkedLine() == null || in.getLinkedLine().getLinkedOutputPort() == null) {
            throw new MatDimException("Quantizer block cannot update dimensions: input not properly connected");
        }

        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (signal == null) {
            throw new MatDimException("Quantizer block cannot update dimensions: input signal is null");
        }

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    public void checkDimension() throws MatDimException {
        // Check dimensions if needed
    }
}
