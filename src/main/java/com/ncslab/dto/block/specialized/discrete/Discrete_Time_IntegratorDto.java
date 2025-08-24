package com.ncslab.dto.block.specialized.discrete;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.discrete.DiscreteBlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;

/**
 * DTO representation of Discrete Time Integrator block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the Discrete Time Integrator block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - Gain: Integrator gain value (default: 1)
 * - InitialCondition: Initial condition value (default: 0) 
 * - IntegratorMethod: Integration method (default: "Forward Euler")
 * - SampleTime: Sample time for discrete integration (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Integer overflow handling (default: "off")
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.Discrete_Time_Integrator")
public class Discrete_Time_IntegratorDto extends DiscreteBlockDto {

    // === Core Discrete Time Integrator Parameters ===
    
    @Builder.Default
    private TypedParameter gain = TypedParameter.of(1.0);
    
    @Builder.Default
    private TypedParameter initialCondition = TypedParameter.of(0.0);
    
    @Builder.Default
    private TypedParameter integratorMethod = TypedParameter.of("Forward Euler");
    
    
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of("off");

    // === Type-Safe Getters ===
    
    public Double getGainValue() {
        return gain != null ? gain.getAsDouble() : 1.0;
    }
    
    public Double getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getAsDouble() : 0.0;
    }
    
    public String getIntegratorMethodValue() {
        return integratorMethod != null ? integratorMethod.getAsString() : "Forward Euler";
    }
    
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public String getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsString() : "off";
    }

    // === DTO Methods ===
    
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate gain parameter
        if (gain != null && !gain.isNumericType()) {
            result.addError("Gain must be a numeric value");
        }
        
        // Validate initial condition
        if (initialCondition != null && !initialCondition.isNumericType()) {
            result.addError("Initial condition must be a numeric value");
        }
        
        // Validate integrator method
        if (integratorMethod != null) {
            String method = integratorMethod.getAsString();
            if (!method.equals("Forward Euler") && !method.equals("Backward Euler") && !method.equals("Trapezoidal")) {
                result.addError("Invalid integrator method: " + method);
            }
        }
        
        
        return result;
    }
    
    /**
     * Create builder with SIMULINK-compatible defaults
     */
    public static Discrete_Time_IntegratorDtoBuilder builderWithDefaults() {
        return Discrete_Time_IntegratorDto.builder()
                .gain(TypedParameter.of(1.0))
                .initialCondition(TypedParameter.of(0.0))
                .integratorMethod(TypedParameter.of("Forward Euler"))
                .sampleTime(TypedParameter.of(-1.0)) // inherited from DiscreteBlockDto
                .outDataTypeStr(TypedParameter.of("Inherit: Same as input"))
                .saturateOnIntegerOverflow(TypedParameter.of("off"));
    }
}