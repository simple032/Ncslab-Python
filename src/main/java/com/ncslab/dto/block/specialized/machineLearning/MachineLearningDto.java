package com.ncslab.dto.block.specialized.machineLearning;

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
 * DTO representation of MachineLearning block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("MachineLearning")
@MigrationCompatible(originalClass = "com.ncslab.block.machineLearning.MachineLearning")
public class MachineLearningDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter modelPath = TypedParameter.of("");
    @Builder.Default
    private TypedParameter framework = TypedParameter.of("PyTorch");
    @Builder.Default
    private TypedParameter inputDimensions = TypedParameter.of("[1]");
    @Builder.Default
    private TypedParameter outputDimensions = TypedParameter.of("[1]");
    
    public String getModelPathValue() {
        return modelPath != null ? modelPath.getAsString() : "";
    }
    
    public String getFrameworkValue() {
        return framework != null ? framework.getAsString() : "PyTorch";
    }
    
    public String getInputDimensionsValue() {
        return inputDimensions != null ? inputDimensions.getAsString() : "[1]";
    }
    
    public String getOutputDimensionsValue() {
        return outputDimensions != null ? outputDimensions.getAsString() : "[1]";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ModelPath", modelPath)
                .put("Framework", framework)
                .put("InputDimensions", inputDimensions)
                .put("OutputDimensions", outputDimensions)
                .build();
    }
}