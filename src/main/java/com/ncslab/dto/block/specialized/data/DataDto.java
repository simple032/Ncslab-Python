package com.ncslab.dto.block.specialized.data;

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
 * DTO representation of Data block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Data")
@MigrationCompatible(originalClass = "com.ncslab.block.data.Data")
public class DataDto extends BlockDto {

    @Builder.Default
    private TypedParameter dataValue = TypedParameter.of(0.0);
    @Builder.Default
    private TypedParameter dataType = TypedParameter.of("double");
    
    public Double getDataValueAsDouble() {
        return dataValue != null ? dataValue.getAsDouble() : 0.0;
    }
    
    public String getDataTypeValue() {
        return dataType != null ? dataType.getAsString() : "double";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("DataValue", dataValue)
                .put("DataType", dataType)
                .build();
    }
}