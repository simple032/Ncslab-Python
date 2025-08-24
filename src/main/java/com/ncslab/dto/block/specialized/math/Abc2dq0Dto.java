package com.ncslab.dto.block.specialized.math;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for abc to dq0 coordinate transformation block.
 * Converts three-phase abc coordinates to rotating dq0 reference frame.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Abc2dq0Dto extends BlockDto {
    
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
    
    public Abc2dq0Dto(String blockName, String blockPath) {
        super(blockName,blockPath);
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