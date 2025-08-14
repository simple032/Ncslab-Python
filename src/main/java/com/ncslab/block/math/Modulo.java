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
 * Modulo block implementing SIMULINK modulo functionality.
 * 
 * SIMULINK Parameters:
 * - ModuloType: "fmod" or "rem"
 * - DivisorSource: "Internal" or "External"
 * - Divisor: Divisor value when DivisorSource is "Internal"
 * - SampleTime: Sample time (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Modulo extends Block {
    
    // === Parameters ===
    private final Parameter moduloType;
    private final Parameter divisorSource;
    private final Parameter divisor;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Internal State ===
    private final boolean useDivisorPort;
    
    // === Static Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("ModuloType", "fmod");
        PARAMETER_DEFAULTS.put("DivisorSource", "Internal");
        PARAMETER_DEFAULTS.put("Divisor", "2");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    
    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();
    
    static {
        outputNames.add("out1");
        inputNames.add("in1"); // Always present (numerator)
        inputNames.add("in2"); // Present when DivisorSource is "External"
    }
    
    // === Constructor ===
    private Modulo(Parameter moduloType, Parameter divisorSource, Parameter divisor,
                   Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                   String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        this.moduloType = moduloType;
        this.divisorSource = divisorSource;
        this.divisor = divisor;
        this.sampleTime = sampleTime;
        this.outDataType = outDataType;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
        
        this.useDivisorPort = "External".equals(divisorSource.getData().getInitString());
        
        // Add parameters to parameter list
        parameterList.add(moduloType);
        parameterList.add(divisorSource);
        parameterList.add(divisor);
        parameterList.add(sampleTime);
        parameterList.add(outDataType);
        parameterList.add(saturateOnIntegerOverflow);
        
        initializePorts();
    }
    
    private void initializePorts() {
        // First input port (always present)
        inputPortList.add(new com.ncslab.block.io.InputPort(this, 1));
        
        // Second input port (divisor) if using external divisor
        if (useDivisorPort) {
            inputPortList.add(new com.ncslab.block.io.InputPort(this, 2));
        }
        
        // One output port with feedthrough
        outputPortList.add(new com.ncslab.block.io.OutputPort(this, 1, true));
    }
    
    // === Static Factory Methods ===
    public static Modulo fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = blockJSON.getString("blockName");
            String blockPath = blockJSON.getString("blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter moduloTypeParam = new Parameter(null, 1, "ModuloType", 
                paramValues.optString("ModuloType", "fmod"));
            Parameter divisorSourceParam = new Parameter(null, 2, "DivisorSource", 
                paramValues.optString("DivisorSource", "Internal"));
            Parameter divisorParam = new Parameter(null, 3, "Divisor", 
                paramValues.optString("Divisor", "2"));
            Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", 
                paramValues.optString("SampleTime", "-1"));
            Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", 
                paramValues.optString("OutDataTypeStr", "Inherit: Same as input"));
            Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", 
                paramValues.optString("SaturateOnIntegerOverflow", "off"));
            
            Modulo block = new Modulo(moduloTypeParam, divisorSourceParam, divisorParam,
                                     sampleTimeParam, outDataTypeParam, saturateParam,
                                     blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, moduloTypeParam, divisorSourceParam, divisorParam,
                                     sampleTimeParam, outDataTypeParam, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Modulo block: " + e.getMessage(), e);
        }
    }
    
    public static Modulo create(String name, String path, String moduloType, String divisorSource, double divisor, NCSLabModel model) {
        return create(name, path, moduloType, divisorSource, divisor, -1.0, "Inherit: Same as input", false, model);
    }
    
    public static Modulo create(String name, String path, String moduloType, String divisorSource, double divisor,
                               double sampleTime, String outDataType, boolean saturateOnOverflow,
                               NCSLabModel model) {
        Parameter moduloTypeParam = new Parameter(null, 1, "ModuloType", moduloType);
        Parameter divisorSourceParam = new Parameter(null, 2, "DivisorSource", divisorSource);
        Parameter divisorParam = new Parameter(null, 3, "Divisor", String.valueOf(divisor));
        Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        Modulo block = new Modulo(moduloTypeParam, divisorSourceParam, divisorParam,
                                 sampleTimeParam, outDataTypeParam, saturateParam,
                                 name, path, "null", model);
        
        setParameterBlockReference(block, moduloTypeParam, divisorSourceParam, divisorParam,
                                 sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Utility Methods ===
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Modulo");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    private static void setParameterBlockReference(Modulo block, Parameter... parameters) {
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
    public String getModuloType() {
        return moduloType.getData().getInitString();
    }
    
    public String getDivisorSource() {
        return divisorSource.getData().getInitString();
    }
    
    public double getDivisorValue() {
        return divisor.getData().getInitValue();
    }

    public boolean isUseDivisorPort() {
        return useDivisorPort;
    }
}