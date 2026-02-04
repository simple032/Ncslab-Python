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
 * DTO representation of NewMotor block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("NewMotor")
@MigrationCompatible(originalClass = "com.ncslab.block.testrig.NewMotor")
public class HGGenerator12Dto extends BlockDto {
    
    @Builder.Default
    private TypedParameter motorK = TypedParameter.of(0.01);
    @Builder.Default
    private TypedParameter motorT = TypedParameter.of(0.09);
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    
    public Double getMotorKValue() {
        return motorK != null ? motorK.getAsDouble() : 0.01;
    }
    
    public Double getMotorTValue() {
        return motorT != null ? motorT.getAsDouble() : 0.09;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("motorK", motorK)
                .put("motorT", motorT)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }
}