package com.ncslab.dto.block.specialized.string;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object for ScanString block.
 *
 * Parses a string into multiple outputs using scanf-style format.
 *
 * Block Behavior:
 * - Input: String signal (text to parse)
 * - Output 1..N: Numeric or string signals
 * - Parameters: Format (e.g., "Value: %d"), NumberOfOutputs
 * - Example: Input="Value: 42", Format="Value: %d" → Output=42
 *
 * @author NCSLab Team
 * @version 1.0
 * @since String Block Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ScanString")
public class ScanStringDto extends BlockDto {

    @Override
    public String getBlockType() {
        return "ScanString";
    }

}
