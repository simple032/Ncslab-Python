package com.ncslab.dto.block.specialized.machineLearning.pt;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.block.specialized.machineLearning.MachineLearningDto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO representation of LinearRegression block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("LinearRegression")
@MigrationCompatible(originalClass = "com.ncslab.block.machineLearning.pt.LinearRegression")
public class LinearRegressionDto extends PTModelDto {
    
    @Builder.Default
    private TypedParameter modelPath = TypedParameter.of("");
    @Builder.Default
    private TypedParameter inputFeatures = TypedParameter.of(1);
    @Builder.Default
    private TypedParameter outputFeatures = TypedParameter.of(1);
    @Builder.Default
    private TypedParameter learningRate = TypedParameter.of(0.01);
    
    public String getModelPathValue() {
        return modelPath != null ? modelPath.getAsString() : "";
    }
    
    public Integer getInputFeaturesValue() {
        return inputFeatures != null ? inputFeatures.getAsInteger() : 1;
    }
    
    public Integer getOutputFeaturesValue() {
        return outputFeatures != null ? outputFeatures.getAsInteger() : 1;
    }
    
    public Double getLearningRateValue() {
        return learningRate != null ? learningRate.getAsDouble() : 0.01;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ModelPath", modelPath)
                .put("InputFeatures", inputFeatures)
                .put("OutputFeatures", outputFeatures)
                .put("LearningRate", learningRate)
                .build();
    }
}