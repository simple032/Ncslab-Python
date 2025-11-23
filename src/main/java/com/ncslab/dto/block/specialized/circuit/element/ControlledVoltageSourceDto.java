package com.ncslab.dto.block.specialized.circuit.element;

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
 * DTO for Controlled Voltage Source circuit element block.
 * Voltage output is controlled by an external input signal (gain factor).
 * Can only operate in Branch mode.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ControlledVoltageSourceDto extends CircuitBlockDto {

    /**
     * Gain factor (voltage multiplication factor)
     */
    @JsonProperty("gain")
    private TypedParameter gainFactor;

    /**
     * Default gain factor
     */
    public static final double DEFAULT_GAIN = 1.0;

    /**
     * Create a controlled voltage source DTO with default gain
     * @param blockName Block name
     * @param blockPath Block path
     * @return ControlledVoltageSourceDto instance
     */
    public static ControlledVoltageSourceDto create(String blockName, String blockPath) {
        return create(blockName, blockPath, DEFAULT_GAIN);
    }

    /**
     * Create a controlled voltage source DTO with specified gain
     * @param blockName Block name
     * @param blockPath Block path
     * @param gain Gain factor
     * @return ControlledVoltageSourceDto instance
     */
    public static ControlledVoltageSourceDto create(String blockName, String blockPath, double gain) {
        return ControlledVoltageSourceDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .gainFactor(TypedParameter.of(gain))
            .elementType("ControlledVoltageSource")
            .circuitType("element")
            .blockModeType("BranchOnly")
            .build();
    }

    /**
     * Get gain factor value as double
     * @return Gain factor
     */
    public double getGainFactorAsDouble() {
        if (gainFactor != null) {
            return gainFactor.getAsDouble();
        }
        return DEFAULT_GAIN;
    }

    /**
     * Validate controlled voltage source-specific parameters
     * @return ValidationResult with errors if invalid
     */
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        // Gain can be any value including negative (for inverting source)
        return result;
    }

    /**
     * Create parameter map for circuit simulation
     * @return Parameter map with gain factor
     */
    @Override
    public Map<String, Object> createCircuitParamValues() {
        Map<String, Object> params = super.createCircuitParamValues();

        if (gainFactor != null) {
            params.put("Gain", gainFactor.getAsString());
        }

        return params;
    }

    /**
     * Get human-readable description
     * @return Description string
     */
    public String getDescription() {
        return String.format("Controlled Voltage Source: Gain=%s",
            gainFactor != null ? gainFactor.getAsString() : DEFAULT_GAIN);
    }
}
