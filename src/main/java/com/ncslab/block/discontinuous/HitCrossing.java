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
import com.ncslab.dto.block.specialized.discontinuous.HitCrossingDto;

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
 * HitCrossing block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * Detects when a signal crosses a specified threshold value.
 * Output is 1 when crossing detected, 0 otherwise.
 *
 * SIMULINK Parameters:
 * - HitCrossingOffset: Crossing level/threshold (default: 0.0)
 * - HitCrossingDirection: "rising", "falling", or "either" (default: "either")
 * - ShowOutputPort: Whether to show output port (default: true)
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 *
 * @author NCSLab Team
 * @version 2025
 */
public class HitCrossing extends DiscontinuousBlock {
    // === SIMULINK-Compatible Parameters ===
    private final Parameter hitCrossingOffset;
    private final Parameter hitCrossingDirection;
    private final Parameter showOutputPort;
    private final Parameter sampleTime;
    private final Parameter outDataType;

    // === State Variables ===
    /** Previous input value for crossing detection */
    private double previousValue;

    /** Previous input matrix for matrix crossing detection */
    private Matrix previousMatrix;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("HitCrossingOffset", "0.0");
        PARAMETER_DEFAULTS.put("HitCrossingDirection", "either");
        PARAMETER_DEFAULTS.put("ShowOutputPort", "on");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
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

        // Output port defaults (crossing detection has no feedthrough)
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
    private HitCrossing(String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Get parameters by name from the automatically populated parameterList (via parseParameterList())
        this.hitCrossingOffset = getParameterByName("HitCrossingOffset");
        this.hitCrossingDirection = getParameterByName("HitCrossingDirection");
        this.showOutputPort = getParameterByName("ShowOutputPort");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Initialize state
        this.previousValue = 0.0;
        this.previousMatrix = null;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, false)); // No feedthrough
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public HitCrossing(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // This calls parseParameterList() automatically

        // Get parameters by name from the automatically populated parameterList
        this.hitCrossingOffset = getParameterByName("HitCrossingOffset");
        this.hitCrossingDirection = getParameterByName("HitCrossingDirection");
        this.showOutputPort = getParameterByName("ShowOutputPort");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Initialize state
        this.previousValue = 0.0;
        this.previousMatrix = null;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, false)); // No feedthrough
    }

    /**
     * DTO-NATIVE Constructor - Creates HitCrossing block directly from BlockDto DTO
     */
    public HitCrossing(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.hitCrossingOffset = getParameterByName("HitCrossingOffset");
        this.hitCrossingDirection = getParameterByName("HitCrossingDirection");
        this.showOutputPort = getParameterByName("ShowOutputPort");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Initialize state
        this.previousValue = 0.0;
        this.previousMatrix = null;

        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, false)); // No feedthrough
    }

    // === Static Factory Method for JSON Deserialization ===
    public static HitCrossing fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");

            return new HitCrossing(blockName, blockPath, blockUUID, model);

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create HitCrossing block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static HitCrossing create(String name, String path, double offset, String direction, NCSLabModel model) {
        return create(name, path, offset, direction, true, -1.0, "Inherit: Same as input", model);
    }

    /**
     * Create a HitCrossing block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param offset Crossing threshold/offset
     * @param direction Crossing direction ("rising", "falling", or "either")
     * @param showOutput Whether to show output port
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param model Parent model
     * @return HitCrossing block instance
     */
    public static HitCrossing create(String name, String path, double offset, String direction,
                                    boolean showOutput, double sampleTime, String outDataType, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        HitCrossingDto dto = HitCrossingDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .hitCrossingOffset(com.ncslab.dto.common.TypedParameter.of(offset))
            .hitCrossingDirection(com.ncslab.dto.common.TypedParameter.of(direction))
            .showOutputPort(com.ncslab.dto.common.TypedParameter.of(showOutput))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid HitCrossing parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new HitCrossing(dto, model);
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
        identity.put("blockType", "HitCrossing");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }

    // === Getter Methods ===
    public double getHitCrossingOffset() {
        if (hitCrossingOffset == null || hitCrossingOffset.getData() == null) {
            throw new IllegalStateException("HitCrossing block offset parameter is not properly configured");
        }
        return hitCrossingOffset.getData().getInitValue();
    }

    public String getHitCrossingDirection() {
        if (hitCrossingDirection == null || hitCrossingDirection.getData() == null) {
            return "either"; // Default
        }
        return hitCrossingDirection.getData().getDataString();
    }

    @Override
    public void calculateInit() {
        super.calculateInit();

        // Initialize previous value to current input
        if (inputPortList != null && !inputPortList.isEmpty()) {
            InputPort inputPort = inputPortList.get(0);
            if (inputPort != null && inputPort.getData() != null) {
                Data inputData = inputPort.getData();
                if (inputData.getDataType() == DataType.MATRIX) {
                    previousMatrix = inputData.getMatrix().copy();
                } else {
                    previousValue = inputData.getInitValue();
                }
            }
        }
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK HitCrossing block: detects signal crossing threshold

        // Fail fast - validate required ports and parameters exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new IllegalStateException("HitCrossing block cannot calculate output: no input ports configured");
        }
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new IllegalStateException("HitCrossing block cannot calculate output: no output ports configured");
        }

        InputPort inputPort = inputPortList.get(0);
        if (inputPort == null || inputPort.getData() == null) {
            throw new IllegalStateException("HitCrossing block cannot calculate output: input data is null");
        }

        OutputPort outputPort = outputPortList.get(0);
        if (outputPort == null) {
            throw new IllegalStateException("HitCrossing block cannot calculate output: output port is null");
        }

        Data inputData = inputPort.getData();
        double threshold = getHitCrossingOffset();
        String direction = getHitCrossingDirection();
        Data outputData;

        if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix input - check crossing element-wise
            Matrix inputMatrix = inputData.getMatrix();
            Matrix outputMatrix = new Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());

            // Initialize previous matrix if needed
            if (previousMatrix == null ||
                previousMatrix.getRowDimension() != inputMatrix.getRowDimension() ||
                previousMatrix.getColumnDimension() != inputMatrix.getColumnDimension()) {
                previousMatrix = inputMatrix.copy();
            }

            for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                    double currentValue = inputMatrix.get(i, j);
                    double prevValue = previousMatrix.get(i, j);
                    double crossingDetected = detectCrossing(prevValue, currentValue, threshold, direction);
                    outputMatrix.set(i, j, crossingDetected);
                }
            }
            outputData = new Data(outputMatrix);
        } else {
            // Scalar input
            double currentValue = inputData.getInitValue();
            double crossingDetected = detectCrossing(previousValue, currentValue, threshold, direction);
            outputData = new Data(1, 1);
            outputData.setInitValue(crossingDetected);
        }

        outputPort.setData(outputData);
    }

    @Override
    public void calculateUpdate(double t) {
        // Update previous value for next crossing detection
        if (inputPortList != null && !inputPortList.isEmpty()) {
            InputPort inputPort = inputPortList.get(0);
            if (inputPort != null && inputPort.getData() != null) {
                Data inputData = inputPort.getData();
                if (inputData.getDataType() == DataType.MATRIX) {
                    previousMatrix = inputData.getMatrix().copy();
                } else {
                    previousValue = inputData.getInitValue();
                }
            }
        }
    }

    /**
     * Detect if a crossing occurred between previous and current value.
     * @param prevValue Previous input value
     * @param currValue Current input value
     * @param threshold Crossing threshold
     * @param direction Crossing direction ("rising", "falling", or "either")
     * @return 1.0 if crossing detected, 0.0 otherwise
     */
    protected double detectCrossing(double prevValue, double currValue, double threshold, String direction) {
        boolean risingCrossing = prevValue < threshold && currValue >= threshold;
        boolean fallingCrossing = prevValue > threshold && currValue <= threshold;

        boolean crossed = false;
        switch (direction) {
            case "rising":
                crossed = risingCrossing;
                break;
            case "falling":
                crossed = fallingCrossing;
                break;
            case "either":
                crossed = risingCrossing || fallingCrossing;
                break;
            default:
                crossed = risingCrossing || fallingCrossing; // Default to "either"
                break;
        }

        return crossed ? 1.0 : 0.0;
    }

    // === Code Generation Methods ===
    public void generateArraysCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add previous value state variable to context
        if (inputPortList != null && !inputPortList.isEmpty()) {
            InputPort inputPort = inputPortList.get(0);
            if (inputPort != null) {
                context.put("inputHeight", inputPort.getHeight());
                context.put("inputWidth", inputPort.getWidth());
            }
        }

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/HitCrossing/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Fail fast - validate required parameters exist
        if (hitCrossingOffset == null) {
            throw new BlockCreationException("HitCrossing block requires crossing offset parameter for initialization");
        }
        if (hitCrossingDirection == null) {
            throw new BlockCreationException("HitCrossing block requires crossing direction parameter for initialization");
        }

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/HitCrossing/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Fail fast - validate required ports exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new BlockCreationException("HitCrossing block requires input port for code generation");
        }

        // Fail fast - validate required parameters exist
        if (hitCrossingOffset == null) {
            throw new BlockCreationException("HitCrossing block requires crossing offset parameter");
        }
        if (hitCrossingDirection == null) {
            throw new BlockCreationException("HitCrossing block requires crossing direction parameter");
        }

        // Add direction string to context
        context.put("crossingDirection", getHitCrossingDirection());

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/HitCrossing/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateUpdateCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/HitCrossing/update.vm", context);
        code.addUpdateCode(codeStr);
    }

    // === Dimension Management ===
    public void updateDimension() throws MatDimException {
        // Fail fast - validate required ports and connections exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new MatDimException("HitCrossing block cannot update dimensions: no output ports configured");
        }
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new MatDimException("HitCrossing block cannot update dimensions: no input ports configured");
        }

        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);

        if (out == null) {
            throw new MatDimException("HitCrossing block cannot update dimensions: output port is null");
        }
        if (in == null) {
            throw new MatDimException("HitCrossing block cannot update dimensions: input port is null");
        }
        if (in.getLinkedLine() == null || in.getLinkedLine().getLinkedOutputPort() == null) {
            throw new MatDimException("HitCrossing block cannot update dimensions: input not properly connected");
        }

        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (signal == null) {
            throw new MatDimException("HitCrossing block cannot update dimensions: input signal is null");
        }

        // Output has same dimensions as input
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
