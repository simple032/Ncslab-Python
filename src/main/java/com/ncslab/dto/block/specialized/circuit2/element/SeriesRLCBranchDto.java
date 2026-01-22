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
 * DTO for Variable Resistor circuit element block.
 * Resistance is controlled by an external input signal.
 * Can operate in both Branch mode and Link mode.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SeriesRLCBranchDto extends CircuitBlockDto {

    /**
     * Minimum resistance value in Ohms (Ω)
     * Prevents resistance from going to zero
     */
    @JsonProperty("Rmin")
    private TypedParameter minimumResistance;

    /**
     * Default minimum resistance (1 milli-ohm)
     */
    public static final double DEFAULT_MIN_RESISTANCE = 0.001;

    /**
     * Maximum reasonable resistance
     */
    public static final double MAX_RESISTANCE = 1e12;

    /**
     * Create a variable resistor DTO with default minimum resistance
     * @param blockName Block name
     * @param blockPath Block path
     * @return VariableResistorDto instance
     */
    public static SeriesRLCBranchDto create(String blockName, String blockPath) {
        return create(blockName, blockPath, DEFAULT_MIN_RESISTANCE);
    }

    /**
     * Create a variable resistor DTO with specified minimum resistance
     * @param blockName Block name
     * @param blockPath Block path
     * @param minimumResistance Minimum resistance in Ohms
     * @return VariableResistorDto instance
     */
    public static SeriesRLCBranchDto create(String blockName, String blockPath, double minimumResistance) {
    	SeriesRLCBranchDto dto = SeriesRLCBranchDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .minimumResistance(TypedParameter.of(minimumResistance))
            .elementType("VariableResistor")
            .circuitType("element")
            .blockModeType("Anything")
            .build();

        dto.setResistance(minimumResistance);

        return dto;
    }

    /**
     * Get minimum resistance value as double
     * @return Minimum resistance in Ohms
     */
    public double getMinimumResistanceAsDouble() {
        if (minimumResistance != null) {
            return minimumResistance.getAsDouble();
        }
        return DEFAULT_MIN_RESISTANCE;
    }

    /**
     * Validate variable resistor-specific parameters
     * @return ValidationResult with errors if invalid
     */
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate minimum resistance
        if (minimumResistance != null) {
            try {
                double rmin = getMinimumResistanceAsDouble();
                if (rmin <= 0) {
                    result.addError(new ValidationError(
                        "VariableResistor.minimumResistance",
                        "Minimum resistance must be positive",
                        "Rmin",
                        rmin
                    ));
                } else if (rmin > MAX_RESISTANCE) {
                    result.addError(new ValidationError(
                        "VariableResistor.minimumResistance",
                        String.format("Minimum resistance exceeds maximum (%.2e Ω)", MAX_RESISTANCE),
                        "Rmin",
                        rmin
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
     * @return Parameter map with minimum resistance
     */
    @Override
    public Map<String, Object> createCircuitParamValues() {
        Map<String, Object> params = super.createCircuitParamValues();

        if (minimumResistance != null) {
            params.put("Rmin", minimumResistance.getAsString());
        }

        return params;
    }

    /**
     * Get human-readable description
     * @return Description string
     */
    public String getDescription() {
        return String.format("Variable Resistor: Rmin=%s Ω",
            minimumResistance != null ? minimumResistance.getAsString() : DEFAULT_MIN_RESISTANCE);
    }
}
