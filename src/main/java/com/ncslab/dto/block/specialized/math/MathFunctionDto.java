package com.ncslab.dto.block.specialized.math;

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
 * DTO representation of MathFunction block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Math Function")
@MigrationCompatible(originalClass = "com.ncslab.block.math.MathFunction")
public class MathFunctionDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter functionType = TypedParameter.of("sin");
    
    public String getFunctionTypeValue() {
        return functionType != null ? functionType.getAsString() : "sin";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("FunctionType", functionType)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}