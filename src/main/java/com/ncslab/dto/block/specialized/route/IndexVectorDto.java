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

/**
 * DTO representation of IndexVector route block.
 *
 * The IndexVector block generates index vectors for use with Selector and Assignment blocks.
 * It is equivalent to MATLAB's colon operator for index generation.
 *
 * Index Modes:
 * - One-based: Indices start at 1 (MATLAB convention) - default
 * - Zero-based: Indices start at 0 (C/Java convention)
 *
 * Index Notation Formats:
 * - Array notation: [1, 3, 5, 7] - explicit index list
 * - MATLAB range: 1:10 - indices from 1 to 10 with step 1
 * - MATLAB range with step: 1:2:20 - indices from 1 to 20 with step 2
 * - Single value: 5 - single index
 *
 * Port Configuration:
 * - Input: None (source block)
 * - Output: Vector of indices
 *
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("IndexVector")
@MigrationCompatible(originalClass = "com.ncslab.block.route.IndexVector")
public class IndexVectorDto extends BlockDto {

    /**
     * Index mode selection
     * Default: "One-based"
     * Options: "Zero-based", "One-based"
     */
    @Builder.Default
    private TypedParameter indexMode = TypedParameter.of("One-based");

    /**
     * Index parameter array specification
     * Can be array notation [1, 3, 5] or MATLAB syntax "1:2:10"
     * Default: "1:10"
     * Note: Base indexing depends on IndexMode setting
     */
    @Builder.Default
    private TypedParameter indexParamArray = TypedParameter.of("1:10");

    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    /**
     * Output data type specification
     * Default: "int32"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("int32");

    // Valid index modes
    private static final List<String> VALID_INDEX_MODES = Arrays.asList(
        "Zero-based", "One-based"
    );

    // ===== PARAMETER ACCESS HELPERS =====

    public String getIndexModeValue() {
        return indexMode != null ? indexMode.getAsString() : "One-based";
    }

    public String getIndexParamArrayValue() {
        return indexParamArray != null ? indexParamArray.getAsString() : "1:10";
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "int32";
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

        // Validate index parameter array format
        if (indexParamArray != null) {
            String array = indexParamArray.getAsString();
            if (array != null) {
                if (!isValidIndexSpecification(array)) {
                    result.addError("Invalid index specification format. Use [1, 3, 5] or MATLAB syntax like 1:2:10");
                }
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
     * Validate index specification format
     * Supports: [1, 3, 5], [1,3,5], 1:2:10, 1:10, single integer, etc.
     */
    private boolean isValidIndexSpecification(String spec) {
        if (spec == null || spec.trim().isEmpty()) {
            return false;
        }

        String trimmed = spec.trim();

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
     * Check if index mode is zero-based
     */
    public boolean isZeroBased() {
        return "Zero-based".equals(getIndexModeValue());
    }

    /**
     * Check if index mode is one-based
     */
    public boolean isOneBased() {
        return "One-based".equals(getIndexModeValue());
    }

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
     * Parse index specification string to integer array
     * Handles both [1, 3, 5] and MATLAB syntax 1:2:10
     * @return Array of indices (as specified, not adjusted for zero/one-based)
     */
    public int[] parseIndexVector() {
        String spec = getIndexParamArrayValue();
        if (spec == null || spec.trim().isEmpty()) {
            return new int[]{1}; // Default to first element
        }

        String trimmed = spec.trim();

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
     * Get the number of input ports (always 0 for IndexVector block - source block)
     */
    public int getInputPortCount() {
        return 0;
    }

    /**
     * Get the number of output ports (always 1 for IndexVector block)
     */
    public int getOutputPortCount() {
        return 1;
    }

    /**
     * Get the expected output vector size based on index specification
     */
    public int getOutputVectorSize() {
        return parseIndexVector().length;
    }

    @Override
    public IndexVectorDto copy() {
        return IndexVectorDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .indexMode(indexMode != null ? indexMode.copy() : null)
                .indexParamArray(indexParamArray != null ? indexParamArray.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("IndexMode", indexMode)
                .put("IndexParamArray", indexParamArray)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    @Override
    public String toString() {
        return String.format("IndexVectorDto{id=%d, name='%s', type='%s', indexMode='%s', indexParamArray='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getIndexModeValue(),
                           getIndexParamArrayValue());
    }
}
