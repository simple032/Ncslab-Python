package com.ncslab.dto.block.specialized.elect;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

/**
 * DTO representation of LimitingLink block.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("LimitingLink")
@MigrationCompatible(originalClass = "com.ncslab.block.elect.LimitingLink")
public class LimitingLinkDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter upperLimit = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter lowerLimit = TypedParameter.of(-1.0);
    @Builder.Default
    private TypedParameter gain = TypedParameter.of(1.0);
    
    public Double getUpperLimitValue() {
        return upperLimit != null ? upperLimit.getAsDouble() : 1.0;
    }
    
    public Double getLowerLimitValue() {
        return lowerLimit != null ? lowerLimit.getAsDouble() : -1.0;
    }
    
    public Double getGainValue() {
        return gain != null ? gain.getAsDouble() : 1.0;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("UpperLimit", upperLimit)
                .put("LowerLimit", lowerLimit)
                .put("Gain", gain)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}