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
 * DTO representation of RotaryInvertedPendulum block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("RotaryInvertedPendulum")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.RotaryInvertedPendulum")
public class RotaryInvertedPendulumDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter armMass = TypedParameter.of(0.095);
    @Builder.Default
    private TypedParameter pendulumMass = TypedParameter.of(0.024);
    @Builder.Default
    private TypedParameter armLength = TypedParameter.of(0.085);
    @Builder.Default
    private TypedParameter pendulumLength = TypedParameter.of(0.129);
    @Builder.Default
    private TypedParameter gravity = TypedParameter.of(9.81);
    
    public Double getArmMassValue() {
        return armMass != null ? armMass.getAsDouble() : 0.095;
    }
    
    public Double getPendulumMassValue() {
        return pendulumMass != null ? pendulumMass.getAsDouble() : 0.024;
    }
    
    public Double getArmLengthValue() {
        return armLength != null ? armLength.getAsDouble() : 0.085;
    }
    
    public Double getPendulumLengthValue() {
        return pendulumLength != null ? pendulumLength.getAsDouble() : 0.129;
    }
    
    public Double getGravityValue() {
        return gravity != null ? gravity.getAsDouble() : 9.81;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ArmMass", armMass)
                .put("PendulumMass", pendulumMass)
                .put("ArmLength", armLength)
                .put("PendulumLength", pendulumLength)
                .put("Gravity", gravity)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}