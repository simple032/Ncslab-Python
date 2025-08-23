package com.ncslab.dto.block.specialized.lookupTable;

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
 * DTO representation of DynamicLookupTableBlock.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DynamicLookupTableBlock")
@MigrationCompatible(originalClass = "com.ncslab.block.lookupTable.DynamicLookupTableBlock")
public class DynamicLookupTableBlockDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as first input");
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as first input";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }
}