package com.ncslab.dto.block.specialized.elect;

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
 * DTO representation of DiodeCurrent block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DiodeCurrent")
@MigrationCompatible(originalClass = "com.ncslab.block.elect.DiodeCurrent")
public class DiodeCurrentDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter vf = TypedParameter.of(0.7);
    
    @Builder.Default
    private TypedParameter ron = TypedParameter.of(0.001);
    
    @Builder.Default
    private TypedParameter goff = TypedParameter.of(1e-6);
    
    public Double getVfValue() {
        return vf != null ? vf.getAsDouble() : 0.7;
    }
    
    public Double getRonValue() {
        return ron != null ? ron.getAsDouble() : 0.001;
    }
    
    public Double getGoffValue() {
        return goff != null ? goff.getAsDouble() : 1e-6;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Vf", vf)
                .put("Ron", ron)
                .put("Goff", goff)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}