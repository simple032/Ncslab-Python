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
public class Modulo extends MathBlock {
    
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
    
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
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

    /**
     * DTO-NATIVE Constructor - Creates Modulo block directly from ModuloDto DTO
     */
    public Modulo(com.ncslab.dto.block.specialized.math.ModuloDto moduloDto, NCSLabModel model) {
        this(
            createParameterFromTyped(moduloDto.getModuloType(), 1, "ModuloType"),
            createParameterFromTyped(moduloDto.getDivisorSource(), 2, "DivisorSource"),
            createParameterFromTyped(moduloDto.getDivisor(), 3, "Divisor"),
            createParameterFromTyped(moduloDto.getSampleTime(), 4, "SampleTime"),
            createParameterFromTyped(moduloDto.getOutDataTypeStr(), 5, "OutDataTypeStr"),
            createParameterFromTyped(moduloDto.getSaturateOnIntegerOverflow(), 6, "SaturateOnIntegerOverflow"),
            moduloDto.getBlockName(),
            moduloDto.getBlockPath(),
            moduloDto.getBlockUUID() != null ? moduloDto.getBlockUUID() : "null",
            model
        );
        System.out.println("DTO-NATIVE: Modulo block created successfully from ModuloDto - " + moduloDto.getBlockName());
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
    
    @Override
    public void calculateOutput(double t) {
        // SIMULINK Modulo block: computes modulo operation (remainder)
        Data numeratorData = inputPortList.get(0).getData();
        Data denominatorData;
        String moduloTypeValue = getModuloType();
        
        // Get denominator from either internal parameter or external port
        if (useDivisorPort && inputPortList.size() > 1) {
            denominatorData = inputPortList.get(1).getData();
        } else {
            // Use internal divisor parameter
            double divisorValue = getDivisorValue();
            denominatorData = new Data(1, 1);
            denominatorData.setInitValue(divisorValue);
        }
        
        Data outputData = performModuloOperation(numeratorData, denominatorData, moduloTypeValue);
        outputPortList.get(0).setData(outputData);
    }
    
    private Data performModuloOperation(Data numeratorData, Data denominatorData, String moduloType) {
        if (numeratorData.getDataType() == DataType.MATRIX || denominatorData.getDataType() == DataType.MATRIX) {
            // Matrix operation - apply modulo element-wise
            if (numeratorData.getDataType() == DataType.MATRIX && denominatorData.getDataType() == DataType.MATRIX) {
                // Both matrices
                Jama.Matrix numeratorMatrix = numeratorData.getMatrix();
                Jama.Matrix denominatorMatrix = denominatorData.getMatrix();
                Jama.Matrix outputMatrix = new Jama.Matrix(numeratorMatrix.getRowDimension(), numeratorMatrix.getColumnDimension());
                
                for (int i = 0; i < numeratorMatrix.getRowDimension(); i++) {
                    for (int j = 0; j < numeratorMatrix.getColumnDimension(); j++) {
                        double numerator = numeratorMatrix.get(i, j);
                        double denominator = denominatorMatrix.get(i, j);
                        double result = computeModulo(numerator, denominator, moduloType);
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
                        double result = computeModulo(numerator, denominatorValue, moduloType);
                        outputMatrix.set(i, j, result);
                    }
                }
                return new Data(outputMatrix);
            } else {
                // Numerator is scalar, denominator is matrix
                double numeratorValue = numeratorData.getInitValue();
                Jama.Matrix denominatorMatrix = denominatorData.getMatrix();
                Jama.Matrix outputMatrix = new Jama.Matrix(denominatorMatrix.getRowDimension(), denominatorMatrix.getColumnDimension());
                
                for (int i = 0; i < denominatorMatrix.getRowDimension(); i++) {
                    for (int j = 0; j < denominatorMatrix.getColumnDimension(); j++) {
                        double denominator = denominatorMatrix.get(i, j);
                        double result = computeModulo(numeratorValue, denominator, moduloType);
                        outputMatrix.set(i, j, result);
                    }
                }
                return new Data(outputMatrix);
            }
        } else {
            // Scalar inputs
            double numeratorValue = numeratorData.getInitValue();
            double denominatorValue = denominatorData.getInitValue();
            double result = computeModulo(numeratorValue, denominatorValue, moduloType);
            Data outputData = new Data(1, 1);
            outputData.setInitValue(result);
            return outputData;
        }
    }
    
    private double computeModulo(double numerator, double denominator, String moduloType) {
        if (denominator == 0) {
            return Double.NaN; // Division by zero
        }
        
        switch (moduloType) {
            case "fmod":
                // C-style fmod: sign follows numerator
                return numerator % denominator;
            case "rem":
                // MATLAB rem: IEEE remainder operation (sign follows numerator, different from fmod for negative denominators)
                double result = numerator % denominator;
                if (result != 0 && Math.signum(numerator) != Math.signum(denominator)) {
                    // Adjust for IEEE remainder when signs differ
                    return result;
                }
                return result;
            default:
                return numerator % denominator; // Default to fmod behavior
        }
    }
}