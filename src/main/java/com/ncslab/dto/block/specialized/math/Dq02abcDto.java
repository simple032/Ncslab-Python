package com.ncslab.dto.block.specialized.math;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for dq0 to abc coordinate transformation block.
 * Converts rotating dq0 reference frame to three-phase abc coordinates.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Dq02abcDto extends BlockDto {
    
    /**
     * Alignment of the a-phase with respect to the d-axis.
     * Default: "cos"
     */
    private TypedParameter alignment;
    
    /**
     * Whether to include zero-sequence component.
     * Default: true
     */
    private TypedParameter includeZeroSequence;
    
    public Dq02abcDto(String blockName, String blockPath) {
        super("dq02abc", blockName, blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (alignment == null) {
            alignment = TypedParameter.of("cos");
        }
        if (includeZeroSequence == null) {
            includeZeroSequence = TypedParameter.of(true);
        }
    }
}