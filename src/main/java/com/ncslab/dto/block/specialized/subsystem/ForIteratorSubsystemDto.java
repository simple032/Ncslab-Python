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
 * DTO for For Iterator Subsystem block - Fixed iteration count subsystem execution.
 *
 * <p>This block executes its containing subsystem a fixed number of times per
 * simulation time step. The subsystem executes in a for-loop pattern where the
 * iteration index can optionally be provided to internal blocks.</p>
 *
 * <p><b>SIMULINK-Compatible Parameters:</b></p>
 * <ul>
 *   <li><b>IterationLimit</b>: Number of iterations per time step (default: 10)</li>
 *   <li><b>ShowIterationNumber</b>: "on" or "off" (default) - creates output port for iteration index</li>
 *   <li><b>ResetState</b>: "each time step" (default) or "at start of simulation" - state reset behavior</li>
 *   <li><b>IndexMode</b>: "Zero-based" or "One-based" (default) - iteration index starting value</li>
 * </ul>
 *
 * <p><b>Iteration Behavior:</b></p>
 * <ul>
 *   <li><b>Fixed Iterations</b>: Executes exactly IterationLimit times per time step</li>
 *   <li><b>Iteration Index</b>: Available to internal blocks if ShowIterationNumber="on"</li>
 *   <li><b>State Reset</b>: Controls when subsystem states are reset to initial conditions</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>0 or 1 output port (iteration index if ShowIterationNumber="on")</li>
 *   <li>Regular In/Out blocks for subsystem data flow (inherited from Subsystem)</li>
 * </ul>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>IterationLimit must be positive integer (>= 1)</li>
 *   <li>ShowIterationNumber must be "on" or "off"</li>
 *   <li>ResetState must be "each time step" or "at start of simulation"</li>
 *   <li>IndexMode must be "Zero-based" or "One-based"</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ForIteratorSubsystem")
public class ForIteratorSubsystemDto extends SubsystemDto {

    /**
     * Number of iterations to execute per time step.
     * Must be a positive integer (>= 1).
     * Default: 10
     */
    private TypedParameter iterationLimit = TypedParameter.of(10);

    /**
     * Controls whether an output port is created for the iteration index.
     * "on" - Creates output port that provides current iteration number
     * "off" - No iteration index output port (default)
     */
    private TypedParameter showIterationNumber = TypedParameter.of("off");

    /**
     * Controls when subsystem states are reset.
     * "each time step" - States reset at the start of each time step (default)
     * "at start of simulation" - States reset only at simulation initialization
     */
    private TypedParameter resetState = TypedParameter.of("each time step");

    /**
     * Controls the starting value for iteration index.
     * "One-based" - Iteration index starts at 1 (default, MATLAB-style)
     * "Zero-based" - Iteration index starts at 0 (C-style)
     */
    private TypedParameter indexMode = TypedParameter.of("One-based");

    /**
     * Constructs ForIteratorSubsystemDto with individual parameters.
     *
     * @param blockName           Name of the for iterator subsystem block
     * @param blockPath           Path of the block in the model hierarchy
     * @param iterationLimit      Number of iterations per time step
     * @param showIterationNumber Output port for iteration index ("on" or "off")
     * @param resetState          State reset behavior
     * @param indexMode           Iteration index mode ("Zero-based" or "One-based")
     */
    public ForIteratorSubsystemDto(String blockName, String blockPath,
                                   TypedParameter iterationLimit,
                                   TypedParameter showIterationNumber,
                                   TypedParameter resetState,
                                   TypedParameter indexMode) {
        super(blockName, blockPath);
        this.iterationLimit = iterationLimit;
        this.showIterationNumber = showIterationNumber;
        this.resetState = resetState;
        this.indexMode = indexMode;
    }

    /**
     * Constructs ForIteratorSubsystemDto with typed parameter map.
     *
     * @param blockName  Name of the for iterator subsystem block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public ForIteratorSubsystemDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.iterationLimit = parameters.getTypedParameter("IterationLimit", Integer.class, 10);
        this.showIterationNumber = parameters.getTypedParameter("ShowIterationNumber", String.class, "off");
        this.resetState = parameters.getTypedParameter("ResetState", String.class, "each time step");
        this.indexMode = parameters.getTypedParameter("IndexMode", String.class, "One-based");
    }

    /**
     * Creates ForIteratorSubsystemDto with specified block metadata.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position  Block position in diagram
     * @param dimension Block visual dimensions
     */
    public ForIteratorSubsystemDto(String blockName, String blockPath,
                                   BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
        this.iterationLimit = TypedParameter.of(10);
        this.showIterationNumber = TypedParameter.of("off");
        this.resetState = TypedParameter.of("each time step");
        this.indexMode = TypedParameter.of("One-based");
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate IterationLimit
        int limit = getIterationLimitValue();
        if (limit < 1) {
            addValidationError("IterationLimit must be a positive integer (>= 1), got: " + limit);
            return false;
        }

        // Validate ShowIterationNumber
        String showIter = getShowIterationNumberValue();
        if (!isValidOnOffParameter(showIter)) {
            addValidationError("ShowIterationNumber must be 'on' or 'off', got: " + showIter);
            return false;
        }

        // Validate ResetState
        String reset = getResetStateValue();
        if (!isValidResetStateMode(reset)) {
            addValidationError("ResetState must be 'each time step' or 'at start of simulation', got: " + reset);
            return false;
        }

        // Validate IndexMode
        String indexModeValue = getIndexModeValue();
        if (!isValidIndexMode(indexModeValue)) {
            addValidationError("IndexMode must be 'Zero-based' or 'One-based', got: " + indexModeValue);
            return false;
        }

        return true;
    }

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();

        // Validate IterationLimit
        int limit = getIterationLimitValue();
        if (limit < 1) {
            errors.add("IterationLimit must be a positive integer (>= 1), got: " + limit);
        }

        // Validate ShowIterationNumber
        String showIter = getShowIterationNumberValue();
        if (!isValidOnOffParameter(showIter)) {
            errors.add("ShowIterationNumber must be 'on' or 'off', got: " + showIter);
        }

        // Validate ResetState
        String reset = getResetStateValue();
        if (!isValidResetStateMode(reset)) {
            errors.add("ResetState must be 'each time step' or 'at start of simulation', got: " + reset);
        }

        // Validate IndexMode
        String indexModeValue = getIndexModeValue();
        if (!isValidIndexMode(indexModeValue)) {
            errors.add("IndexMode must be 'Zero-based' or 'One-based', got: " + indexModeValue);
        }

        return errors;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the iteration limit value.
     *
     * @return Number of iterations per time step
     */
    public int getIterationLimitValue() {
        if (iterationLimit != null && iterationLimit.getAsInteger() != null) {
            return iterationLimit.getAsInteger();
        }
        return 10; // Default
    }

    /**
     * Gets the show iteration number value.
     *
     * @return Show iteration number setting ("on" or "off")
     */
    public String getShowIterationNumberValue() {
        if (showIterationNumber != null && showIterationNumber.getAsString() != null) {
            return showIterationNumber.getAsString();
        }
        return "off"; // Default
    }

    /**
     * Gets the reset state value.
     *
     * @return Reset state mode ("each time step" or "at start of simulation")
     */
    public String getResetStateValue() {
        if (resetState != null && resetState.getAsString() != null) {
            return resetState.getAsString();
        }
        return "each time step"; // Default
    }

    /**
     * Gets the index mode value.
     *
     * @return Index mode ("Zero-based" or "One-based")
     */
    public String getIndexModeValue() {
        if (indexMode != null && indexMode.getAsString() != null) {
            return indexMode.getAsString();
        }
        return "One-based"; // Default
    }

    // === Helper Methods ===

    /**
     * Checks if iteration number output port is shown.
     *
     * @return true if iteration index is provided as output
     */
    public boolean hasIterationOutput() {
        return "on".equalsIgnoreCase(getShowIterationNumberValue());
    }

    /**
     * Checks if states reset at each time step.
     *
     * @return true if states reset at the start of each time step
     */
    public boolean resetsEachTimeStep() {
        return "each time step".equalsIgnoreCase(getResetStateValue());
    }

    /**
     * Checks if states reset only at simulation start.
     *
     * @return true if states reset only at simulation initialization
     */
    public boolean resetsAtSimulationStart() {
        return "at start of simulation".equalsIgnoreCase(getResetStateValue());
    }

    /**
     * Checks if iteration index is zero-based.
     *
     * @return true if iteration index starts at 0
     */
    public boolean isZeroBasedIndex() {
        return "Zero-based".equalsIgnoreCase(getIndexModeValue());
    }

    /**
     * Checks if iteration index is one-based.
     *
     * @return true if iteration index starts at 1
     */
    public boolean isOneBasedIndex() {
        return "One-based".equalsIgnoreCase(getIndexModeValue());
    }

    /**
     * Gets the starting index value based on index mode.
     *
     * @return Starting index (0 for zero-based, 1 for one-based)
     */
    public int getStartIndex() {
        return isZeroBasedIndex() ? 0 : 1;
    }

    /**
     * Gets the ending index value based on index mode and iteration limit.
     *
     * @return Ending index (exclusive for zero-based, inclusive for one-based)
     */
    public int getEndIndex() {
        return isZeroBasedIndex() ? getIterationLimitValue() : getIterationLimitValue();
    }

    // === Validation Helpers ===

    /**
     * Validates on/off parameter.
     *
     * @param value Parameter value string
     * @return true if value is valid ("on" or "off")
     */
    private boolean isValidOnOffParameter(String value) {
        return value != null && (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("off"));
    }

    /**
     * Validates reset state mode parameter.
     *
     * @param mode Reset state mode string
     * @return true if mode is valid
     */
    private boolean isValidResetStateMode(String mode) {
        return mode != null &&
               (mode.equalsIgnoreCase("each time step") ||
                mode.equalsIgnoreCase("at start of simulation"));
    }

    /**
     * Validates index mode parameter.
     *
     * @param mode Index mode string
     * @return true if mode is valid
     */
    private boolean isValidIndexMode(String mode) {
        return mode != null &&
               (mode.equalsIgnoreCase("Zero-based") || mode.equalsIgnoreCase("One-based"));
    }

    // === Factory Methods ===

    @Override
    public ForIteratorSubsystemDto copy() {
        ForIteratorSubsystemDto copy = new ForIteratorSubsystemDto();

        // Copy base fields
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());

        // Copy DTO-specific fields
        copy.iterationLimit = iterationLimit != null ? iterationLimit.copy() : null;
        copy.showIterationNumber = showIterationNumber != null ? showIterationNumber.copy() : null;
        copy.resetState = resetState != null ? resetState.copy() : null;
        copy.indexMode = indexMode != null ? indexMode.copy() : null;

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
                .put("IterationLimit", iterationLimit)
                .put("ShowIterationNumber", showIterationNumber)
                .put("ResetState", resetState)
                .put("IndexMode", indexMode)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "IterationLimit", "Number of iterations per time step (positive integer >= 1)",
            "ShowIterationNumber", "Enable iteration index output port ('on' or 'off')",
            "ResetState", "State reset behavior ('each time step' or 'at start of simulation')",
            "IndexMode", "Iteration index starting value ('Zero-based' or 'One-based')"
        );
    }

    @Override
    public String toString() {
        return String.format(
            "ForIteratorSubsystemDto{blockName='%s', iterationLimit=%d, showIterationNumber='%s', resetState='%s', indexMode='%s'}",
            getBlockName(), getIterationLimitValue(), getShowIterationNumberValue(),
            getResetStateValue(), getIndexModeValue()
        );
    }
}
