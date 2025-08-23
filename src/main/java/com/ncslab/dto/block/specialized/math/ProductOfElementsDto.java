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
 * DTO for Product of Elements block.
 * Computes the product of all elements in a vector or matrix.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductOfElementsDto extends BlockDto {
    
    /**
     * Dimension along which to compute the product.
     * Options: "All", "Column", "Row", "Specified dimension"
     * Default: "All"
     */
    @JsonProperty("dimension")
    private TypedParameter productDimension;
    
    /**
     * Specific dimension number when dimension is "Specified dimension".
     * Default: 1
     */
    private TypedParameter specifiedDimension;
    
    public ProductOfElementsDto(String blockName, String blockPath) {
        super("ProductOfElements", blockName, blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (productDimension == null) {
            productDimension = TypedParameter.of("All");
        }
        if (specifiedDimension == null) {
            specifiedDimension = TypedParameter.of(1);
        }
    }
}