package com.ncslab.dto.block.specialized.continuous;

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
 * DTO representation of OldPIDController block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("PIDController")
@MigrationCompatible(originalClass = "com.ncslab.block.continuous.OldPIDController")
public class OldPIDControllerDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter proportionalGain = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter integralGain = TypedParameter.of(1.0);
    @Builder.Default
    private TypedParameter derivativeGain = TypedParameter.of(0.0);    
    
    public Double getProportionalGainValue() {
        return proportionalGain != null ? proportionalGain.getAsDouble() : 1.0;
    }
    
    public Double getIntegralGainValue() {
        return integralGain != null ? integralGain.getAsDouble() : 1.0;
    }
    
    public Double getDerivativeGainValue() {
        return derivativeGain != null ? derivativeGain.getAsDouble() : 0.0;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ProportionalGain", proportionalGain)
                .put("IntegralGain", integralGain)
                .put("DerivativeGain", derivativeGain)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}