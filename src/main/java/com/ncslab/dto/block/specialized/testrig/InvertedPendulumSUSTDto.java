package com.ncslab.dto.block.specialized.testrig;

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
 * DTO representation of InvertedPendulumSUST block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("InvertedPendulumSUST")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.InvertedPendulumSUST")
public class InvertedPendulumSUSTDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter vspeed = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter enaOrDIS = TypedParameter.of(1);
    
    public Double getVspeedValue() {
        return vspeed != null ? vspeed.getAsDouble() : 1.0;
    }
    
    public Integer getEnaOrDISValue() {
        return enaOrDIS != null ? enaOrDIS.getAsInteger() : 1;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Vspeed", vspeed)
                .put("ENAOrDIS", enaOrDIS)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}