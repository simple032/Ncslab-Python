package com.ncslab.block.logicAndBit;

import com.ncslab.block.logicAndBit.LogicBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.logic.RelationalOperatorDto;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.util.TemplateManager;
import java.util.Map;
import java.util.HashMap;

/**
 * RelationalOperator block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - Operator: Relational operation to perform (==, !=, <, <=, >, >=)
 * - LogicDataType: Output data type for logic operations
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class RelationalOperator extends LogicBlock {

    // Legacy field for backward compatibility
    String relop;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter operator;
    private final Parameter logicDataType;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // SIMULINK parameter names
        
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2");
    }

    // === Parameter Defaults ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("Operator", "==");
        PARAMETER_DEFAULTS.put("LogicDataType", "boolean");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    // === Private Constructor with Typed Parameters ===
    private RelationalOperator(Parameter operator, Parameter logicDataType, Parameter sampleTime,
                              Parameter outDataType, Parameter saturateOnIntegerOverflow,
                              String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Assign parameters
        this.operator = Objects.requireNonNull(operator, "Operator parameter cannot be null");
        this.logicDataType = Objects.requireNonNull(logicDataType, "Logic data type parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Store legacy relop for backward compatibility
        this.relop = operator.getInitString();
        if("~=".equals(this.relop)) {
            this.relop = "!=";
        }
        
        // Create ports
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        OutputPort output = new OutputPort(this, 1, true);
        outputPortList.add(output);
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public RelationalOperator(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Determine operator value from legacy parameters
        String operatorValue;
        if (paramValues.has("relop")) {
            operatorValue = paramValues.getString("relop");
        } else {
            operatorValue = paramValues.getString("Operator");
        }
        
        // Create legacy parameters for backward compatibility
        this.operator = getParameterByName("Operator");
        this.logicDataType = getParameterByName("LogicDataType");
        this.sampleTime = getParameterByName("SampleTime"); // -1 for inherited
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Add all parameters to parameter list

        // Store legacy relop for backward compatibility
        this.relop = operatorValue;
        if (relop.equals("~=")) {
            relop = "!=";
        }

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        OutputPort output = new OutputPort(this, 1, true);
        outputPortList.add(output);
    }    /**
     * DTO-NATIVE Constructor - Creates RelationalOperator block directly from RelationalOperatorDto
     */
    public RelationalOperator(RelationalOperatorDto dto, NCSLabModel model) {
        super(dto, model);

        // Initialize parameters from DTO with null safety
        String operatorValue = dto.getOperator() != null ? dto.getOperator().getAsString() : "==";
        this.operator = getParameterByName("Operator");
        
        String logicDataTypeValue = dto.getLogicDataType() != null ? dto.getLogicDataType().getAsString() : "boolean";
        this.logicDataType = getParameterByName("LogicDataType");
        
        double sampleTimeValue = dto.getSampleTime() != null ? dto.getSampleTime().getAsDouble() : -1.0;
        this.sampleTime = getParameterByName("SampleTime");
        
        String outDataTypeValue = dto.getOutDataTypeStr() != null ? dto.getOutDataTypeStr().getAsString() : "Inherit: Logical (see Configuration Parameters: Optimization)";
        this.outDataType = getParameterByName("OutDataTypeStr");
        
        String saturateValue = dto.getSaturateOnIntegerOverflow() != null ? dto.getSaturateOnIntegerOverflow().getAsString() : "off";
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Store legacy relop for backward compatibility (normalize ~= to !=)
        this.relop = operatorValue;
        if("~=".equals(this.relop)) {
            this.relop = "!=";
        }
        
        // Create ports
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        OutputPort output = new OutputPort(this, 1, true);
        outputPortList.add(output);

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + dto.getBlockName());
    }




    // === Static Factory Method for JSON Deserialization ===
    public static RelationalOperator fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter operator = createOperatorFromJSON(paramValues, blockName);
            Parameter logicDataType = createLogicDataTypeFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            RelationalOperator block = new RelationalOperator(operator, logicDataType, sampleTime,
                                                              outDataType, saturateParam,
                                                              blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, operator, logicDataType, sampleTime,
                                     outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create RelationalOperator block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static RelationalOperator create(String name, String path, String operator, NCSLabModel model) {
        return create(name, path, operator, "boolean", -1.0, 
                     "Inherit: Logical (see Configuration Parameters: Optimization)", false, model);
    }
    
    public static RelationalOperator create(String name, String path, String operator, String logicDataType,
                                           double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter operatorParam = new Parameter(null, 1, "Operator", operator);
        Parameter logicDataTypeParam = new Parameter(null, 2, "LogicDataType", logicDataType);
        Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        RelationalOperator block = new RelationalOperator(operatorParam, logicDataTypeParam, sampleTimeParam,
                                                          outDataTypeParam, saturateParam,
                                                          name, path, "null", model);
        
        setParameterBlockReference(block, operatorParam, logicDataTypeParam, sampleTimeParam,
                                 outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createOperatorFromJSON(JSONObject paramValues, String blockName) {
        String operatorValue;
        if (paramValues.has("relop")) {
            operatorValue = paramValues.getString("relop");
        } else {
            operatorValue = paramValues.optString("Operator", "==");
        }
        if ("~=".equals(operatorValue)) {
            operatorValue = "!=";
        }
        return new Parameter(null, 1, "Operator", operatorValue);
    }
    
    private static Parameter createLogicDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String logicDataTypeValue = paramValues.optString("LogicDataType", "boolean");
        return new Parameter(null, 2, "LogicDataType", logicDataTypeValue);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        return new Parameter(null, 4, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateValue);
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
    
    private static void setParameterBlockReference(RelationalOperator block, Parameter... parameters) {
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
        identity.put("blockType", "RelationalOperator");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    @Override
    public void calculateInit() {
        // Initialization logic for RelationalOperator block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData1 = inputPortList.get(0).getData();
        Data inputData2 = inputPortList.get(1).getData();

        Data resultData;
        switch (inputData1.getDataType()) {
            case REAL:
                resultData = new Data(compareValues(inputData1.getInitValue(), inputData2.getInitValue(), relop));
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData1.getMatrix().getRowDimension(), inputData1.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData1.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData1.getMatrix().getColumnDimension(); j++) {
                        matrixResult.set(i, j, compareValues(inputData1.getMatrix().get(i, j), inputData2.getMatrix().get(i, j), relop));
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
    }

    private double compareValues(double inputValue1, double inputValue2, String operator) {
        switch (operator) {
            case "==":
                return inputValue1 == inputValue2 ? 1.0 : 0.0;
            case "!=":
                return inputValue1 != inputValue2 ? 1.0 : 0.0;
            case "<":
                return inputValue1 < inputValue2 ? 1.0 : 0.0;
            case "<=":
                return inputValue1 <= inputValue2 ? 1.0 : 0.0;
            case ">":
                return inputValue1 > inputValue2 ? 1.0 : 0.0;
            case ">=":
                return inputValue1 >= inputValue2 ? 1.0 : 0.0;
            default:
                return 0.0;
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        OutputPort out = outputPortList.get(0);
        OutputSignal signal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        
        // Use proper C variable names instead of Java object references
        // Signal names already include Block{id}_Output{port} prefix, don't add another prefix
        context.put("signal1Name", signal1.getName()); // Already properly prefixed
        context.put("signal2Name", signal2.getName()); // Already properly prefixed
        context.put("opsName", out.getOutputSignalC().getName());
        context.put("relop", relop);
        
        // Add input port variables
        context.put("inputSignal1", getInputPortVariable(0));
        context.put("inputSignal2", getInputPortVariable(1));
        context.put("outputSignal", getOutputPortVariable(0));

        String codeStr = TemplateManager.renderTemplate("c/logicAndBit/RelationalOperator/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal signal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (signal1.getHeight() != signal2.getHeight() || signal1.getWidth() != signal2.getWidth()) {
            MatDimException e = new MatDimException("Block " + this.blockName + " two input dimension don't match!\n \n");
            throw (e);
        }
        out.setHeight(signal1.getHeight());
        out.setWidth(signal1.getWidth());
        out.getOutputSignalC().setHeight(signal1.getHeight());
        out.getOutputSignalC().setWidth(signal1.getWidth());
        out.getOutputSignalC().setDataType(signal1.getDataType());
    }

    public void checkDimension() throws MatDimException {
    }
}
