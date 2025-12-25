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
 * DTO for For Each Subsystem block - Applies subsystem to each element/partition of input vectors.
 *
 * <p>This block partitions input signals and applies the subsystem contents to each partition
 * independently. The subsystem executes once per partition element, similar to a for-each loop
 * over array elements.</p>
 *
 * <p><b>SIMULINK-Compatible Parameters:</b></p>
 * <ul>
 *   <li><b>PartitionDimension</b>: Which dimension to partition (1 or 2, default: 1)</li>
 *   <li><b>PartitionWidth</b>: Width of each partition slice (default: 1)</li>
 *   <li><b>ResetStates</b>: "held" or "reset" - state behavior across iterations (default: "held")</li>
 *   <li><b>ShowIterationIndex</b>: true/false - creates output port for iteration index (default: false)</li>
 * </ul>
 *
 * <p><b>Partitioning Behavior:</b></p>
 * <ul>
 *   <li><b>Dimension 1 Partition</b>: Partitions along rows (vertical slicing)</li>
 *   <li><b>Dimension 2 Partition</b>: Partitions along columns (horizontal slicing)</li>
 *   <li><b>Partition Width</b>: Number of elements in each partition</li>
 *   <li><b>Concatenation</b>: Outputs are concatenated along the partition dimension</li>
 * </ul>
 *
 * <p><b>Example (1-D vector [a b c d], PartitionWidth=1, PartitionDimension=1):</b></p>
 * <pre>
 * Iteration 1: Subsystem processes [a], outputs [y1]
 * Iteration 2: Subsystem processes [b], outputs [y2]
 * Iteration 3: Subsystem processes [c], outputs [y3]
 * Iteration 4: Subsystem processes [d], outputs [y4]
 * Final output: [y1 y2 y3 y4]
 * </pre>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>Input ports: Defined by In blocks within subsystem (receive partitioned signals)</li>
 *   <li>Output ports: Defined by Out blocks within subsystem (concatenated outputs)</li>
 *   <li>Optional iteration index output if ShowIterationIndex=true</li>
 * </ul>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>PartitionDimension must be 1 or 2</li>
 *   <li>PartitionWidth must be positive integer (>= 1)</li>
 *   <li>ResetStates must be "held" or "reset"</li>
 *   <li>ShowIterationIndex must be boolean</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ForEachSubsystem")
public class ForEachSubsystemDto extends SubsystemDto {

    /**
     * Which dimension to partition along (1 for rows/vertical, 2 for columns/horizontal).
     * Default: 1
     */
    private TypedParameter partitionDimension = TypedParameter.of(1);

    /**
     * Width of each partition slice in elements.
     * Default: 1
     */
    private TypedParameter partitionWidth = TypedParameter.of(1);

    /**
     * State behavior across iterations.
     * "held" - States persist across iterations (default)
     * "reset" - States reset before each partition iteration
     */
    private TypedParameter resetStates = TypedParameter.of("held");

    /**
     * Controls whether an output port is created for the iteration index.
     * true - Creates output port that provides current iteration number
     * false - No iteration index output port (default)
     */
    private TypedParameter showIterationIndex = TypedParameter.of(false);

    /**
     * Constructs ForEachSubsystemDto with individual parameters.
     *
     * @param blockName            Name of the for-each subsystem block
     * @param blockPath            Path of the block in the model hierarchy
     * @param partitionDimension   Dimension to partition (1 or 2)
     * @param partitionWidth       Width of each partition slice
     * @param resetStates          State reset behavior ("held" or "reset")
     * @param showIterationIndex   Whether to output iteration index
     */
    public ForEachSubsystemDto(String blockName, String blockPath,
                               TypedParameter partitionDimension,
                               TypedParameter partitionWidth,
                               TypedParameter resetStates,
                               TypedParameter showIterationIndex) {
        super(blockName, blockPath);
        this.partitionDimension = partitionDimension;
        this.partitionWidth = partitionWidth;
        this.resetStates = resetStates;
        this.showIterationIndex = showIterationIndex;
    }

    /**
     * Constructs ForEachSubsystemDto with typed parameter map.
     *
     * @param blockName  Name of the for-each subsystem block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public ForEachSubsystemDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.partitionDimension = parameters.getTypedParameter("PartitionDimension", Integer.class, 1);
        this.partitionWidth = parameters.getTypedParameter("PartitionWidth", Integer.class, 1);
        this.resetStates = parameters.getTypedParameter("ResetStates", String.class, "held");
        this.showIterationIndex = parameters.getTypedParameter("ShowIterationIndex", Boolean.class, false);
    }

    /**
     * Creates ForEachSubsystemDto with specified block metadata.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position  Block position in diagram
     * @param dimension Block visual dimensions
     */
    public ForEachSubsystemDto(String blockName, String blockPath,
                               BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
        this.partitionDimension = TypedParameter.of(1);
        this.partitionWidth = TypedParameter.of(1);
        this.resetStates = TypedParameter.of("held");
        this.showIterationIndex = TypedParameter.of(false);
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate PartitionDimension
        int dim = getPartitionDimensionValue();
        if (dim != 1 && dim != 2) {
            addValidationError("PartitionDimension must be 1 or 2, got: " + dim);
            return false;
        }

        // Validate PartitionWidth
        int width = getPartitionWidthValue();
        if (width < 1) {
            addValidationError("PartitionWidth must be a positive integer (>= 1), got: " + width);
            return false;
        }

        // Validate ResetStates
        String reset = getResetStatesValue();
        if (!isValidResetStatesMode(reset)) {
            addValidationError("ResetStates must be 'held' or 'reset', got: " + reset);
            return false;
        }

        return true;
    }

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();

        // Validate PartitionDimension
        int dim = getPartitionDimensionValue();
        if (dim != 1 && dim != 2) {
            errors.add("PartitionDimension must be 1 or 2, got: " + dim);
        }

        // Validate PartitionWidth
        int width = getPartitionWidthValue();
        if (width < 1) {
            errors.add("PartitionWidth must be a positive integer (>= 1), got: " + width);
        }
        if (width > 1000) { // Reasonable upper limit
            errors.add("PartitionWidth exceeds reasonable limit (1000), got: " + width);
        }

        // Validate ResetStates
        String reset = getResetStatesValue();
        if (!isValidResetStatesMode(reset)) {
            errors.add("ResetStates must be 'held' or 'reset', got: " + reset);
        }

        return errors;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the partition dimension value.
     *
     * @return Partition dimension (1 or 2)
     */
    public int getPartitionDimensionValue() {
        if (partitionDimension != null && partitionDimension.getAsInteger() != null) {
            return partitionDimension.getAsInteger();
        }
        return 1; // Default
    }

    /**
     * Gets the partition width value.
     *
     * @return Width of each partition slice
     */
    public int getPartitionWidthValue() {
        if (partitionWidth != null && partitionWidth.getAsInteger() != null) {
            return partitionWidth.getAsInteger();
        }
        return 1; // Default
    }

    /**
     * Gets the reset states value.
     *
     * @return Reset states mode ("held" or "reset")
     */
    public String getResetStatesValue() {
        if (resetStates != null && resetStates.getAsString() != null) {
            return resetStates.getAsString();
        }
        return "held"; // Default
    }

    /**
     * Gets the show iteration index value.
     *
     * @return true if iteration index should be shown
     */
    public boolean getShowIterationIndexValue() {
        if (showIterationIndex != null && showIterationIndex.getAsBoolean() != null) {
            return showIterationIndex.getAsBoolean();
        }
        return false; // Default
    }

    // === Helper Methods ===

    /**
     * Checks if states are held across iterations.
     *
     * @return true if states persist across iterations
     */
    public boolean statesAreHeld() {
        return "held".equalsIgnoreCase(getResetStatesValue());
    }

    /**
     * Checks if states reset before each iteration.
     *
     * @return true if states reset before each partition iteration
     */
    public boolean statesAreReset() {
        return "reset".equalsIgnoreCase(getResetStatesValue());
    }

    /**
     * Checks if partition dimension is 1 (rows/vertical).
     *
     * @return true if partitioning along dimension 1
     */
    public boolean isPartitionDimension1() {
        return getPartitionDimensionValue() == 1;
    }

    /**
     * Checks if partition dimension is 2 (columns/horizontal).
     *
     * @return true if partitioning along dimension 2
     */
    public boolean isPartitionDimension2() {
        return getPartitionDimensionValue() == 2;
    }

    // === Validation Helpers ===

    /**
     * Validates reset states mode parameter.
     *
     * @param mode Reset states mode string
     * @return true if mode is valid
     */
    private boolean isValidResetStatesMode(String mode) {
        return mode != null &&
               (mode.equalsIgnoreCase("held") || mode.equalsIgnoreCase("reset"));
    }

    // === Factory Methods ===

    @Override
    public ForEachSubsystemDto copy() {
        ForEachSubsystemDto copy = new ForEachSubsystemDto();

        // Copy base fields
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());

        // Copy DTO-specific fields
        copy.partitionDimension = partitionDimension != null ? partitionDimension.copy() : null;
        copy.partitionWidth = partitionWidth != null ? partitionWidth.copy() : null;
        copy.resetStates = resetStates != null ? resetStates.copy() : null;
        copy.showIterationIndex = showIterationIndex != null ? showIterationIndex.copy() : null;

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
                .put("PartitionDimension", partitionDimension)
                .put("PartitionWidth", partitionWidth)
                .put("ResetStates", resetStates)
                .put("ShowIterationIndex", showIterationIndex)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "PartitionDimension", "Which dimension to partition along (1 for rows, 2 for columns)",
            "PartitionWidth", "Width of each partition slice in elements (positive integer >= 1)",
            "ResetStates", "State behavior across iterations ('held' or 'reset')",
            "ShowIterationIndex", "Enable iteration index output port (true or false)"
        );
    }

    @Override
    public String toString() {
        return String.format(
            "ForEachSubsystemDto{blockName='%s', partitionDimension=%d, partitionWidth=%d, resetStates='%s', showIterationIndex=%s}",
            getBlockName(), getPartitionDimensionValue(), getPartitionWidthValue(),
            getResetStatesValue(), getShowIterationIndexValue()
        );
    }
}
