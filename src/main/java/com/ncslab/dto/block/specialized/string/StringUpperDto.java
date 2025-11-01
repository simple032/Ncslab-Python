package com.ncslab.dto.block.specialized.string;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

/**
 * DTO for StringUpper block.
 *
 * Converts all characters in a string to uppercase.
 *
 * Block Behavior:
 * - Input: String signal
 * - Output: String signal with all characters in uppercase
 * - No parameters required
 *
 * Examples:
 * - Input="Hello World" → Output="HELLO WORLD"
 * - Input="test123" → Output="TEST123"
 *
 * @author NCSLab Team
 * @version 1.0
 * @since Phase 4 String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("StringUpper")
public class StringUpperDto extends BlockDto {

    @Override
    public String getBlockType() {
        return "StringUpper";
    }
}
