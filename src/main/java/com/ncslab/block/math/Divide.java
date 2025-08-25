package com.ncslab.block.math;

import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.Parameter;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.BlockCreationException;

import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Divide block implementing SIMULINK divide functionality.
 * 
 * SIMULINK Parameters:
 * - DivideMethod: "Element-wise(./.)" or "Matrix(/.)"
 * - SampleTime: Sample time (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Divide extends MathBlock {
    
    // === Parameters ===
    private final Parameter divideMethod;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;
    
    // === Static Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("DivideMethod", "Element-wise(./.)");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    static {
        outputNames.add("out1");
        inputNames.add("in1"); // Numerator
        inputNames.add("in2"); // Denominator
    }
    
    // === Constructor ===
    private Divide(Parameter divideMethod, Parameter sampleTime,
                   Parameter outDataType, Parameter saturateOnIntegerOverflow,
                   String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        this.divideMethod = divideMethod;
        this.sampleTime = sampleTime;
        this.outDataType = outDataType;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
        
        // Add parameters to parameter list
        parameterList.add(divideMethod);
        parameterList.add(sampleTime);
        parameterList.add(outDataType);
        parameterList.add(saturateOnIntegerOverflow);
        
        initializePorts();
    }
    
    private void initializePorts() {
        // Two input ports: numerator and denominator
        inputPortList.add(new com.ncslab.block.io.InputPort(this, 1));
        inputPortList.add(new com.ncslab.block.io.InputPort(this, 2));
        
        // One output port with feedthrough
        outputPortList.add(new com.ncslab.block.io.OutputPort(this, 1, true));
    }
    
    /**
     * DivideDto Constructor - Creates Divide block directly from DivideDto
     */
    public Divide(com.ncslab.dto.block.specialized.math.DivideDto dto, NCSLabModel model) {
        super(createBlockIdentity(dto.getBlockName(), dto.getBlockPath(), dto.getBlockUUID()), model);
        
        // Extract parameter values from DTO
        String divideMethodValue = dto.getDivideMethodValue() != null ? dto.getDivideMethodValue() : "Element-wise(./.)";
        String sampleTimeValue = dto.getSampleTime() != null ? dto.getSampleTime().getAsString() : "-1";
        String outDataTypeValue = dto.getOutDataTypeStrValue() != null ? dto.getOutDataTypeStrValue() : "Inherit: Same as input";
        String saturateValue = dto.getSaturateOnIntegerOverflowValue() != null && dto.getSaturateOnIntegerOverflowValue() ? "on" : "off";
        
        // Initialize parameters with extracted values
        this.divideMethod = getParameterByName("DivideMethod");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Add parameters to parameter list
        parameterList.add(this.divideMethod);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);
        
        // Initialize ports
        initializePorts();
        
        System.out.println("DivideDto: " + getClass().getSimpleName() + " block created successfully from DivideDto - " + dto.getBlockName());
    }
    
    // === Static Factory Methods ===
    public static Divide fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = blockJSON.getString("blockName");
            String blockPath = blockJSON.getString("blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter divideMethodParam = new Parameter(null, 1, "DivideMethod", 
                paramValues.optString("DivideMethod", "Element-wise(./.)"));
            Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", 
                paramValues.optString("SampleTime", "-1"));
            Parameter outDataTypeParam = new Parameter(null, 3, "OutDataTypeStr", 
                paramValues.optString("OutDataTypeStr", "Inherit: Same as input"));
            Parameter saturateParam = new Parameter(null, 4, "SaturateOnIntegerOverflow", 
                paramValues.optString("SaturateOnIntegerOverflow", "off"));
            
            Divide block = new Divide(divideMethodParam, sampleTimeParam,
                                     outDataTypeParam, saturateParam,
                                     blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, divideMethodParam, sampleTimeParam,
                                     outDataTypeParam, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Divide block: " + e.getMessage(), e);
        }
    }
    
    public static Divide create(String name, String path, String divideMethod, NCSLabModel model) {
        return create(name, path, divideMethod, -1.0, "Inherit: Same as input", false, model);
    }
    
    public static Divide create(String name, String path, String divideMethod,
                               double sampleTime, String outDataType, boolean saturateOnOverflow,
                               NCSLabModel model) {
        Parameter divideMethodParam = new Parameter(null, 1, "DivideMethod", divideMethod);
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 3, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 4, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        Divide block = new Divide(divideMethodParam, sampleTimeParam,
                                 outDataTypeParam, saturateParam,
                                 name, path, "null", model);
        
        setParameterBlockReference(block, divideMethodParam, sampleTimeParam,
                                 outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Utility Methods ===
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Divide");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to satisfy base constructor
        return identity;
    }
    
    private static void setParameterBlockReference(Divide block, Parameter... parameters) {
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
    public String getDivideMethod() {
        return divideMethod.getData().getInitString();
    }
    
    @Override
    public void calculateOutput(double t) {
        // SIMULINK Divide block: computes numerator/denominator
        Data numeratorData = inputPortList.get(0).getData();
        Data denominatorData = inputPortList.get(1).getData();
        String divideMethodValue = getDivideMethod();
        Data outputData;
        
        if (numeratorData.getDataType() == DataType.MATRIX || denominatorData.getDataType() == DataType.MATRIX) {
            // Matrix operation
            if ("Matrix(/.)".equals(divideMethodValue)) {
                // Matrix division operation (numerator/denominator where denominator can be matrix)
                if (numeratorData.getDataType() == DataType.MATRIX && denominatorData.getDataType() == DataType.MATRIX) {
                    // Matrix / Matrix = numerator * inverse(denominator)
                    Jama.Matrix numeratorMatrix = numeratorData.getMatrix();
                    Jama.Matrix denominatorMatrix = denominatorData.getMatrix();
                    try {
                        Jama.Matrix inverseMatrix = denominatorMatrix.inverse();
                        Jama.Matrix outputMatrix = numeratorMatrix.times(inverseMatrix);
                        outputData = new Data(outputMatrix);
                    } catch (RuntimeException e) {
                        // Singular matrix - fallback to element-wise
                        outputData = performElementWiseDivide(numeratorData, denominatorData);
                    }
                } else {
                    // Element-wise fallback for mixed types
                    outputData = performElementWiseDivide(numeratorData, denominatorData);
                }
            } else {
                // Element-wise division operation
                outputData = performElementWiseDivide(numeratorData, denominatorData);
            }
        } else {
            // Scalar inputs
            double numeratorValue = numeratorData.getInitValue();
            double denominatorValue = denominatorData.getInitValue();
            double result = denominatorValue != 0 ? numeratorValue / denominatorValue : Double.POSITIVE_INFINITY;
            outputData = new Data(1, 1);
            outputData.setInitValue(result);
        }
        
        outputPortList.get(0).setData(outputData);
    }
    
    private Data performElementWiseDivide(Data numeratorData, Data denominatorData) {
        if (numeratorData.getDataType() == DataType.MATRIX && denominatorData.getDataType() == DataType.MATRIX) {
            // Both matrices - element-wise division
            Jama.Matrix numeratorMatrix = numeratorData.getMatrix();
            Jama.Matrix denominatorMatrix = denominatorData.getMatrix();
            Jama.Matrix outputMatrix = new Jama.Matrix(numeratorMatrix.getRowDimension(), numeratorMatrix.getColumnDimension());
            
            for (int i = 0; i < numeratorMatrix.getRowDimension(); i++) {
                for (int j = 0; j < numeratorMatrix.getColumnDimension(); j++) {
                    double numerator = numeratorMatrix.get(i, j);
                    double denominator = denominatorMatrix.get(i, j);
                    double result = denominator != 0 ? numerator / denominator : Double.POSITIVE_INFINITY;
                    outputMatrix.set(i, j, result);
                }
            }
            return new Data(outputMatrix);
        } else if (numeratorData.getDataType() == DataType.MATRIX) {
            // Numerator is matrix, denominator is scalar
            Jama.Matrix numeratorMatrix = numeratorData.getMatrix();
            double denominatorValue = denominatorData.getInitValue();
            Jama.Matrix outputMatrix = new Jama.Matrix(numeratorMatrix.getRowDimension(), numeratorMatrix.getColumnDimension());
            
            for (int i = 0; i < numeratorMatrix.getRowDimension(); i++) {
                for (int j = 0; j < numeratorMatrix.getColumnDimension(); j++) {
                    double numerator = numeratorMatrix.get(i, j);
                    double result = denominatorValue != 0 ? numerator / denominatorValue : Double.POSITIVE_INFINITY;
                    outputMatrix.set(i, j, result);
                }
            }
            return new Data(outputMatrix);
        } else if (denominatorData.getDataType() == DataType.MATRIX) {
            // Numerator is scalar, denominator is matrix
            double numeratorValue = numeratorData.getInitValue();
            Jama.Matrix denominatorMatrix = denominatorData.getMatrix();
            Jama.Matrix outputMatrix = new Jama.Matrix(denominatorMatrix.getRowDimension(), denominatorMatrix.getColumnDimension());
            
            for (int i = 0; i < denominatorMatrix.getRowDimension(); i++) {
                for (int j = 0; j < denominatorMatrix.getColumnDimension(); j++) {
                    double denominator = denominatorMatrix.get(i, j);
                    double result = denominator != 0 ? numeratorValue / denominator : Double.POSITIVE_INFINITY;
                    outputMatrix.set(i, j, result);
                }
            }
            return new Data(outputMatrix);
        } else {
            // Both scalars
            double result = denominatorData.getInitValue() != 0 ? numeratorData.getInitValue() / denominatorData.getInitValue() : Double.POSITIVE_INFINITY;
            Data outputData = new Data(1, 1);
            outputData.setInitValue(result);
            return outputData;
        }
    }
}