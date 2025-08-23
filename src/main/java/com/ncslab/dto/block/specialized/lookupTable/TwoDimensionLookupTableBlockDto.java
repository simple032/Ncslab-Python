package com.ncslab.dto.block.specialized.lookupTable;

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
 * DTO representation of TwoDimensionLookupTableBlock.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("TwoDimensionLookupTableBlock")
@MigrationCompatible(originalClass = "com.ncslab.block.lookupTable.TwoDimensionLookupTableBlock")
public class TwoDimensionLookupTableBlockDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter rowIndexValues = TypedParameter.of("[0,1,2]");
    @Builder.Default
    private TypedParameter columnIndexValues = TypedParameter.of("[0,1,2]");
    @Builder.Default
    private TypedParameter outputValues = TypedParameter.of("[[0,1,2],[3,4,5],[6,7,8]]");
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as first input");
    
    public String getRowIndexValuesValue() {
        return rowIndexValues != null ? rowIndexValues.getAsString() : "[0,1,2]";
    }
    
    public String getColumnIndexValuesValue() {
        return columnIndexValues != null ? columnIndexValues.getAsString() : "[0,1,2]";
    }
    
    public String getOutputValuesValue() {
        return outputValues != null ? outputValues.getAsString() : "[[0,1,2],[3,4,5],[6,7,8]]";
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as first input";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("RowIndexValues", rowIndexValues)
                .put("ColumnIndexValues", columnIndexValues)
                .put("OutputValues", outputValues)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }
}