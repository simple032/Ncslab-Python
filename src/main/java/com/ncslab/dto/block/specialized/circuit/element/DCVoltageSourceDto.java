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
 * DTO for DC Voltage Source circuit element block.
 * Provides constant DC voltage output.
 * Can only operate in Branch mode.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DCVoltageSourceDto extends CircuitBlockDto {

    /**
     * DC voltage value in Volts (V)
     */
    @JsonProperty("v0")
    private TypedParameter voltageValue;

    /**
     * Maximum reasonable DC voltage (for safety validation)
     */
    public static final double MAX_VOLTAGE = 10000.0; // 10kV

    /**
     * Create a DC voltage source DTO with specified voltage
     * @param blockName Block name
     * @param blockPath Block path
     * @param voltage Voltage value in Volts
     * @return DCVoltageSourceDto instance
     */
    public static DCVoltageSourceDto create(String blockName, String blockPath, double voltage) {
        DCVoltageSourceDto dto = DCVoltageSourceDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .voltageValue(TypedParameter.of(voltage))
            .elementType("DCVoltageSource")
            .circuitType("element")
            .blockModeType("BranchOnly")
            .build();

        // Set the voltage in the base class field as well
        dto.setVoltage(voltage);

        return dto;
    }

    /**
     * Create a DC voltage source DTO with specified voltage (string format)
     * @param blockName Block name
     * @param blockPath Block path
     * @param voltage Voltage value as string (supports expressions)
     * @return DCVoltageSourceDto instance
     */
    public static DCVoltageSourceDto create(String blockName, String blockPath, String voltage) {
        DCVoltageSourceDto dto = DCVoltageSourceDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .voltageValue(TypedParameter.of(voltage))
            .elementType("DCVoltageSource")
            .circuitType("element")
            .blockModeType("BranchOnly")
            .build();

        // Try to parse and set numeric value
        try {
            double value = Double.parseDouble(voltage);
            dto.setVoltage(value);
        } catch (NumberFormatException e) {
            // Leave as expression
        }

        return dto;
    }

    /**
     * Get voltage value as double
     * @return Voltage in Volts
     */
    public double getVoltageValueAsDouble() {
        if (voltageValue != null) {
            return voltageValue.getAsDouble();
        }
        if (getVoltage() != null) {
            return getVoltage();
        }
        return 5.0; // Default 5V
    }

    /**
     * Get voltage value as string (for expressions)
     * @return Voltage as string
     */
    public String getVoltageValueAsString() {
        if (voltageValue != null) {
            return voltageValue.getAsString();
        }
        if (getVoltage() != null) {
            return String.valueOf(getVoltage());
        }
        return "5.0"; // Default 5V
    }

    /**
     * Validate DC voltage source-specific parameters
     * @return ValidationResult with errors if invalid
     */
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate voltage value exists
        if (voltageValue == null && getVoltage() == null) {
            result.addError(new ValidationError(
                "DCVoltageSource.voltage",
                "Voltage value is required",
                "v0",
                null
            ));
        }

        // Validate voltage value range (if numeric)
        try {
            double v = getVoltageValueAsDouble();
            if (Math.abs(v) > MAX_VOLTAGE) {
                result.addError(new ValidationError(
                    "DCVoltageSource.voltage",
                    String.format("Voltage magnitude exceeds maximum (±%.0f V)", MAX_VOLTAGE),
                    "v0",
                    v
                ));
            }
        } catch (NumberFormatException e) {
            // Non-numeric value (expression) - skip numeric validation
        }

        return result;
    }

    /**
     * Create parameter map for circuit simulation
     * @return Parameter map with voltage value
     */
    @Override
    public Map<String, Object> createCircuitParamValues() {
        Map<String, Object> params = super.createCircuitParamValues();

        if (voltageValue != null) {
            params.put("v0", voltageValue.getAsString());
        } else if (getVoltage() != null) {
            params.put("v0", getVoltage());
        }

        return params;
    }

    /**
     * Get human-readable description
     * @return Description string
     */
    public String getDescription() {
        return String.format("DC Voltage Source: %s V", getVoltageValueAsString());
    }
}
