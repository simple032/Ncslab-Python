package com.ncslab.dto.block.specialized.machineLearning.tf;

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
 * DTO representation of TFModel block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("TFModel")
@MigrationCompatible(originalClass = "com.ncslab.block.machineLearning.tf.TFModel")
public class TFModelDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter modelPath = TypedParameter.of("");
    @Builder.Default
    private TypedParameter inputTensorName = TypedParameter.of("input");
    @Builder.Default
    private TypedParameter outputTensorName = TypedParameter.of("output");
    @Builder.Default
    private TypedParameter inputShape = TypedParameter.of("[1]");
    @Builder.Default
    private TypedParameter outputShape = TypedParameter.of("[1]");
    
    public String getModelPathValue() {
        return modelPath != null ? modelPath.getAsString() : "";
    }
    
    public String getInputTensorNameValue() {
        return inputTensorName != null ? inputTensorName.getAsString() : "input";
    }
    
    public String getOutputTensorNameValue() {
        return outputTensorName != null ? outputTensorName.getAsString() : "output";
    }
    
    public String getInputShapeValue() {
        return inputShape != null ? inputShape.getAsString() : "[1]";
    }
    
    public String getOutputShapeValue() {
        return outputShape != null ? outputShape.getAsString() : "[1]";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ModelPath", modelPath)
                .put("InputTensorName", inputTensorName)
                .put("OutputTensorName", outputTensorName)
                .put("InputShape", inputShape)
                .put("OutputShape", outputShape)
                .build();
    }
}