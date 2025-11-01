package com.ncslab.dto.block.specialized.string;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for StringToDouble block.
 *
 * Converts a string to a double numeric value.
 *
 * Block Behavior:
 * - Input: String signal (text representation of number)
 * - Output: Double/real numeric value
 * - No parameters required
 * - Uses standard string-to-double conversion
 * - Invalid strings result in NaN output
 *
 * Examples:
 * - Input="3.14159" → Output=3.14159
 * - Input="42" → Output=42.0
 * - Input="-2.5e3" → Output=-2500.0
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
@JsonTypeName("StringToDouble")
public class StringToDoubleDto extends BlockDto {

    @Override
    public String getBlockType() {
        return "StringToDouble";
    }
}
