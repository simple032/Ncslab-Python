package com.ncslab.dto.block.specialized.function;

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
 * DTO representation of SFunction block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("SFunction")
@MigrationCompatible(originalClass = "com.ncslab.block.function.SFunction")
public class SFunctionDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter functionName = TypedParameter.of("");
    @Builder.Default
    private TypedParameter sfunctionParameters = TypedParameter.of("");
    
    public String getFunctionNameValue() {
        return functionName != null ? functionName.getAsString() : "";
    }
    
    public String getSfunctionParametersValue() {
        return sfunctionParameters != null ? sfunctionParameters.getAsString() : "";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("FunctionName", functionName)
                .put("Parameters", sfunctionParameters)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}