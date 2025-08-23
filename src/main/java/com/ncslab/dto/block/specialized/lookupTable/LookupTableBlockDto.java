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
import lombok.NoArgsConstructor;

/**
 * DTO representation of LookupTableBlock.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("LookupTableBlock")
@MigrationCompatible(originalClass = "com.ncslab.block.lookupTable.LookupTableBlock")
public class LookupTableBlockDto extends BlockDto {
    
    private TypedParameter tableData;
    @Builder.Default
    private TypedParameter interpolationMethod = TypedParameter.of("Linear");
    @Builder.Default
    private TypedParameter extrapolationMethod = TypedParameter.of("Clip");
    
    public String getTableDataValue() {
        return tableData != null ? tableData.getAsString() : "[0, 1; 0, 1]";
    }
    
    public String getInterpolationMethodValue() {
        return interpolationMethod != null ? interpolationMethod.getAsString() : "Linear";
    }
    
    public String getExtrapolationMethodValue() {
        return extrapolationMethod != null ? extrapolationMethod.getAsString() : "Clip";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("TableData", tableData)
                .put("InterpolationMethod", interpolationMethod)
                .put("ExtrapolationMethod", extrapolationMethod)
                .build();
    }
}