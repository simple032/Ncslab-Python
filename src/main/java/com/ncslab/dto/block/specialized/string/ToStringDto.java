package com.ncslab.dto.block.specialized.string;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object for ToString block.
 *
 * Converts numeric value to string signal. No parameters required.
 *
 * Block Behavior:
 * - Input: Numeric scalar (any numeric type)
 * - Output: String representation
 * - Example: 123 → "123", 3.14 → "3.14"
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ToString")
public class ToStringDto extends BlockDto {

    @Override
    public String getBlockType() {
        return "ToString";
    }

    /**
     * Validates ToString-specific parameters (none required).
     *
     * @throws IllegalArgumentException if validation fails
     */
}
