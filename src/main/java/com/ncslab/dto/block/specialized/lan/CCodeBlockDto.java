package com.ncslab.dto.block.specialized.lan;

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
 * DTO representation of CCodeBlock block.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("CCodeBlock")
@MigrationCompatible(originalClass = "com.ncslab.block.lan.CCodeBlock")
public class CCodeBlockDto extends BlockDto {
    
    @Builder.Default
    private TypedParameter codeText = TypedParameter.of("// C code here");
    @Builder.Default
    private TypedParameter headerFiles = TypedParameter.of("");
    @Builder.Default
    private TypedParameter libraryFiles = TypedParameter.of("");
    
    public String getCodeTextValue() {
        return codeText != null ? codeText.getAsString() : "// C code here";
    }
    
    public String getHeaderFilesValue() {
        return headerFiles != null ? headerFiles.getAsString() : "";
    }
    
    public String getLibraryFilesValue() {
        return libraryFiles != null ? libraryFiles.getAsString() : "";
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("CodeText", codeText)
                .put("HeaderFiles", headerFiles)
                .put("LibraryFiles", libraryFiles)
                .build();
    }
}