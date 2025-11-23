package com.ncslab.dto.block.specialized.circuit.element;

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
 * DTO for Capacitor circuit element block.
 * Represents a capacitor with configurable capacitance value.
 * Can only operate in Branch mode (stores voltage, produces current).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CapacitorDto extends CircuitBlockDto {

    /**
     * Capacitance value in Farads (F)
     * Must be positive and non-zero
     */
    @JsonProperty("c")
    private TypedParameter capacitanceValue;

    /**
     * Initial voltage across capacitor (V)
     */
    @JsonProperty("initialVoltage")
    private TypedParameter initialVoltage;

    /**
     * Minimum allowed capacitance value (1 pico-farad)
     */
    public static final double MIN_CAPACITANCE = 1e-12;

    /**
     * Maximum allowed capacitance value (1000 Farads)
     */
    public static final double MAX_CAPACITANCE = 1000.0;

    /**
     * Create a capacitor DTO with specified capacitance
     * @param blockName Block name
     * @param blockPath Block path
     * @param capacitance Capacitance value in Farads
     * @return CapacitorDto instance
     */
    public static CapacitorDto create(String blockName, String blockPath, double capacitance) {
        return create(blockName, blockPath, capacitance, 0.0);
    }

    /**
     * Create a capacitor DTO with specified capacitance and initial voltage
     * @param blockName Block name
     * @param blockPath Block path
     * @param capacitance Capacitance value in Farads
     * @param initialVoltage Initial voltage in Volts
     * @return CapacitorDto instance
     */
    public static CapacitorDto create(String blockName, String blockPath, double capacitance, double initialVoltage) {
        CapacitorDto dto = CapacitorDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .capacitanceValue(TypedParameter.of(capacitance))
            .initialVoltage(TypedParameter.of(initialVoltage))
            .elementType("Capacitor")
            .circuitType("element")
            .blockModeType("BranchOnly")
            .build();

        // Set the capacitance in the base class field as well
        dto.setCapacitance(capacitance);

        return dto;
    }

    /**
     * Create a capacitor DTO with specified capacitance (string format)
     * @param blockName Block name
     * @param blockPath Block path
     * @param capacitance Capacitance value as string (supports expressions)
     * @return CapacitorDto instance
     */
    public static CapacitorDto create(String blockName, String blockPath, String capacitance) {
        return create(blockName, blockPath, capacitance, "0");
    }

    /**
     * Create a capacitor DTO with specified capacitance and initial voltage (string format)
     * @param blockName Block name
     * @param blockPath Block path
     * @param capacitance Capacitance value as string
     * @param initialVoltage Initial voltage as string
     * @return CapacitorDto instance
     */
    public static CapacitorDto create(String blockName, String blockPath, String capacitance, String initialVoltage) {
        CapacitorDto dto = CapacitorDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .capacitanceValue(TypedParameter.of(capacitance))
            .initialVoltage(TypedParameter.of(initialVoltage))
            .elementType("Capacitor")
            .circuitType("element")
            .blockModeType("BranchOnly")
            .build();

        // Try to parse and set numeric value
        try {
            double value = Double.parseDouble(capacitance);
            dto.setCapacitance(value);
        } catch (NumberFormatException e) {
            // Leave as expression
        }

        return dto;
    }

    /**
     * Get capacitance value as double
     * @return Capacitance in Farads
     */
    public double getCapacitanceValueAsDouble() {
        if (capacitanceValue != null) {
            return capacitanceValue.getAsDouble();
        }
        if (getCapacitance() != null) {
            return getCapacitance();
        }
        return 1e-6; // Default 1μF
    }

    /**
     * Get capacitance value as string (for expressions)
     * @return Capacitance as string
     */
    public String getCapacitanceValueAsString() {
        if (capacitanceValue != null) {
            return capacitanceValue.getAsString();
        }
        if (getCapacitance() != null) {
            return String.valueOf(getCapacitance());
        }
        return "1e-6"; // Default 1μF
    }

    /**
     * Get initial voltage value as double
     * @return Initial voltage in Volts
     */
    public double getInitialVoltageAsDouble() {
        if (initialVoltage != null) {
            return initialVoltage.getAsDouble();
        }
        return 0.0;
    }

    /**
     * Validate capacitor-specific parameters
     * @return ValidationResult with errors if invalid
     */
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate capacitance value exists
        if (capacitanceValue == null && getCapacitance() == null) {
            result.addError(new ValidationError(
                "Capacitor.capacitance",
                "Capacitance value is required",
                "c",
                null
            ));
        }

        // Validate capacitance value range (if numeric)
        try {
            double c = getCapacitanceValueAsDouble();
            if (c <= 0) {
                result.addError(new ValidationError(
                    "Capacitor.capacitance",
                    "Capacitance must be positive",
                    "c",
                    c
                ));
            } else if (c < MIN_CAPACITANCE) {
                result.addError(new ValidationError(
                    "Capacitor.capacitance",
                    String.format("Capacitance too small (min: %.2e F)", MIN_CAPACITANCE),
                    "c",
                    c
                ));
            } else if (c > MAX_CAPACITANCE) {
                result.addError(new ValidationError(
                    "Capacitor.capacitance",
                    String.format("Capacitance too large (max: %.2e F)", MAX_CAPACITANCE),
                    "c",
                    c
                ));
            }
        } catch (NumberFormatException e) {
            // Non-numeric value (expression) - skip numeric validation
        }

        return result;
    }

    /**
     * Create parameter map for circuit simulation
     * @return Parameter map with capacitance value
     */
    @Override
    public Map<String, Object> createCircuitParamValues() {
        Map<String, Object> params = super.createCircuitParamValues();

        if (capacitanceValue != null) {
            params.put("c", capacitanceValue.getAsString());
        } else if (getCapacitance() != null) {
            params.put("c", getCapacitance());
        }

        if (initialVoltage != null) {
            params.put("InitialCondition", initialVoltage.getAsString());
        }

        return params;
    }

    /**
     * Get human-readable description
     * @return Description string
     */
    public String getDescription() {
        return String.format("Capacitor: %s F (V0: %s V)",
            getCapacitanceValueAsString(),
            initialVoltage != null ? initialVoltage.getAsString() : "0");
    }
}
