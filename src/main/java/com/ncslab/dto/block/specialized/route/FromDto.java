package com.ncslab.dto.block.specialized.route;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for From block.
 * Receives signal from a corresponding Goto block via a tag name.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FromDto extends BlockDto {
    
    /**
     * Goto tag to receive signal from.
     * This must match a Goto block's tag in the model.
     */
    private TypedParameter gotoTag;
    
    /**
     * Tag visibility.
     * Options: "local", "scoped", "global"
     * Default: "scoped"
     */
    private TypedParameter tagVisibility;
    
    public FromDto(String blockName, String blockPath) {
        super(blockName,blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (gotoTag == null) {
            gotoTag = TypedParameter.of("");
        }
        if (tagVisibility == null) {
            tagVisibility = TypedParameter.of("scoped");
        }
    }
}