package com.ncslab.dto.block.specialized.route;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * DTO representation of Selector route block.
 *
 * The Selector block extracts specified elements from input signal (vector or matrix).
 * It supports multiple selection modes for flexible signal element extraction.
 *
 * Selection Modes:
 * - Index Vector: Select specific elements by index [1, 3, 5] or MATLAB syntax "1:2:10"
 * - Starting Index: Select range from starting index
 * - Index Option: All, Rows, Columns, or custom
 *
 * Port Configuration:
 * - Input: Vector or matrix signal
 * - Output: Selected elements as vector or scalar
 *
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Selector")
@MigrationCompatible(originalClass = "com.ncslab.block.route.Selector")
public class SelectorDto extends BlockDto {

    /**
     * Index mode selection
     * Default: "Index vector"
     * Options: "Index vector", "Starting index", "Index option"
     */
    @Builder.Default
    private TypedParameter indexMode = TypedParameter.of("Index vector");

    /**
     * Index vector specification
     * Can be array notation [1, 3, 5] or MATLAB syntax "1:2:10"
     * Default: "[1]"
     * Note: Uses MATLAB-style 1-based indexing
     */
    @Builder.Default
    private TypedParameter indexVector = TypedParameter.of("[1]");

    /**
     * Index options (for Index option mode)
     * Default: "All"
     * Options: "All", "Rows", "Columns"
     */
    @Builder.Default
    private TypedParameter indexOptions = TypedParameter.of("All");

    /**
     * Number of dimensions
     * Default: 1 (vector)
     * Options: 1 (vector), 2 (matrix)
     */
    @Builder.Default
    private TypedParameter numberOfDimensions = TypedParameter.of(1);

    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");

    // Valid index modes
    private static final List<String> VALID_INDEX_MODES = Arrays.asList(
        "Index vector", "Starting index", "Index option"
    );

    // Valid index options
    private static final List<String> VALID_INDEX_OPTIONS = Arrays.asList(
        "All", "Rows", "Columns"
    );

    // ===== PARAMETER ACCESS HELPERS =====

    public String getIndexModeValue() {
        return indexMode != null ? indexMode.getAsString() : "Index vector";
    }

    public String getIndexVectorValue() {
        return indexVector != null ? indexVector.getAsString() : "[1]";
    }

    public String getIndexOptionsValue() {
        return indexOptions != null ? indexOptions.getAsString() : "All";
    }

    public Integer getNumberOfDimensionsValue() {
        return numberOfDimensions != null ? numberOfDimensions.getAsInteger() : 1;
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate index mode
        if (indexMode != null) {
            String mode = indexMode.getAsString();
            if (mode != null && !VALID_INDEX_MODES.contains(mode)) {
                result.addError("Index mode must be one of: " + VALID_INDEX_MODES);
            }
        }

        // Validate index vector format
        if (indexVector != null) {
            String vector = indexVector.getAsString();
            if (vector != null) {
                if (!isValidIndexVector(vector)) {
                    result.addError("Invalid index vector format. Use [1, 3, 5] or MATLAB syntax like 1:2:10");
                }
            }
        }

        // Validate index options
        if (indexOptions != null) {
            String options = indexOptions.getAsString();
            if (options != null && !VALID_INDEX_OPTIONS.contains(options)) {
                result.addError("Index options must be one of: " + VALID_INDEX_OPTIONS);
            }
        }

        // Validate number of dimensions
        if (numberOfDimensions != null) {
            Integer dims = numberOfDimensions.getAsInteger();
            if (dims != null && (dims < 1 || dims > 2)) {
                result.addError("Number of dimensions must be 1 (vector) or 2 (matrix)");
            }
        }

        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }

        return result;
    }

    /**
     * Validate index vector format
     * Supports: [1, 3, 5], [1,3,5], 1:2:10, 1:10, etc.
     */
    private boolean isValidIndexVector(String vector) {
        if (vector == null || vector.trim().isEmpty()) {
            return false;
        }

        // Remove whitespace for easier parsing
        String trimmed = vector.trim();

        // Array notation: [1, 3, 5] or [1,3,5]
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String content = trimmed.substring(1, trimmed.length() - 1).trim();
            if (content.isEmpty()) {
                return false;
            }
            // Check comma-separated integers
            String[] parts = content.split(",");
            for (String part : parts) {
                try {
                    Integer.parseInt(part.trim());
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }

        // MATLAB range notation: 1:2:10 or 1:10
        if (trimmed.contains(":")) {
            String[] parts = trimmed.split(":");
            if (parts.length < 2 || parts.length > 3) {
                return false;
            }
            // All parts must be integers
            for (String part : parts) {
                try {
                    Integer.parseInt(part.trim());
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }

        // Single integer
        try {
            Integer.parseInt(trimmed);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if this block operates in continuous time
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Check if this block inherits its sample time
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Check if index mode is "Index vector"
     */
    public boolean isIndexVectorMode() {
        return "Index vector".equals(getIndexModeValue());
    }

    /**
     * Check if index mode is "Starting index"
     */
    public boolean isStartingIndexMode() {
        return "Starting index".equals(getIndexModeValue());
    }

    /**
     * Check if index mode is "Index option"
     */
    public boolean isIndexOptionMode() {
        return "Index option".equals(getIndexModeValue());
    }

    /**
     * Parse index vector string to integer array
     * Handles both [1, 3, 5] and MATLAB syntax 1:2:10
     * @return Array of indices (1-based as per MATLAB convention)
     */
    public int[] parseIndexVector() {
        String vector = getIndexVectorValue();
        if (vector == null || vector.trim().isEmpty()) {
            return new int[]{1}; // Default to first element
        }

        String trimmed = vector.trim();

        // Array notation: [1, 3, 5]
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String content = trimmed.substring(1, trimmed.length() - 1).trim();
            if (content.isEmpty()) {
                return new int[]{1};
            }
            String[] parts = content.split(",");
            int[] indices = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                indices[i] = Integer.parseInt(parts[i].trim());
            }
            return indices;
        }

        // MATLAB range notation: 1:2:10 (start:step:end) or 1:10 (start:end)
        if (trimmed.contains(":")) {
            String[] parts = trimmed.split(":");
            if (parts.length == 2) {
                // start:end (step = 1)
                int start = Integer.parseInt(parts[0].trim());
                int end = Integer.parseInt(parts[1].trim());
                return generateRange(start, 1, end);
            } else if (parts.length == 3) {
                // start:step:end
                int start = Integer.parseInt(parts[0].trim());
                int step = Integer.parseInt(parts[1].trim());
                int end = Integer.parseInt(parts[2].trim());
                return generateRange(start, step, end);
            }
        }

        // Single integer
        try {
            int index = Integer.parseInt(trimmed);
            return new int[]{index};
        } catch (NumberFormatException e) {
            return new int[]{1}; // Default
        }
    }

    /**
     * Generate range array from MATLAB-style start:step:end
     */
    private int[] generateRange(int start, int step, int end) {
        if (step == 0) {
            return new int[]{start};
        }

        List<Integer> indices = new ArrayList<>();
        if (step > 0) {
            for (int i = start; i <= end; i += step) {
                indices.add(i);
            }
        } else {
            for (int i = start; i >= end; i += step) {
                indices.add(i);
            }
        }

        return indices.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Get the number of input ports (always 1 for Selector block)
     */
    public int getInputPortCount() {
        return 1;
    }

    /**
     * Get the number of output ports (always 1 for Selector block)
     */
    public int getOutputPortCount() {
        return 1;
    }

    @Override
    public SelectorDto copy() {
        return SelectorDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .indexMode(indexMode != null ? indexMode.copy() : null)
                .indexVector(indexVector != null ? indexVector.copy() : null)
                .indexOptions(indexOptions != null ? indexOptions.copy() : null)
                .numberOfDimensions(numberOfDimensions != null ? numberOfDimensions.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("IndexMode", indexMode)
                .put("IndexVector", indexVector)
                .put("IndexOptions", indexOptions)
                .put("NumberOfDimensions", numberOfDimensions)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    @Override
    public String toString() {
        return String.format("SelectorDto{id=%d, name='%s', type='%s', indexMode='%s', indexVector='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getIndexModeValue(),
                           getIndexVectorValue());
    }
}
