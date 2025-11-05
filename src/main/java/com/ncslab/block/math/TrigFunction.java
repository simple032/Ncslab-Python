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

        // Add parameters to parameterList for template context population
        parameterList.add(this.function);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);


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
        this.function = getParameterByName("Function");
        
        // Create missing SIMULINK parameters with defaults
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates TrigFunction block directly from BlockDto DTO
     */
    public TrigFunction(TrigFunctionDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = blockDto.validate();
        if (!validation.isValid()) {
            throw new BlockCreationException("DTO validation failed: " + validation.getErrors());
        }

        // Retrieve parameters initialized by base class
        // Note: TrigFunctionDto uses "FunctionType" parameter name
        this.function = getParameterByName("Function");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // CRITICAL: Extract function value BEFORE initializePorts()
        // initializePorts() needs trigFunction to create correct number of inputs for atan2
        this.trigFunction = this.function.getInitString();

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
    
    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static TrigFunction create(String name, String path, String trigFunction, NCSLabModel model) {
        return create(name, path, trigFunction, -1.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a TrigFunction block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param trigFunction Trigonometric function type (sin, cos, tan, asin, acos, atan, atan2)
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return TrigFunction block instance
     */
    public static TrigFunction create(String name, String path, String trigFunction, double sampleTime,
                                     String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        TrigFunctionDto dto = TrigFunctionDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .functionType(com.ncslab.dto.common.TypedParameter.of(trigFunction))
            .outputSignalType(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid TrigFunction parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new TrigFunction(dto, model);
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
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

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
        // SIMULINK Trigonometric Function block: computes trig functions
        Data inputData = inputPortList.get(0).getData();
        Data outputData;

        if (!trigFunction.equals("atan2")) {
            // Single input trigonometric functions
            if (inputData.getDataType() == DataType.MATRIX) {
                // Matrix input - apply trig function element-wise
                Jama.Matrix inputMatrix = inputData.getMatrix();
                Jama.Matrix outputMatrix = new Jama.Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
                
                for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                    for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                        double value = inputMatrix.get(i, j);
                        double trigValue = applyTrigonometricFunction(value, trigFunction);
                        outputMatrix.set(i, j, trigValue);
                    }
                }
                outputData = new Data(outputMatrix);
            } else {
                // Scalar input
                double trigValue = applyTrigonometricFunction(inputData.getInitValue(), trigFunction);
                outputData = new Data(1, 1);
                outputData.setInitValue(trigValue);
            }
        } else {
            // atan2 function requires two inputs
            Data secondInputData = inputPortList.get(1).getData();
            
            if (inputData.getDataType() == DataType.MATRIX && secondInputData.getDataType() == DataType.MATRIX) {
                // Matrix inputs - apply atan2 element-wise
                Jama.Matrix inputMatrix1 = inputData.getMatrix();
                Jama.Matrix inputMatrix2 = secondInputData.getMatrix();
                Jama.Matrix outputMatrix = new Jama.Matrix(inputMatrix1.getRowDimension(), inputMatrix1.getColumnDimension());
                
                for (int i = 0; i < inputMatrix1.getRowDimension(); i++) {
                    for (int j = 0; j < inputMatrix1.getColumnDimension(); j++) {
                        double y = inputMatrix1.get(i, j);
                        double x = inputMatrix2.get(i, j);
                        double atanValue = Math.atan2(y, x);
                        outputMatrix.set(i, j, atanValue);
                    }
                }
                outputData = new Data(outputMatrix);
            } else {
                // Scalar inputs
                double trigValue = Math.atan2(inputData.getInitValue(), secondInputData.getInitValue());
                outputData = new Data(1, 1);
                outputData.setInitValue(trigValue);
            }
        }

        outputPortList.get(0).setData(outputData);
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

