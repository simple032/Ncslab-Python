package com.ncslab.dto.block.specialized.subsystem;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.block.BlockPositionDto;
import com.ncslab.dto.block.BlockDimensionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * DTO for If block - Conditional execution control with multiple branches.
 *
 * <p>The If block evaluates conditional expressions and activates corresponding
 * action subsystems based on which condition is true. It enables branching logic
 * in models through if-elseif-else structures.</p>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>NumberOfInputs</b>: Number of input ports (u1, u2, ...), default 1</li>
 *   <li><b>IfExpression</b>: Primary if condition expression (e.g., "u1 > 0")</li>
 *   <li><b>ElseIfExpressions</b>: Array of additional elseif conditions</li>
 *   <li><b>ShowElse</b>: Whether to have else branch output, default false</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 * </ul>
 *
 * <p><b>Conditional Logic:</b></p>
 * <ul>
 *   <li>Evaluates conditions in order (if, elseif1, elseif2, ..., else)</li>
 *   <li>First true condition triggers corresponding action output port</li>
 *   <li>Outputs are action signals (function-call type) connecting to IfActionSubsystems</li>
 *   <li>Each condition can reference multiple inputs (u1, u2, etc.)</li>
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
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>NumberOfInputs must be >= 1</li>
 *   <li>IfExpression cannot be empty</li>
 *   <li>Expression syntax must be valid</li>
 *   <li>Input references (u1, u2, etc.) must not exceed NumberOfInputs</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-22
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("If")
public class IfDto extends BlockDto {

    /**
     * Number of input ports (u1, u2, u3, ...).
     * Determines how many data inputs the If block accepts.
     */
    private TypedParameter numberOfInputs = TypedParameter.of(1);

    /**
     * Primary if condition expression.
     * Expression syntax: "u1 > 0", "u1 >= u2", etc.
     */
    private TypedParameter ifExpression = TypedParameter.of("u1 ~= 0");

    /**
     * Array of elseif condition expressions.
     * Each element is a condition string evaluated in order after the if condition.
     */
    private TypedParameter elseIfExpressions = TypedParameter.of(new ArrayList<String>());

    /**
     * Whether to show else branch output port.
     * If true, adds a final output port for the else case.
     */
    private TypedParameter showElse = TypedParameter.of(false);

    /**
     * Output data type for action signals.
     * Default: "Action" for function-call type.
     */
    private TypedParameter outDataTypeStr = TypedParameter.of("Action");

    /**
     * Constructs IfDto with basic parameters.
     */
    public IfDto(String blockName, String blockPath) {
        super(blockName, blockPath);
        this.numberOfInputs = TypedParameter.of(1);
        this.ifExpression = TypedParameter.of("u1 ~= 0");
        this.elseIfExpressions = TypedParameter.of(new ArrayList<String>());
        this.showElse = TypedParameter.of(false);
        this.sampleTime = TypedParameter.of(-1.0);
    }

    /**
     * Constructs IfDto with individual parameters.
     *
     * @param blockName          Name of the If block
     * @param blockPath          Path of the If block in the model hierarchy
     * @param numberOfInputs     Number of input ports
     * @param ifExpression       Primary if condition
     * @param elseIfExpressions  Array of elseif conditions
     * @param showElse           Whether to show else output
     * @param sampleTime         Sample time parameter
     */
    public IfDto(String blockName, String blockPath,
                 TypedParameter numberOfInputs,
                 TypedParameter ifExpression,
                 TypedParameter elseIfExpressions,
                 TypedParameter showElse,
                 TypedParameter sampleTime) {
        super(blockName, blockPath);
        this.numberOfInputs = numberOfInputs;
        this.ifExpression = ifExpression;
        this.elseIfExpressions = elseIfExpressions;
        this.showElse = showElse;
        this.sampleTime = sampleTime;
        this.outDataTypeStr = TypedParameter.of("Action");
    }

    /**
     * Constructs IfDto with typed parameter map.
     *
     * @param blockName  Name of the If block
     * @param blockPath  Path of the If block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public IfDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.numberOfInputs = parameters.getTypedParameter("NumberOfInputs", Integer.class, 1);
        this.ifExpression = parameters.getTypedParameter("IfExpression", String.class, "u1 ~= 0");
        this.elseIfExpressions = parameters.getTypedParameter("ElseIfExpressions", List.class, new ArrayList<String>());
        this.showElse = parameters.getTypedParameter("ShowElse", Boolean.class, false);
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
        this.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class, "Action");
    }

    /**
     * Creates IfDto with specified block metadata and default parameters.
     *
     * @param blockName  Block instance name
     * @param blockPath  Hierarchical path in model
     * @param position   Block position in diagram
     * @param dimension  Block visual dimensions
     */
    public IfDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
        this.numberOfInputs = TypedParameter.of(1);
        this.ifExpression = TypedParameter.of("u1 ~= 0");
        this.elseIfExpressions = TypedParameter.of(new ArrayList<String>());
        this.showElse = TypedParameter.of(false);
        this.sampleTime = TypedParameter.of(-1.0);
        this.outDataTypeStr = TypedParameter.of("Action");
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate number of inputs
        if (numberOfInputs == null || numberOfInputs.getAsInteger() == null) {
            addValidationError("NumberOfInputs cannot be null");
            return false;
        }

        int numInputs = getNumberOfInputsValue();
        if (numInputs < 1) {
            addValidationError("NumberOfInputs must be at least 1");
            return false;
        }

        // Validate if expression
        if (ifExpression == null || ifExpression.getAsString() == null ||
            ifExpression.getAsString().trim().isEmpty()) {
            addValidationError("IfExpression cannot be empty");
            return false;
        }

        // Validate sample time
        if (sampleTime == null || sampleTime.getAsDouble() == null) {
            addValidationError("Sample time cannot be null");
            return false;
        }

        double sampleTimeValue = getSampleTimeValue();
        if (sampleTimeValue < -1.0 || Double.isNaN(sampleTimeValue) || Double.isInfinite(sampleTimeValue)) {
            addValidationError("Sample time must be >= -1.0 and finite");
            return false;
        }

        return true;
    }

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();

        // Validate number of inputs
        if (numberOfInputs != null && numberOfInputs.getAsInteger() != null) {
            int numInputs = getNumberOfInputsValue();
            if (numInputs < 1) {
                errors.add("NumberOfInputs must be at least 1");
            }
            if (numInputs > 32) { // Reasonable limit
                errors.add("NumberOfInputs exceeds reasonable limit (32)");
            }
        }

        // Validate if expression
        if (ifExpression != null && ifExpression.getAsString() != null) {
            String expr = getIfExpressionValue();
            if (expr.trim().isEmpty()) {
                errors.add("IfExpression cannot be empty");
            }
            // Basic syntax validation could be added here
        }

        // Validate elseif expressions
        if (elseIfExpressions != null) {
            List<String> elseIfList = getElseIfExpressionsValue();
            if (elseIfList.size() > 16) { // Reasonable limit
                errors.add("Too many ElseIf conditions (max 16)");
            }
        }

        // Validate sample time
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            double stValue = getSampleTimeValue();
            if (stValue < -1.0 || Double.isNaN(stValue) || Double.isInfinite(stValue)) {
                errors.add("Sample time must be >= -1.0 and finite");
            }
        }

        return errors;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the number of input ports.
     *
     * @return Number of input ports
     */
    public int getNumberOfInputsValue() {
        if (numberOfInputs != null && numberOfInputs.getAsInteger() != null) {
            return numberOfInputs.getAsInteger();
        }
        return 1;
    }

    /**
     * Gets the if expression value.
     *
     * @return If condition expression
     */
    public String getIfExpressionValue() {
        if (ifExpression != null && ifExpression.getAsString() != null) {
            return ifExpression.getAsString();
        }
        return "u1 ~= 0";
    }

    /**
     * Gets the elseif expressions as a list.
     *
     * @return List of elseif condition expressions
     */
    @SuppressWarnings("unchecked")
    public List<String> getElseIfExpressionsValue() {
        if (elseIfExpressions != null) {
            Object value = elseIfExpressions.getValue();
            if (value instanceof List) {
                return (List<String>) value;
            }
        }
        return new ArrayList<>();
    }

    /**
     * Gets the show else flag value.
     *
     * @return true if else branch should be shown
     */
    public boolean getShowElseValue() {
        if (showElse != null && showElse.getAsBoolean() != null) {
            return showElse.getAsBoolean();
        }
        return false;
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time for If block
     */
    public double getSampleTimeValue() {
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            return sampleTime.getAsDouble();
        }
        return -1.0;
    }

    /**
     * Gets the output data type string.
     *
     * @return Output data type ("Action" for function-call)
     */
    public String getOutDataTypeStrValue() {
        if (outDataTypeStr != null && outDataTypeStr.getAsString() != null) {
            return outDataTypeStr.getAsString();
        }
        return "Action";
    }

    // === Helper Methods ===

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
     * Checks if the If block is configured for continuous time operation.
     *
     * @return true if sample time is 0 (continuous)
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if the If block inherits its sample time.
     *
     * @return true if sample time is -1 (inherited)
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Checks if the If block is configured for discrete time operation.
     *
     * @return true if sample time is positive (discrete)
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }

    // === Factory Methods ===

    @Override
    public IfDto copy() {
        IfDto copy = new IfDto();

        // Copy base fields
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());

        // Copy DTO-specific fields
        copy.numberOfInputs = numberOfInputs != null ? numberOfInputs.copy() : null;
        copy.ifExpression = ifExpression != null ? ifExpression.copy() : null;
        copy.elseIfExpressions = elseIfExpressions != null ? elseIfExpressions.copy() : null;
        copy.showElse = showElse != null ? showElse.copy() : null;
        copy.outDataTypeStr = outDataTypeStr != null ? outDataTypeStr.copy() : null;

        return copy;
    }

    /**
     * Creates a TypedParameterMap from this DTO's parameters.
     *
     * @return TypedParameterMap containing all block parameters
     */
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("NumberOfInputs", numberOfInputs)
                .put("IfExpression", ifExpression)
                .put("ElseIfExpressions", elseIfExpressions)
                .put("ShowElse", showElse)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "NumberOfInputs", "Number of input ports for condition evaluation",
            "IfExpression", "Primary if condition expression (e.g., 'u1 > 0')",
            "ElseIfExpressions", "Array of additional elseif condition expressions",
            "ShowElse", "Whether to show else branch output port",
            "SampleTime", "Sample time for If block (-1 for inherited, 0 for continuous)",
            "OutDataTypeStr", "Output data type (Action for function-call)"
        );
    }

    @Override
    public String toString() {
        return String.format("IfDto{blockName='%s', inputs=%d, outputs=%d, ifExpr='%s', elseIfs=%d, showElse=%s}",
                           getBlockName(), getNumberOfInputsValue(), getNumberOfOutputs(),
                           getIfExpressionValue(), getElseIfExpressionsValue().size(), getShowElseValue());
    }
}
