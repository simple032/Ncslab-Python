package com.ncslab.block.math;

import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.Parameter;
import com.ncslab.dto.block.specialized.math.ExponentialDto;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.BlockCreationException;
import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Exponential block implementing SIMULINK exponential functionality.
 * 
 * SIMULINK Parameters:
 * - ExpType: "exp", "exp10", "exp2", "expn"
 * - CustomBase: Base for "expn" type (default 10)
 * - ZeroCrossing: Enable zero crossing detection
 * - SampleTime: Sample time (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Exponential extends MathBlock {
    
    // === Parameters ===
    private Parameter expType;
    private Parameter customBase;
    private Parameter zeroCrossing;
    private Parameter sampleTime;
    private Parameter outDataType;
    private Parameter saturateOnIntegerOverflow;
    
    // === Static Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("ExpType", "exp");
        PARAMETER_DEFAULTS.put("CustomBase", "10");
        PARAMETER_DEFAULTS.put("ZeroCrossing", "on");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }
    
    @Deprecated
    public Exponential(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        initializeBlock();
    }
    
    
    public Exponential(ExponentialDto dto, NCSLabModel model) {
        super(dto, model);
        
        // Validate DTO
        dto.validate();
        
        // Extract parameters from ExponentialDto (relying on @Builder.Default for defaults)
        String expTypeValue = dto.getExpTypeValue();
        String customBaseValue = dto.getCustomBaseValue().toString();
        String zeroCrossingValue = dto.getZeroCrossingValue() ? "on" : "off";
        String sampleTimeValue = dto.getSampleTimeValue().toString();
        String outDataTypeValue = dto.getOutDataTypeStrValue();
        String saturateValue = dto.getSaturateOnIntegerOverflowValue() ? "on" : "off";
        
        // Initialize final parameters from DTO
        this.expType = getParameterByName("ExpType");
        this.customBase = getParameterByName("CustomBase");
        this.zeroCrossing = getParameterByName("ZeroCrossing");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        initializePorts();
    }
    
    public static Exponential fromDto(ExponentialDto dto, NCSLabModel model) {
        return new Exponential(dto, model);
    }
    
    private void initializeBlock() {
        // Legacy JSONObject initialization
        this.expType = getParameterByName("ExpType");
        this.customBase = getParameterByName("CustomBase");
        this.zeroCrossing = getParameterByName("ZeroCrossing");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        initializePorts();
    }
    
    private void initializePorts() {
        // One input port
        inputPortList.add(new com.ncslab.block.io.InputPort(this, 1));
        
        // One output port with feedthrough
        outputPortList.add(new com.ncslab.block.io.OutputPort(this, 1, true));
    }
    
    // === Static Factory Methods ===
    public static Exponential fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = blockJSON.getString("blockName");
            String blockPath = blockJSON.getString("blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter expTypeParam = new Parameter(null, 1, "ExpType", 
                paramValues.optString("ExpType", "exp"));
            Parameter customBaseParam = new Parameter(null, 2, "CustomBase", 
                paramValues.optString("CustomBase", "10"));
            Parameter zeroCrossingParam = new Parameter(null, 3, "ZeroCrossing", 
                paramValues.optString("ZeroCrossing", "on"));
            Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", 
                paramValues.optString("SampleTime", "-1"));
            Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", 
                paramValues.optString("OutDataTypeStr", "Inherit: Same as input"));
            Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", 
                paramValues.optString("SaturateOnIntegerOverflow", "off"));
            
            // Create block using existing constructor
            JSONObject blockIdentity = createBlockIdentity(blockName, blockPath, blockUUID);
            blockIdentity.getJSONObject("paramValues").put("ExpType", expTypeParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("CustomBase", customBaseParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("ZeroCrossing", zeroCrossingParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("SampleTime", sampleTimeParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("OutDataTypeStr", outDataTypeParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("SaturateOnIntegerOverflow", saturateParam.getInitString());
            
            Exponential block = new Exponential(blockIdentity, model);
            
            setParameterBlockReference(block, expTypeParam, customBaseParam, zeroCrossingParam,
                                     sampleTimeParam, outDataTypeParam, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Exponential block: " + e.getMessage(), e);
        }
    }
    
    public static Exponential create(String name, String path, String expType, NCSLabModel model) {
        return create(name, path, expType, 10.0, -1.0, "Inherit: Same as input", false, model);
    }
    
    public static Exponential create(String name, String path, String expType, double customBase,
                                    double sampleTime, String outDataType, boolean saturateOnOverflow,
                                    NCSLabModel model) {
        Parameter expTypeParam = new Parameter(null, 1, "ExpType", expType);
        Parameter customBaseParam = new Parameter(null, 2, "CustomBase", String.valueOf(customBase));
        Parameter zeroCrossingParam = new Parameter(null, 3, "ZeroCrossing", "on");
        Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        // Create block using existing constructor
        JSONObject blockIdentity = createBlockIdentity(name, path, "null");
        blockIdentity.getJSONObject("paramValues").put("ExpType", expTypeParam.getInitString());
        blockIdentity.getJSONObject("paramValues").put("CustomBase", customBaseParam.getInitString());
        blockIdentity.getJSONObject("paramValues").put("ZeroCrossing", zeroCrossingParam.getInitString());
        blockIdentity.getJSONObject("paramValues").put("SampleTime", sampleTimeParam.getInitString());
        blockIdentity.getJSONObject("paramValues").put("OutDataTypeStr", outDataTypeParam.getInitString());
        blockIdentity.getJSONObject("paramValues").put("SaturateOnIntegerOverflow", saturateParam.getInitString());
        
        Exponential block = new Exponential(blockIdentity, model);
        
        setParameterBlockReference(block, expTypeParam, customBaseParam, zeroCrossingParam,
                                 sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Utility Methods ===
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Exponential");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to satisfy base constructor
        return identity;
    }
    
    private static void setParameterBlockReference(Exponential block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
            } catch (Exception e) {
                // Fallback: parameter will work for basic operations
            }
        }
    }

    // === DTO Helper Methods ===
    private static Parameter createParameterFromTyped(com.ncslab.dto.common.TypedParameter typedParam, int number, String name) {
        if (typedParam == null || typedParam.getValue() == null) {
            throw new IllegalArgumentException("TypedParameter " + name + " cannot be null");
        }
        
        String stringValue;
        if (typedParam.getValue() instanceof Boolean) {
            stringValue = ((Boolean) typedParam.getValue()) ? "on" : "off";
        } else {
            stringValue = typedParam.getValue().toString();
        }
        
        return new Parameter(null, number, name, stringValue);
    }
    
    // === Getter Methods ===
    public String getExpType() {
        return expType.getData().getInitString();
    }
    
    public double getCustomBase() {
        return customBase.getData().getInitValue();
    }
    
    @Override
    public void calculateOutput(double t) {
        // SIMULINK Exponential block: computes exponential functions
        Data inputData = inputPortList.get(0).getData();
        String expTypeValue = getExpType();
        Data outputData;
        
        if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix input - apply exponential function element-wise
            Jama.Matrix inputMatrix = inputData.getMatrix();
            Jama.Matrix outputMatrix = new Jama.Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
            
            for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                    double value = inputMatrix.get(i, j);
                    double expValue = applyExponentialFunction(value, expTypeValue);
                    outputMatrix.set(i, j, expValue);
                }
            }
            outputData = new Data(outputMatrix);
        } else {
            // Scalar input
            double inputValue = inputData.getInitValue();
            double expValue = applyExponentialFunction(inputValue, expTypeValue);
            outputData = new Data(1, 1);
            outputData.setInitValue(expValue);
        }
        
        outputPortList.get(0).setData(outputData);
    }
    
    private double applyExponentialFunction(double value, String expType) {
        switch (expType) {
            case "exp":
                return Math.exp(value);
            case "exp2":
                return Math.pow(2, value);
            case "exp10":
                return Math.pow(10, value);
            case "custom":
                double base = getCustomBase();
                return Math.pow(base, value);
            default:
                return Math.exp(value); // Default to natural exponential
        }
    }
}