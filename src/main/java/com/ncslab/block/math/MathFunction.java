package com.ncslab.block.math;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

/**
 * MathFunction block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Operator: Mathematical function (sin, cos, tan, log, exp, sqrt, pow, etc.)
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class MathFunction extends Block {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter operator;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Operational Settings ===
    private final String mathOperator;
    
    // === Static Parameter Definitions ===
    
    // Parameter defaults
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Operator", "exp");  // Default operator
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final Vector<String> outputNames = new Vector<>();
    
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // SIMULINK parameter names
        
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }
    
    // === Private Constructor with Typed Parameters ===
    private MathFunction(Parameter operator, Parameter sampleTime, Parameter outDataType, 
                        Parameter saturateOnIntegerOverflow, String blockName, String blockPath, 
                        String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Extract operator value
        this.mathOperator = operator.getInitString();
        
        // Validate parameters
        validateParameters(operator, sampleTime);
        
        // Assign parameters
        this.operator = Objects.requireNonNull(operator, "Operator parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Initialize ports
        initializePorts();
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public MathFunction(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        
        // Extract legacy operator
        String operatorValue;
        if (paramValues.has("Operator"))
            operatorValue = paramValues.getString("Operator");
        else
            operatorValue = paramValues.getString("MathFunctionOperator");
        this.mathOperator = operatorValue;
        
        // Create legacy operator parameter
        this.operator = new Parameter(this, 1, "Operator", operatorValue);
        
        // Create missing SIMULINK parameters with defaults
        this.sampleTime = new Parameter(this, 2, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 3, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 4, "SaturateOnIntegerOverflow", "off");
        
        // Add all parameters to parameter list
        
        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates MathFunction block directly from BlockJson DTO
     */
    public MathFunction(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Extract legacy operator
        String operatorValue;
        if (paramValues.has("Operator"))
            operatorValue = paramValues.getString("Operator");
        else
            operatorValue = paramValues.getString("MathFunctionOperator");
        this.mathOperator = operatorValue;

        // Initialize final parameters from DTO
        this.operator = new Parameter(this, 1, "Operator", "operatorValue");
        this.sampleTime = new Parameter(this, 2, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 3, "OutDataTypeStr", "Inherit: Same as input");
        this.saturateOnIntegerOverflow = new Parameter(this, 4, "SaturateOnIntegerOverflow", "off");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static MathFunction fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter operator = createOperatorFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            MathFunction block = new MathFunction(operator, sampleTime, outDataType, saturateParam,
                                                 blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, operator, sampleTime, outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create MathFunction block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static MathFunction create(String name, String path, String mathOperator, NCSLabModel model) {
        return create(name, path, mathOperator, -1.0, "Inherit: Same as input", false, model);
    }
    
    public static MathFunction create(String name, String path, String mathOperator, double sampleTime,
                                     String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter operator = new Parameter(null, 1, "Operator", mathOperator);
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 3, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 4, "SaturateOnIntegerOverflow", String.valueOf(saturateOnOverflow));
        
        MathFunction block = new MathFunction(operator, sampleTimeParam, outDataTypeParam, saturateParam,
                                             name, path, "null", model);
        
        setParameterBlockReference(block, operator, sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Parameter Validation ===
    private static void validateParameters(Parameter operator, Parameter sampleTime) {
        String operatorValue = operator.getInitString();
        if (operatorValue == null || operatorValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Operator parameter cannot be empty");
        }
        
        // Validate operator type
        String[] validOperators = {"sin", "cos", "tan", "asin", "acos", "atan", "sqrt", "exp", "log", 
                                  "abs", "floor", "ceil", "round", "sign", "pow", "transpose"};
        boolean isValid = false;
        for (String validOp : validOperators) {
            if (validOp.equals(operatorValue)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new IllegalArgumentException("Invalid math operator: " + operatorValue);
        }
        
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createOperatorFromJSON(JSONObject paramValues, String blockName) {
        String operatorValue;
        if (paramValues.has("Operator"))
            operatorValue = paramValues.getString("Operator");
        else
            operatorValue = paramValues.optString("MathFunctionOperator", "sin");
        return new Parameter(null, 1, "Operator", operatorValue);
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
    
    private static void setParameterBlockReference(MathFunction block, Parameter... parameters) {
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
        identity.put("blockType", "MathFunction");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    // === Port Initialization ===
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
        
        // Add second input port for pow operation
        if (Objects.equals(mathOperator, "pow")) {
            inputPortList.add(new InputPort(this, 2));
            inputNames.clear();
            inputNames.add("in1");
            inputNames.add("in2");
        }
    }

    // === Code Generation Methods (preserved from original) ===
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add MathFunction-specific context variables
        context.put("mathOperator", mathOperator);
        
        String codeStr = TemplateManager.renderTemplate("m/math/MathFunction/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add MathFunction-specific context variables
        context.put("function", mathOperator);
        context.put("mathOperator", mathOperator);

        String codeStr = TemplateManager.renderTemplate("c/math/MathFunction/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (mathOperator.equals("transpose")) {
            out.setHeight(signal.getWidth());
            out.setWidth(signal.getHeight());
            out.getOutputSignalC().setHeight(signal.getWidth());
            out.getOutputSignalC().setWidth(signal.getHeight());
            out.getOutputSignalC().setDataType(signal.getDataType());
        } else {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateInit() {
        // Initialization logic for MathFunction block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();

        Data resultData;
        switch (inputData.getDataType()) {
            case REAL:
                double inputValue = inputData.getInitValue();
                if ("pow".equals(mathOperator)) {
                    double exponent = inputPortList.get(1).getData().getInitValue();
                    resultData = new Data(Math.pow(inputValue, exponent));
                } else {
                    resultData = new Data(applyMathFunction(inputValue, mathOperator));
                }
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                        double inputValueMatrix = inputData.getMatrix().get(i, j);
                        if ("pow".equals(mathOperator)) {
                            double exponent = inputPortList.get(1).getData().getMatrix().get(i, j);
                            matrixResult.set(i, j, Math.pow(inputValueMatrix, exponent));
                        } else {
                            matrixResult.set(i, j, applyMathFunction(inputValueMatrix, mathOperator));
                        }
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
    }

    private double applyMathFunction(double value, String function) {
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
            case "sqrt":
                return Math.sqrt(value);
            case "exp":
                return Math.exp(value);
            case "log":
                return Math.log(value);
            case "abs":
                return Math.abs(value);
            case "floor":
                return Math.floor(value);
            case "ceil":
                return Math.ceil(value);
            case "round":
                return Math.round(value);
            case "sign":
                return value > 0 ? 1.0 : (value < 0 ? -1.0 : 0.0);
            default:
                return 0;
        }
    }
}

