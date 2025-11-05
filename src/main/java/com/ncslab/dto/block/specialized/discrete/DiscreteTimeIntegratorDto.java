package com.ncslab.dto.block.specialized.discrete;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for Discrete-Time Integrator block.
 * Performs discrete-time integration using various methods.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DiscreteTimeIntegrator")
public class DiscreteTimeIntegratorDto extends BlockDto {
    
    /**
     * Gain for the integrator.
     * Default: 1.0
     */
    private TypedParameter gain;
    
    /**
     * Initial condition of the integrator.
     * Default: 0.0
     */
    private TypedParameter initialCondition;
    
    /**
     * Sample time for discrete integration.
     * Default: -1 (inherited)
     */
    private TypedParameter sampleTime;
    
    /**
     * Integration method.
     * Options: "Forward Euler", "Backward Euler", "Trapezoidal"
     * Default: "Forward Euler"
     */
    private TypedParameter integrationMethod;
    
    /**
     * Whether to limit the output.
     * Default: false
     */
    private TypedParameter limitOutput;
    
    /**
     * Upper saturation limit.
     * Default: inf
     */
    private TypedParameter upperSaturationLimit;
    
    /**
     * Lower saturation limit.
     * Default: -inf
     */
    private TypedParameter lowerSaturationLimit;
    
    public DiscreteTimeIntegratorDto(String blockName, String blockPath) {
        super(blockName, blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (gain == null) {
            gain = TypedParameter.of(1.0);
        }
        if (initialCondition == null) {
            initialCondition = TypedParameter.of(0.0);
        }
        if (sampleTime == null) {
            sampleTime = TypedParameter.of(-1.0);
        }
        if (integrationMethod == null) {
            integrationMethod = TypedParameter.of("Forward Euler");
        }
        if (limitOutput == null) {
            limitOutput = TypedParameter.of(false);
        }
        if (upperSaturationLimit == null) {
            upperSaturationLimit = TypedParameter.of(Double.POSITIVE_INFINITY);
        }
        if (lowerSaturationLimit == null) {
            lowerSaturationLimit = TypedParameter.of(Double.NEGATIVE_INFINITY);
        }
    }
}