package com.ncslab.dto.block.specialized.machineLearning.pt;

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
 * DTO representation of CNN block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("CNN")
@MigrationCompatible(originalClass = "com.ncslab.block.machineLearning.pt.CNN")
public class CNNDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter modelPath = TypedParameter.of("");
    @Builder.Default
    private TypedParameter inputChannels = TypedParameter.of(1);
    @Builder.Default
    private TypedParameter outputClasses = TypedParameter.of(10);
    @Builder.Default
    private TypedParameter kernelSize = TypedParameter.of(3);
    @Builder.Default
    private TypedParameter hiddenLayers = TypedParameter.of("[64, 128]");
    
    public String getModelPathValue() {
        return modelPath != null ? modelPath.getAsString() : "";
    }
    
    public Integer getInputChannelsValue() {
        return inputChannels != null ? inputChannels.getAsInteger() : 1;
    }
    
    public Integer getOutputClassesValue() {
        return outputClasses != null ? outputClasses.getAsInteger() : 10;
    }
    
    public Integer getKernelSizeValue() {
        return kernelSize != null ? kernelSize.getAsInteger() : 3;
    }
    
    public String getHiddenLayersValue() {
        return hiddenLayers != null ? hiddenLayers.getAsString() : "[64, 128]";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ModelPath", modelPath)
                .put("InputChannels", inputChannels)
                .put("OutputClasses", outputClasses)
                .put("KernelSize", kernelSize)
                .put("HiddenLayers", hiddenLayers)
                .build();
    }
}