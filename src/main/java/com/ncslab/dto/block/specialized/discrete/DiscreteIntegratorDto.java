package com.ncslab.dto.block.specialized.discrete;

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

/**
 * DTO representation of Discrete Time Integrator block.
 * 
 * The Discrete Time Integrator block performs discrete-time integration
 * of the input signal. It supports various integration methods including
 * Forward Euler, Backward Euler, and Trapezoidal rule.
 * 
 * The integration is performed as: y[n] = y[n-1] + K*T*u[n]
 * where K is the gain, T is the sample time, and u is the input.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DiscreteIntegrator")
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.Discrete_Time_Integrator")
public class DiscreteIntegratorDto extends BlockDto {
    
    // Integration method constants
    public static final String FORWARD_EULER = "Forward Euler";
    public static final String BACKWARD_EULER = "Backward Euler";
    public static final String TRAPEZOIDAL = "Trapezoidal";
    
    // Valid integration methods
    private static final List<String> VALID_METHODS = Arrays.asList(FORWARD_EULER, BACKWARD_EULER, TRAPEZOIDAL);
    
    /**
     * Gain value for integration
     * Default: 1.0
     */
    @Builder.Default
    private TypedParameter gain = TypedParameter.of(1.0);
    
    /**
     * Initial condition of the integrator
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter initialCondition = TypedParameter.of(0.0);
    
    /**
     * Integration method
     * Default: "Forward Euler"
     * Options: "Forward Euler", "Backward Euler", "Trapezoidal"
     */
    @Builder.Default
    private TypedParameter integratorMethod = TypedParameter.of(FORWARD_EULER);
    
    /**
     * Sample time for discrete operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    
    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Double getGainValue() {
        return gain != null ? gain.getAsDouble() : 1.0;
    }
    
    public Double getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getAsDouble() : 0.0;
    }
    
    public String getIntegratorMethodValue() {
        return integratorMethod != null ? integratorMethod.getAsString() : FORWARD_EULER;
    }
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate gain
        if (gain != null) {
            try {
                Double gainVal = gain.getAsDouble();
                if (gainVal != null && (Double.isNaN(gainVal) || Double.isInfinite(gainVal))) {
                    result.addError("Gain must be finite");
                }
            } catch (Exception e) {
                result.addError("Invalid gain format: " + e.getMessage());
            }
        }
        
        // Validate initial condition
        if (initialCondition != null) {
            try {
                Double icVal = initialCondition.getAsDouble();
                if (icVal != null && (Double.isNaN(icVal) || Double.isInfinite(icVal))) {
                    result.addError("Initial condition must be finite");
                }
            } catch (Exception e) {
                result.addError("Invalid initial condition format: " + e.getMessage());
            }
        }
        
        // Validate integration method
        if (integratorMethod != null) {
            String method = integratorMethod.getAsString();
            if (method != null && !VALID_METHODS.contains(method)) {
                result.addError("Integration method must be one of: " + VALID_METHODS);
            }
        }
        
        // Validate discrete sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null) {
                if (st != -1.0 && st <= 0.0) {
                    result.addError("Sample time must be positive or -1 (inherited)");
                }
                if (Double.isNaN(st) || Double.isInfinite(st)) {
                    result.addError("Sample time must be finite");
                }
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if sample time is inherited (-1)
     */
    public boolean isInheritedSampleTime() {
        return getSampleTimeValue() == -1.0;
    }
    
    /**
     * Check if sample time is discrete (positive value)
     */
    public boolean isDiscreteSampleTime() {
        return getSampleTimeValue() > 0.0;
    }
    
    /**
     * Check if using forward Euler method
     */
    public boolean isForwardEuler() {
        return FORWARD_EULER.equals(getIntegratorMethodValue());
    }
    
    /**
     * Check if using backward Euler method
     */
    public boolean isBackwardEuler() {
        return BACKWARD_EULER.equals(getIntegratorMethodValue());
    }
    
    /**
     * Check if using trapezoidal method
     */
    public boolean isTrapezoidal() {
        return TRAPEZOIDAL.equals(getIntegratorMethodValue());
    }
    
    /**
     * Get integration coefficient (T * K) where T is sample time and K is gain
     */
    public double getIntegrationCoefficient() {
        if (isDiscreteSampleTime()) {
            return getSampleTimeValue() * getGainValue();
        }
        return Double.NaN; // Cannot determine without knowing inherited sample time
    }
    
    /**
     * Check if gain is unity (no scaling)
     */
    public boolean isUnityGain() {
        return Math.abs(getGainValue() - 1.0) < 1e-15;
    }
    
    /**
     * Check if initial condition is zero
     */
    public boolean isZeroInitialCondition() {
        return Math.abs(getInitialConditionValue()) < 1e-15;
    }
    
    /**
     * Create discrete integrator with specific parameters
     */
    public static DiscreteIntegratorDto create(String name, String path, double gain, 
                                             double initialCondition, String method, double sampleTime) {
        return DiscreteIntegratorDto.builder()
                .blockName(name)
                .blockPath(path)
                .gain(TypedParameter.of(gain))
                .initialCondition(TypedParameter.of(initialCondition))
                .integratorMethod(TypedParameter.of(method))
                .sampleTime(TypedParameter.of(sampleTime))
                .outDataTypeStr(TypedParameter.of("Inherit: Same as input"))
                .saturateOnIntegerOverflow(TypedParameter.of(false))
                .build();
    }
    
    /**
     * Create forward Euler integrator
     */
    public static DiscreteIntegratorDto createForwardEuler(String name, String path, double gain, 
                                                          double initialCondition, double sampleTime) {
        return create(name, path, gain, initialCondition, FORWARD_EULER, sampleTime);
    }
    
    /**
     * Create backward Euler integrator
     */
    public static DiscreteIntegratorDto createBackwardEuler(String name, String path, double gain, 
                                                           double initialCondition, double sampleTime) {
        return create(name, path, gain, initialCondition, BACKWARD_EULER, sampleTime);
    }
    
    /**
     * Create trapezoidal integrator
     */
    public static DiscreteIntegratorDto createTrapezoidal(String name, String path, double gain, 
                                                         double initialCondition, double sampleTime) {
        return create(name, path, gain, initialCondition, TRAPEZOIDAL, sampleTime);
    }
    
    @Override
    public DiscreteIntegratorDto copy() {
        return DiscreteIntegratorDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .gain(gain != null ? gain.copy() : null)
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
                .integratorMethod(integratorMethod != null ? integratorMethod.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Gain", gain)
                .put("InitialCondition", initialCondition)
                .put("IntegratorMethod", integratorMethod)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("DiscreteIntegratorDto{id=%d, name='%s', type='%s', gain=%s, method='%s', sampleTime=%s}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getGainValue(),
                           getIntegratorMethodValue(),
                           getSampleTimeValue());
    }
}