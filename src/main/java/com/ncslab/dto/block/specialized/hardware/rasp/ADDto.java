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
 * DTO representation of AD block for Raspberry Pi.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("AD")
@MigrationCompatible(originalClass = "com.ncslab.block.hardware.rasp.AD")
public class ADDto extends BlockDto {
    
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