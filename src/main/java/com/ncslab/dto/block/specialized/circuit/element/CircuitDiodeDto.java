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
 * DTO for Diode circuit element block.
 * Represents a semiconductor diode with configurable parameters.
 * Can operate in both Branch mode and Link mode.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitDiodeDto extends CircuitBlockDto {

    /**
     * Forward voltage drop in Volts (V)
     */
    @JsonProperty("Vf")
    private TypedParameter forwardVoltage;

    /**
     * On-resistance in Ohms (Ω)
     */
    @JsonProperty("Ron")
    private TypedParameter onResistance;

    /**
     * Off-conductance in Siemens (S)
     */
    @JsonProperty("Goff")
    private TypedParameter offConductance;

    /**
     * Create a diode DTO with default parameters
     * @param blockName Block name
     * @param blockPath Block path
     * @return CircuitDiodeDto instance
     */
    public static CircuitDiodeDto create(String blockName, String blockPath) {
        return create(blockName, blockPath, 0.7, 0.001, 1e-5);
    }

    /**
     * Create a diode DTO with specified parameters
     * @param blockName Block name
     * @param blockPath Block path
     * @param forwardVoltage Forward voltage drop (V)
     * @param onResistance On-resistance (Ω)
     * @param offConductance Off-conductance (S)
     * @return CircuitDiodeDto instance
     */
    public static CircuitDiodeDto create(String blockName, String blockPath,
                                        double forwardVoltage, double onResistance, double offConductance) {
        return CircuitDiodeDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .forwardVoltage(TypedParameter.of(forwardVoltage))
            .onResistance(TypedParameter.of(onResistance))
            .offConductance(TypedParameter.of(offConductance))
            .elementType("Diode")
            .circuitType("element")
            .blockModeType("Anything")
            .build();
    }

    /**
     * Validate diode-specific parameters
     * @return ValidationResult with errors if invalid
     */
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate forward voltage
        if (forwardVoltage != null) {
            try {
                double vf = forwardVoltage.getAsDouble();
                if (vf < 0) {
                    result.addError(new ValidationError(
                        "Diode.forwardVoltage",
                        "Forward voltage must be non-negative",
                        "Vf",
                        vf
                    ));
                }
            } catch (NumberFormatException e) {
                // Skip numeric validation for expressions
            }
        }

        // Validate on-resistance
        if (onResistance != null) {
            try {
                double ron = onResistance.getAsDouble();
                if (ron <= 0) {
                    result.addError(new ValidationError(
                        "Diode.onResistance",
                        "On-resistance must be positive",
                        "Ron",
                        ron
                    ));
                }
            } catch (NumberFormatException e) {
                // Skip numeric validation
            }
        }

        // Validate off-conductance
        if (offConductance != null) {
            try {
                double goff = offConductance.getAsDouble();
                if (goff < 0) {
                    result.addError(new ValidationError(
                        "Diode.offConductance",
                        "Off-conductance must be non-negative",
                        "Goff",
                        goff
                    ));
                }
            } catch (NumberFormatException e) {
                // Skip numeric validation
            }
        }

        return result;
    }

    /**
     * Create parameter map for circuit simulation
     * @return Parameter map with diode parameters
     */
    @Override
    public Map<String, Object> createCircuitParamValues() {
        Map<String, Object> params = super.createCircuitParamValues();

        if (forwardVoltage != null) {
            params.put("Vf", forwardVoltage.getAsString());
        }
        if (onResistance != null) {
            params.put("Ron", onResistance.getAsString());
        }
        if (offConductance != null) {
            params.put("Goff", offConductance.getAsString());
        }

        return params;
    }

    /**
     * Get human-readable description
     * @return Description string
     */
    public String getDescription() {
        return String.format("Diode: Vf=%s V, Ron=%s Ω, Goff=%s S",
            forwardVoltage != null ? forwardVoltage.getAsString() : "0.7",
            onResistance != null ? onResistance.getAsString() : "0.001",
            offConductance != null ? offConductance.getAsString() : "1e-5");
    }
}
