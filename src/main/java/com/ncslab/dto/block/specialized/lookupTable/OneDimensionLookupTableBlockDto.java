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
 * DTO representation of OneDimensionLookupTableBlock.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("OneDimensionLookupTableBlock")
@MigrationCompatible(originalClass = "com.ncslab.block.lookupTable.OneDimensionLookupTableBlock")
public class OneDimensionLookupTableBlockDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter inputValues = TypedParameter.of("[0,1,2,3,4,5]");
    @Builder.Default
    private TypedParameter outputValues = TypedParameter.of("[0,1,4,9,16,25]");
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as first input");
    
    public String getInputValuesValue() {
        return inputValues != null ? inputValues.getAsString() : "[0,1,2,3,4,5]";
    }
    
    public String getOutputValuesValue() {
        return outputValues != null ? outputValues.getAsString() : "[0,1,4,9,16,25]";
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as first input";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("InputValues", inputValues)
                .put("OutputValues", outputValues)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }
}