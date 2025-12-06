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
import com.ncslab.dto.block.specialized.discontinuous.SaturationDynamicDto;

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
 * SaturationDynamic block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * Implements signal saturation with dynamically adjustable upper and lower bounds provided
 * as input signals. Unlike the Saturation block which uses fixed parameter values, this block
 * receives the limits from input ports, allowing real-time adjustment of saturation boundaries.
 *
 * Input Ports:
 * - Port 1: Signal input (u)
 * - Port 2: Upper limit (up)
 * - Port 3: Lower limit (lo)
 *
 * Output: y = min(max(u, lo), up)
 *
 * SIMULINK Parameters:
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * @author NCSLab Team
 * @version 2025
 */
public class SaturationDynamic extends DiscontinuousBlock {

    // === SIMULINK-Compatible Parameters ===
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
        inputNames.add("in1");  // Signal input
        inputNames.add("in2");  // Upper limit
        inputNames.add("in3");  // Lower limit

        // Input port defaults
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

        Map<String, Object> input3 = new HashMap<>();
        input3.put("name", "in3");
        input3.put("width", 1);
        input3.put("height", 1);
        input3.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input3);

        // Output port defaults (saturation has feedthrough)
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
    private SaturationDynamic(String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Get parameters by name from the automatically populated parameterList (via parseParameterList())
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Create ports: signal, upper limit, lower limit
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        inputPortList.add(new InputPort(this, 3));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public SaturationDynamic(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // This calls parseParameterList() automatically

        // Get parameters by name from the automatically populated parameterList
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Create ports: signal, upper limit, lower limit
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        inputPortList.add(new InputPort(this, 3));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    /**
     * DTO-NATIVE Constructor - Creates SaturationDynamic block directly from BlockDto DTO
     */
    public SaturationDynamic(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        inputPortList.add(new InputPort(this, 3));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Static Factory Method for JSON Deserialization ===
    public static SaturationDynamic fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");

            return new SaturationDynamic(blockName, blockPath, blockUUID, model);

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create SaturationDynamic block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static SaturationDynamic create(String name, String path, NCSLabModel model) {
        return create(name, path, -1.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a SaturationDynamic block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return SaturationDynamic block instance
     */
    public static SaturationDynamic create(String name, String path,
                                          double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        SaturationDynamicDto dto = SaturationDynamicDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid SaturationDynamic parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new SaturationDynamic(dto, model);
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
        identity.put("blockType", "SaturationDynamic");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }

    // === Getter Methods ===
    public double getSampleTime() {
        if (sampleTime == null || sampleTime.getData() == null) {
            return -1.0;
        }
        return sampleTime.getData().getInitValue();
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK SaturationDynamic block: limits signal to dynamically specified range

        // Fail fast - validate required ports exist
        if (inputPortList == null || inputPortList.size() < 3) {
            throw new IllegalStateException("SaturationDynamic block requires 3 input ports");
        }
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new IllegalStateException("SaturationDynamic block cannot calculate output: no output ports configured");
        }

        InputPort signalPort = inputPortList.get(0);
        InputPort upperLimitPort = inputPortList.get(1);
        InputPort lowerLimitPort = inputPortList.get(2);

        if (signalPort == null || signalPort.getData() == null) {
            throw new IllegalStateException("SaturationDynamic block cannot calculate output: signal input data is null");
        }
        if (upperLimitPort == null || upperLimitPort.getData() == null) {
            throw new IllegalStateException("SaturationDynamic block cannot calculate output: upper limit input data is null");
        }
        if (lowerLimitPort == null || lowerLimitPort.getData() == null) {
            throw new IllegalStateException("SaturationDynamic block cannot calculate output: lower limit input data is null");
        }

        OutputPort outputPort = outputPortList.get(0);
        if (outputPort == null) {
            throw new IllegalStateException("SaturationDynamic block cannot calculate output: output port is null");
        }

        Data signalData = signalPort.getData();
        Data upperLimitData = upperLimitPort.getData();
        Data lowerLimitData = lowerLimitPort.getData();

        // Get scalar limit values (limits are typically scalars)
        double upperLim = upperLimitData.getInitValue();
        double lowerLim = lowerLimitData.getInitValue();

        Data outputData;

        if (signalData.getDataType() == DataType.MATRIX) {
            // Matrix input - apply saturation element-wise
            Matrix inputMatrix = signalData.getMatrix();
            Matrix outputMatrix = new Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());

            for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                    double value = inputMatrix.get(i, j);
                    double saturatedValue = applySaturation(value, lowerLim, upperLim);
                    outputMatrix.set(i, j, saturatedValue);
                }
            }
            outputData = new Data(outputMatrix);
        } else {
            // Scalar input
            double inputValue = signalData.getInitValue();
            double saturatedValue = applySaturation(inputValue, lowerLim, upperLim);
            outputData = new Data(1, 1);
            outputData.setInitValue(saturatedValue);
        }

        outputPort.setData(outputData);
    }

    /**
     * Apply saturation limits to a value.
     * @param value Input value to saturate
     * @param lowerLimit Lower saturation limit
     * @param upperLimit Upper saturation limit
     * @return Saturated value within specified limits
     */
    protected double applySaturation(double value, double lowerLimit, double upperLimit) {
        if (value < lowerLimit) {
            return lowerLimit;
        } else if (value > upperLimit) {
            return upperLimit;
        } else {
            return value;
        }
    }

    // === Code Generation Methods ===
    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Fail fast - validate required ports exist
        if (inputPortList == null || inputPortList.size() < 3) {
            throw new BlockCreationException("SaturationDynamic block requires 3 input ports for code generation");
        }

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/SaturationDynamic/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        // No initialization needed for stateless dynamic saturation
    }

    // === Dimension Management ===
    public void updateDimension() throws MatDimException {
        // Fail fast - validate required ports and connections exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new MatDimException("SaturationDynamic block cannot update dimensions: no output ports configured");
        }
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new MatDimException("SaturationDynamic block cannot update dimensions: no input ports configured");
        }

        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);

        if (out == null) {
            throw new MatDimException("SaturationDynamic block cannot update dimensions: output port is null");
        }
        if (in == null) {
            throw new MatDimException("SaturationDynamic block cannot update dimensions: input port is null");
        }
        if (in.getLinkedLine() == null || in.getLinkedLine().getLinkedOutputPort() == null) {
            throw new MatDimException("SaturationDynamic block cannot update dimensions: input not properly connected");
        }

        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (signal == null) {
            throw new MatDimException("SaturationDynamic block cannot update dimensions: input signal is null");
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
