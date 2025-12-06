package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.data.Data;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.block.specialized.subsystem.IfDto;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * If block - Conditional execution control with multiple branches.
 *
 * <p>The If block evaluates conditional expressions and activates corresponding
 * action subsystems based on which condition is true. It enables branching logic
 * in models through if-elseif-else structures.</p>
 *
 * <p><b>SIMULINK Parameters:</b></p>
 * <ul>
 *   <li><b>NumberOfInputs</b>: Number of input ports (u1, u2, ...), default 1</li>
 *   <li><b>IfExpression</b>: Primary if condition expression (e.g., "u1 > 0")</li>
 *   <li><b>ElseIfExpressions</b>: Array of additional elseif conditions</li>
 *   <li><b>ShowElse</b>: Whether to have else branch output, default false</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 * </ul>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li><b>Multi-Branch Logic</b>: Supports if, elseif, and optional else branches</li>
 *   <li><b>Expression Evaluation</b>: Evaluates conditions in order until first true</li>
 *   <li><b>Action Outputs</b>: Outputs function-call signals to IfActionSubsystems</li>
 *   <li><b>Multiple Inputs</b>: Can reference multiple inputs (u1, u2, etc.) in expressions</li>
 * </ul>
 *
 * <p><b>Expression Syntax:</b></p>
 * <ul>
 *   <li>Input references: u1, u2, u3, ... (corresponding to input port numbers)</li>
 *   <li>Comparison operators: ==, ~=, >, >=, <, <=</li>
 *   <li>Logical operators: && (and), || (or), ~ (not)</li>
 *   <li>Example: "u1 > 0 && u2 <= 10"</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>N input ports (data inputs, configured by NumberOfInputs)</li>
 *   <li>M action output ports (1 for if + num elseif + 1 optional else)</li>
 *   <li>Output ports are function-call type, connect to IfActionSubsystem action ports</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-22
 */
public class If extends Block {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter numberOfInputs;
    @Getter
    private final Parameter ifExpression;
    @Getter
    private final Parameter elseIfExpressions;
    private final Parameter showElse;
    private final Parameter outDataTypeStr;
    private final Parameter sampleTime;

    // === Port References ===
    private List<InputPort> dataInputs;
    private List<OutputPort> actionOutputs;

    // === Evaluation State ===
    @Getter
    private int activeOutputIndex = -1; // Which output is currently active (-1 = none)

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("NumberOfInputs", "1");
        PARAMETER_DEFAULTS.put("IfExpression", "u1 ~= 0");
        PARAMETER_DEFAULTS.put("ElseIfExpressions", "[]"); // Empty JSON array
        PARAMETER_DEFAULTS.put("ShowElse", "off");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Action");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names - dynamically constructed based on configuration
        inputNames.add("u1"); // Default first input
        outputNames.add("if"); // Default if output
    }

    // === Private Constructor with Typed Parameters ===
    private If(Parameter numberOfInputs, Parameter ifExpression, Parameter elseIfExpressions,
               Parameter showElse, Parameter outDataTypeStr, Parameter sampleTime,
               String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.numberOfInputs = Objects.requireNonNull(numberOfInputs, "NumberOfInputs parameter cannot be null");
        this.ifExpression = Objects.requireNonNull(ifExpression, "IfExpression parameter cannot be null");
        this.elseIfExpressions = Objects.requireNonNull(elseIfExpressions, "ElseIfExpressions parameter cannot be null");
        this.showElse = Objects.requireNonNull(showElse, "ShowElse parameter cannot be null");
        this.outDataTypeStr = Objects.requireNonNull(outDataTypeStr, "OutDataTypeStr parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.numberOfInputs);
        parameterList.add(this.ifExpression);
        parameterList.add(this.elseIfExpressions);
        parameterList.add(this.showElse);
        parameterList.add(this.outDataTypeStr);
        parameterList.add(this.sampleTime);

        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public If(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create SIMULINK parameters with defaults
        this.numberOfInputs = getParameterByName("NumberOfInputs");
        this.ifExpression = getParameterByName("IfExpression");
        this.elseIfExpressions = getParameterByName("ElseIfExpressions");
        this.showElse = getParameterByName("ShowElse");
        this.outDataTypeStr = getParameterByName("OutDataTypeStr");
        this.sampleTime = getParameterByName("SampleTime");

        initializePorts();
    }

    /**
     * DTO-NATIVE Constructor - Creates If block directly from IfDto DTO.
     */
    public If(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.numberOfInputs = getParameterByName("NumberOfInputs");
        this.ifExpression = getParameterByName("IfExpression");
        this.elseIfExpressions = getParameterByName("ElseIfExpressions");
        this.showElse = getParameterByName("ShowElse");
        this.outDataTypeStr = getParameterByName("OutDataTypeStr");
        this.sampleTime = getParameterByName("SampleTime");

        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static If fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter numberOfInputs = createNumberOfInputsFromJSON(paramValues);
            Parameter ifExpression = createIfExpressionFromJSON(paramValues);
            Parameter elseIfExpressions = createElseIfExpressionsFromJSON(paramValues);
            Parameter showElse = createShowElseFromJSON(paramValues);
            Parameter outDataTypeStr = createOutDataTypeStrFromJSON(paramValues);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues);

            If block = new If(numberOfInputs, ifExpression, elseIfExpressions, showElse,
                            outDataTypeStr, sampleTime,
                            blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, numberOfInputs, ifExpression, elseIfExpressions,
                                      showElse, outDataTypeStr, sampleTime);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create If block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static If create(String name, String path, NCSLabModel model) {
        return create(name, path, 1, "u1 ~= 0", new String[]{}, false, -1.0, model);
    }

    /**
     * Create an If block with full parameters (DTO-based approach).
     *
     * @param name Block name
     * @param path Block path
     * @param numberOfInputs Number of input ports
     * @param ifExpression Primary if condition
     * @param elseIfExpressions Array of elseif conditions
     * @param showElse Whether to show else output
     * @param sampleTime Sample time (0 for continuous, -1 for inherited, >0 for discrete)
     * @param model Parent model
     * @return If block instance
     */
    public static If create(String name, String path, int numberOfInputs, String ifExpression,
                           String[] elseIfExpressions, boolean showElse, double sampleTime,
                           NCSLabModel model) {
        // Build DTO using constructor with individual parameters
        List<String> elseIfList = new ArrayList<>();
        if (elseIfExpressions != null) {
            for (String expr : elseIfExpressions) {
                elseIfList.add(expr);
            }
        }

        IfDto dto = new IfDto(
            name,
            path,
            com.ncslab.dto.common.TypedParameter.of(numberOfInputs),
            com.ncslab.dto.common.TypedParameter.of(ifExpression),
            com.ncslab.dto.common.TypedParameter.of(elseIfList),
            com.ncslab.dto.common.TypedParameter.of(showElse),
            com.ncslab.dto.common.TypedParameter.of(sampleTime)
        );

        // Set blockUUID
        dto.setBlockUUID("null");

        // Validate DTO (automatic validation)
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid If parameters: " + dto.getValidationErrors());
        }

        // Use DTO constructor
        return new If(dto, model);
    }

    /**
     * Factory method to create If from IfDto.
     *
     * @param dto   IfDto containing block configuration
     * @param model NCSLabModel containing the block diagram
     * @return Created If block
     */
    public static If createFromDto(IfDto dto, NCSLabModel model) {
        if (dto == null) {
            throw new IllegalArgumentException("IfDto cannot be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("NCSLabModel cannot be null");
        }

        // Validate DTO before creating block
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid IfDto: " + dto.getValidationErrors());
        }

        return new If(dto, model);
    }

    // === Code Generation Methods ===

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add If-specific context
        context.put("numberOfInputs", getNumberOfInputsValue());
        context.put("ifExpression", getIfExpressionValue());
        context.put("elseIfExpressions", getElseIfExpressionsValue());
        context.put("showElse", getShowElseValue());
        context.put("numberOfOutputs", getNumberOfOutputs());
        context.put("activeOutputIndex", activeOutputIndex);

        // Add input signal names
        List<String> inputSignalNames = new ArrayList<>();
        for (int i = 0; i < dataInputs.size(); i++) {
            inputSignalNames.add(getInputPortVariable(i));
        }
        context.put("inputSignalNames", inputSignalNames);

        // Add output signal names
        List<String> outputSignalNames = new ArrayList<>();
        for (int i = 0; i < actionOutputs.size(); i++) {
            outputSignalNames.add(getOutputPortVariable(i));
        }
        context.put("outputSignalNames", outputSignalNames);

        String outputCode = TemplateManager.renderTemplate("c/subsystem/If/output.vm", context);
        code.addOutputCode(outputCode);
    }

    // === Initialization and Execution Methods ===

    @Override
    public void calculateInit() {
        activeOutputIndex = -1;
    }

    @Override
    public void calculateOutput(double t) {
        // Get input values for expression evaluation
        double[] inputValues = new double[dataInputs.size()];
        for (int i = 0; i < dataInputs.size(); i++) {
            InputPort port = dataInputs.get(i);
            if (port.getLinkedLine() != null && port.getLinkedLine().getLinkedOutputPort() != null) {
                inputValues[i] = port.getData().getInitValue();
            } else {
                inputValues[i] = 0.0;
            }
        }

        // Evaluate conditions in order
        List<String> allConditions = getAllConditions();
        activeOutputIndex = -1;

        for (int i = 0; i < allConditions.size(); i++) {
            String condition = allConditions.get(i);
            if (evaluateCondition(condition, inputValues)) {
                activeOutputIndex = i;
                break;
            }
        }

        // If no condition was true and we have an else output
        if (activeOutputIndex == -1 && getShowElseValue()) {
            activeOutputIndex = allConditions.size(); // Else output is the last one
        }

        // Set action outputs (1.0 for active, 0.0 for inactive)
        for (int i = 0; i < actionOutputs.size(); i++) {
            OutputPort port = actionOutputs.get(i);
            Data outputData = new Data();
            outputData.setInitValue(i == activeOutputIndex ? 1.0 : 0.0);
            port.setData(outputData);
        }
    }

    @Override
    public void updateDimension() throws MatDimException {
        // If block outputs are scalar action signals (function-call type)
        for (OutputPort port : actionOutputs) {
            port.setHeight(1);
            port.setWidth(1);
            port.getOutputSignalC().setHeight(1);
            port.getOutputSignalC().setWidth(1);
            port.getOutputSignalC().setDataType(com.ncslab.block.data.DataType.REAL);
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // No additional dimension checks needed for If block
    }

    // === Port Initialization ===
    private void initializePorts() {
        dataInputs = new ArrayList<>();
        actionOutputs = new ArrayList<>();

        // Create data input ports
        int numInputs = getNumberOfInputsValue();
        for (int i = 0; i < numInputs; i++) {
            InputPort input = new InputPort(this, i + 1);
            inputPortList.add(input);
            dataInputs.add(input);
        }

        // Create action output ports
        int numOutputs = getNumberOfOutputs();
        for (int i = 0; i < numOutputs; i++) {
            OutputPort output = new OutputPort(this, i + 1, true); // Action outputs have feedthrough
            outputPortList.add(output);
            actionOutputs.add(output);
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createNumberOfInputsFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("NumberOfInputs", "1");
        return new Parameter(null, 1, "NumberOfInputs", value);
    }

    private static Parameter createIfExpressionFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("IfExpression", "u1 ~= 0");
        return new Parameter(null, 2, "IfExpression", value);
    }

    private static Parameter createElseIfExpressionsFromJSON(JSONObject paramValues) {
        // Handle both string and array formats
        String value = "[]";
        if (paramValues.has("ElseIfExpressions")) {
            Object obj = paramValues.get("ElseIfExpressions");
            if (obj instanceof JSONArray) {
                value = ((JSONArray) obj).toString();
            } else if (obj instanceof String) {
                value = (String) obj;
            }
        }
        return new Parameter(null, 3, "ElseIfExpressions", value);
    }

    private static Parameter createShowElseFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("ShowElse", "off");
        return new Parameter(null, 4, "ShowElse", value);
    }

    private static Parameter createOutDataTypeStrFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("OutDataTypeStr", "Action");
        return new Parameter(null, 5, "OutDataTypeStr", value);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 6, "SampleTime", value);
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

    private static void setParameterBlockReference(If block, Parameter... parameters) {
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
        identity.put("blockType", "If");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Accessor Methods ===

    /**
     * Gets the number of input ports.
     *
     * @return Number of input ports
     */
    public int getNumberOfInputsValue() {
        try {
            return (int) numberOfInputs.getData().getInitValue();
        } catch (Exception e) {
            return 1; // Default
        }
    }

    /**
     * Gets the if expression value.
     *
     * @return If condition expression
     */
    public String getIfExpressionValue() {
        return ifExpression.getData().getInitString();
    }

    /**
     * Gets the elseif expressions as a list.
     *
     * @return List of elseif condition expressions
     */
    public List<String> getElseIfExpressionsValue() {
        List<String> result = new ArrayList<>();
        try {
            String jsonStr = elseIfExpressions.getData().getInitString();
            if (jsonStr != null && !jsonStr.trim().isEmpty() && !jsonStr.equals("[]")) {
                JSONArray array = new JSONArray(jsonStr);
                for (int i = 0; i < array.length(); i++) {
                    result.add(array.getString(i));
                }
            }
        } catch (Exception e) {
            // Return empty list if parsing fails
        }
        return result;
    }

    /**
     * Gets the show else flag value.
     *
     * @return true if else branch should be shown
     */
    public boolean getShowElseValue() {
        String value = showElse.getData().getInitString();
        return "on".equals(value) || "true".equals(value);
    }

    /**
     * Gets all condition expressions in evaluation order.
     *
     * @return List of all conditions (if + elseifs)
     */
    public List<String> getAllConditions() {
        List<String> conditions = new ArrayList<>();
        conditions.add(getIfExpressionValue());
        conditions.addAll(getElseIfExpressionsValue());
        return conditions;
    }

    /**
     * Gets the total number of output ports.
     * Formula: 1 (if) + numElseIf + (showElse ? 1 : 0)
     *
     * @return Total number of action output ports
     */
    public int getNumberOfOutputs() {
        int count = 1; // If output
        count += getElseIfExpressionsValue().size(); // ElseIf outputs
        if (getShowElseValue()) {
            count += 1; // Else output
        }
        return count;
    }

    /**
     * Evaluates a condition expression with given input values.
     * Simple implementation for basic expressions.
     *
     * @param condition Condition expression string
     * @param inputs    Array of input values (u1=inputs[0], u2=inputs[1], etc.)
     * @return true if condition is satisfied
     */
    private boolean evaluateCondition(String condition, double[] inputs) {
        // Simple expression evaluator
        // Replace u1, u2, etc. with actual values and evaluate
        String expr = condition;

        // Replace input references with values
        for (int i = 0; i < inputs.length; i++) {
            String inputRef = "u" + (i + 1);
            expr = expr.replace(inputRef, String.valueOf(inputs[i]));
        }

        // Replace MATLAB operators with Java equivalents
        expr = expr.replace("~=", "!=");
        expr = expr.replace("&&", "&&");
        expr = expr.replace("||", "||");

        // Simple evaluation for common cases
        try {
            // For basic comparisons, use simple parsing
            if (expr.contains(">=")) {
                String[] parts = expr.split(">=");
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left >= right;
            } else if (expr.contains("<=")) {
                String[] parts = expr.split("<=");
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left <= right;
            } else if (expr.contains(">")) {
                String[] parts = expr.split(">");
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left > right;
            } else if (expr.contains("<")) {
                String[] parts = expr.split("<");
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left < right;
            } else if (expr.contains("==")) {
                String[] parts = expr.split("==");
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return Math.abs(left - right) < 1e-12;
            } else if (expr.contains("!=")) {
                String[] parts = expr.split("!=");
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return Math.abs(left - right) >= 1e-12;
            }
        } catch (Exception e) {
            // If parsing fails, return false (safe default)
            System.err.println("Error evaluating If condition: " + condition + " - " + e.getMessage());
        }

        return false; // Default to false if evaluation fails
    }

    /**
     * Converts MATLAB-style expression to C expression.
     * Replaces MATLAB operators with C equivalents.
     *
     * @param expression MATLAB expression string
     * @return C-compatible expression string
     */
    public String convertExpressionToC(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return "0"; // Default false
        }

        String cExpr = expression;

        // Replace MATLAB operators with C equivalents
        cExpr = cExpr.replace("~=", "!=");
        cExpr = cExpr.replace("~", "!");
        cExpr = cExpr.replace("&&", "&&");
        cExpr = cExpr.replace("||", "||");

        // No need to replace u1, u2, etc. - they're already defined as variables in template

        return cExpr;
    }
}
