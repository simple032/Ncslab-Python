package com.ncslab.dto.block.specialized.discrete;

import com.fasterxml.jackson.annotation.JsonTypeName;
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
 * DTO representation of Discrete Transfer Function (z-domain) block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the Discrete Transfer Function block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - SampleTime: Sample time for discrete transfer function (-1 for inherited)
 * - OutDataTypeStr: Output data type specification  
 * - SaturateOnIntegerOverflow: Integer overflow handling (default: "off")
 * 
 * Note: This block receives numerator and denominator coefficients via input ports
 * rather than as block parameters.
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
@JsonTypeName("Discrete_Transfer_Fcnz")
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.Discrete_Transfer_Fcnz")
public class Discrete_Transfer_FcnzDto extends DiscreteBlockDto {

    // === Core Discrete Transfer Function Parameters ===
    
    
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of("off");

    // === Type-Safe Getters ===
    
    
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
        
        
        // Validate saturate setting
        if (saturateOnIntegerOverflow != null) {
            String saturate = saturateOnIntegerOverflow.getAsString();
            if (!saturate.equals("off") && !saturate.equals("on")) {
                result.addError("SaturateOnIntegerOverflow must be 'on' or 'off'");
            }
        }
        
        return result;
    }
    
    /**
     * Create builder with SIMULINK-compatible defaults
     */
    public static Discrete_Transfer_FcnzDtoBuilder builderWithDefaults() {
        return Discrete_Transfer_FcnzDto.builder()
                .sampleTime(TypedParameter.of(-1.0)) // inherited from DiscreteBlockDto
                .outDataTypeStr(TypedParameter.of("Inherit: Same as input"))
                .saturateOnIntegerOverflow(TypedParameter.of("off"));
    }
}