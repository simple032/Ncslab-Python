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
 * DTO representation of InvertedPendulum block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("InvertedPendulum")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.InvertedPendulum")
public class InvertedPendulumDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter cartMass = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter pendulumMass = TypedParameter.of(0.1);
    @Builder.Default
    private TypedParameter pendulumLength = TypedParameter.of(0.5);
    @Builder.Default
    private TypedParameter gravity = TypedParameter.of(9.81);
    @Builder.Default
    private TypedParameter friction = TypedParameter.of(0.1);
    
    public Double getCartMassValue() {
        return cartMass != null ? cartMass.getAsDouble() : 1.0;
    }
    
    public Double getPendulumMassValue() {
        return pendulumMass != null ? pendulumMass.getAsDouble() : 0.1;
    }
    
    public Double getPendulumLengthValue() {
        return pendulumLength != null ? pendulumLength.getAsDouble() : 0.5;
    }
    
    public Double getGravityValue() {
        return gravity != null ? gravity.getAsDouble() : 9.81;
    }
    
    public Double getFrictionValue() {
        return friction != null ? friction.getAsDouble() : 0.1;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("CartMass", cartMass)
                .put("PendulumMass", pendulumMass)
                .put("PendulumLength", pendulumLength)
                .put("Gravity", gravity)
                .put("Friction", friction)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}