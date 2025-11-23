package com.ncslab.dto.block.specialized.matrix;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO representation of PermuteDimensions block.
 *
 * Rearranges the dimensions of multi-dimensional arrays similar to MATLAB's permute() function.
 * Example: permute(A, [2,1,3]) transposes the first two dimensions of a 3D array.
 *
 * For 2D matrices: [2,1] performs a transpose operation
 * For higher dimensions: rearranges dimensions according to the specified permutation order
 *
 * @see com.ncslab.block.matrix.PermuteDimensions
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("PermuteDimensions")
@MigrationCompatible(originalClass = "com.ncslab.block.matrix.PermuteDimensions")
public class PermuteDimensionsDto extends BlockDto {

    /**
     * Permutation order specifying how to rearrange dimensions.
     * Format: comma-separated integers representing dimension indices (1-based)
     * Example: "2,1" for 2D transpose
     * Example: "3,1,2" for 3D permutation
     * Must be a valid permutation of [1,2,...,n] where n is the number of dimensions
     */
    @Builder.Default
    private TypedParameter permutationOrder = TypedParameter.of("2,1");

    /**
     * Output data type specification
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");

    /**
     * Integer overflow saturation setting
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of("off");

    /**
     * Gets the permutation order as a string.
     *
     * @return Permutation order string (e.g., "2,1" or "3,1,2")
     */
    public String getPermutationOrderValue() {
        return permutationOrder != null ? permutationOrder.getAsString() : "2,1";
    }

    /**
     * Gets the permutation order as an array of integers.
     *
     * @return Array of dimension indices representing the permutation
     */
    public int[] getPermutationOrderArray() {
        String orderStr = getPermutationOrderValue();
        String[] parts = orderStr.split(",");
        int[] order = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            order[i] = Integer.parseInt(parts[i].trim());
        }
        return order;
    }

    /**
     * Gets the output data type string.
     *
     * @return Output data type specification
     */
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }

    /**
     * Gets the integer overflow saturation setting.
     *
     * @return "on" or "off"
     */
    public String getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsString() : "off";
    }

    /**
     * Validates the permutation order.
     *
     * @param numDimensions Number of dimensions in the input signal
     * @return true if valid, false otherwise
     */
    public boolean isValidPermutation(int numDimensions) {
        try {
            int[] order = getPermutationOrderArray();

            // Check length matches number of dimensions
            if (order.length != numDimensions) {
                return false;
            }

            // Check each value is in range [1, numDimensions]
            boolean[] seen = new boolean[numDimensions + 1];
            for (int idx : order) {
                if (idx < 1 || idx > numDimensions) {
                    return false;
                }
                if (seen[idx]) {
                    return false; // Duplicate value
                }
                seen[idx] = true;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("PermutationOrder", permutationOrder)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
}
