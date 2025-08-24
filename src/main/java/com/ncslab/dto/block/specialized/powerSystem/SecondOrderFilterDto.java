package com.ncslab.dto.block.specialized.powerSystem;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for Second Order Filter block.
 * Implements a second-order filter for power system applications.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SecondOrderFilterDto extends BlockDto {
    
    /**
     * Natural frequency of the filter (rad/s).
     * Default: 1.0
     */
    private TypedParameter naturalFrequency;
    
    /**
     * Damping ratio of the filter.
     * Default: 0.707 (critically damped)
     */
    private TypedParameter dampingRatio;
    
    /**
     * Initial condition for the first state.
     * Default: 0.0
     */
    private TypedParameter initialCondition1;
    
    /**
     * Initial condition for the second state.
     * Default: 0.0
     */
    private TypedParameter initialCondition2;
    
    public SecondOrderFilterDto(String blockName, String blockPath) {
        super(blockName,blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (naturalFrequency == null) {
            naturalFrequency = TypedParameter.of(1.0);
        }
        if (dampingRatio == null) {
            dampingRatio = TypedParameter.of(0.707);
        }
        if (initialCondition1 == null) {
            initialCondition1 = TypedParameter.of(0.0);
        }
        if (initialCondition2 == null) {
            initialCondition2 = TypedParameter.of(0.0);
        }
    }
}