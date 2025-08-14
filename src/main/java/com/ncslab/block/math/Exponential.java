package com.ncslab.block.math;

import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.BlockCreationException;
import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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
public class Exponential extends Block {
    
    // === Parameters ===
    private final Parameter expType;
    private final Parameter customBase;
    private final Parameter zeroCrossing;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
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
    
    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();
    
    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }
    
    // === Constructor ===
    private Exponential(Parameter expType, Parameter customBase, Parameter zeroCrossing,
                        Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                        String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        this.expType = expType;
        this.customBase = customBase;
        this.zeroCrossing = zeroCrossing;
        this.sampleTime = sampleTime;
        this.outDataType = outDataType;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
        
        // Add parameters to parameter list
        parameterList.add(expType);
        parameterList.add(customBase);
        parameterList.add(zeroCrossing);
        parameterList.add(sampleTime);
        parameterList.add(outDataType);
        parameterList.add(saturateOnIntegerOverflow);
        
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
            
            Exponential block = new Exponential(expTypeParam, customBaseParam, zeroCrossingParam,
                                               sampleTimeParam, outDataTypeParam, saturateParam,
                                               blockName, blockPath, blockUUID, model);
            
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
        
        Exponential block = new Exponential(expTypeParam, customBaseParam, zeroCrossingParam,
                                           sampleTimeParam, outDataTypeParam, saturateParam,
                                           name, path, "null", model);
        
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
    
    // === Getter Methods ===
    public String getExpType() {
        return expType.getData().getInitString();
    }
    
    public double getCustomBase() {
        return customBase.getData().getInitValue();
    }
}