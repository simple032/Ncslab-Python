package com.ncslab.dto.block.specialized.source;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for Repeating Sequence source block.
 * Outputs a repeating sequence of values at specified time points.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RepeatingSequenceDto extends BlockDto {
    
    /**
     * Time values for the sequence points.
     * Default: [0 1]
     */
    private TypedParameter timeValues;
    
    /**
     * Output values corresponding to time points.
     * Default: [0 1]
     */
    private TypedParameter outputValues;
    
    public RepeatingSequenceDto(String blockName, String blockPath) {
        super(blockName,blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (timeValues == null) {
            timeValues = TypedParameter.of("[0 1]");
        }
        if (outputValues == null) {
            outputValues = TypedParameter.of("[0 1]");
        }
    }
}