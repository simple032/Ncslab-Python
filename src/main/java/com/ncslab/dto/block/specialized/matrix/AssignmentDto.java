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
 * DTO representation of Assignment block.
 *
 * Assigns values to specific elements of a signal, similar to MATLAB indexed assignment.
 * Example: A(indices) = values replaces specified elements with new values.
 *
 * Supports multiple assignment modes:
 * - Element assignment: A(i,j) = value
 * - Row assignment: A(i,:) = values
 * - Column assignment: A(:,j) = values
 * - Index vector: A(indices) = values
 *
 * @see com.ncslab.block.matrix.Assignment
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Assignment")
@MigrationCompatible(originalClass = "com.ncslab.block.matrix.Assignment")
public class AssignmentDto extends BlockDto {

    /**
     * Assignment mode determines how indices are interpreted.
     * Valid values: "element", "row", "column", "index"
     */
    @Builder.Default
    private TypedParameter assignmentMode = TypedParameter.of("element");

    /**
     * Index specification for assignment.
     * Format depends on assignment mode:
     * - element: "row,col" (e.g., "2,3")
     * - row: "row_number" (e.g., "2")
     * - column: "col_number" (e.g., "3")
     * - index: "i1,i2,i3,..." (e.g., "1,2,5,7" for linear indices)
     */
    @Builder.Default
    private TypedParameter indices = TypedParameter.of("1,1");

    /**
     * Number of input ports (always 2: original signal and values to assign)
     */
    @Builder.Default
    private TypedParameter numberOfInputs = TypedParameter.of("2");

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
     * Gets the assignment mode.
     *
     * @return Assignment mode string ("element", "row", "column", or "index")
     */
    public String getAssignmentModeValue() {
        return assignmentMode != null ? assignmentMode.getAsString() : "element";
    }

    /**
     * Gets the indices specification as a string.
     *
     * @return Index specification string
     */
    public String getIndicesValue() {
        return indices != null ? indices.getAsString() : "1,1";
    }

    /**
     * Gets the indices as an integer array.
     *
     * @return Array of indices
     */
    public int[] getIndicesArray() {
        String indicesStr = getIndicesValue();
        String[] parts = indicesStr.split(",");
        int[] indexArray = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            indexArray[i] = Integer.parseInt(parts[i].trim());
        }
        return indexArray;
    }

    /**
     * Gets the number of inputs.
     *
     * @return Number of input ports (should always be 2)
     */
    public int getNumberOfInputsValue() {
        return numberOfInputs != null ? numberOfInputs.getAsInteger() : 2;
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
     * Validates the assignment configuration.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValidConfiguration() {
        try {
            String mode = getAssignmentModeValue();
            int[] indexArray = getIndicesArray();

            // Validate mode
            if (!mode.equals("element") && !mode.equals("row") &&
                !mode.equals("column") && !mode.equals("index")) {
                return false;
            }

            // Validate indices based on mode
            switch (mode) {
                case "element":
                    // Should have exactly 2 indices (row, col)
                    return indexArray.length == 2 && indexArray[0] >= 1 && indexArray[1] >= 1;
                case "row":
                case "column":
                    // Should have exactly 1 index
                    return indexArray.length == 1 && indexArray[0] >= 1;
                case "index":
                    // Should have at least 1 index, all >= 1
                    if (indexArray.length < 1) return false;
                    for (int idx : indexArray) {
                        if (idx < 1) return false;
                    }
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("AssignmentMode", assignmentMode)
                .put("Indices", indices)
                .put("NumberOfInputs", numberOfInputs)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
}
