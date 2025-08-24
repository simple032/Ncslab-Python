package com.ncslab.dto.block.specialized.logic;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for Compare To Constant block.
 * Compares input signal to a constant value using specified relational operator.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CompareToConstantDto extends BlockDto {
    
    /**
     * Relational operator for comparison.
     * Options: "==", "!=", "<", "<=", ">", ">="
     * Default: "=="
     */
    private TypedParameter relationalOperator;
    
    /**
     * Constant value to compare against.
     * Default: 0
     */
    private TypedParameter constantValue;
    
    public CompareToConstantDto(String blockName, String blockPath) {
        super(blockName,blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (relationalOperator == null) {
            relationalOperator = TypedParameter.of("==");
        }
        if (constantValue == null) {
            constantValue = TypedParameter.of(0);
        }
    }
}