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
 * DTO representation of BallPlateSystem block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("BallPlateSystem")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.BallPlateSystem")
public class BallPlateSystemDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter ballMass = TypedParameter.of(0.027);
    @Builder.Default
    private TypedParameter ballRadius = TypedParameter.of(0.02);
    @Builder.Default
    private TypedParameter plateSize = TypedParameter.of(0.42);
    @Builder.Default
    private TypedParameter gravity = TypedParameter.of(9.81);
    @Builder.Default
    private TypedParameter frictionCoeff = TypedParameter.of(0.05);
    
    public Double getBallMassValue() {
        return ballMass != null ? ballMass.getAsDouble() : 0.027;
    }
    
    public Double getBallRadiusValue() {
        return ballRadius != null ? ballRadius.getAsDouble() : 0.02;
    }
    
    public Double getPlateSizeValue() {
        return plateSize != null ? plateSize.getAsDouble() : 0.42;
    }
    
    public Double getGravityValue() {
        return gravity != null ? gravity.getAsDouble() : 9.81;
    }
    
    public Double getFrictionCoeffValue() {
        return frictionCoeff != null ? frictionCoeff.getAsDouble() : 0.05;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("BallMass", ballMass)
                .put("BallRadius", ballRadius)
                .put("PlateSize", plateSize)
                .put("Gravity", gravity)
                .put("FrictionCoeff", frictionCoeff)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}