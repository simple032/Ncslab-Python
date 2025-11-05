package com.ncslab.block.math;

import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.Parameter;
import com.ncslab.dto.block.specialized.math.MinMaxDto;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.BlockCreationException;
import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * MinMax block implementing SIMULINK MinMax functionality.
 * 
 * SIMULINK Parameters:
 * - Function: "min" or "max"
 * - NumInputs: Number of inputs
 * - SampleTime: Sample time (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class MinMax extends MathBlock {
    
    // === Parameters ===
    private final Parameter function;
    private final Parameter numInputs;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("Function", "min");
        PARAMETER_DEFAULTS.put("NumInputs", "2");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    static {
        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2"); // Default 2 inputs
    }
    
    // === Constructor ===
    private MinMax(Parameter function, Parameter numInputs, Parameter sampleTime,
                   Parameter outDataType, Parameter saturateOnIntegerOverflow,
                   String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        this.function = function;
        this.numInputs = numInputs;
        this.sampleTime = sampleTime;
        this.outDataType = outDataType;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
        
        // Add parameters to parameter list
        parameterList.add(function);
        parameterList.add(numInputs);
        parameterList.add(sampleTime);
        parameterList.add(outDataType);
        parameterList.add(saturateOnIntegerOverflow);
        
        initializePorts();
    }
    
    // === DTO Constructor ===
    public MinMax(com.ncslab.dto.block.specialized.math.MinMaxDto dto, NCSLabModel model) {
        super(dto, model);

        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new BlockCreationException("DTO validation failed: " + validation.getErrors());
        }

        // Retrieve parameters initialized by base class
        this.function = getParameterByName("Function");
        this.numInputs = getParameterByName("NumInputs");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        initializePorts();

        System.out.println("DTO-NATIVE: MinMax block created successfully - " + dto.getBlockName());
    }
    
    private void initializePorts() {
        // Create input ports based on NumInputs parameter
        int numInputsValue = (int) numInputs.getData().getInitValue();
        for (int i = 1; i <= numInputsValue; i++) {
            inputPortList.add(new com.ncslab.block.io.InputPort(this, i));
        }
        
        // Create single output port
        outputPortList.add(new com.ncslab.block.io.OutputPort(this, 1, true)); // Has feedthrough
    }
    
    // === Static Factory Methods ===
    public static MinMax fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = blockJSON.getString("blockName");
            String blockPath = blockJSON.getString("blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter functionParam = new Parameter(null, 1, "Function", 
                paramValues.optString("Function", "min"));
            Parameter numInputsParam = new Parameter(null, 2, "NumInputs", 
                paramValues.optString("NumInputs", "2"));
            Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", 
                paramValues.optString("SampleTime", "-1"));
            Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", 
                paramValues.optString("OutDataTypeStr", "Inherit: Same as input"));
            Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", 
                paramValues.optString("SaturateOnIntegerOverflow", "off"));
            
            MinMax block = new MinMax(functionParam, numInputsParam, sampleTimeParam,
                                     outDataTypeParam, saturateParam,
                                     blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, functionParam, numInputsParam, sampleTimeParam,
                                     outDataTypeParam, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create MinMax block: " + e.getMessage(), e);
        }
    }
    
    public static MinMax create(String name, String path, String function, int numInputs, NCSLabModel model) {
        return create(name, path, function, numInputs, -1.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a MinMax block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param function Function type ("min" or "max")
     * @param numInputs Number of input ports
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return MinMax block instance
     */
    public static MinMax create(String name, String path, String function, int numInputs,
                               double sampleTime, String outDataType, boolean saturateOnOverflow,
                               NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        MinMaxDto dto = MinMaxDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .function(com.ncslab.dto.common.TypedParameter.of(function))
            .numInputs(com.ncslab.dto.common.TypedParameter.of(numInputs))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid MinMax parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new MinMax(dto, model);
    }
    
    // === Utility Methods ===
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "MinMax");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    private static void setParameterBlockReference(MinMax block, Parameter... parameters) {
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
    public String getFunction() {
        return function.getData().getInitString();
    }
    
    public int getNumInputs() {
        return (int) numInputs.getData().getInitValue();
    }
    
    @Override
    public void calculateOutput(double t) {
        // SIMULINK MinMax block: computes minimum or maximum of inputs
        String functionValue = getFunction();
        boolean isMin = "min".equals(functionValue);
        Data outputData;
        
        // Get all input data
        List<Data> inputDataList = new ArrayList<>();
        for (int i = 0; i < inputPortList.size(); i++) {
            inputDataList.add(inputPortList.get(i).getData());
        }
        
        // Check if any input is a matrix
        boolean hasMatrix = inputDataList.stream().anyMatch(data -> data.getDataType() == DataType.MATRIX);
        
        if (hasMatrix) {
            // Matrix operation - find dimensions
            Data firstMatrixData = inputDataList.stream()
                .filter(data -> data.getDataType() == DataType.MATRIX)
                .findFirst()
                .orElse(inputDataList.get(0));
            
            if (firstMatrixData.getDataType() == DataType.MATRIX) {
                Jama.Matrix firstMatrix = firstMatrixData.getMatrix();
                Jama.Matrix outputMatrix = new Jama.Matrix(firstMatrix.getRowDimension(), firstMatrix.getColumnDimension());
                
                for (int i = 0; i < firstMatrix.getRowDimension(); i++) {
                    for (int j = 0; j < firstMatrix.getColumnDimension(); j++) {
                        double result = isMin ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                        
                        for (Data inputData : inputDataList) {
                            double value;
                            if (inputData.getDataType() == DataType.MATRIX) {
                                value = inputData.getMatrix().get(i, j);
                            } else {
                                value = inputData.getInitValue();
                            }
                            
                            if (isMin) {
                                result = Math.min(result, value);
                            } else {
                                result = Math.max(result, value);
                            }
                        }
                        outputMatrix.set(i, j, result);
                    }
                }
                outputData = new Data(outputMatrix);
            } else {
                // All scalars
                outputData = calculateScalarMinMax(inputDataList, isMin);
            }
        } else {
            // All scalar inputs
            outputData = calculateScalarMinMax(inputDataList, isMin);
        }
        
        outputPortList.get(0).setData(outputData);
    }
    
    private Data calculateScalarMinMax(List<Data> inputDataList, boolean isMin) {
        double result = isMin ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        
        for (Data inputData : inputDataList) {
            double value = inputData.getInitValue();
            if (isMin) {
                result = Math.min(result, value);
            } else {
                result = Math.max(result, value);
            }
        }
        
        Data outputData = new Data(1, 1);
        outputData.setInitValue(result);
        return outputData;
    }
}