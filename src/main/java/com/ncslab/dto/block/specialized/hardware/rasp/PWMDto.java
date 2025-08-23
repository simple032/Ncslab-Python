package com.ncslab.dto.block.specialized.hardware.rasp;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of PWM block for Raspberry Pi.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("PWM")
@MigrationCompatible(originalClass = "com.ncslab.block.hardware.rasp.PWM")
public class PWMDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter pinNumber = TypedParameter.of(18);
    @Builder.Default
    private TypedParameter frequency = TypedParameter.of(1000.0);
    @Builder.Default
    private TypedParameter dutyCycle = TypedParameter.of(50.0);
    
    public Integer getPinNumberValue() {
        return pinNumber != null ? pinNumber.getAsInteger() : 18;
    }
    
    public Double getFrequencyValue() {
        return frequency != null ? frequency.getAsDouble() : 1000.0;
    }
    
    public Double getDutyCycleValue() {
        return dutyCycle != null ? dutyCycle.getAsDouble() : 50.0;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("PinNumber", pinNumber)
                .put("Frequency", frequency)
                .put("DutyCycle", dutyCycle)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}