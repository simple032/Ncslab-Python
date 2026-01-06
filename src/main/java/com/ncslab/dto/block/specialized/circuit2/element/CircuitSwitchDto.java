package com.ncslab.dto.block.specialized.circuit2.element;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncslab.dto.communication.CircuitBlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.mapper.validation.ValidationError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.HashMap;

/**
 * DTO for Resistor circuit element block.
 * Represents a resistor with configurable resistance value.
 * Can operate in both Branch mode (V to I) and Link mode (I to V).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class CircuitSwitchDto extends CircuitBlockDto {
	
    /**
     * Create a resistor DTO with specified resistance
     * @param blockName Block name
     * @param blockPath Block path
     * @param resistance Resistance value in Ohms
     * @return ResistorDto instance
     */
    public static CircuitSwitchDto create(String blockName, String blockPath) {
    	CircuitSwitchDto dto = CircuitSwitchDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .elementType("CircuitSwitch")
            .circuitType("element")
            .blockModeType("Anything")
            .build();


        return dto;
    }

    
}
