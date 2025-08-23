package com.ncslab.dto.block.specialized.lookupTable;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for Two-Dimension Lookup Table block.
 * Performs two-dimensional table lookup with interpolation.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TwoDimensionLookupTableDto extends BlockDto {
    
    /**
     * Vector of first input values (row breakpoints).
     * Default: [0 1 2]
     */
    private TypedParameter rowInputValues;
    
    /**
     * Vector of second input values (column breakpoints).
     * Default: [0 1 2]
     */
    private TypedParameter columnInputValues;
    
    /**
     * Matrix of output values corresponding to input combinations.
     * Default: [[0 1 2]; [1 2 3]; [2 3 4]]
     */
    private TypedParameter outputValues;
    
    /**
     * Interpolation method.
     * Options: "Linear", "Nearest", "Cubic spline"
     * Default: "Linear"
     */
    private TypedParameter interpolationMethod;
    
    /**
     * Extrapolation method.
     * Options: "Linear", "Clip", "Error"
     * Default: "Linear"
     */
    private TypedParameter extrapolationMethod;
    
    public TwoDimensionLookupTableDto(String blockName, String blockPath) {
        super("TwoDimensionLookupTable", blockName, blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (rowInputValues == null) {
            rowInputValues = TypedParameter.of("[0 1 2]");
        }
        if (columnInputValues == null) {
            columnInputValues = TypedParameter.of("[0 1 2]");
        }
        if (outputValues == null) {
            outputValues = TypedParameter.of("[[0 1 2]; [1 2 3]; [2 3 4]]");
        }
        if (interpolationMethod == null) {
            interpolationMethod = TypedParameter.of("Linear");
        }
        if (extrapolationMethod == null) {
            extrapolationMethod = TypedParameter.of("Linear");
        }
    }
}