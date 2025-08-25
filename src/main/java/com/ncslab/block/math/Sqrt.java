package com.ncslab.block.math;

import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.SqrtDto;
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
 * Sqrt block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Function: Sqrt function type (sqrt, rSqrt, signedSqrt)  
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Sqrt extends MathBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter function;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Function", "sqrt");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
        
        // SIMULINK parameter names
        
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }
    
    // === Private Constructor with Typed Parameters ===
    private Sqrt(Parameter function, Parameter sampleTime, Parameter outDataType, 
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
        // Initialize ports
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Sqrt(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        
        // Create legacy function parameter
        String functionValue;
        if (paramValues.has("SqrtFunction"))
            functionValue = paramValues.getString("SqrtFunction");
        else
            functionValue = paramValues.getString("Function");
        this.function = new Parameter(this, 1, "Function", functionValue);
        
        // Create missing SIMULINK parameters with defaults
        this.sampleTime = new Parameter(this, 2, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 3, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 4, "SaturateOnIntegerOverflow", "off");
        
        // Add all parameters to parameter list
        
        // Initialize ports
        initializePorts();
    }
    
    /**
     * DTO Constructor - Creates Sqrt block directly from SqrtDto
     * @param dto The SqrtDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     */
    public Sqrt(SqrtDto dto, NCSLabModel model) {
        super(dto, model);
        
        // Extract and validate DTO parameters
        String functionValue = dto.getFunctionValue();
        String sampleTimeStr = dto.getSampleTime() != null ? dto.getSampleTime().getAsString() : "-1";
        String outDataTypeValue = dto.getOutDataTypeStrValue();
        String saturateValue = dto.getSaturateOnIntegerOverflowValue() ? "on" : "off";
        
        // Create parameters from DTO values
        this.function = new Parameter(this, 1, "Function", functionValue);
        this.sampleTime = new Parameter(this, 2, "SampleTime", sampleTimeStr);
        this.outDataType = new Parameter(this, 3, "OutDataTypeStr", outDataTypeValue);
        this.saturateOnIntegerOverflow = new Parameter(this, 4, "SaturateOnIntegerOverflow", saturateValue);
        
        // Validate parameters
        validateParameters(this.function, this.sampleTime);
        
        // Initialize ports
        initializePorts();
        
        System.out.println("DTO-NATIVE: Sqrt block created successfully from SqrtDto - " + dto.getBlockName());
    }
    
    /**
     * DTO-NATIVE Constructor - Creates Sqrt block directly from BlockDto DTO
     * @deprecated Use specific SqrtDto constructor instead
     */
    @Deprecated
    
    // === Static Factory Method for JSON Deserialization ===
    public static Sqrt fromJSON(JSONObject blockJSON, NCSLabModel model) {
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
            
            Sqrt block = new Sqrt(function, sampleTime, outDataType, saturateParam,
                                 blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, function, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Sqrt block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Sqrt create(String name, String path, String sqrtFunction, NCSLabModel model) {
        return create(name, path, sqrtFunction, -1.0, "Inherit: Same as input", false, model);
    }
    
    public static Sqrt create(String name, String path, String sqrtFunction, double sampleTime,
                             String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter function = new Parameter(null, 1, "Function", sqrtFunction);
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 3, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 4, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));
        
        Sqrt block = new Sqrt(function, sampleTimeParam, outDataTypeParam, saturateParam,
                             name, path, "null", model);
        
        setParameterBlockReference(block, function, sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter function, Parameter sampleTime) {
        String functionValue = function.getInitString();
        if (functionValue == null || functionValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Function parameter cannot be empty");
        }
        
        // Validate function type
        if (!functionValue.equals("sqrt") && !functionValue.equals("rSqrt") && !functionValue.equals("signedSqrt")) {
            throw new IllegalArgumentException("Function must be 'sqrt', 'rSqrt', or 'signedSqrt'");
        }
        
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createFunctionFromJSON(JSONObject paramValues, String blockName) {
        String functionValue;
        if (paramValues.has("SqrtFunction"))
            functionValue = paramValues.getString("SqrtFunction");
        else
            functionValue = paramValues.optString("Function", "sqrt");
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
    
    private static void setParameterBlockReference(Sqrt block, Parameter... parameters) {
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
        identity.put("blockType", "Sqrt");
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

    // === Code Generation Methods (preserved from original) ===
    public void generateOutputCodeC(CodeStructC code) {
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("function", function);

        String codeStr = TemplateManager.renderTemplate("c/math/Sqrt/output.vm", context);
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
        // Initialization logic for Sqrt block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();
        String functionValue = function.getInitString();
        
        double sqrtValue;
        double inputValue = inputData.getInitValue();
        
        switch (functionValue) {
            case "sqrt":
                sqrtValue = Math.sqrt(inputValue);
                break;
            case "rSqrt":
                sqrtValue = 1.0 / Math.sqrt(inputValue);
                break;
            case "signedSqrt":
                sqrtValue = inputValue >= 0 ? Math.sqrt(inputValue) : -Math.sqrt(-inputValue);
                break;
            default:
                sqrtValue = Math.sqrt(inputValue);
                break;
        }
        
        Data resultData = new Data(sqrtValue);
        out.setData(resultData);
    }
}

