package com.ncslab.dto.block.specialized.matrix;

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
 * DTO representation of MatrixConcatenate block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("MatrixConcatenate")
@MigrationCompatible(originalClass = "com.ncslab.block.matrix.MatrixConcatenate")
public class MatrixConcatenateDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter concatenationDimension = TypedParameter.of(1);
    @Builder.Default
    private TypedParameter numInputs = TypedParameter.of(2);
    
    public Integer getConcatenationDimensionValue() {
        return concatenationDimension != null ? concatenationDimension.getAsInteger() : 1;
    }
    
    public Integer getNumInputsValue() {
        return numInputs != null ? numInputs.getAsInteger() : 2;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ConcatenationDimension", concatenationDimension)
                .put("NumInputs", numInputs)
                .build();
    }
}