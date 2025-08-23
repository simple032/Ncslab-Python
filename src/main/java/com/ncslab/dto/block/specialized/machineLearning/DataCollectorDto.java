package com.ncslab.dto.block.specialized.machineLearning;

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
 * DTO representation of DataCollector block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DataCollector")
@MigrationCompatible(originalClass = "com.ncslab.block.machineLearning.DataCollector")
public class DataCollectorDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter fileName = TypedParameter.of("data.csv");
    @Builder.Default
    private TypedParameter bufferSize = TypedParameter.of(1000);
    
    public String getFileNameValue() {
        return fileName != null ? fileName.getAsString() : "data.csv";
    }
    
    public Integer getBufferSizeValue() {
        return bufferSize != null ? bufferSize.getAsInteger() : 1000;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("FileName", fileName)
                .put("BufferSize", bufferSize)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}