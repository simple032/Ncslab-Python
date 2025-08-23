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
 * DTO representation of DoubleTank block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DoubleTank")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.DoubleTank")
public class DoubleTankDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter tank1Area = TypedParameter.of(50.0);
    @Builder.Default
    private TypedParameter tank2Area = TypedParameter.of(50.0);
    @Builder.Default
    private TypedParameter outletArea = TypedParameter.of(0.5);
    @Builder.Default
    private TypedParameter connectionArea = TypedParameter.of(0.5);
    @Builder.Default
    private TypedParameter gravity = TypedParameter.of(9.81);
    @Builder.Default
    private TypedParameter maxInflow = TypedParameter.of(100.0);
    
    public Double getTank1AreaValue() {
        return tank1Area != null ? tank1Area.getAsDouble() : 50.0;
    }
    
    public Double getTank2AreaValue() {
        return tank2Area != null ? tank2Area.getAsDouble() : 50.0;
    }
    
    public Double getOutletAreaValue() {
        return outletArea != null ? outletArea.getAsDouble() : 0.5;
    }
    
    public Double getConnectionAreaValue() {
        return connectionArea != null ? connectionArea.getAsDouble() : 0.5;
    }
    
    public Double getGravityValue() {
        return gravity != null ? gravity.getAsDouble() : 9.81;
    }
    
    public Double getMaxInflowValue() {
        return maxInflow != null ? maxInflow.getAsDouble() : 100.0;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Tank1Area", tank1Area)
                .put("Tank2Area", tank2Area)
                .put("OutletArea", outletArea)
                .put("ConnectionArea", connectionArea)
                .put("Gravity", gravity)
                .put("MaxInflow", maxInflow)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}