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
 * DTO representation of WaterLevel block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("WaterLevel")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.WaterLevel")
public class WaterLevelDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter tankArea = TypedParameter.of(100.0);
    @Builder.Default
    private TypedParameter outletCoeff = TypedParameter.of(0.5);
    @Builder.Default
    private TypedParameter gravity = TypedParameter.of(9.81);
    
    public Double getTankAreaValue() {
        return tankArea != null ? tankArea.getAsDouble() : 100.0;
    }
    
    public Double getOutletCoeffValue() {
        return outletCoeff != null ? outletCoeff.getAsDouble() : 0.5;
    }
    
    public Double getGravityValue() {
        return gravity != null ? gravity.getAsDouble() : 9.81;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("TankArea", tankArea)
                .put("OutletCoeff", outletCoeff)
                .put("Gravity", gravity)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}