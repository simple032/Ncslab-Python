package com.ncslab.dto.block.specialized.route;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for Goto block.
 * Sends signal to corresponding From blocks via a tag name.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GotoDto extends BlockDto {
    
    /**
     * Goto tag name for signal routing.
     * From blocks use this tag to receive the signal.
     */
    private TypedParameter gotoTag;
    
    /**
     * Tag visibility.
     * Options: "local", "scoped", "global"
     * Default: "scoped"
     */
    private TypedParameter tagVisibility;
    
    public GotoDto(String blockName, String blockPath) {
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