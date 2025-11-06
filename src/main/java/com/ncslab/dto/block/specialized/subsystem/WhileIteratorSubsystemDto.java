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
import java.util.Map;

/**
 * DTO for WhileIteratorSubsystem block - Executes while a condition is true.
 *
 * <p>This block represents a subsystem that iterates while a condition signal is true,
 * implementing while-loop semantics in the block diagram. The iteration continues
 * as long as the condition input is non-zero AND the iteration count is below MaxIterations.</p>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>MaxIterations</b>: Maximum iterations to prevent infinite loops (default: 10)</li>
 *   <li><b>ShowIterationNumber</b>: "on" or "off" - creates output port for iteration index</li>
 *   <li><b>ResetState</b>: "each time step" or "at start of simulation" - when to reset state</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 input port: condition signal (condition is evaluated from internal condition output)</li>
 *   <li>0 or 1 output port: iteration number (only if ShowIterationNumber="on")</li>
 *   <li>Regular In/Out blocks for data flow (inherited from Subsystem)</li>
 * </ul>
 *
 * <p><b>While Loop Semantics:</b></p>
 * <ul>
 *   <li>Evaluates condition from special internal condition block</li>
 *   <li>Executes subsystem contents while condition is true AND iterations < MaxIterations</li>
 *   <li>Iteration counter starts at 1</li>
 *   <li>Resets state based on ResetState parameter</li>
 * </ul>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>MaxIterations must be positive integer (typically 1-1000)</li>
 *   <li>ShowIterationNumber must be "on" or "off"</li>
 *   <li>ResetState must be "each time step" or "at start of simulation"</li>
 *   <li>Must contain a condition output block for loop evaluation</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-22
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("WhileIteratorSubsystem")
public class WhileIteratorSubsystemDto extends SubsystemDto {

    /**
     * Maximum iterations parameter.
     * Prevents infinite loops by limiting the number of iterations.
     */
    private TypedParameter maxIterations = TypedParameter.of(10);

    /**
     * Show iteration number parameter.
     * When "on", creates an output port providing the current iteration number.
     */
    private TypedParameter showIterationNumber = TypedParameter.of("off");

    /**
     * Reset state parameter.
     * Controls when the iteration state is reset:
     * - "each time step": Reset at every simulation step
     * - "at start of simulation": Reset only at simulation start
     */
    private TypedParameter resetState = TypedParameter.of("each time step");

    /**
     * Constructs WhileIteratorSubsystemDto with individual parameters.
     *
     * @param blockName     Name of the while iterator subsystem
     * @param blockPath     Path of the subsystem in the model hierarchy
     * @param maxIterations Maximum number of iterations
     * @param showIterationNumber "on" or "off"
     * @param resetState    "each time step" or "at start of simulation"
     * @param sampleTime    Sample time parameter
     */
    public WhileIteratorSubsystemDto(String blockName, String blockPath,
                                    TypedParameter maxIterations,
                                    TypedParameter showIterationNumber,
                                    TypedParameter resetState,
                                    TypedParameter sampleTime) {
        super();
        setBlockName(blockName);
        setBlockPath(blockPath);
        this.maxIterations = maxIterations;
        this.showIterationNumber = showIterationNumber;
        this.resetState = resetState;
        this.sampleTime = sampleTime;
    }

    /**
     * Constructs WhileIteratorSubsystemDto with typed parameter map.
     *
     * @param blockName  Name of the while iterator subsystem
     * @param blockPath  Path of the subsystem in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public WhileIteratorSubsystemDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath, parameters);
        this.maxIterations = parameters.getTypedParameter("MaxIterations", Integer.class, 10);
        this.showIterationNumber = parameters.getTypedParameter("ShowIterationNumber", String.class, "off");
        this.resetState = parameters.getTypedParameter("ResetState", String.class, "each time step");
    }

    /**
     * Creates WhileIteratorSubsystemDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     */
    public WhileIteratorSubsystemDto(String blockName, String blockPath,
                                     BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
        this.maxIterations = TypedParameter.of(10);
        this.showIterationNumber = TypedParameter.of("off");
        this.resetState = TypedParameter.of("each time step");
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate max iterations
        if (maxIterations == null || maxIterations.getAsInteger() == null) {
            addValidationError("MaxIterations cannot be null");
            return false;
        }

        int maxIterValue = getMaxIterationsValue();
        if (maxIterValue < 1 || maxIterValue > 10000) {
            addValidationError("MaxIterations must be between 1 and 10000");
            return false;
        }

        // Validate show iteration number
        if (showIterationNumber != null && showIterationNumber.getAsString() != null) {
            String showIterValue = getShowIterationNumberValue();
            if (!showIterValue.equals("on") && !showIterValue.equals("off")) {
                addValidationError("ShowIterationNumber must be 'on' or 'off'");
                return false;
            }
        }

        // Validate reset state
        if (resetState != null && resetState.getAsString() != null) {
            String resetValue = getResetStateValue();
            if (!resetValue.equals("each time step") && !resetValue.equals("at start of simulation")) {
                addValidationError("ResetState must be 'each time step' or 'at start of simulation'");
                return false;
            }
        }

        return true;
    }

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();

        // Validate max iterations
        if (maxIterations != null && maxIterations.getAsInteger() != null) {
            int maxIterValue = getMaxIterationsValue();
            if (maxIterValue < 1) {
                errors.add("MaxIterations must be at least 1");
            }
            if (maxIterValue > 10000) {
                errors.add("MaxIterations exceeds reasonable limit (10000)");
            }
        }

        // Validate show iteration number
        if (showIterationNumber != null && showIterationNumber.getAsString() != null) {
            String showIterValue = getShowIterationNumberValue();
            if (!showIterValue.equals("on") && !showIterValue.equals("off")) {
                errors.add("ShowIterationNumber must be 'on' or 'off'");
            }
        }

        // Validate reset state
        if (resetState != null && resetState.getAsString() != null) {
            String resetValue = getResetStateValue();
            if (!resetValue.equals("each time step") && !resetValue.equals("at start of simulation")) {
                errors.add("ResetState must be 'each time step' or 'at start of simulation'");
            }
        }

        return errors;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the maximum iterations value.
     *
     * @return Maximum number of iterations
     */
    public int getMaxIterationsValue() {
        if (maxIterations != null && maxIterations.getAsInteger() != null) {
            return maxIterations.getAsInteger();
        }
        return 10; // Default
    }

    /**
     * Gets the show iteration number setting.
     *
     * @return "on" or "off"
     */
    public String getShowIterationNumberValue() {
        if (showIterationNumber != null && showIterationNumber.getAsString() != null) {
            return showIterationNumber.getAsString();
        }
        return "off"; // Default
    }

    /**
     * Gets the reset state setting.
     *
     * @return "each time step" or "at start of simulation"
     */
    public String getResetStateValue() {
        if (resetState != null && resetState.getAsString() != null) {
            return resetState.getAsString();
        }
        return "each time step"; // Default
    }

    // === Helper Methods ===

    /**
     * Checks if iteration number output port should be shown.
     *
     * @return true if ShowIterationNumber is "on"
     */
    public boolean shouldShowIterationNumber() {
        return "on".equalsIgnoreCase(getShowIterationNumberValue());
    }

    /**
     * Checks if state should be reset each time step.
     *
     * @return true if ResetState is "each time step"
     */
    public boolean shouldResetEachTimeStep() {
        return "each time step".equalsIgnoreCase(getResetStateValue());
    }

    /**
     * Checks if state should be reset only at simulation start.
     *
     * @return true if ResetState is "at start of simulation"
     */
    public boolean shouldResetAtSimulationStart() {
        return "at start of simulation".equalsIgnoreCase(getResetStateValue());
    }

    /**
     * Updates the maximum iterations value.
     *
     * @param maxIter New maximum iterations value (must be positive)
     */
    public void setMaxIterationsValue(int maxIter) {
        if (maxIter < 1) {
            throw new IllegalArgumentException("MaxIterations must be at least 1");
        }
        this.maxIterations = TypedParameter.of(maxIter);
    }

    /**
     * Updates the show iteration number setting.
     *
     * @param show "on" or "off"
     */
    public void setShowIterationNumberValue(String show) {
        if (!show.equals("on") && !show.equals("off")) {
            throw new IllegalArgumentException("ShowIterationNumber must be 'on' or 'off'");
        }
        this.showIterationNumber = TypedParameter.of(show);
    }

    /**
     * Updates the reset state setting.
     *
     * @param reset "each time step" or "at start of simulation"
     */
    public void setResetStateValue(String reset) {
        if (!reset.equals("each time step") && !reset.equals("at start of simulation")) {
            throw new IllegalArgumentException("ResetState must be 'each time step' or 'at start of simulation'");
        }
        this.resetState = TypedParameter.of(reset);
    }

    // === Factory Methods ===

    @Override
    public WhileIteratorSubsystemDto copy() {
        WhileIteratorSubsystemDto copy = new WhileIteratorSubsystemDto();

        // Copy base fields
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());

        // Copy subsystem fields
        copy.setSubsystemDescription(getSubsystemDescription());
        copy.setShowPortLabels(getShowPortLabels());
        copy.setReadOnly(getReadOnly());
        copy.setMaskType(getMaskType());
        copy.setNumInputPorts(getNumInputPorts());
        copy.setNumOutputPorts(getNumOutputPorts());

        // Copy DTO-specific fields
        copy.maxIterations = maxIterations != null ? maxIterations.copy() : null;
        copy.showIterationNumber = showIterationNumber != null ? showIterationNumber.copy() : null;
        copy.resetState = resetState != null ? resetState.copy() : null;

        return copy;
    }

    /**
     * Creates a TypedParameterMap from this DTO's parameters.
     *
     * @return TypedParameterMap containing all block parameters
     */
    @Override
    public TypedParameterMap toParameterMap() {
        TypedParameterMap baseParams = super.toParameterMap();
        TypedParameterMap.TypedParameterMapBuilder builder = TypedParameterMap.builder();

        // Add all base parameters
        for (Map.Entry<String, TypedParameter> entry : baseParams.entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }

        // Add WhileIteratorSubsystem-specific parameters
        builder.put("MaxIterations", maxIterations);
        builder.put("ShowIterationNumber", showIterationNumber);
        builder.put("ResetState", resetState);

        return builder.build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "MaxIterations", "Maximum iterations to prevent infinite loops (default: 10)",
            "ShowIterationNumber", "Display iteration number on output port ('on' or 'off')",
            "ResetState", "When to reset iteration state ('each time step' or 'at start of simulation')"
        );
    }

    @Override
    public String toString() {
        return String.format("WhileIteratorSubsystemDto{blockName='%s', maxIterations=%d, showIterationNumber='%s', resetState='%s', sampleTime=%.3f}",
                           getBlockName(), getMaxIterationsValue(), getShowIterationNumberValue(),
                           getResetStateValue(), getSampleTimeValue());
    }
}
