package com.ncslab.dto.block.specialized.powerSystem;

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
 * DTO representation of secondOrderFiliter block.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("secondOrderFiliter")
@MigrationCompatible(originalClass = "com.ncslab.block.powerSystem.secondOrderFiliter")
public class SecondOrderFiliterDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter naturalFrequency = TypedParameter.of(50.0);
    @Builder.Default
    private TypedParameter dampingRatio = TypedParameter.of(0.707);
    
    public Double getNaturalFrequencyValue() {
        return naturalFrequency != null ? naturalFrequency.getAsDouble() : 50.0;
    }
    
    public Double getDampingRatioValue() {
        return dampingRatio != null ? dampingRatio.getAsDouble() : 0.707;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("naturalFrequency", naturalFrequency)
                .put("dampingRatio", dampingRatio)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}