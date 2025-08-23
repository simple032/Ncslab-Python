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
 * DTO representation of BallBeamSystem block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("BallBeamSystem")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.BallBeamSystem")
public class BallBeamSystemDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter ballMass = TypedParameter.of(0.111);
    @Builder.Default
    private TypedParameter ballRadius = TypedParameter.of(0.015);
    @Builder.Default
    private TypedParameter beamLength = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter gravity = TypedParameter.of(9.81);
    @Builder.Default
    private TypedParameter leverArm = TypedParameter.of(0.03);
    @Builder.Default
    private TypedParameter frictionCoeff = TypedParameter.of(0.05);
    
    public Double getBallMassValue() {
        return ballMass != null ? ballMass.getAsDouble() : 0.111;
    }
    
    public Double getBallRadiusValue() {
        return ballRadius != null ? ballRadius.getAsDouble() : 0.015;
    }
    
    public Double getBeamLengthValue() {
        return beamLength != null ? beamLength.getAsDouble() : 1.0;
    }
    
    public Double getGravityValue() {
        return gravity != null ? gravity.getAsDouble() : 9.81;
    }
    
    public Double getLeverArmValue() {
        return leverArm != null ? leverArm.getAsDouble() : 0.03;
    }
    
    public Double getFrictionCoeffValue() {
        return frictionCoeff != null ? frictionCoeff.getAsDouble() : 0.05;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("BallMass", ballMass)
                .put("BallRadius", ballRadius)
                .put("BeamLength", beamLength)
                .put("Gravity", gravity)
                .put("LeverArm", leverArm)
                .put("FrictionCoeff", frictionCoeff)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}