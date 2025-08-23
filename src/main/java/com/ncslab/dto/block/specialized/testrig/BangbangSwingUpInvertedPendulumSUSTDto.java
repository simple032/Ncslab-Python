package com.ncslab.dto.block.specialized.testrig;

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
 * DTO representation of BangbangSwingUpInvertedPendulumSUST block.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("BangbangSwingUpInvertedPendulumSUST")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.BangbangSwingUpInvertedPendulumSUST")
public class BangbangSwingUpInvertedPendulumSUSTDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter v = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter vel = TypedParameter.of(1.0);
    
    public Double getVValue() {
        return v != null ? v.getAsDouble() : 1.0;
    }
    
    public Double getVelValue() {
        return vel != null ? vel.getAsDouble() : 1.0;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("v", v)
                .put("vel", vel)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}