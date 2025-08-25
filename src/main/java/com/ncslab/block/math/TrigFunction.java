package com.ncslab.block.math;

import com.ncslab.block.data.Data;
import com.ncslab.block.io.Parameter;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.TrigFunctionDto;
import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.io.InputPort;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * TrigFunction block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Function: Trigonometric function (sin, cos, tan, asin, acos, atan, atan2)
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class TrigFunction extends MathBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter function;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Operational Settings ===
    private final String trigFunction;
    
    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Function", "sin");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
        
        // SIMULINK parameter names
        
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }
    
    // === Private Constructor with Typed Parameters ===
    private TrigFunction(Parameter function, Parameter sampleTime, Parameter outDataType, 
                        Parameter saturateOnIntegerOverflow, String blockName, String blockPath, 
                        String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Extract function value
        this.trigFunction = function.getInitString();
        
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
    public TrigFunction(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        
        // Extract legacy function
        String trigFuncString;
        if (paramValues.has("Function")) {
            trigFuncString = paramValues.getString("Function");
        } else {
            trigFuncString = paramValues.getString("TrigonometricFunction");
        }
        this.trigFunction = trigFuncString;
        
        // Create legacy function parameter
        this.function = new Parameter(this, 1, "Function", trigFuncString);
        
        // Create missing SIMULINK parameters with defaults
        this.sampleTime = new Parameter(this, 2, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 3, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 4, "SaturateOnIntegerOverflow", "off");
        
        // Add all parameters to parameter list
        
        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates TrigFunction block directly from BlockDto DTO
     */
    public TrigFunction(TrigFunctionDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.function = new Parameter(this, 1, "Function", "sin");
        this.trigFunction = "sin"; // Initialize final field
        this.sampleTime = new Parameter(this, 2, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 3, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 4, "SaturateOnIntegerOverflow", "off");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }


    // === Static Factory Method for JSON Deserialization ===
    public static TrigFunction fromJSON(JSONObject blockJSON, NCSLabModel model) {
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
            
            TrigFunction block = new TrigFunction(function, sampleTime, outDataType, saturateParam,
                                                 blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, function, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create TrigFunction block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static TrigFunction create(String name, String path, String trigFunction, NCSLabModel model) {
        return create(name, path, trigFunction, -1.0, "Inherit: Same as input", false, model);
    }
    
    public static TrigFunction create(String name, String path, String trigFunction, double sampleTime,
                                     String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter function = new Parameter(null, 1, "Function", trigFunction);
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 3, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 4, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));
        
        TrigFunction block = new TrigFunction(function, sampleTimeParam, outDataTypeParam, saturateParam,
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
        String[] validFunctions = {"sin", "cos", "tan", "asin", "acos", "atan", "atan2"};
        boolean isValid = false;
        for (String validFunc : validFunctions) {
            if (validFunc.equals(functionValue)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new IllegalArgumentException("Invalid trigonometric function: " + functionValue);
        }
        
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createFunctionFromJSON(JSONObject paramValues, String blockName) {
        String functionValue;
        if (paramValues.has("Function")) {
            functionValue = paramValues.getString("Function");
        } else {
            functionValue = paramValues.optString("TrigonometricFunction", "sin");
        }
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
    
    private static void setParameterBlockReference(TrigFunction block, Parameter... parameters) {
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
        identity.put("blockType", "TrigFunction");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    // === Port Initialization ===
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
        
        // Add second input port for atan2 operation
        if (trigFunction.equals("atan2")) {
            inputPortList.add(new InputPort(this, 2));
            inputNames.clear();
            inputNames.add("in1");
            inputNames.add("in2");
        }
    }

    // === Code Generation Methods (using Velocity templates) ===
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        
        context.put("block", this);
        context.put("trigFunction", this.trigFunction);
        context.put("function", this.function);
        context.put("sampleTime", this.sampleTime);
        context.put("outDataType", this.outDataType);
        context.put("saturateOnIntegerOverflow", this.saturateOnIntegerOverflow);
        
        // Add input signal and data type context
        if (!inputPortList.isEmpty()) {
            OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            context.put("signal", signal);
            context.put("realDataType", com.ncslab.block.data.DataType.REAL);
        }
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/math/TrigFunction/output.vm", context);
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
        // Initialization logic for TrigFunction block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();
        double trigValue;

        if (!trigFunction.equals("atan2")) {
            trigValue = applyTrigonometricFunction(inputData.getInitValue(), trigFunction);
        } else {
            Data secondInputData = inputPortList.get(1).getData();
            trigValue = Math.atan2(inputData.getInitValue(), secondInputData.getInitValue());
        }

        Data resultData = new Data(trigValue);
        out.setData(resultData);
    }

    private double applyTrigonometricFunction(double value, String function) {
        switch (function) {
            case "sin":
                return Math.sin(value);
            case "cos":
                return Math.cos(value);
            case "tan":
                return Math.tan(value);
            case "asin":
                return Math.asin(value);
            case "acos":
                return Math.acos(value);
            case "atan":
                return Math.atan(value);
            default:
                return value;
        }
    }
}

