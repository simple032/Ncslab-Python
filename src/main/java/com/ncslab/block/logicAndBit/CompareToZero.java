package com.ncslab.block.logicAndBit;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.logic.CompareToZeroDto;

import com.ncslab.block.logicAndBit.LogicBlock;
import Jama.Matrix;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * CompareToZero block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - RelationalOperator: Comparison operator (==, !=, <, <=, >, >=)
 * - LogicDataType: Output data type for logic operations
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class CompareToZero extends LogicBlock{

    // Legacy field for backward compatibility
    String relop;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter relationalOperator;
    private final Parameter logicDataType;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        // SIMULINK parameter names

        // Port names
        outputNames.add("out1");
        inputNames.add("in1");

        // Parameter defaults
        PARAMETER_DEFAULTS.put("RelationalOperator", ">=");
        PARAMETER_DEFAULTS.put("LogicDataType", "boolean");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    // === Private Constructor with Typed Parameters ===
    private CompareToZero(Parameter relationalOperator, Parameter logicDataType, Parameter sampleTime,
                         Parameter outDataType, Parameter saturateOnIntegerOverflow,
                         String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.relationalOperator = Objects.requireNonNull(relationalOperator, "Relational operator parameter cannot be null");
        this.logicDataType = Objects.requireNonNull(logicDataType, "Logic data type parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Store legacy relop for backward compatibility
        this.relop = relationalOperator.getInitString();
        if("~=".equals(this.relop)) {
            this.relop = "!=";
        }

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
	public CompareToZero(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);
        String relopValue = "";
        // Create legacy parameters for backward compatibility
        if(paramValues.has("RelationalOperator")) {
            relopValue = paramValues.getString("RelationalOperator");
        }else if(paramValues.has("Operator")) {
            relopValue = paramValues.getString("Operator");
        }
        if ("~=".equals(relopValue)) {
            relopValue = "!=";
        }
        this.relationalOperator = getParameterByName("RelationalOperator");

        // Create missing SIMULINK parameters with defaults
        this.logicDataType = getParameterByName("LogicDataType");
        this.sampleTime = getParameterByName("SampleTime"); // -1 for inherited
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Add all parameters to parameter list

        // Create ports
		inputPortList.add(new InputPort(this, 1));
		outputPortList.add(new OutputPort(this, 1, true));
    }
    /**
     * DTO Constructor - Creates CompareToZero block from CompareToZeroDto with proper parameter mapping
     */
    public CompareToZero(com.ncslab.dto.block.specialized.logic.CompareToZeroDto dto, NCSLabModel model) {
        super(dto, model);

        // Extract parameters from DTO
        this.relationalOperator = getParameterByName("RelationalOperator");
        this.logicDataType = getParameterByName("LogicDataType");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Store legacy relop for backward compatibility
        this.relop = dto.getRelationalOperatorValue();
        if("~=".equals(this.relop)) {
            this.relop = "!=";
        }

        // Initialize ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));

        System.out.println("DTO: " + getClass().getSimpleName() + " block created from CompareToZeroDto - " + dto.getBlockName());
    }    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }



    // === Static Factory Method for JSON Deserialization ===
    public static CompareToZero fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter relationalOperator = createRelationalOperatorFromJSON(paramValues, blockName);
            Parameter logicDataType = createLogicDataTypeFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            CompareToZero block = new CompareToZero(relationalOperator, logicDataType, sampleTime,
                                                   outDataType, saturateParam,
                                                   blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, relationalOperator, logicDataType, sampleTime,
                                     outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create CompareToZero block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static CompareToZero create(String name, String path, String relationalOperator, NCSLabModel model) {
        return create(name, path, relationalOperator, "boolean", -1.0,
                     "Inherit: Logical (see Configuration Parameters: Optimization)", false, model);
    }

    public static CompareToZero create(String name, String path, String relationalOperator, String logicDataType,
                                      double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter relationalOperatorParam = new Parameter(null, 1, "RelationalOperator", relationalOperator);
        Parameter logicDataTypeParam = new Parameter(null, 2, "LogicDataType", logicDataType);
        Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");

        CompareToZero block = new CompareToZero(relationalOperatorParam, logicDataTypeParam, sampleTimeParam,
                                               outDataTypeParam, saturateParam,
                                               name, path, "null", model);

        setParameterBlockReference(block, relationalOperatorParam, logicDataTypeParam, sampleTimeParam,
                                 outDataTypeParam, saturateParam);

        return block;
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createRelationalOperatorFromJSON(JSONObject paramValues, String blockName) {
        String relopValue = paramValues.optString("relop", "==");
        if ("~=".equals(relopValue)) {
            relopValue = "!=";
        }
        return new Parameter(null, 1, "RelationalOperator", relopValue);
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

    private static void setParameterBlockReference(CompareToZero block, Parameter... parameters) {
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
        identity.put("blockType", "CompareToZero");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    @Override
    public void calculateInit() {
        // Initialization logic for CompareToZero block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();
        String relationalOperatorValue = relationalOperator.getInitString();

        Data resultData;
        switch (inputData.getDataType()) {
            case REAL:
                resultData = new Data(compare(inputData.getInitValue(), relationalOperatorValue));
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                        matrixResult.set(i, j, compare(inputData.getMatrix().get(i, j), relationalOperatorValue));
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
    }

    private double compare(double inputValue, String operator) {
        switch (operator) {
            case "==":
                return inputValue == 0 ? 1.0 : 0.0;
            case "!=":
                return inputValue != 0 ? 1.0 : 0.0;
            case "<":
                return inputValue < 0 ? 1.0 : 0.0;
            case "<=":
                return inputValue <= 0 ? 1.0 : 0.0;
            case ">":
                return inputValue > 0 ? 1.0 : 0.0;
            case ">=":
                return inputValue >= 0 ? 1.0 : 0.0;
            default:
                return 0.0;
        }
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode="";
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        
        context.put("block", this);
        context.put("relationalOperator", relationalOperator.getInitString());
        context.put("logicDataType", logicDataType);
        context.put("sampleTime", sampleTime);
        context.put("outDataType", outDataType);
        context.put("saturateOnIntegerOverflow", saturateOnIntegerOverflow);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/logicAndBit/CompareToZero/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException{
        OutputPort out  = outputPortList.get(0);
        InputPort in  = inputPortList.get(0);
        OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
   }

    public void checkDimension() throws MatDimException{
    }

}
