package com.ncslab.dto.block.specialized.string;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object for ASCIIToString block.
 *
 * Converts uint8 vector to string signal.
 * Example: [72, 101, 108, 108, 111] → "Hello"
 *
 * This block has no parameters - it performs simple conversion.
 *
 * @author NCSLab Team
 * @version 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ASCIIToString")
public class ASCIIToStringDto extends BlockDto {
    // No specific parameters for ASCII to String conversion
    // The block simply converts uint8 vector input to string output

    @Override
    public String getBlockType() {
        return "ASCIIToString";
    }
}
