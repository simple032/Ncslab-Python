package com.ncslab.dto.block.specialized.machineLearning.pt;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

/**
 * DTO representation of PTModel block.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("PTModel")
@MigrationCompatible(originalClass = "com.ncslab.block.machineLearning.pt.PTModel")
public class PTModelDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter loadPath = TypedParameter.of("None");
    @Builder.Default
    private TypedParameter savePath = TypedParameter.of("None");
    
    public String getLoadPathValue() {
        return loadPath != null ? loadPath.getAsString() : "None";
    }
    
    public String getSavePathValue() {
        return savePath != null ? savePath.getAsString() : "None";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("loadPath", loadPath)
                .put("savePath", savePath)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
}