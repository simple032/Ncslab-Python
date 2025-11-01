package com.ncslab.dto.block.specialized.string;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object for StringFind block.
 *
 * Searches for pattern in string and returns 1-based index. No parameters required.
 *
 * Block Behavior:
 * - Input 1: String to search in
 * - Input 2: Pattern to search for
 * - Output: int32 index (1-based, 0 if not found)
 * - Example: "HelloWorld", "World" → 6
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("StringFind")
public class StringFindDto extends BlockDto {

    @Override
    public String getBlockType() {
        return "StringFind";
    }

}
