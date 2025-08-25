package com.ncslab.block.logicAndBit;

import com.ncslab.block.logicAndBit.LogicBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.logic.LogicalOperatorDto;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.util.TemplateManager;

/**
 * LogicOperator block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Operator: Logic operation to perform (AND, OR, NAND, NOR, XOR, NOT)
 * - Inputs: Number of input ports
 * - AllPortsSameDT: Force all ports to have same data type
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class LogicOperator extends LogicBlock {
    private double num;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter operator;
    private final Parameter inputs;
    private final Parameter allPortsSameDT;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Operator", "AND");
        PARAMETER_DEFAULTS.put("Inputs", "2");
        PARAMETER_DEFAULTS.put("AllPortsSameDT", "on");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "boolean");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // SIMULINK parameter names
        
        // Port names
        outputNames.add("out1");
        // Input names are dynamic based on number of inputs
    }

    // === Private Constructor with Typed Parameters ===
    private LogicOperator(Parameter operator, Parameter inputs, Parameter allPortsSameDT,
                         Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                         String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Assign parameters
        this.operator = Objects.requireNonNull(operator, "Operator parameter cannot be null");
        this.inputs = Objects.requireNonNull(inputs, "Inputs parameter cannot be null");
        this.allPortsSameDT = Objects.requireNonNull(allPortsSameDT, "AllPortsSameDT parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Parse number of inputs and create ports
        this.num = Double.parseDouble(inputs.getInitString());
        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);
        
        for (int i = 0; i < num; i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public LogicOperator(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.operator = getParameterByName("Operator");
        this.inputs = getParameterByName("Inputs");
        this.allPortsSameDT = getParameterByName("AllPortsSameDT");
        this.sampleTime = getParameterByName("SampleTime"); // -1 for inherited
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Add all parameters to parameter list

        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);
        paraseParamValues();
    }

    public void paraseParamValues() {
        num = paramValues.getDouble("Inputs");
        for (int i = 0; i < num; i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }
    }    /**
     * DTO-NATIVE Constructor - Creates LogicOperator block directly from LogicalOperatorDto
     */
    public LogicOperator(LogicalOperatorDto dto, NCSLabModel model) {
        super(dto, model);

        // Initialize parameters from DTO with null safety
        this.operator = getParameterByName("Operator");
        int inputsValue = dto.getInputs() != null ? dto.getInputs().getAsInteger() : 2;
        this.inputs = getParameterByName("Inputs");
        
        String allPortsSameDTValue = dto.getAllPortsSameDT() != null ? dto.getAllPortsSameDT().getAsString() : "on";
        this.allPortsSameDT = getParameterByName("AllPortsSameDT");
        
        double sampleTimeValue = dto.getSampleTime() != null ? dto.getSampleTime().getAsDouble() : -1.0;
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Parse number of inputs and create ports
        this.num = dto.getInputsValue();
        
        // Create ports based on DTO configuration
        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);
        
        for (int i = 0; i < num; i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + dto.getBlockName());
    }


    // === Static Factory Method for JSON Deserialization ===
    public static LogicOperator fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter operator = createOperatorFromJSON(paramValues, blockName);
            Parameter inputs = createInputsFromJSON(paramValues, blockName);
            Parameter allPortsSameDT = createAllPortsSameDTFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            LogicOperator block = new LogicOperator(operator, inputs, allPortsSameDT, sampleTime,
                                                   outDataType, saturateParam,
                                                   blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, operator, inputs, allPortsSameDT, sampleTime,
                                     outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create LogicOperator block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static LogicOperator create(String name, String path, String operator, int numberOfInputs, NCSLabModel model) {
        return create(name, path, operator, String.valueOf(numberOfInputs), "on", -1.0, 
                     "Inherit: Logical (see Configuration Parameters: Optimization)", false, model);
    }
    
    public static LogicOperator create(String name, String path, String operator, String inputs, String allPortsSameDT,
                                      double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter operatorParam = new Parameter(null, 1, "Operator", operator);
        Parameter inputsParam = new Parameter(null, 2, "Inputs", inputs);
        Parameter allPortsSameDTParam = new Parameter(null, 3, "AllPortsSameDT", allPortsSameDT);
        Parameter sampleTimeParam = new Parameter(null, 4, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 5, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        LogicOperator block = new LogicOperator(operatorParam, inputsParam, allPortsSameDTParam, sampleTimeParam,
                                               outDataTypeParam, saturateParam,
                                               name, path, "null", model);
        
        setParameterBlockReference(block, operatorParam, inputsParam, allPortsSameDTParam, sampleTimeParam,
                                 outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createOperatorFromJSON(JSONObject paramValues, String blockName) {
        String operatorValue = paramValues.optString("Operator", "AND");
        return new Parameter(null, 1, "Operator", operatorValue);
    }
    
    private static Parameter createInputsFromJSON(JSONObject paramValues, String blockName) {
        String inputsValue = String.valueOf(paramValues.optDouble("Inputs", 2));
        return new Parameter(null, 2, "Inputs", inputsValue);
    }
    
    private static Parameter createAllPortsSameDTFromJSON(JSONObject paramValues, String blockName) {
        String allPortsSameDTValue = paramValues.optString("AllPortsSameDT", "on");
        return new Parameter(null, 3, "AllPortsSameDT", allPortsSameDTValue);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 4, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        return new Parameter(null, 5, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 6, "SaturateOnIntegerOverflow", saturateValue);
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
    
    private static void setParameterBlockReference(LogicOperator block, Parameter... parameters) {
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
        identity.put("blockType", "LogicOperator");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    @Override
    public void calculateInit() {
        // Initialization logic for LogicOperator block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data resultData = null;

        switch (out.getOutputSignalC().getDataType()) {
            case REAL:
                double firstValue = inputPortList.get(0).getData().getInitValue();
                for (int i = 1; i < num; i++) {
                    Data inputData = inputPortList.get(i).getData();
                    if (inputData.getDataType() != DataType.REAL) {
                        break;
                    }
                    firstValue = applyOperator(firstValue, inputData.getInitValue(), operator.getInitString());
                }
                resultData = new Data(firstValue);
                break;
            case MATRIX:
                Matrix firstMatrix = inputPortList.get(0).getData().getMatrix();
                for (int i = 1; i < num; i++) {
                    Data inputData = inputPortList.get(i).getData();
                    if (inputData.getDataType() != DataType.MATRIX) {
                        break;
                    }
                    firstMatrix = applyMatrixOperator(firstMatrix, inputData.getMatrix(), operator.getInitString());
                }
                resultData = new Data(firstMatrix);
                break;
        }

        out.setData(resultData);
    }

    private double applyOperator(double a, double b, String operator) {
        switch (operator) {
            case "AND":
                return a != 0 && b != 0 ? 1.0 : 0.0;
            case "OR":
                return a != 0 || b != 0 ? 1.0 : 0.0;
            case "NAND":
                return !(a != 0 && b != 0) ? 1.0 : 0.0;
            case "NOR":
                return !(a != 0 || b != 0) ? 1.0 : 0.0;
            case "XOR":
                return (a != 0 || b != 0) && !(a != 0 && b != 0) ? 1.0 : 0.0;
            case "NOT":
                return a == 0 ? 1.0 : 0.0;
            default:
                return 0.0;
        }
    }

    private Matrix applyMatrixOperator(Matrix a, Matrix b, String operator) {
        Matrix result = new Matrix(a.getRowDimension(), a.getColumnDimension());
        for (int i = 0; i < a.getRowDimension(); i++) {
            for (int j = 0; j < a.getColumnDimension(); j++) {
                result.set(i, j, applyOperator(a.get(i, j), b.get(i, j), operator));
            }
        }
        return result;
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputPort out  = outputPortList.get(0);
        OutputSignal signal1=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        String operatorValue = operator.getInitString();
        OutputSignal signal[]=new OutputSignal[(int) num];
        for(int i = 0; i < num; i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        }
        context.put("block", this);
        context.put("inputs", inputPortList); // 输入端口列表
        context.put("inputLength", num);
        context.put("outputs", getOutputPortVariables()); // 输出端口变量（假设为List<OutputSignal>）
        context.put("opsName", out.getOutputSignalC().getName());
        context.put("operator", operatorValue);
        context.put("signal1", signal1);
        String codeStr = TemplateManager.renderTemplate("c/logicAndBit/LogicOperator/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal[] signal = new OutputSignal[(int) num];
        for (int i = 0; i < num; i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        }
        int m = signal[0].getHeight();
        int n = signal[0].getWidth();
        int v = 1;
        for (OutputSignal x : signal) {
            if ((x.getHeight() != m) || (x.getWidth() != n)) {
                v = 0;
                MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions doesn't match !\n \n");
                throw (e);
            }
        }

        if (v == 1) {
            out.setHeight(signal[0].getHeight());
            out.setWidth(signal[0].getWidth());
            out.getOutputSignalC().setHeight(signal[0].getHeight());
            out.getOutputSignalC().setWidth(signal[0].getWidth());
            out.getOutputSignalC().setDataType(signal[0].getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
        if (operator.getInitString().equals("NOT") && num > 1) {
            MatDimException e = new MatDimException("when Block " + this.blockName + " operater is NOT, there must be one input!\n \n");
            throw (e);
        }
    }
}
