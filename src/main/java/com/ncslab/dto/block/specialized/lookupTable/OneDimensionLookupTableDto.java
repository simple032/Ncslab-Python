package com.ncslab.dto.block.specialized.lookupTable;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for One-Dimension Lookup Table block.
 * Performs one-dimensional table lookup with interpolation.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OneDimensionLookupTableDto extends BlockDto {
    
    /**
     * Vector of input values (breakpoints).
     * Default: [0 1 2 3 4 5]
     */
    private TypedParameter inputValues;
    
    /**
     * Vector of output values corresponding to input values.
     * Default: [0 1 4 9 16 25]
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
    
    public OneDimensionLookupTableDto(String blockName, String blockPath) {
        super("OneDimensionLookupTable", blockName, blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (inputValues == null) {
            inputValues = TypedParameter.of("[0 1 2 3 4 5]");
        }
        if (outputValues == null) {
            outputValues = TypedParameter.of("[0 1 4 9 16 25]");
        }
        if (interpolationMethod == null) {
            interpolationMethod = TypedParameter.of("Linear");
        }
        if (extrapolationMethod == null) {
            extrapolationMethod = TypedParameter.of("Linear");
        }
    }
}