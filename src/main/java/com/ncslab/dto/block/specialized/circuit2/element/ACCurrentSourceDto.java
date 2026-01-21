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
 * DTO for AC Voltage Source circuit element block.
 * Provides sinusoidal AC voltage output.
 * Can only operate in Branch mode.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ACCurrentSourceDto extends CircuitBlockDto {

    /**
     * Peak amplitude in Volts (V)
     */
    @JsonProperty("amp")
    private TypedParameter amplitudeValue;

    /**
     * Frequency in Hz
     */
    @JsonProperty("frequency")
    private TypedParameter frequencyValue;

    /**
     * Phase shift in degrees
     */
    @JsonProperty("shift")
    private TypedParameter phaseShift;

    /**
     * Maximum reasonable amplitude (for safety validation)
     */
    public static final double MAX_AMPLITUDE = 10000.0; // 10kV

    /**
     * Maximum reasonable frequency
     */
    public static final double MAX_FREQUENCY = 1e9; // 1 GHz

    /**
     * Create an AC voltage source DTO with specified parameters
     * @param blockName Block name
     * @param blockPath Block path
     * @param amplitude Peak amplitude in Volts
     * @param frequency Frequency in Hz
     * @return ACVoltageSourceDto instance
     */
    public static ACCurrentSourceDto create(String blockName, String blockPath, double amplitude, double frequency) {
        return create(blockName, blockPath, amplitude, frequency, 0.0);
    }

    /**
     * Create an AC voltage source DTO with specified parameters including phase
     * @param blockName Block name
     * @param blockPath Block path
     * @param amplitude Peak amplitude in Volts
     * @param frequency Frequency in Hz
     * @param phaseShift Phase shift in degrees
     * @return ACVoltageSourceDto instance
     */
    public static ACCurrentSourceDto create(String blockName, String blockPath, double amplitude, double frequency, double phaseShift) {
    	ACCurrentSourceDto dto = ACCurrentSourceDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .amplitudeValue(TypedParameter.of(amplitude))
            .frequencyValue(TypedParameter.of(frequency))
            .phaseShift(TypedParameter.of(phaseShift))
            .elementType("ACCurrentSource")
            .circuitType("element")
            .blockModeType("BranchOnly")
            .build();

        // Set values in the base class fields as well
        dto.setAmplitude(amplitude);
        dto.setFrequency(frequency);
        dto.setPhase(phaseShift);

        return dto;
    }

    /**
     * Create an AC voltage source DTO with string parameters
     * @param blockName Block name
     * @param blockPath Block path
     * @param amplitude Amplitude as string
     * @param frequency Frequency as string
     * @return ACVoltageSourceDto instance
     */
    public static ACVoltageSourceDto create(String blockName, String blockPath, String amplitude, String frequency) {
        return create(blockName, blockPath, amplitude, frequency, "0");
    }

    /**
     * Create an AC voltage source DTO with string parameters including phase
     * @param blockName Block name
     * @param blockPath Block path
     * @param amplitude Amplitude as string
     * @param frequency Frequency as string
     * @param phaseShift Phase shift as string
     * @return ACVoltageSourceDto instance
     */
    public static ACVoltageSourceDto create(String blockName, String blockPath, String amplitude, String frequency, String phaseShift) {
        ACVoltageSourceDto dto = ACVoltageSourceDto.builder()
            .blockName(blockName)
            .blockPath(blockPath)
            .blockUUID("null")
            .amplitudeValue(TypedParameter.of(amplitude))
            .frequencyValue(TypedParameter.of(frequency))
            .phaseShift(TypedParameter.of(phaseShift))
            .elementType("ACVoltageSource")
            .circuitType("element")
            .blockModeType("BranchOnly")
            .build();

        // Try to parse and set numeric values
        try {
            dto.setAmplitude(Double.parseDouble(amplitude));
            dto.setFrequency(Double.parseDouble(frequency));
            dto.setPhase(Double.parseDouble(phaseShift));
        } catch (NumberFormatException e) {
            // Leave as expressions
        }

        return dto;
    }

    /**
     * Get amplitude value as double
     * @return Amplitude in Volts
     */
    public double getAmplitudeValueAsDouble() {
        if (amplitudeValue != null) {
            return amplitudeValue.getAsDouble();
        }
        if (getAmplitude() != null) {
            return getAmplitude();
        }
        return 10.0; // Default 10V
    }

    /**
     * Get frequency value as double
     * @return Frequency in Hz
     */
    public double getFrequencyValueAsDouble() {
        if (frequencyValue != null) {
            return frequencyValue.getAsDouble();
        }
        if (getFrequency() != null) {
            return getFrequency();
        }
        return 50.0; // Default 50Hz
    }

    /**
     * Get phase shift value as double
     * @return Phase shift in degrees
     */
    public double getPhaseShiftAsDouble() {
        if (phaseShift != null) {
            return phaseShift.getAsDouble();
        }
        if (getPhase() != null) {
            return getPhase();
        }
        return 0.0; // Default 0°
    }

    /**
     * Validate AC voltage source-specific parameters
     * @return ValidationResult with errors if invalid
     */
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate amplitude
        if (amplitudeValue == null && getAmplitude() == null) {
            result.addError(new ValidationError(
                "ACVoltageSource.amplitude",
                "Amplitude value is required",
                "amp",
                null
            ));
        }

        // Validate frequency
        if (frequencyValue == null && getFrequency() == null) {
            result.addError(new ValidationError(
                "ACVoltageSource.frequency",
                "Frequency value is required",
                "frequency",
                null
            ));
        }

        // Validate numeric ranges
        try {
            double amp = getAmplitudeValueAsDouble();
            if (amp < 0) {
                result.addError(new ValidationError(
                    "ACVoltageSource.amplitude",
                    "Amplitude must be non-negative",
                    "amp",
                    amp
                ));
            } else if (amp > MAX_AMPLITUDE) {
                result.addError(new ValidationError(
                    "ACVoltageSource.amplitude",
                    String.format("Amplitude exceeds maximum (%.0f V)", MAX_AMPLITUDE),
                    "amp",
                    amp
                ));
            }

            double freq = getFrequencyValueAsDouble();
            if (freq <= 0) {
                result.addError(new ValidationError(
                    "ACVoltageSource.frequency",
                    "Frequency must be positive",
                    "frequency",
                    freq
                ));
            } else if (freq > MAX_FREQUENCY) {
                result.addError(new ValidationError(
                    "ACVoltageSource.frequency",
                    String.format("Frequency exceeds maximum (%.2e Hz)", MAX_FREQUENCY),
                    "frequency",
                    freq
                ));
            }
        } catch (NumberFormatException e) {
            // Non-numeric values - skip numeric validation
        }

        return result;
    }

    /**
     * Create parameter map for circuit simulation
     * @return Parameter map with AC source parameters
     */
    @Override
    public Map<String, Object> createCircuitParamValues() {
        Map<String, Object> params = super.createCircuitParamValues();

        if (amplitudeValue != null) {
            params.put("amp", amplitudeValue.getAsString());
        }
        if (frequencyValue != null) {
            params.put("frequency", frequencyValue.getAsString());
        }
        if (phaseShift != null) {
            params.put("shift", phaseShift.getAsString());
        }

        return params;
    }

    /**
     * Get human-readable description
     * @return Description string
     */
    public String getDescription() {
        return String.format("AC Voltage Source: %s V @ %s Hz (Phase: %s°)",
            amplitudeValue != null ? amplitudeValue.getAsString() : "?",
            frequencyValue != null ? frequencyValue.getAsString() : "?",
            phaseShift != null ? phaseShift.getAsString() : "0");
    }
}
