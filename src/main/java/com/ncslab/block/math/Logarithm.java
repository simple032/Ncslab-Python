package com.ncslab.block.math;

import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.BlockCreationException;
import lombok.Getter;

import org.apache.commons.lang3.reflect.Typed;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Logarithm block implementing SIMULINK logarithm functionality.  
 * 
 * SIMULINK Parameters:
 * - LogType: "ln", "log10", "log2", "logn"
 * - CustomBase: Base for "logn" type (default 10)
 * - ZeroCrossing: Enable zero crossing detection
 * - SampleTime: Sample time (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Logarithm extends Block {
    
    // === Parameters ===
    private final Parameter logType;
    private final Parameter customBase;
    private final Parameter zeroCrossing;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("LogType", "ln");
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
    
    // === Constructor ===
    private Logarithm(Parameter logType, Parameter customBase, Parameter zeroCrossing,
                      Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                      String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        this.logType = logType;
        this.customBase = customBase;
        this.zeroCrossing = zeroCrossing;
        this.sampleTime = sampleTime;
        this.outDataType = outDataType;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
        
        // Add parameters to parameter list
        parameterList.add(logType);
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

    /**
     * DTO-NATIVE Constructor - Creates Logarithm block directly from LogarithmDto DTO
     */
    public Logarithm(com.ncslab.dto.block.specialized.math.LogarithmDto logarithmDto, NCSLabModel model) {
        this(
            createParameterFromTyped(logarithmDto.getLogType(), 1, "LogType"),
            createParameterFromTyped(TypedParameter.of(logarithmDto.getCustomBase()), 2, "CustomBase"),
            createParameterFromTyped(TypedParameter.of(logarithmDto.getZeroCrossing()), 3, "ZeroCrossing"),
            createParameterFromTyped(logarithmDto.getSampleTime(), 4, "SampleTime"),
            createParameterFromTyped(logarithmDto.getOutDataTypeStr(), 5, "OutDataTypeStr"),
            createParameterFromTyped(logarithmDto.getSaturateOnIntegerOverflow(), 6, "SaturateOnIntegerOverflow"),
            logarithmDto.getBlockName(),
            logarithmDto.getBlockPath(),
            logarithmDto.getBlockUUID() != null ? logarithmDto.getBlockUUID() : "null",
            model
        );
        System.out.println("DTO-NATIVE: Logarithm block created successfully from LogarithmDto - " + logarithmDto.getBlockName());
    }
    
    // === Static Factory Methods ===
    public static Logarithm fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = blockJSON.getString("blockName");
            String blockPath = blockJSON.getString("blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter logTypeParam = new Parameter(null, 1, "LogType", 
                paramValues.optString("LogType", "ln"));
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
            
            Logarithm block = new Logarithm(logTypeParam, customBaseParam, zeroCrossingParam,
                                           sampleTimeParam, outDataTypeParam, saturateParam,
                                           blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, logTypeParam, customBaseParam, zeroCrossingParam,
                                     sampleTimeParam, outDataTypeParam, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Logarithm block: " + e.getMessage(), e);
        }
    }
    
    public static Logarithm create(String name, String path, String logType, NCSLabModel model) {
        return create(name, path, logType, 10.0, -1.0, "Inherit: Same as input", false, model);
    }
    
    public static Logarithm create(String name, String path, String logType, double customBase,
                                  double sampleTime, String outDataType, boolean saturateOnOverflow,
                                  NCSLabModel model) {
        Parameter logTypeParam = new Parameter(null, 1, "LogType", logType);
        Parameter customBaseParam = new Parameter(null, 2, "CustomBase", String.valueOf(customBase));
        Parameter zeroCrossingParam = new Parameter(null, 3, "ZeroCrossing", "on");
        Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        Logarithm block = new Logarithm(logTypeParam, customBaseParam, zeroCrossingParam,
                                       sampleTimeParam, outDataTypeParam, saturateParam,
                                       name, path, "null", model);
        
        setParameterBlockReference(block, logTypeParam, customBaseParam, zeroCrossingParam,
                                 sampleTimeParam, outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Utility Methods ===
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Logarithm");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    private static void setParameterBlockReference(Logarithm block, Parameter... parameters) {
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
    public String getLogType() {
        return logType.getData().getInitString();
    }
    
    public double getCustomBase() {
        return customBase.getData().getInitValue();
    }
}