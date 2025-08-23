package com.ncslab.dto.block.specialized.matrix;

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
 * DTO representation of Submatrix block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Submatrix")
@MigrationCompatible(originalClass = "com.ncslab.block.matrix.Submatrix")
public class SubmatrixDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter startRow = TypedParameter.of("1");
    @Builder.Default
    private TypedParameter endRow = TypedParameter.of("1");
    @Builder.Default
    private TypedParameter startColumn = TypedParameter.of("1");
    @Builder.Default
    private TypedParameter endColumn = TypedParameter.of("1");
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of("off");
    
    public String getStartRowValue() {
        return startRow != null ? startRow.getAsString() : "1";
    }
    
    public String getEndRowValue() {
        return endRow != null ? endRow.getAsString() : "1";
    }
    
    public String getStartColumnValue() {
        return startColumn != null ? startColumn.getAsString() : "1";
    }
    
    public String getEndColumnValue() {
        return endColumn != null ? endColumn.getAsString() : "1";
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public String getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsString() : "off";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("StartRow", startRow)
                .put("EndRow", endRow)
                .put("StartColumn", startColumn)
                .put("EndColumn", endColumn)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
}