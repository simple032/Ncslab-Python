package com.ncslab.dto.block.specialized.math;

import com.ncslab.dto.core.BlockDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for Sum of Elements block.
 * Computes the sum of all elements in a vector or matrix.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SumOfElementsDto extends BlockDto {
    
    /**
     * Dimension along which to compute the sum.
     * Options: "All", "Column", "Row", "Specified dimension"
     * Default: "All"
     */
    @JsonProperty("dimension")
    private TypedParameter sumDimension;
    
    /**
     * Specific dimension number when dimension is "Specified dimension".
     * Default: 1
     */
    private TypedParameter specifiedDimension;
    
    public SumOfElementsDto(String blockName, String blockPath) {
        super(blockName,blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (sumDimension == null) {
            sumDimension = TypedParameter.of("All");
        }
        if (specifiedDimension == null) {
            specifiedDimension = TypedParameter.of(1);
        }
    }
}