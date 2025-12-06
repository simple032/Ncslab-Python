package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.RoundingFunctionDto;

import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * RoundingFunction block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - Operator: Type of rounding operation (floor, ceil, round, fix)
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 *
 * Operations:
 * - floor: Round toward negative infinity (Math.floor)
 * - ceil: Round toward positive infinity (Math.ceil)
 * - round: Round toward nearest integer (Math.round)
 * - fix: Round toward zero / truncate (Math.trunc)
 *
 * All operations are element-wise for matrix inputs.
 */
public class RoundingFunction extends MathBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter operator;
    private final Parameter sampleTime;
    private final Parameter outDataType;

    // Cached operator string for performance
    private String operatorString;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Operator", "floor");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
    }

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");

        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);

        // Output port defaults (rounding has feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private RoundingFunction(Parameter operator, Parameter sampleTime, Parameter outDataType,
                            String blockName, String blockPath,
                            String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(operator, sampleTime);

        // Assign parameters
        this.operator = Objects.requireNonNull(operator, "Operator parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");

        // Cache operator string
        this.operatorString = operator.getInitString();
        if ("fix".equals(operatorString)) {
            operatorString = "trunc";
        }

        // Add parameters to parameterList for template context population
        parameterList.add(this.operator);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);

        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public RoundingFunction(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Use name-based parameter access instead of index-based
        this.operator = getParameterByName("Operator");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Cache operator string
        this.operatorString = operator.getInitString();
        if ("fix".equals(operatorString)) {
            operatorString = "trunc";
        }

        initializePorts();
    }

    /**
     * DTO Constructor - Creates RoundingFunction block directly from RoundingFunctionDto DTO
     */
    public RoundingFunction(RoundingFunctionDto dto, NCSLabModel model) {
        super(dto, model);

        // Extract parameters from DTO with defaults
        String operatorValue = dto.getOperatorValue();
        String sampleTimeValue = dto.getSampleTime() != null ? (dto.getSampleTime().getAsString()) : "-1";
        String outDataTypeValue = dto.getOutDataTypeStrValue();

        // Validate operator
        if (!isValidOperator(operatorValue)) {
            throw new IllegalArgumentException("Invalid operator: " + operatorValue +
                    ". Must be one of: floor, ceil, round, fix");
        }

        // Validate sample time
        double sampleTimeDouble = Double.parseDouble(sampleTimeValue);
        if (sampleTimeDouble != -1.0 && sampleTimeDouble <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }

        // Initialize parameters
        this.operator = getParameterByName("Operator");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");

        // Cache operator string
        this.operatorString = operatorValue;
        if ("fix".equals(operatorString)) {
            operatorString = "trunc";
        }

        initializePorts();

        System.out.println("DTO-SPECIFIC: RoundingFunction block created successfully from RoundingFunctionDto - " +
                dto.getBlockName() + " [operator=" + operatorValue + "]");
    }

    /**
     * Factory method to create RoundingFunction block from RoundingFunctionDto.
     *
     * @param dto The RoundingFunctionDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New RoundingFunction block instance
     * @throws BlockCreationException if block creation fails
     */
    public static RoundingFunction createFromDto(RoundingFunctionDto dto, NCSLabModel model) throws BlockCreationException {
        return new RoundingFunction(dto, model);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static RoundingFunction fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter operator = createOperatorFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);

            RoundingFunction block = new RoundingFunction(operator, sampleTime, outDataType,
                                   blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, operator, sampleTime, outDataType);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create RoundingFunction block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Create a RoundingFunction block with default parameters using DTO-based construction.
     *
     * @param name Block name
     * @param path Block path
     * @param model Parent model
     * @return Configured RoundingFunction block instance
     */
    public static RoundingFunction create(String name, String path, NCSLabModel model) {
        return create(name, path, "floor", -1.0, "Inherit: Same as input", model);
    }

    /**
     * Create a RoundingFunction block with full parameters using DTO-based construction.
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param operator Rounding operator ("floor", "ceil", "round", "fix")
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param outDataType Output data type specification
     * @param model Parent model
     * @return Configured RoundingFunction block instance
     */
    public static RoundingFunction create(String name, String path, String operator,
                                         double sampleTime, String outDataType, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        RoundingFunctionDto dto = RoundingFunctionDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .operator(com.ncslab.dto.common.TypedParameter.of(operator))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid RoundingFunction parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new RoundingFunction(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter operator, Parameter sampleTime) {
        // Validate operator
        String operatorValue = operator.getInitString();
        if (!isValidOperator(operatorValue)) {
            throw new IllegalArgumentException("Invalid operator: " + operatorValue +
                    ". Must be one of: floor, ceil, round, fix");
        }

        // Validate sample time
        double sampleTimeValue = sampleTime.getDouble();
        if (sampleTimeValue != -1.0 && sampleTimeValue <= 0.0) {
            throw new IllegalArgumentException("Sample time must be positive or -1 (inherited)");
        }
    }

    private static boolean isValidOperator(String op) {
        return "floor".equals(op) || "ceil".equals(op) ||
               "round".equals(op) || "fix".equals(op);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createOperatorFromJSON(JSONObject paramValues, String blockName) {
        String operatorValue = paramValues.optString("Operator", "floor");
        return new Parameter(null, 1, "Operator", operatorValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 2, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 3, "OutDataTypeStr", outDataTypeValue);
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

    private static void setParameterBlockReference(RoundingFunction block, Parameter... parameters) {
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
        identity.put("blockType", "RoundingFunction");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject()); // Add empty paramValues to avoid JSONException
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Code Generation Methods ===
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode="";
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/math/RoundingFunction/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    public void checkDimension() throws MatDimException {
        // Check dimensions if needed
    }

    @Override
    public void calculateOutput(double t) {
        // SIMULINK RoundingFunction block: applies rounding operation to input
        Data inputData = inputPortList.get(0).getData();
        Data outputData;

        if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix input - apply rounding function element-wise
            Jama.Matrix inputMatrix = inputData.getMatrix();
            Jama.Matrix outputMatrix = new Jama.Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());

            for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                    double value = inputMatrix.get(i, j);
                    double roundedValue = applyRoundingFunction(value, operatorString);
                    outputMatrix.set(i, j, roundedValue);
                }
            }
            outputData = new Data(outputMatrix);
        } else {
            // Scalar input - apply rounding function directly
            double inputValue = inputData.getInitValue();
            double roundedValue = applyRoundingFunction(inputValue, operatorString);
            outputData = new Data(1, 1);
            outputData.setInitValue(roundedValue);
        }

        outputPortList.get(0).setData(outputData);
    }

    /**
     * Apply the specified rounding function to a value
     *
     * @param value Input value
     * @param operator Rounding operator ("floor", "ceil", "round", "trunc")
     * @return Rounded value
     */
    private double applyRoundingFunction(double value, String operator) {
        switch (operator) {
            case "floor":
                return Math.floor(value);
            case "ceil":
                return Math.ceil(value);
            case "round":
                return Math.round(value);
            case "trunc":
            case "fix":
                // Truncate toward zero
                return value >= 0 ? Math.floor(value) : Math.ceil(value);
            default:
                return Math.floor(value); // Default to floor
        }
    }
}
