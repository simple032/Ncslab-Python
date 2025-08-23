package com.ncslab.dto.block.specialized.ensembleModel;

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
 * DTO representation of EnsembleModel block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("EnsembleModel")
@MigrationCompatible(originalClass = "com.ncslab.block.ensembleModel.EnsembleModel")
public class EnsembleModelDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter modelFile = TypedParameter.of("");
    @Builder.Default
    private TypedParameter modelType = TypedParameter.of("Random Forest");
    @Builder.Default
    private TypedParameter inputDimensions = TypedParameter.of(1);
    @Builder.Default
    private TypedParameter outputDimensions = TypedParameter.of(1);
    
    public String getModelFileValue() {
        return modelFile != null ? modelFile.getAsString() : "";
    }
    
    public String getModelTypeValue() {
        return modelType != null ? modelType.getAsString() : "Random Forest";
    }
    
    public Integer getInputDimensionsValue() {
        return inputDimensions != null ? inputDimensions.getAsInteger() : 1;
    }
    
    public Integer getOutputDimensionsValue() {
        return outputDimensions != null ? outputDimensions.getAsInteger() : 1;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ModelFile", modelFile)
                .put("ModelType", modelType)
                .put("InputDimensions", inputDimensions)
                .put("OutputDimensions", outputDimensions)
                .build();
    }
}