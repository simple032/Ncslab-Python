package com.ncslab.dto.block.specialized.lan;

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
 * DTO representation of MCodeBlock block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("MCodeBlock")
@MigrationCompatible(originalClass = "com.ncslab.block.lan.MCodeBlock")
public class MCodeBlockDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter matlabCode = TypedParameter.of("% MATLAB code here");
    @Builder.Default
    private TypedParameter functionName = TypedParameter.of("");
    @Builder.Default
    private TypedParameter inputVariables = TypedParameter.of("u");
    @Builder.Default
    private TypedParameter outputVariables = TypedParameter.of("y");
    
    public String getMatlabCodeValue() {
        return matlabCode != null ? matlabCode.getAsString() : "% MATLAB code here";
    }
    
    public String getFunctionNameValue() {
        return functionName != null ? functionName.getAsString() : "";
    }
    
    public String getInputVariablesValue() {
        return inputVariables != null ? inputVariables.getAsString() : "u";
    }
    
    public String getOutputVariablesValue() {
        return outputVariables != null ? outputVariables.getAsString() : "y";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("MatlabCode", matlabCode)
                .put("FunctionName", functionName)
                .put("InputVariables", inputVariables)
                .put("OutputVariables", outputVariables)
                .build();
    }
}