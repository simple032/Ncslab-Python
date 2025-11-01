package com.ncslab.dto.block.specialized.string;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object for StringCompare block.
 *
 * Compares two strings for equality with optional case sensitivity.
 *
 * Block Behavior:
 * - Input 1: First string
 * - Input 2: Second string
 * - Output: Boolean (1.0 if equal, 0.0 if not equal)
 * - Parameter: CaseSensitive ("on" or "off", default: "on")
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("StringCompare")
public class StringCompareDto extends BlockDto {

    @Override
    public String getBlockType() {
        return "StringCompare";
    }

}
