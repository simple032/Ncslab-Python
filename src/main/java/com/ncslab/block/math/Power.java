package com.ncslab.block.math;

import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.Parameter;
import com.ncslab.dto.block.specialized.math.PowerDto;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.BlockCreationException;
import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Power block implementing SIMULINK power functionality.
 * 
 * SIMULINK Parameters:
 * - PowerMethod: "Element-wise(.^)" or "Matrix(^)"
 * - SampleTime: Sample time (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Power extends MathBlock {
    
    // === Parameters ===
    private Parameter powerMethod;
    private Parameter sampleTime;
    private Parameter outDataType;
    private Parameter saturateOnIntegerOverflow;
    
    // === Static Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("PowerMethod", "Element-wise(.^)");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    static {
        outputNames.add("out1");
        inputNames.add("in1"); // Base
        inputNames.add("in2"); // Exponent
    }
    
    @Deprecated
    public Power(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        initializeBlock();
    }
    
    
    public Power(PowerDto dto, NCSLabModel model) {
        super(dto, model);
        
        // Validate DTO
        dto.validate();
        
        // Extract parameters from PowerDto (relying on @Builder.Default for defaults)
        String powerMethodValue = dto.getPowerMethodValue();
        String sampleTimeValue = dto.getSampleTimeValue().toString();
        String outDataTypeValue = dto.getOutDataTypeStrValue();
        String saturateValue = dto.getSaturateOnIntegerOverflowValue() ? "on" : "off";
        
        // Initialize final parameters from DTO
        this.powerMethod = getParameterByName("PowerMethod");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        initializePorts();
    }
    
    public static Power fromDto(PowerDto dto, NCSLabModel model) {
        return new Power(dto, model);
    }
    
    private void initializeBlock() {
        // Legacy JSONObject initialization
        this.powerMethod = getParameterByName("PowerMethod");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        initializePorts();
    }
    
    private void initializePorts() {
        // Two input ports: base and exponent
        inputPortList.add(new com.ncslab.block.io.InputPort(this, 1));
        inputPortList.add(new com.ncslab.block.io.InputPort(this, 2));
        
        // One output port with feedthrough
        outputPortList.add(new com.ncslab.block.io.OutputPort(this, 1, true));
    }
    
    // === Static Factory Methods ===
    public static Power fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = blockJSON.getString("blockName");
            String blockPath = blockJSON.getString("blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter powerMethodParam = new Parameter(null, 1, "PowerMethod", 
                paramValues.optString("PowerMethod", "Element-wise(.^)"));
            Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", 
                paramValues.optString("SampleTime", "-1"));
            Parameter outDataTypeParam = new Parameter(null, 3, "OutDataTypeStr", 
                paramValues.optString("OutDataTypeStr", "Inherit: Same as input"));
            Parameter saturateParam = new Parameter(null, 4, "SaturateOnIntegerOverflow", 
                paramValues.optString("SaturateOnIntegerOverflow", "off"));
            
            // Create block using existing constructor
            JSONObject blockIdentity = createBlockIdentity(blockName, blockPath, blockUUID);
            blockIdentity.getJSONObject("paramValues").put("PowerMethod", powerMethodParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("SampleTime", sampleTimeParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("OutDataTypeStr", outDataTypeParam.getInitString());
            blockIdentity.getJSONObject("paramValues").put("SaturateOnIntegerOverflow", saturateParam.getInitString());
            
            Power block = new Power(blockIdentity, model);
            
            setParameterBlockReference(block, powerMethodParam, sampleTimeParam,
                                     outDataTypeParam, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Power block: " + e.getMessage(), e);
        }
    }
    
    public static Power create(String name, String path, String powerMethod, NCSLabModel model) {
        return create(name, path, powerMethod, -1.0, "Inherit: Same as input", false, model);
    }

    /**
     * Create a Power block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param powerMethod Power method ("Element-wise(.^)" or "Matrix(^)")
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return Power block instance
     */
    public static Power create(String name, String path, String powerMethod,
                              double sampleTime, String outDataType, boolean saturateOnOverflow,
                              NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        PowerDto dto = PowerDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .powerMethod(com.ncslab.dto.common.TypedParameter.of(powerMethod))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Power parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Power(dto, model);
    }
    
    // === Utility Methods ===
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Power");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to satisfy base constructor
        return identity;
    }
    
    private static void setParameterBlockReference(Power block, Parameter... parameters) {
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
    public String getPowerMethod() {
        return powerMethod.getData().getInitString();
    }
    
    @Override
    public void calculateOutput(double t) {
        // SIMULINK Power block: computes base^exponent
        Data baseData = inputPortList.get(0).getData();
        Data exponentData = inputPortList.get(1).getData();
        String powerMethodValue = getPowerMethod();
        Data outputData;
        
        if (baseData.getDataType() == DataType.MATRIX || exponentData.getDataType() == DataType.MATRIX) {
            // Matrix operation
            if ("Matrix(^)".equals(powerMethodValue)) {
                // Matrix power operation (base^exponent where exponent is scalar)
                if (baseData.getDataType() == DataType.MATRIX && exponentData.getDataType() != DataType.MATRIX) {
                    Jama.Matrix baseMatrix = baseData.getMatrix();
                    double exp = exponentData.getInitValue();
                    Jama.Matrix outputMatrix = baseMatrix.copy();
                    
                    // For matrix power, we need to compute matrix^n which is matrix multiplication
                    if (exp == 0) {
                        // Identity matrix
                        outputMatrix = Jama.Matrix.identity(baseMatrix.getRowDimension(), baseMatrix.getColumnDimension());
                    } else if (exp > 0 && exp == Math.floor(exp)) {
                        // Integer power - repeated matrix multiplication
                        int intExp = (int) exp;
                        Jama.Matrix result = Jama.Matrix.identity(baseMatrix.getRowDimension(), baseMatrix.getColumnDimension());
                        for (int i = 0; i < intExp; i++) {
                            result = result.times(baseMatrix);
                        }
                        outputMatrix = result;
                    } else {
                        // Non-integer or negative power - element-wise fallback
                        for (int i = 0; i < baseMatrix.getRowDimension(); i++) {
                            for (int j = 0; j < baseMatrix.getColumnDimension(); j++) {
                                double value = Math.pow(baseMatrix.get(i, j), exp);
                                outputMatrix.set(i, j, value);
                            }
                        }
                    }
                    outputData = new Data(outputMatrix);
                } else {
                    // Element-wise fallback for incompatible matrix operations
                    outputData = performElementWisePower(baseData, exponentData);
                }
            } else {
                // Element-wise power operation
                outputData = performElementWisePower(baseData, exponentData);
            }
        } else {
            // Scalar inputs
            double baseValue = baseData.getInitValue();
            double expValue = exponentData.getInitValue();
            double result = Math.pow(baseValue, expValue);
            outputData = new Data(1, 1);
            outputData.setInitValue(result);
        }
        
        outputPortList.get(0).setData(outputData);
    }
    
    private Data performElementWisePower(Data baseData, Data exponentData) {
        if (baseData.getDataType() == DataType.MATRIX && exponentData.getDataType() == DataType.MATRIX) {
            // Both matrices - element-wise power
            Jama.Matrix baseMatrix = baseData.getMatrix();
            Jama.Matrix expMatrix = exponentData.getMatrix();
            Jama.Matrix outputMatrix = new Jama.Matrix(baseMatrix.getRowDimension(), baseMatrix.getColumnDimension());
            
            for (int i = 0; i < baseMatrix.getRowDimension(); i++) {
                for (int j = 0; j < baseMatrix.getColumnDimension(); j++) {
                    double base = baseMatrix.get(i, j);
                    double exp = expMatrix.get(i, j);
                    outputMatrix.set(i, j, Math.pow(base, exp));
                }
            }
            return new Data(outputMatrix);
        } else if (baseData.getDataType() == DataType.MATRIX) {
            // Base is matrix, exponent is scalar
            Jama.Matrix baseMatrix = baseData.getMatrix();
            double expValue = exponentData.getInitValue();
            Jama.Matrix outputMatrix = new Jama.Matrix(baseMatrix.getRowDimension(), baseMatrix.getColumnDimension());
            
            for (int i = 0; i < baseMatrix.getRowDimension(); i++) {
                for (int j = 0; j < baseMatrix.getColumnDimension(); j++) {
                    double base = baseMatrix.get(i, j);
                    outputMatrix.set(i, j, Math.pow(base, expValue));
                }
            }
            return new Data(outputMatrix);
        } else if (exponentData.getDataType() == DataType.MATRIX) {
            // Base is scalar, exponent is matrix
            double baseValue = baseData.getInitValue();
            Jama.Matrix expMatrix = exponentData.getMatrix();
            Jama.Matrix outputMatrix = new Jama.Matrix(expMatrix.getRowDimension(), expMatrix.getColumnDimension());
            
            for (int i = 0; i < expMatrix.getRowDimension(); i++) {
                for (int j = 0; j < expMatrix.getColumnDimension(); j++) {
                    double exp = expMatrix.get(i, j);
                    outputMatrix.set(i, j, Math.pow(baseValue, exp));
                }
            }
            return new Data(outputMatrix);
        } else {
            // Both scalars
            double result = Math.pow(baseData.getInitValue(), exponentData.getInitValue());
            Data outputData = new Data(1, 1);
            outputData.setInitValue(result);
            return outputData;
        }
    }
}