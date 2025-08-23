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
 * DTO representation of GPIO block for Raspberry Pi.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("GPIO")
@MigrationCompatible(originalClass = "com.ncslab.block.hardware.rasp.GPIO")
public class GPIODto extends BlockDto {
    
    @Builder.Default
    private TypedParameter pinNumber = TypedParameter.of(18);
    @Builder.Default
    private TypedParameter direction = TypedParameter.of("Output");
    @Builder.Default
    private TypedParameter initialValue = TypedParameter.of(0);
    
    public Integer getPinNumberValue() {
        return pinNumber != null ? pinNumber.getAsInteger() : 18;
    }
    
    public String getDirectionValue() {
        return direction != null ? direction.getAsString() : "Output";
    }
    
    public Integer getInitialValueValue() {
        return initialValue != null ? initialValue.getAsInteger() : 0;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("PinNumber", pinNumber)
                .put("Direction", direction)
                .put("InitialValue", initialValue)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}