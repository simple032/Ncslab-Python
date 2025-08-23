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
 * DTO representation of DCMotorAngle block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DCMotorAngle")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.DCMotorAngle")
public class DCMotorAngleDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter resistance = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter inductance = TypedParameter.of(0.5);
    @Builder.Default
    private TypedParameter momentOfInertia = TypedParameter.of(0.01);
    @Builder.Default
    private TypedParameter motorConstant = TypedParameter.of(0.01);
    @Builder.Default
    private TypedParameter backEmfConstant = TypedParameter.of(0.01);
    @Builder.Default
    private TypedParameter viscousFriction = TypedParameter.of(0.1);
    
    public Double getResistanceValue() {
        return resistance != null ? resistance.getAsDouble() : 1.0;
    }
    
    public Double getInductanceValue() {
        return inductance != null ? inductance.getAsDouble() : 0.5;
    }
    
    public Double getMomentOfInertiaValue() {
        return momentOfInertia != null ? momentOfInertia.getAsDouble() : 0.01;
    }
    
    public Double getMotorConstantValue() {
        return motorConstant != null ? motorConstant.getAsDouble() : 0.01;
    }
    
    public Double getBackEmfConstantValue() {
        return backEmfConstant != null ? backEmfConstant.getAsDouble() : 0.01;
    }
    
    public Double getViscousFrictionValue() {
        return viscousFriction != null ? viscousFriction.getAsDouble() : 0.1;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Resistance", resistance)
                .put("Inductance", inductance)
                .put("MomentOfInertia", momentOfInertia)
                .put("MotorConstant", motorConstant)
                .put("BackEmfConstant", backEmfConstant)
                .put("ViscousFriction", viscousFriction)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}