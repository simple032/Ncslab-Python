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
 * DTO representation of IdentityMatrix block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("IdentityMatrix")
@MigrationCompatible(originalClass = "com.ncslab.block.matrix.IdentityMatrix")
public class IdentityMatrixDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter matrixSize = TypedParameter.of(3);
    
    public Integer getMatrixSizeValue() {
        return matrixSize != null ? matrixSize.getAsInteger() : 3;
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("MatrixSize", matrixSize)
                .build();
    }
}