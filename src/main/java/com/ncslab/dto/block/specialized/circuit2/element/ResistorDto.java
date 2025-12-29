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
@AllArgsConstructor
public class ResistorDto extends CircuitBlockDto {

    /**
     * Resistance value in Ohms (Ω)
     * Must be positive and non-zero
     */
    @JsonProperty("R")
    private TypedParameter resistanceValue;

    /**
     * Minimum allowed resistance value (1 micro-ohm)
     */
    public static final double MIN_RESISTANCE = 1e-6;

    /**
     * Maximum allowed resistance value (1 tera-ohm)
     */
    public static final double MAX_RESISTANCE = 1e12;

    /**
     * Create a resistor DTO with specified resistance
     * @param blockName Block name
     * @param blockPath Block path
     * @param resistance Resistance value in Ohms
     * @return ResistorDto instance
     */
    public static ResistorDto create(String blockName, String blockPath, double resistance) {
        ResistorDto dto = ResistorDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .resistanceValue(TypedParameter.of(resistance))
            .elementType("Resistor")
            .circuitType("element")
            .blockModeType("Anything")
            .build();

        // Set the resistance in the base class field as well
        dto.setResistance(resistance);

        return dto;
    }

    /**
     * Create a resistor DTO with specified resistance (string format)
     * @param blockName Block name
     * @param blockPath Block path
     * @param resistance Resistance value as string (supports expressions)
     * @return ResistorDto instance
     */
    public static ResistorDto create(String blockName, String blockPath, String resistance) {
        ResistorDto dto = ResistorDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .resistanceValue(TypedParameter.of(resistance))
            .elementType("Resistor")
            .circuitType("element")
            .blockModeType("Anything")
            .build();

        // Try to parse and set numeric value
        try {
            double value = Double.parseDouble(resistance);
            dto.setResistance(value);
        } catch (NumberFormatException e) {
            // Leave as expression
        }

        return dto;
    }

    /**
     * Get resistance value as double
     * @return Resistance in Ohms
     */
    public double getResistanceValueAsDouble() {
        if (resistanceValue != null) {
            return resistanceValue.getAsDouble();
        }
        if (getResistance() != null) {
            return getResistance();
        }
        return 1000.0; // Default 1kΩ
    }

    /**
     * Get resistance value as string (for expressions)
     * @return Resistance as string
     */
    public String getResistanceValueAsString() {
        if (resistanceValue != null) {
            return resistanceValue.getAsString();
        }
        if (getResistance() != null) {
            return String.valueOf(getResistance());
        }
        return "1000.0"; // Default 1kΩ
    }

    /**
     * Validate resistor-specific parameters
     * @return ValidationResult with errors if invalid
     */
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate resistance value exists
        if (resistanceValue == null && getResistance() == null) {
            result.addError(new ValidationError(
                "Resistor.resistance",
                "Resistance value is required",
                "R",
                null
            ));
        }

        // Validate resistance value range (if numeric)
        try {
            double r = getResistanceValueAsDouble();
            if (r <= 0) {
                result.addError(new ValidationError(
                    "Resistor.resistance",
                    "Resistance must be positive",
                    "R",
                    r
                ));
            } else if (r < MIN_RESISTANCE) {
                result.addError(new ValidationError(
                    "Resistor.resistance",
                    String.format("Resistance too small (min: %.2e Ω)", MIN_RESISTANCE),
                    "R",
                    r
                ));
            } else if (r > MAX_RESISTANCE) {
                result.addError(new ValidationError(
                    "Resistor.resistance",
                    String.format("Resistance too large (max: %.2e Ω)", MAX_RESISTANCE),
                    "R",
                    r
                ));
            }
        } catch (NumberFormatException e) {
            // Non-numeric value (expression) - skip numeric validation
        }

        return result;
    }

    /**
     * Create parameter map for circuit simulation
     * @return Parameter map with resistance value
     */
    @Override
    public Map<String, Object> createCircuitParamValues() {
        Map<String, Object> params = super.createCircuitParamValues();

        if (resistanceValue != null) {
            params.put("R", resistanceValue.getAsString());
        } else if (getResistance() != null) {
            params.put("R", getResistance());
        }

        return params;
    }

    /**
     * Get human-readable description
     * @return Description string
     */
    public String getDescription() {
        return String.format("Resistor: %s Ω", getResistanceValueAsString());
    }
}
