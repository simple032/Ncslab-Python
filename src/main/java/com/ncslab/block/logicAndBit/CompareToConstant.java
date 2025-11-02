package com.ncslab.block.logicAndBit;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputSignal;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.logic.CompareToConstantDto;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.logicAndBit.LogicBlock;
import Jama.Matrix;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.util.TemplateManager;

/**
 * CompareToConstant block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - ConstantValue: Constant value to compare against
 * - RelationalOperator: Comparison operator (==, !=, <, <=, >, >=)
 * - LogicDataType: Output data type for logic operations
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class CompareToConstant extends LogicBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter constantValue;
    private final Parameter relationalOperator;
    private final Parameter logicDataType;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Port References ===
    private OutputPort output;
    private InputPort input;

    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("ConstantValue", "0");
        PARAMETER_DEFAULTS.put("RelationalOperator", "==");
        PARAMETER_DEFAULTS.put("LogicDataType", "boolean");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");

        // SIMULINK parameter names

        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }

    // === Private Constructor with Typed Parameters ===
    private CompareToConstant(Parameter constantValue, Parameter relationalOperator, Parameter logicDataType,
                             Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                             String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.constantValue = Objects.requireNonNull(constantValue, "Constant value parameter cannot be null");
        this.relationalOperator = Objects.requireNonNull(relationalOperator, "Relational operator parameter cannot be null");
        this.logicDataType = Objects.requireNonNull(logicDataType, "Logic data type parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.constantValue);
        parameterList.add(this.relationalOperator);
        parameterList.add(this.logicDataType);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);

        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public CompareToConstant(JSONObject blockIn, NCSLabModel model) {
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

        this.constantValue = getParameterByName("ConstantValue");
        this.relationalOperator = getParameterByName("RelationalOperator");

        // Create missing SIMULINK parameters with defaults
        this.logicDataType = getParameterByName("LogicDataType");
        this.sampleTime = getParameterByName("SampleTime"); // -1 for inherited
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        // Initialize ports
        initializePorts();
    }    /**
     * DTO-NATIVE Constructor - Creates CompareToConstant block directly from BlockDto DTO
     */
    public CompareToConstant(CompareToConstantDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.constantValue = getParameterByName("Constantvalue");
        this.relationalOperator = getParameterByName("Relationaloperator");
        this.logicDataType = getParameterByName("Logicdatatype");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }



    // === Static Factory Method for JSON Deserialization ===
    public static CompareToConstant fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter constantValue = createConstantValueFromJSON(paramValues, blockName);
            Parameter relationalOperator = createRelationalOperatorFromJSON(paramValues, blockName);
            Parameter logicDataType = createLogicDataTypeFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            CompareToConstant block = new CompareToConstant(constantValue, relationalOperator, logicDataType,
                                                           sampleTime, outDataType, saturateParam,
                                                           blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, constantValue, relationalOperator, logicDataType,
                                     sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create CompareToConstant block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (Simple Overload) ===
    public static CompareToConstant create(String name, String path, double constantValue, String relationalOperator, NCSLabModel model) {
        return create(name, path, constantValue, relationalOperator, "boolean", -1.0,
                     "Inherit: Logical (see Configuration Parameters: Optimization)", false, model);
    }

    /**
     * Create a CompareToConstant block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * NOTE: CompareToConstantDto currently only supports constantValue and relationalOperator fields.
     * The additional parameters (logicDataType, sampleTime, outDataType, saturateOnOverflow) are
     * stored in the base BlockDto parameter map for backward compatibility.
     *
     * @param name Block name
     * @param path Block path
     * @param constantValue Constant value to compare against
     * @param relationalOperator Comparison operator (==, !=, <, <=, >, >=)
     * @param logicDataType Output logic data type (typically "boolean")
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return CompareToConstant block instance
     */
    public static CompareToConstant create(String name, String path, double constantValue, String relationalOperator,
                                          String logicDataType, double sampleTime, String outDataType,
                                          boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        // NOTE: CompareToConstantDto is missing some fields, so we only set what exists
        CompareToConstantDto dto = CompareToConstantDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .constantValue(com.ncslab.dto.common.TypedParameter.of(constantValue))
            .relationalOperator(com.ncslab.dto.common.TypedParameter.of(relationalOperator))
            .build();

        // Add missing parameters to the DTO's parameter map (workaround for incomplete DTO)
        if (dto.getParameters() == null) {
            dto.setParameters(new com.ncslab.dto.common.TypedParameterMap());
        }
        dto.getParameters().put("LogicDataType", com.ncslab.dto.common.TypedParameter.of(logicDataType));
        dto.getParameters().put("SampleTime", com.ncslab.dto.common.TypedParameter.of(sampleTime));
        dto.getParameters().put("OutDataTypeStr", com.ncslab.dto.common.TypedParameter.of(outDataType));
        dto.getParameters().put("SaturateOnIntegerOverflow", com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow));

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid CompareToConstant parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new CompareToConstant(dto, model);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createConstantValueFromJSON(JSONObject paramValues, String blockName) {
        String constantValueStr = paramValues.optString("const", "0.0");
        return new Parameter(null, 1, "ConstantValue", constantValueStr);
    }
    private static Parameter createRelationalOperatorFromJSON(JSONObject paramValues, String blockName) {
        String relopValue = paramValues.optString("relop", "==");
        if ("~=".equals(relopValue)) {
            relopValue = "!=";
        }
        return new Parameter(null, 2, "RelationalOperator", relopValue);
    }
    private static Parameter createLogicDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String logicDataTypeValue = paramValues.optString("LogicDataType", "boolean");
        return new Parameter(null, 3, "LogicDataType", logicDataTypeValue);
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
    private static void setParameterBlockReference(CompareToConstant block, Parameter... parameters) {
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
        identity.put("blockType", "CompareToConstant");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    // === Port Initialization ===
    private void initializePorts() {
        // Main input port
        input = new InputPort(this, 1);
        inputPortList.add(input);

        // Main output port (has feedthrough)
        output = new OutputPort(this, 1, true);
        outputPortList.add(output);
    }
    
    // === Getter Methods ===
    public double getConstantValue() {
        return constantValue.getData().getInitValue();
    }

    public String getRelationalOperator() {
        return relationalOperator.getData().getInitString();
    }

    // === Code Generation Methods ===
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/logicAndBit/CompareToConstant/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(DataType.REAL); // Logic output as REAL (0.0 or 1.0)
    }

    @Override
    public void checkDimension() throws MatDimException {
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK CompareToConstant block: compares input to constant value
        Data inputData = inputPortList.get(0).getData();
        double constantVal = getConstantValue();
        String operator = getRelationalOperator();
        Data outputData;
        
        if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix input - apply comparison element-wise
            Jama.Matrix inputMatrix = inputData.getMatrix();
            Jama.Matrix outputMatrix = new Jama.Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
            
            for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                    double value = inputMatrix.get(i, j);
                    boolean result = compare(value, constantVal, operator);
                    outputMatrix.set(i, j, fromBoolean(result));
                }
            }
            outputData = new Data(outputMatrix);
        } else {
            // Scalar input
            double inputValue = inputData.getInitValue();
            boolean result = compare(inputValue, constantVal, operator);
            outputData = new Data(1, 1);
            outputData.setInitValue(fromBoolean(result));
        }
        
        outputPortList.get(0).setData(outputData);
    }
}
