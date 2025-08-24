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
import lombok.Builder;

/**
 * DTO representation of LogisticRegression block.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("LogisticRegression")
@MigrationCompatible(originalClass = "com.ncslab.block.machineLearning.pt.LogisticRegression")
public class LogisticRegressionDto extends PTModelDto {
    
    @Builder.Default
    private TypedParameter inputFeatures = TypedParameter.of(1);
    @Builder.Default
    private TypedParameter outputFeatures = TypedParameter.of(1);
    @Builder.Default
    private TypedParameter learningRate = TypedParameter.of(0.001);
    @Builder.Default
    private TypedParameter loadPath = TypedParameter.of("None");
    @Builder.Default
    private TypedParameter savePath = TypedParameter.of("None");
    
    public Integer getInputFeaturesValue() {
        return inputFeatures != null ? inputFeatures.getAsInteger() : 1;
    }
    
    public Integer getOutputFeaturesValue() {
        return outputFeatures != null ? outputFeatures.getAsInteger() : 1;
    }
    
    public Double getLearningRateValue() {
        return learningRate != null ? learningRate.getAsDouble() : 0.001;
    }
    
    public String getLoadPathValue() {
        return loadPath != null ? loadPath.getAsString() : "None";
    }
    
    public String getSavePathValue() {
        return savePath != null ? savePath.getAsString() : "None";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("inputFeatures", inputFeatures)
                .put("outputFeatures", outputFeatures)
                .put("learningRate", learningRate)
                .put("loadPath", loadPath)
                .put("savePath", savePath)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}