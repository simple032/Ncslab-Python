package com.ncslab.dto.block.specialized.circuit2.element;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ncslab.dto.communication.CircuitBlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * DTO for Voltage Sensor circuit element block.
 * Measures voltage across its terminals and outputs the measured value.
 * Acts as an ideal voltmeter (infinite input impedance, zero current draw).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentSensorDto extends CircuitBlockDto {

    /**
     * Sensor gain/scale factor (default 1.0 for direct measurement)
     */
    @JsonProperty("scale")
    private TypedParameter scaleFactor;

    /**
     * Voltage offset for calibration (V)
     */
    @JsonProperty("offset")
    private TypedParameter voltageOffset;

    /**
     * Default scale factor (1.0 = no scaling)
     */
    public static final double DEFAULT_SCALE = 1.0;

    /**
     * Default voltage offset (0.0 = no offset)
     */
    public static final double DEFAULT_OFFSET = 0.0;

    /**
     * Create a voltage sensor DTO with default parameters (ideal sensor)
     * @param blockName Block name
     * @param blockPath Block path
     * @return VoltageSensorDto instance
     */
    public static CurrentSensorDto create(String blockName, String blockPath) {
        return create(blockName, blockPath, DEFAULT_SCALE, DEFAULT_OFFSET);
    }

    /**
     * Create a voltage sensor DTO with specified scale factor
     * @param blockName Block name
     * @param blockPath Block path
     * @param scale Scale factor
     * @return VoltageSensorDto instance
     */
    public static CurrentSensorDto create(String blockName, String blockPath, double scale) {
        return create(blockName, blockPath, scale, DEFAULT_OFFSET);
    }

    /**
     * Create a voltage sensor DTO with specified scale and offset
     * @param blockName Block name
     * @param blockPath Block path
     * @param scale Scale factor
     * @param offset Voltage offset in Volts
     * @return VoltageSensorDto instance
     */
    public static CurrentSensorDto create(String blockName, String blockPath, double scale, double offset) {
        return CurrentSensorDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .scaleFactor(TypedParameter.of(scale))
            .voltageOffset(TypedParameter.of(offset))
            .elementType("VoltageSensor")
            .circuitType("element")
            .blockModeType("Anything")
            .build();
    }

    /**
     * Get scale factor value as double
     * @return Scale factor
     */
    public double getScaleFactorAsDouble() {
        if (scaleFactor != null) {
            return scaleFactor.getAsDouble();
        }
        return DEFAULT_SCALE;
    }

    /**
     * Get voltage offset value as double
     * @return Voltage offset in Volts
     */
    public double getVoltageOffsetAsDouble() {
        if (voltageOffset != null) {
            return voltageOffset.getAsDouble();
        }
        return DEFAULT_OFFSET;
    }

    /**
     * Validate voltage sensor-specific parameters
     * @return ValidationResult with errors if invalid
     */
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        // Scale and offset can be any values
        return result;
    }

    /**
     * Create parameter map for circuit simulation
     * @return Parameter map with sensor parameters
     */
    @Override
    public Map<String, Object> createCircuitParamValues() {
        Map<String, Object> params = super.createCircuitParamValues();

        if (scaleFactor != null) {
            params.put("Scale", scaleFactor.getAsString());
        }
        if (voltageOffset != null) {
            params.put("Offset", voltageOffset.getAsString());
        }

        return params;
    }

    /**
     * Get human-readable description
     * @return Description string
     */
    public String getDescription() {
        return String.format("Voltage Sensor: Scale=%s, Offset=%s V",
            scaleFactor != null ? scaleFactor.getAsString() : DEFAULT_SCALE,
            voltageOffset != null ? voltageOffset.getAsString() : DEFAULT_OFFSET);
    }
}
