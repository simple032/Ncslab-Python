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

/**
 * DTO for Inductor circuit element block.
 * Represents an inductor with configurable inductance value.
 * Can only operate in Link mode (stores current, produces voltage).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InductorDto extends CircuitBlockDto {

    /**
     * Inductance value in Henries (H)
     * Must be positive and non-zero
     */
    @JsonProperty("l")
    private TypedParameter inductanceValue;

    /**
     * Initial current through inductor (A)
     */
    @JsonProperty("initialCurrent")
    private TypedParameter initialCurrent;

    /**
     * Minimum allowed inductance value (1 nano-henry)
     */
    public static final double MIN_INDUCTANCE = 1e-9;

    /**
     * Maximum allowed inductance value (1000 Henries)
     */
    public static final double MAX_INDUCTANCE = 1000.0;

    /**
     * Create an inductor DTO with specified inductance
     * @param blockName Block name
     * @param blockPath Block path
     * @param inductance Inductance value in Henries
     * @return InductorDto instance
     */
    public static InductorDto create(String blockName, String blockPath, double inductance) {
        return create(blockName, blockPath, inductance, 0.0);
    }

    /**
     * Create an inductor DTO with specified inductance and initial current
     * @param blockName Block name
     * @param blockPath Block path
     * @param inductance Inductance value in Henries
     * @param initialCurrent Initial current in Amperes
     * @return InductorDto instance
     */
    public static InductorDto create(String blockName, String blockPath, double inductance, double initialCurrent) {
        InductorDto dto = InductorDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .inductanceValue(TypedParameter.of(inductance))
            .initialCurrent(TypedParameter.of(initialCurrent))
            .elementType("Inductor")
            .circuitType("element")
            .blockModeType("LinkOnly")
            .build();

        // Set the inductance in the base class field as well
        dto.setInductance(inductance);

        return dto;
    }

    /**
     * Create an inductor DTO with specified inductance (string format)
     * @param blockName Block name
     * @param blockPath Block path
     * @param inductance Inductance value as string (supports expressions)
     * @return InductorDto instance
     */
    public static InductorDto create(String blockName, String blockPath, String inductance) {
        return create(blockName, blockPath, inductance, "0");
    }

    /**
     * Create an inductor DTO with specified inductance and initial current (string format)
     * @param blockName Block name
     * @param blockPath Block path
     * @param inductance Inductance value as string
     * @param initialCurrent Initial current as string
     * @return InductorDto instance
     */
    public static InductorDto create(String blockName, String blockPath, String inductance, String initialCurrent) {
        InductorDto dto = InductorDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .inductanceValue(TypedParameter.of(inductance))
            .initialCurrent(TypedParameter.of(initialCurrent))
            .elementType("Inductor")
            .circuitType("element")
            .blockModeType("LinkOnly")
            .build();

        // Try to parse and set numeric value
        try {
            double value = Double.parseDouble(inductance);
            dto.setInductance(value);
        } catch (NumberFormatException e) {
            // Leave as expression
        }

        return dto;
    }

    /**
     * Get inductance value as double
     * @return Inductance in Henries
     */
    public double getInductanceValueAsDouble() {
        if (inductanceValue != null) {
            return inductanceValue.getAsDouble();
        }
        if (getInductance() != null) {
            return getInductance();
        }
        return 1e-3; // Default 1mH
    }

    /**
     * Get inductance value as string (for expressions)
     * @return Inductance as string
     */
    public String getInductanceValueAsString() {
        if (inductanceValue != null) {
            return inductanceValue.getAsString();
        }
        if (getInductance() != null) {
            return String.valueOf(getInductance());
        }
        return "1e-3"; // Default 1mH
    }

    /**
     * Get initial current value as double
     * @return Initial current in Amperes
     */
    public double getInitialCurrentAsDouble() {
        if (initialCurrent != null) {
            return initialCurrent.getAsDouble();
        }
        return 0.0;
    }

    /**
     * Validate inductor-specific parameters
     * @return ValidationResult with errors if invalid
     */
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate inductance value exists
        if (inductanceValue == null && getInductance() == null) {
            result.addError(new ValidationError(
                "Inductor.inductance",
                "Inductance value is required",
                "l",
                null
            ));
        }

        // Validate inductance value range (if numeric)
        try {
            double l = getInductanceValueAsDouble();
            if (l <= 0) {
                result.addError(new ValidationError(
                    "Inductor.inductance",
                    "Inductance must be positive",
                    "l",
                    l
                ));
            } else if (l < MIN_INDUCTANCE) {
                result.addError(new ValidationError(
                    "Inductor.inductance",
                    String.format("Inductance too small (min: %.2e H)", MIN_INDUCTANCE),
                    "l",
                    l
                ));
            } else if (l > MAX_INDUCTANCE) {
                result.addError(new ValidationError(
                    "Inductor.inductance",
                    String.format("Inductance too large (max: %.2e H)", MAX_INDUCTANCE),
                    "l",
                    l
                ));
            }
        } catch (NumberFormatException e) {
            // Non-numeric value (expression) - skip numeric validation
        }

        return result;
    }

    /**
     * Create parameter map for circuit simulation
     * @return Parameter map with inductance value
     */
    @Override
    public Map<String, Object> createCircuitParamValues() {
        Map<String, Object> params = super.createCircuitParamValues();

        if (inductanceValue != null) {
            params.put("l", inductanceValue.getAsString());
        } else if (getInductance() != null) {
            params.put("l", getInductance());
        }

        if (initialCurrent != null) {
            params.put("InitialCondition", initialCurrent.getAsString());
        }

        return params;
    }

    /**
     * Get human-readable description
     * @return Description string
     */
    public String getDescription() {
        return String.format("Inductor: %s H (I0: %s A)",
            getInductanceValueAsString(),
            initialCurrent != null ? initialCurrent.getAsString() : "0");
    }
}
