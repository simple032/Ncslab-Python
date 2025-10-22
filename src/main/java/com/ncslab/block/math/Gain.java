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
import com.ncslab.dto.block.specialized.math.GainDto;

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
 * Gain block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * Multiplies input signal by a configurable gain value using either element-wise
 * or matrix multiplication. Supports scalar, vector, and matrix gain values.
 *
 * SIMULINK Parameters:
 * - Gain: Gain value (scalar or matrix)
 * - Multiplication: Element-wise (K.*u) or Matrix (K*u) multiplication mode
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * @author NCSLab Team
 * @version 2025
 */
public class Gain extends MathBlock {

    // === SIMULINK-Compatible Parameters ===
    /** Gain value parameter (scalar, vector, or matrix) */
    @Getter
    protected Parameter gain;
    
    /** Sample time parameter for discrete operation */
    private final Parameter sampleTime;
    
    /** Output data type specification parameter */
    private final Parameter outDataType;
    
    /** Integer overflow handling parameter */
    private final Parameter saturateOnIntegerOverflow;
    
    // === Operational Settings ===
    /** True for matrix multiplication (K*u), false for element-wise (K.*u) */
    private final boolean matrixMultiplication;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Gain", "1");
        PARAMETER_DEFAULTS.put("Multiplication", "Element-wise(K.*u)");
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
        
        // Output port defaults (gain has feedthrough)
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
    private Gain(Parameter gain, Parameter sampleTime, Parameter outDataType,
                Parameter saturateOnIntegerOverflow, boolean matrixMultiplication,
                String blockName, String blockPath, String blockUUID,
                NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(gain, sampleTime);

        // Assign parameters
        this.gain = Objects.requireNonNull(gain, "Gain parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        this.matrixMultiplication = matrixMultiplication;
        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Gain(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Use name-based parameter access instead of index-based
        this.gain = getParameterByName("Gain");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Extract multiplication mode for backward compatibility
        Parameter multiplicationParam = getParameterByName("Multiplication");
        this.matrixMultiplication = "Matrix(*)".equals(multiplicationParam.getInitString());

        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates Gain block directly from BlockDto DTO
     */
    public Gain(GainDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.gain = getParameterByName("Gain");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Extract multiplication mode for backward compatibility
        Parameter multiplicationParam = getParameterByName("Multiplication");
        this.matrixMultiplication = multiplicationParam != null && "Matrix(*)".equals(multiplicationParam.getInitString());

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }



    // === Static Factory Method for JSON Deserialization ===
    public static Gain fromJSON(JSONObject blockJSON, NCSLabModel model) {
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
            Parameter gain = createGainFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            // Parse multiplication mode
            boolean matrixMultiplication = "Matrix(*)".equals(paramValues.optString("Multiplication", "Element-wise(*)"));

            Gain block = new Gain(gain, sampleTime, outDataType, saturateParam,
                                 matrixMultiplication, blockName, blockPath, blockUUID, model);

            // Set block reference in parameters (required for Parameter constructor compatibility)
            setParameterBlockReference(block, gain, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Gain block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static Gain create(String name, String path, double gainValue, NCSLabModel model) {
        return create(name, path, gainValue, false, -1.0, "Inherit: Same as input", false, model);
    }

    public static Gain create(String name, String path, double gainValue,
                             boolean matrixMultiplication, double sampleTime,
                             String outDataType, boolean saturateOnOverflow,
                             NCSLabModel model) {
        // Create parameters
        Parameter gain = new Parameter(null, 1, "Gain", String.valueOf(gainValue));
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 3, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 4, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));

        Gain block = new Gain(gain, sampleTimeParam, outDataTypeParam, saturateParam,
                             matrixMultiplication, name, path, "null", model);

        // Set block reference in parameters
        setParameterBlockReference(block, gain, sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter gain, Parameter sampleTime) {
        // Validate gain value
        if (gain.getDataType() == DataType.REAL) {
            double gainValue = gain.getDouble();
            if (Double.isNaN(gainValue) || Double.isInfinite(gainValue)) {
                throw new IllegalArgumentException("Gain value must be finite");
            }
        }

        // Validate sample time
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createGainFromJSON(JSONObject paramValues, String blockName) {
        String gainValue = paramValues.optString("Gain", "1");
        return new Parameter(null, 1, "Gain", gainValue);
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

    private static void setParameterBlockReference(Gain block, Parameter... parameters) {
        for (Parameter param : parameters) {
            // This is a workaround for the Parameter constructor requiring a Block reference
            // In a future refactor, Parameter should be made immutable
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
            } catch (Exception e) {
                // Fallback: create new parameter with block reference
                // This should be improved in future Parameter class refactoring
            }
        }
    }

    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Gain");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Create input port
        inputPortList.add(new InputPort(this, 1));

        // Create output port with feedthrough (gain is instantaneous)
        outputPortList.add(new OutputPort(this, 1, true));
    }
    // === Code Generation Methods (preserved from original) ===
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String codeStr = TemplateManager.renderTemplate("m/math/Gain/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add Gain-specific context
        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        // For C templates - use string variable names, not objects
        context.put("outputSignal", out.getOutputSignalC().getName());
        context.put("inputSignal", ops.getOutputSignalC().getName());
        // For MATLAB templates that need objects - provide separate object references
        context.put("outputSignalObject", out.getOutputSignalC());
        context.put("inputSignalObject", ops.getOutputSignalC());
        context.put("matrixMultiplication", this.matrixMultiplication);
        
        String codeStr = TemplateManager.renderTemplate("m/math/Gain/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // String codeStr = TemplateManager.renderTemplate("c/math/Gain/init.vm", context);
        // code.addInitCode(codeStr);
    }

    private boolean isMatrixMultiplication() {
        return matrixMultiplication;
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add Gain-specific context
        context.put("multiplication", isMatrixMultiplication());
        context.put("matrixMultiplication", isMatrixMultiplication());

        String codeStr = TemplateManager.renderTemplate("c/math/Gain/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (!this.matrixMultiplication) {
            switch (getGain().getDataType()) {
                case REAL:
                    out.setHeight(signal.getHeight());
                    out.setWidth(signal.getWidth());
                    out.getOutputSignalC().setHeight(signal.getHeight());
                    out.getOutputSignalC().setWidth(signal.getWidth());
                    out.getOutputSignalC().setDataType(signal.getDataType());
                    break;
                case MATRIX:
                    switch (signal.getDataType()) {
                        case REAL:
                            out.setHeight(getGain().getHeight());
                            out.setWidth(getGain().getWidth());
                            out.getOutputSignalC().setHeight(getGain().getHeight());
                            out.getOutputSignalC().setWidth(getGain().getWidth());
                            out.getOutputSignalC().setDataType(getGain().getDataType());
                            break;
                        case MATRIX:
                            if (signal.getHeight() != getGain().getHeight() || signal.getWidth() != getGain().getWidth()) {
                                MatDimException e = new MatDimException("Block " + this.blockName + " input dimension doesn't match the gain dimension!\n \n");
                                throw(e);
                            }
                            out.setHeight(signal.getHeight());
                            out.setWidth(signal.getWidth());
                            out.getOutputSignalC().setHeight(signal.getHeight());
                            out.getOutputSignalC().setWidth(signal.getWidth());
                            out.getOutputSignalC().setDataType(signal.getDataType());
                            break;
                    }
                    break;
            }
        } else {
            if (signal.getWidth() != getGain().getHeight()) {
                MatDimException e = new MatDimException("Block " + this.blockName + " input dimension doesn't match the gain dimension!\n \n");
                throw(e);
            }
            out.setHeight(signal.getHeight());
            out.setWidth(getGain().getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(getGain().getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        }
    }

    public void checkDimension() throws MatDimException {
        // No additional dimension checks needed for gain block
    }

    // === Runtime Simulation API (restored) ===
    @Override
    public void calculateInit() {
        
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data data = inputPortList.get(0).getData().times(gain.getData());        
        out.setData(data);
    }
}
