package com.ncslab.dto.block.specialized.string;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for StringToSingle block.
 *
 * Converts a string to a single/float numeric value.
 *
 * Block Behavior:
 * - Input: String signal (text representation of number)
 * - Output: Single/float numeric value (32-bit precision)
 * - No parameters required
 * - Uses standard string-to-float conversion
 * - Invalid strings result in NaN output
 *
 * Examples:
 * - Input="3.14" → Output=3.14f
 * - Input="42" → Output=42.0f
 * - Input="-2.5" → Output=-2.5f
 * - Input="invalid" → Output=NaN
 *
 * @author NCSLab Team
 * @version 1.0
 * @since Phase 3 String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("StringToSingle")
public class StringToSingleDto extends BlockDto {

    @Override
    public String getBlockType() {
        return "StringToSingle";
    }
}
