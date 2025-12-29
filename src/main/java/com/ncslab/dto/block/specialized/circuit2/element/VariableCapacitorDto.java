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
 * DTO for Variable Capacitor circuit element block.
 * Capacitance is controlled by an external input signal.
 * Can only operate in Branch mode.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VariableCapacitorDto extends CircuitBlockDto {

    /**
     * Minimum capacitance value in Farads (F)
     * Prevents capacitance from going to zero
     */
    @JsonProperty("Cmin")
    private TypedParameter minimumCapacitance;

    /**
     * Initial voltage across capacitor (V)
     */
    @JsonProperty("initialVoltage")
    private TypedParameter initialVoltage;

    /**
     * Default minimum capacitance (1 pico-farad)
     */
    public static final double DEFAULT_MIN_CAPACITANCE = 1e-12;

    /**
     * Maximum reasonable capacitance
     */
    public static final double MAX_CAPACITANCE = 1000.0;

    /**
     * Create a variable capacitor DTO with default minimum capacitance
     * @param blockName Block name
     * @param blockPath Block path
     * @return VariableCapacitorDto instance
     */
    public static VariableCapacitorDto create(String blockName, String blockPath) {
        return create(blockName, blockPath, DEFAULT_MIN_CAPACITANCE, 0.0);
    }

    /**
     * Create a variable capacitor DTO with specified parameters
     * @param blockName Block name
     * @param blockPath Block path
     * @param minimumCapacitance Minimum capacitance in Farads
     * @param initialVoltage Initial voltage in Volts
     * @return VariableCapacitorDto instance
     */
    public static VariableCapacitorDto create(String blockName, String blockPath,
                                             double minimumCapacitance, double initialVoltage) {
        VariableCapacitorDto dto = VariableCapacitorDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .minimumCapacitance(TypedParameter.of(minimumCapacitance))
            .initialVoltage(TypedParameter.of(initialVoltage))
            .elementType("VariableCapacitor")
            .circuitType("element")
            .blockModeType("BranchOnly")
            .build();

        dto.setCapacitance(minimumCapacitance);

        return dto;
    }

    /**
     * Get minimum capacitance value as double
     * @return Minimum capacitance in Farads
     */
    public double getMinimumCapacitanceAsDouble() {
        if (minimumCapacitance != null) {
            return minimumCapacitance.getAsDouble();
        }
        return DEFAULT_MIN_CAPACITANCE;
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
     * Validate variable capacitor-specific parameters
     * @return ValidationResult with errors if invalid
     */
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate minimum capacitance
        if (minimumCapacitance != null) {
            try {
                double cmin = getMinimumCapacitanceAsDouble();
                if (cmin <= 0) {
                    result.addError(new ValidationError(
                        "VariableCapacitor.minimumCapacitance",
                        "Minimum capacitance must be positive",
                        "Cmin",
                        cmin
                    ));
                } else if (cmin > MAX_CAPACITANCE) {
                    result.addError(new ValidationError(
                        "VariableCapacitor.minimumCapacitance",
                        String.format("Minimum capacitance exceeds maximum (%.2e F)", MAX_CAPACITANCE),
                        "Cmin",
                        cmin
                    ));
                }
            } catch (NumberFormatException e) {
                // Skip numeric validation for expressions
            }
        }

        return result;
    }

    /**
     * Create parameter map for circuit simulation
     * @return Parameter map with variable capacitor parameters
     */
    @Override
    public Map<String, Object> createCircuitParamValues() {
        Map<String, Object> params = super.createCircuitParamValues();

        if (minimumCapacitance != null) {
            params.put("Cmin", minimumCapacitance.getAsString());
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
        return String.format("Variable Capacitor: Cmin=%s F (V0: %s V)",
            minimumCapacitance != null ? minimumCapacitance.getAsString() : DEFAULT_MIN_CAPACITANCE,
            initialVoltage != null ? initialVoltage.getAsString() : "0");
    }
}
