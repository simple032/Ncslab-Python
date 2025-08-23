package com.ncslab.dto.block.specialized.math;

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
 * DTO representation of TrigFunction block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("TrigFunction")
@MigrationCompatible(originalClass = "com.ncslab.block.math.TrigFunction")
public class TrigFunctionDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter functionType = TypedParameter.of("sin");
    @Builder.Default
    private TypedParameter outputSignalType = TypedParameter.of("auto");
    @Builder.Default
    private TypedParameter approximationMethod = TypedParameter.of("CORDIC");
    
    public String getFunctionTypeValue() {
        return functionType != null ? functionType.getAsString() : "sin";
    }
    
    public String getOutputSignalTypeValue() {
        return outputSignalType != null ? outputSignalType.getAsString() : "auto";
    }
    
    public String getApproximationMethodValue() {
        return approximationMethod != null ? approximationMethod.getAsString() : "CORDIC";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("FunctionType", functionType)
                .put("OutputSignalType", outputSignalType)
                .put("ApproximationMethod", approximationMethod)
                .build();
    }
}