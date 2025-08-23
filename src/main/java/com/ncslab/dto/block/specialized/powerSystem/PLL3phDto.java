package com.ncslab.dto.block.specialized.powerSystem;

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
 * DTO representation of pll3ph block (3-phase phase-locked loop).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("pll3ph")
@MigrationCompatible(originalClass = "com.ncslab.block.powerSystem.pll3ph")
public class PLL3phDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter nominalFrequency = TypedParameter.of(50.0);
    @Builder.Default
    private TypedParameter kp = TypedParameter.of(100.0);
    @Builder.Default
    private TypedParameter ki = TypedParameter.of(1000.0);
    
    public Double getNominalFrequencyValue() {
        return nominalFrequency != null ? nominalFrequency.getAsDouble() : 50.0;
    }
    
    public Double getKpValue() {
        return kp != null ? kp.getAsDouble() : 100.0;
    }
    
    public Double getKiValue() {
        return ki != null ? ki.getAsDouble() : 1000.0;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("NominalFrequency", nominalFrequency)
                .put("Kp", kp)
                .put("Ki", ki)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}