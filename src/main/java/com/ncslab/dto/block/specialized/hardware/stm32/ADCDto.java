package com.ncslab.dto.block.specialized.hardware.stm32;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO representation of ADC block for STM32.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ADC")
@MigrationCompatible(originalClass = "com.ncslab.block.hardware.stm32.ADC")
public class ADCDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter channel = TypedParameter.of(0);
    @Builder.Default
    private TypedParameter resolution = TypedParameter.of(12);
    @Builder.Default
    private TypedParameter referenceVoltage = TypedParameter.of(3.3);
    
    public Integer getChannelValue() {
        return channel != null ? channel.getAsInteger() : 0;
    }
    
    public Integer getResolutionValue() {
        return resolution != null ? resolution.getAsInteger() : 12;
    }
    
    public Double getReferenceVoltageValue() {
        return referenceVoltage != null ? referenceVoltage.getAsDouble() : 3.3;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Channel", channel)
                .put("Resolution", resolution)
                .put("ReferenceVoltage", referenceVoltage)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}