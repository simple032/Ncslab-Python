package com.ncslab.dto.block.specialized.source;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.block.BlockPositionDto;
import com.ncslab.dto.block.BlockDimensionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * DTO for SineWave block - Sine wave signal generation with configurable parameters.
 * 
 * <p>This block generates a sine wave signal with SIMULINK-compatible parameters:
 * <ul>
 *   <li><b>Amplitude</b>: Amplitude of sine wave</li>
 *   <li><b>Bias</b>: DC offset (bias) of the signal</li>
 *   <li><b>Frequency</b>: Frequency of sine wave in rad/s</li>
 *   <li><b>Phase</b>: Phase shift in radians</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 *   <li><b>Samples</b>: Number of samples per frame</li>
 *   <li><b>TimeSource</b>: Time source (Use simulation time, Use external signal)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type specification</li>
 *   <li><b>SaturateOnIntegerOverflow</b>: Handle integer overflow behavior</li>
 * </ul>
 * </p>
 * 
 * <p><b>Signal Formula:</b></p>
 * <ul>
 *   <li>output = amplitude * sin(frequency * t + phase) + bias</li>
 * </ul>
 * 
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Amplitude must be finite</li>
 *   <li>Frequency must be positive and finite</li>
 *   <li>Phase must be finite</li>
 *   <li>Bias must be finite</li>
 *   <li>Samples must be positive integer</li>
 *   <li>SampleTime must be >= 0 or -1 (inherited)</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@Jacksonized
@JsonTypeName("SineWave")
public class SineWaveDto extends BlockDto {

    /**
     * Amplitude of sine wave.
     * Controls the peak magnitude of the sinusoidal signal.
     */
    @Builder.Default
    private TypedParameter amplitude = TypedParameter.of(1.0);

    /**
     * DC offset (bias) of the signal.
     * This value is added to the sine wave output.
     */
    @Builder.Default
    private TypedParameter bias = TypedParameter.of(0.0);

    /**
     * Frequency of sine wave in rad/s.
     * Must be positive for meaningful signal generation.
     */
    @Builder.Default
    private TypedParameter frequency = TypedParameter.of(1.0);

    /**
     * Phase shift in radians.
     * Controls the horizontal shift of the sine wave.
     */
    @Builder.Default
    private TypedParameter phase = TypedParameter.of(0.0);

    /**
     * Sample time for discrete operation.
     * -1 for inherited, 0 for continuous, >0 for discrete
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(0.0);

    /**
     * Number of samples per frame.
     * For vector output generation in discrete-time systems.
     */
    @Builder.Default
    private TypedParameter samples = TypedParameter.of(1);

    /**
     * Time source for the sine wave.
     * Can be "Use simulation time" or "Use external signal".
     */
    @Builder.Default
    private TypedParameter timeSource = TypedParameter.of("Use simulation time");

    /**
     * Output data type specification.
     * Controls the data type of the block output.
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("double");

    /**
     * Handle integer overflow behavior.
     * When enabled, saturates on integer overflow instead of wrapping.
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of("off");

    /**
     * Creates SineWaveDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     */
    public SineWaveDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName,blockPath, position, dimension);
    }

    /**
     * Creates SineWaveDto with comprehensive sine wave configuration.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param amplitude Amplitude of sine wave
     * @param frequency Frequency of sine wave in rad/s
     * @param phase Phase shift in radians
     * @param bias DC offset of the signal
     */
    public SineWaveDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension,
                       double amplitude, double frequency, double phase, double bias) {
        super(blockName,blockPath, position, dimension);
        this.amplitude = TypedParameter.of(amplitude);
        this.bias = TypedParameter.of(bias);
        this.frequency = TypedParameter.of(frequency);
        this.phase = TypedParameter.of(phase);
        this.sampleTime = TypedParameter.of(0.0); // Continuous by default
        this.samples = TypedParameter.of(1);
        this.timeSource = TypedParameter.of("Use simulation time");
        this.outDataTypeStr = TypedParameter.of("double");
        this.saturateOnIntegerOverflow = TypedParameter.of("off");
    }

    /**
     * Factory method for creating SineWaveDto from parameter map.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param parameters Typed parameter map
     * @return Configured SineWaveDto instance
     */
    public static SineWaveDto fromParameters(String blockName, String blockPath, 
                                            BlockPositionDto position, BlockDimensionDto dimension,
                                            TypedParameterMap parameters) {
        SineWaveDto dto = new SineWaveDto(blockName, blockPath, position, dimension);
        
        dto.amplitude = parameters.getTypedParameter("Amplitude", Double.class, 1.0);
        dto.bias = parameters.getTypedParameter("Bias", Double.class, 0.0);
        dto.frequency = parameters.getTypedParameter("Frequency", Double.class, 1.0);
        dto.phase = parameters.getTypedParameter("Phase", Double.class, 0.0);
        dto.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, 0.0);
        dto.samples = parameters.getTypedParameter("Samples", Integer.class, 1);
        dto.timeSource = parameters.getTypedParameter("TimeSource", String.class, "Use simulation time");
        dto.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class, "double");
        dto.saturateOnIntegerOverflow = parameters.getTypedParameter("SaturateOnIntegerOverflow", String.class, "off");
        
        return dto;
    }
    // === Validation Methods ===

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();
        
        // Validate amplitude
        if (amplitude != null && amplitude.getAsDouble() != null) {
            Double ampValue = amplitude.getAsDouble();
            if (Double.isNaN(ampValue) || Double.isInfinite(ampValue)) {
                errors.add("Amplitude must be finite");
            }
        }
        
        // Validate frequency
        if (frequency != null && frequency.getAsDouble() != null) {
            Double freqValue = frequency.getAsDouble();
            if (freqValue < 0.0 || Double.isNaN(freqValue) || Double.isInfinite(freqValue)) {
                errors.add("Frequency must be positive and finite");
            }
        }
        
        // Validate phase
        if (phase != null && phase.getAsDouble() != null) {
            Double phaseValue = phase.getAsDouble();
            if (Double.isNaN(phaseValue) || Double.isInfinite(phaseValue)) {
                errors.add("Phase must be finite");
            }
        }
        
        // Validate bias
        if (bias != null && bias.getAsDouble() != null) {
            Double biasValue = bias.getAsDouble();
            if (Double.isNaN(biasValue) || Double.isInfinite(biasValue)) {
                errors.add("Bias must be finite");
            }
        }
        
        // Validate samples
        if (samples != null && samples.getAsInteger() != null) {
            if (samples.getAsDouble() <= 0) {
                errors.add("Samples must be positive");
            }
        }
        
        // Validate sample time
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            Double stValue = sampleTime.getAsDouble();
            if (stValue < -1.0 || Double.isNaN(stValue) || Double.isInfinite(stValue)) {
                errors.add("Sample time must be >= 0 or -1 (inherited)");
            }
        }
        
        return errors;
    }

    @Override
    public boolean isValidConfiguration() {
        return validateParameters().isEmpty() &&
               amplitude != null && amplitude.getAsDouble() != null &&
               frequency != null && frequency.getAsDouble() != null && frequency.getAsDouble() >= 0.0 &&
               phase != null && phase.getAsDouble() != null &&
               bias != null && bias.getAsDouble() != null &&
               samples != null && samples.getAsInteger() != null && samples.getAsDouble() > 0 &&
               sampleTime != null && sampleTime.getAsDouble() != null;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the amplitude value.
     *
     * @return Amplitude of sine wave
     */
    public double getAmplitudeValue() {
        if (amplitude != null && amplitude.getAsDouble() != null) {
            Object value = amplitude.getAsDouble();
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return 1.0;
    }

    /**
     * Gets the bias value.
     *
     * @return DC offset of the signal
     */
    public double getBiasValue() {
        if (bias != null && bias.getAsDouble() != null) {
            Object value = bias.getAsDouble();
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return 0.0;
    }

    /**
     * Gets the frequency value.
     *
     * @return Frequency of sine wave in rad/s
     */
    public double getFrequencyValue() {
        if (frequency != null && frequency.getAsDouble() != null) {
            Object value = frequency.getAsDouble();
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return 1.0;
    }

    /**
     * Gets the phase value.
     *
     * @return Phase shift in radians
     */
    public double getPhaseValue() {
        if (phase != null && phase.getAsDouble() != null) {
            Object value = phase.getAsDouble();
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return 0.0;
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time for discrete operation
     */
    public double getSampleTimeValue() {
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            Object value = sampleTime.getAsDouble();
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return 0.0;
    }

    /**
     * Gets the samples per frame value.
     *
     * @return Number of samples per frame
     */
    public int getSamplesValue() {
        if (samples != null && samples.getAsInteger() != null) {
            Object value = samples.getAsInteger();
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        return 1;
    }

    /**
     * Gets the time source setting.
     *
     * @return Time source string
     */
    public String getTimeSourceValue() {
        if (timeSource != null && timeSource.getAsString() != null) {
            return timeSource.getAsString().toString();
        }
        return "Use simulation time";
    }

    /**
     * Gets the output data type specification.
     *
     * @return Output data type string
     */
    public String getOutDataTypeStrValue() {
        if (outDataTypeStr != null && outDataTypeStr.getAsString() != null) {
            return outDataTypeStr.getAsString();
        }
        return "double";
    }

    /**
     * Checks if integer overflow saturation is enabled.
     *
     * @return true if saturation is enabled
     */
    public boolean isSaturationEnabled() {
        return saturateOnIntegerOverflow != null && 
               "on".equals(saturateOnIntegerOverflow.getAsString());
    }

    /**
     * Checks if the sine wave operates in continuous time mode.
     *
     * @return true if sample time is 0 (continuous)
     */
    public boolean isContinuousTime() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if the sine wave operates in discrete time mode.
     *
     * @return true if sample time is positive (discrete)
     */
    public boolean isDiscreteTime() {
        return getSampleTimeValue() > 0.0;
    }

    /**
     * Checks if external time signal is used.
     *
     * @return true if using external time signal
     */
    public boolean usesExternalTime() {
        return "Use external signal".equals(getTimeSourceValue());
    }

    /**
     * Calculates the sine wave output at a given time.
     *
     * @param time Current simulation time
     * @return Output value at the given time
     */
    public double calculateOutput(double time) {
        double ampValue = getAmplitudeValue();
        double freqValue = getFrequencyValue();
        double phaseValue = getPhaseValue();
        double biasValue = getBiasValue();
        
        return ampValue * Math.sin(freqValue * time + phaseValue) + biasValue;
    }

    // === Helper Methods ===

    @Override
    public SineWaveDto copy() {
        return SineWaveDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .position(getPosition())
                .dimension(getDimension())
                .amplitude(amplitude != null ? amplitude.copy() : null)
                .bias(bias != null ? bias.copy() : null)
                .frequency(frequency != null ? frequency.copy() : null)
                .phase(phase != null ? phase.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .samples(samples != null ? samples.copy() : null)
                .timeSource(timeSource != null ? timeSource.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }

    /**
     * Creates a TypedParameterMap from this DTO's parameters.
     *
     * @return TypedParameterMap containing all block parameters
     */
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Amplitude", amplitude)
                .put("Bias", bias)
                .put("Frequency", frequency)
                .put("Phase", phase)
                .put("SampleTime", sampleTime)
                .put("Samples", samples)
                .put("TimeSource", timeSource)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "Amplitude", "Amplitude of sine wave",
            "Bias", "DC offset (bias) of the signal",
            "Frequency", "Frequency of sine wave in rad/s",
            "Phase", "Phase shift in radians",
            "SampleTime", "Sample time for discrete operation (-1 for inherited, 0 for continuous)",
            "Samples", "Number of samples per frame",
            "TimeSource", "Time source (Use simulation time, Use external signal)",
            "OutDataTypeStr", "Output data type specification",
            "SaturateOnIntegerOverflow", "Handle integer overflow behavior (on/off)"
        );
    }

    @Override
    public String toString() {
        return String.format("SineWaveDto{blockName='%s', amplitude=%.3f, frequency=%.3f, phase=%.3f, bias=%.3f, sampleTime=%.3f}",
                           getBlockName(), getAmplitudeValue(), getFrequencyValue(), getPhaseValue(), getBiasValue(), getSampleTimeValue());
    }
}