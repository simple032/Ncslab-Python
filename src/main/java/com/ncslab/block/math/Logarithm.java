package com.ncslab.block.math;

import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.Parameter;
import com.ncslab.dto.block.specialized.math.LogarithmDto;
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
public class Logarithm extends MathBlock {
    
    // === Parameters ===
    private Parameter logType;
    private Parameter customBase;
    private Parameter zeroCrossing;
    private Parameter sampleTime;
    private Parameter outDataType;
    private Parameter saturateOnIntegerOverflow;
    
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
    
    @Deprecated
    public Logarithm(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        initializeBlock();
    }
    
    
    public Logarithm(LogarithmDto dto, NCSLabModel model) {
        super(dto, model);
        
        // Validate DTO
        dto.validate();
        
        // Extract parameters from LogarithmDto (relying on @Builder.Default for defaults)
        String logTypeValue = dto.getLogTypeValue();
        String customBaseValue = dto.getCustomBaseValue().toString();
        String zeroCrossingValue = dto.getZeroCrossingValue() ? "on" : "off";
        String sampleTimeValue = dto.getSampleTimeValue().toString();
        String outDataTypeValue = dto.getOutDataTypeStrValue();
        String saturateValue = dto.getSaturateOnIntegerOverflowValue() ? "on" : "off";
        
        // Initialize final parameters from DTO
        this.logType = getParameterByName("LogType");
        this.customBase = getParameterByName("CustomBase");
        this.zeroCrossing = getParameterByName("ZeroCrossing");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        initializePorts();
    }
    
    public static Logarithm fromDto(LogarithmDto dto, NCSLabModel model) {
        return new Logarithm(dto, model);
    }
    
    private void initializeBlock() {
        // Legacy JSONObject initialization
        this.logType = getParameterByName("LogType");
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
            
            // Create block using existing constructor
            JSONObject blockIdentity = createBlockIdentity(blockName, blockPath, blockUUID);
            blockIdentity.getJSONObject("paramValues").put("LogType", logTypeParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("CustomBase", customBaseParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("ZeroCrossing", zeroCrossingParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("SampleTime", sampleTimeParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("OutDataTypeStr", outDataTypeParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("SaturateOnIntegerOverflow", saturateParam.getInitString());
            
            Logarithm block = new Logarithm(blockIdentity, model);
            
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

    /**
     * Create a Logarithm block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param logType Logarithm type (ln, log10, log2, logn)
     * @param customBase Custom base value (used when logType is "logn")
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return Logarithm block instance
     */
    public static Logarithm create(String name, String path, String logType, double customBase,
                                  double sampleTime, String outDataType, boolean saturateOnOverflow,
                                  NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        LogarithmDto dto = LogarithmDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .logType(com.ncslab.dto.common.TypedParameter.of(logType))
            .customBase(com.ncslab.dto.common.TypedParameter.of(customBase))
            .zeroCrossing(com.ncslab.dto.common.TypedParameter.of(true))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Logarithm parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Logarithm(dto, model);
    }
    
    // === Utility Methods ===
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Logarithm");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to satisfy base constructor
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
    
    @Override
    public void calculateOutput(double t) {
        // SIMULINK Logarithm block: computes logarithmic functions
        Data inputData = inputPortList.get(0).getData();
        String logTypeValue = getLogType();
        Data outputData;
        
        if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix input - apply logarithm function element-wise
            Jama.Matrix inputMatrix = inputData.getMatrix();
            Jama.Matrix outputMatrix = new Jama.Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
            
            for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                    double value = inputMatrix.get(i, j);
                    double logValue = applyLogarithmFunction(value, logTypeValue);
                    outputMatrix.set(i, j, logValue);
                }
            }
            outputData = new Data(outputMatrix);
        } else {
            // Scalar input
            double inputValue = inputData.getInitValue();
            double logValue = applyLogarithmFunction(inputValue, logTypeValue);
            outputData = new Data(1, 1);
            outputData.setInitValue(logValue);
        }
        
        outputPortList.get(0).setData(outputData);
    }
    
    private double applyLogarithmFunction(double value, String logType) {
        // Handle negative and zero values (return NaN for negative, -Infinity for zero)
        if (value <= 0) {
            return value == 0 ? Double.NEGATIVE_INFINITY : Double.NaN;
        }
        
        switch (logType) {
            case "log":
                return Math.log(value); // Natural logarithm (ln)
            case "log2":
                return Math.log(value) / Math.log(2); // Log base 2
            case "log10":
                return Math.log10(value); // Log base 10
            case "custom":
                double base = getCustomBase();
                if (base <= 0 || base == 1) {
                    return Double.NaN; // Invalid base
                }
                return Math.log(value) / Math.log(base);
            default:
                return Math.log(value); // Default to natural logarithm
        }
    }
}