package com.ncslab.dto.block.specialized.elect;

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
 * DTO representation of Diode block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Diode")
@MigrationCompatible(originalClass = "com.ncslab.block.elect.Diode")
public class DiodeDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter forwardVoltage = TypedParameter.of(0.7);
    @Builder.Default
    private TypedParameter onResistance = TypedParameter.of(0.001);
    @Builder.Default
    private TypedParameter offConductance = TypedParameter.of(1e-12);
    
    public Double getForwardVoltageValue() {
        return forwardVoltage != null ? forwardVoltage.getAsDouble() : 0.7;
    }
    
    public Double getOnResistanceValue() {
        return onResistance != null ? onResistance.getAsDouble() : 0.001;
    }
    
    public Double getOffConductanceValue() {
        return offConductance != null ? offConductance.getAsDouble() : 1e-12;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ForwardVoltage", forwardVoltage)
                .put("OnResistance", onResistance)
                .put("OffConductance", offConductance)
                .build();
    }
}