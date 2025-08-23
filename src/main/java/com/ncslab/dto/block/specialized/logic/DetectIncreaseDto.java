package com.ncslab.dto.block.specialized.logic;

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
 * DTO representation of DetectIncrease block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DetectIncrease")
@MigrationCompatible(originalClass = "com.ncslab.block.logicAndBit.DetectIncrease")
public class DetectIncreaseDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter initialValue = TypedParameter.of(0.0);
    
    public Double getInitialValueValue() {
        return initialValue != null ? initialValue.getAsDouble() : 0.0;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("InitialValue", initialValue)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}