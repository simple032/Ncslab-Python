package com.ncslab.dto.block.specialized.io;

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
 * DTO representation of GlobalVariable block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("GlobalVariable")
@MigrationCompatible(originalClass = "com.ncslab.block.io.GlobalVariable")
public class GlobalVariableDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter variableName = TypedParameter.of("globalVar");
    @Builder.Default
    private TypedParameter initialValue = TypedParameter.of(0.0);
    @Builder.Default
    private TypedParameter dataType = TypedParameter.of("double");
    
    public String getVariableNameValue() {
        return variableName != null ? variableName.getAsString() : "globalVar";
    }
    
    public Double getInitialValueValue() {
        return initialValue != null ? initialValue.getAsDouble() : 0.0;
    }
    
    public String getDataTypeValue() {
        return dataType != null ? dataType.getAsString() : "double";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("VariableName", variableName)
                .put("InitialValue", initialValue)
                .put("DataType", dataType)
                .build();
    }
}