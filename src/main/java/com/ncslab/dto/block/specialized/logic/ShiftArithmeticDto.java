package com.ncslab.dto.block.specialized.logic;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for Shift Arithmetic block.
 * Performs arithmetic bit shift operations on input signals.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ShiftArithmetic")
public class ShiftArithmeticDto extends BlockDto {
    
    /**
     * Shift direction.
     * Options: "Left", "Right"
     * Default: "Left"
     */
    private TypedParameter shiftDirection;
    
    /**
     * Number of bits to shift.
     * Default: 1
     */
    private TypedParameter numberOfBits;
    
    /**
     * Whether to perform arithmetic shift (sign extension).
     * Default: true
     */
    private TypedParameter arithmeticShift;
    
    public ShiftArithmeticDto(String blockName, String blockPath) {
        super(blockName,blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (shiftDirection == null) {
            shiftDirection = TypedParameter.of("Left");
        }
        if (numberOfBits == null) {
            numberOfBits = TypedParameter.of(1);
        }
        if (arithmeticShift == null) {
            arithmeticShift = TypedParameter.of(true);
        }
    }
}