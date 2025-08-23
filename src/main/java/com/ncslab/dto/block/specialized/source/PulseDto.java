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
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * DTO for Pulse block - Pulse generator signal source with configurable timing.
 * 
 * <p>This block generates periodic pulse train signals with SIMULINK-compatible parameters:
 * <ul>
 *   <li><b>Amplitude</b>: Pulse amplitude (height of the pulse)</li>
 *   <li><b>Period</b>: Period of the pulse train (positive value)</li>
 *   <li><b>PulseWidth</b>: Width of the pulse (percentage of period or absolute time)</li>
 *   <li><b>PhaseDelay</b>: Phase delay (time offset for pulse start)</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (0 for continuous)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type specification</li>
 *   <li><b>SaturateOnIntegerOverflow</b>: Handle integer overflow behavior</li>
 * </ul>
 * </p>
 * 
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Period must be positive</li>
 *   <li>PulseWidth must be non-negative and finite</li>
 *   <li>Amplitude must be finite</li>
 *   <li>PhaseDelay must be finite</li>
 *   <li>SampleTime must be non-negative or -1 (inherited)</li>
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
@JsonTypeName("Pulse")
public class PulseDto extends BlockDto {

    /**
     * Pulse amplitude.
     * Controls the height/magnitude of the generated pulse.
     */
    @Builder.Default
    private TypedParameter amplitude = TypedParameter.of(1.0);

    /**
     * Period of the pulse train.
     * Must be a positive value defining the time between pulse repetitions.
     */
    @Builder.Default
    private TypedParameter period = TypedParameter.of(1.0);

    /**
     * Width of the pulse.
     * Can be specified as percentage of period (0-100) or absolute time.
     */
    @Builder.Default
    private TypedParameter pulseWidth = TypedParameter.of(50.0);

    /**
     * Phase delay (time offset).
     * Specifies when the pulse train starts relative to simulation time zero.
     */
    @Builder.Default
    private TypedParameter phaseDelay = TypedParameter.of(0.0);

    /**
     * Sample time for discrete operation.
     * Must be positive for discrete-time operation, 0 for continuous, or -1 for inherited.
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(0.0);

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
     * Creates PulseDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     */
    public PulseDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super("Pulse", blockName, blockPath, position, dimension);
    }

    /**
     * Creates PulseDto with comprehensive pulse configuration.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param amplitude Pulse amplitude
     * @param period Period of the pulse train
     * @param pulseWidth Width of the pulse
     * @param phaseDelay Phase delay (time offset)
     */
    public PulseDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension,
                   double amplitude, double period, double pulseWidth, double phaseDelay) {
        super("Pulse", blockName, blockPath, position, dimension);
        this.amplitude = TypedParameter.of(amplitude);
        this.period = TypedParameter.of(period);
        this.pulseWidth = TypedParameter.of(pulseWidth);
        this.phaseDelay = TypedParameter.of(phaseDelay);
        this.sampleTime = TypedParameter.of(0.0); // Continuous
        this.outDataTypeStr = TypedParameter.of("double");
        this.saturateOnIntegerOverflow = TypedParameter.of("off");
    }

    /**
     * Factory method for creating PulseDto from parameter map.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param parameters Typed parameter map
     * @return Configured PulseDto instance
     */
    public static PulseDto fromParameters(String blockName, String blockPath, 
                                         BlockPositionDto position, BlockDimensionDto dimension,
                                         TypedParameterMap parameters) {
        PulseDto dto = new PulseDto(blockName, blockPath, position, dimension);
        
        dto.amplitude = parameters.getTypedParameter("Amplitude", Double.class, 1.0);
        dto.period = parameters.getTypedParameter("Period", Double.class, 1.0);
        dto.pulseWidth = parameters.getTypedParameter("PulseWidth", Double.class, 50.0);
        dto.phaseDelay = parameters.getTypedParameter("PhaseDelay", Double.class, 0.0);
        dto.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, 0.0);
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
        
        // Validate period
        if (period != null && period.getAsDouble() != null) {
            Double periodValue = period.getAsDouble();
            if (periodValue <= 0.0 || Double.isNaN(periodValue) || Double.isInfinite(periodValue)) {
                errors.add("Period must be positive and finite");
            }
        }
        
        // Validate pulse width
        if (pulseWidth != null && pulseWidth.getAsDouble() != null) {
            Double pwValue = pulseWidth.getAsDouble();
            if (pwValue < 0.0 || Double.isNaN(pwValue) || Double.isInfinite(pwValue)) {
                errors.add("Pulse width must be non-negative and finite");
            }
        }
        
        // Validate phase delay
        if (phaseDelay != null && phaseDelay.getAsDouble() != null) {
            Double pdValue = phaseDelay.getAsDouble();
            if (Double.isNaN(pdValue) || Double.isInfinite(pdValue)) {
                errors.add("Phase delay must be finite");
            }
        }
        
        // Validate sample time
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            Double stValue = sampleTime.getAsDouble();
            if (stValue < -1.0 || Double.isNaN(stValue) || Double.isInfinite(stValue)) {
                errors.add("Sample time must be non-negative or -1 (inherited)");
            }
        }
        
        return errors;
    }

    @Override
    public boolean isValidConfiguration() {
        return validateParameters().isEmpty() &&
               amplitude != null && amplitude.getAsDouble() != null &&
               period != null && period.getAsDouble() != null && period.getAsDouble() > 0.0 &&
               pulseWidth != null && pulseWidth.getAsDouble() != null &&
               phaseDelay != null && phaseDelay.getAsDouble() != null &&
               sampleTime != null && sampleTime.getAsDouble() != null;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the pulse amplitude value.
     *
     * @return Pulse amplitude
     */
    public double getAmplitudeValue() {
        return amplitude != null ? amplitude.getAsDouble() : 1.0;
    }

    /**
     * Gets the period value.
     *
     * @return Period of the pulse train
     */
    public double getPeriodValue() {
        return period != null ? period.getAsDouble() : 1.0;
    }

    /**
     * Gets the pulse width value.
     *
     * @return Pulse width (% of period or absolute time)
     */
    public double getPulseWidthValue() {
        return pulseWidth != null ? pulseWidth.getAsDouble() : 50.0;
    }

    /**
     * Gets the phase delay value.
     *
     * @return Phase delay (time offset)
     */
    public double getPhaseDelayValue() {
        return phaseDelay != null ? phaseDelay.getAsDouble() : 0.0;
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time for discrete operation
     */
    public double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : 0.0;
    }

    /**
     * Checks if the pulse operates in continuous time mode.
     *
     * @return true if sample time is 0 (continuous), false otherwise
     */
    public boolean isContinuousTime() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if the pulse operates in discrete time mode.
     *
     * @return true if sample time is positive (discrete), false otherwise
     */
    public boolean isDiscreteTime() {
        return getSampleTimeValue() > 0.0;
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
     * Calculates the duty cycle as a fraction (0.0 to 1.0).
     *
     * @return Duty cycle as fraction of period
     */
    public double getDutyCycleFraction() {
        if (getPulseWidthValue() <= 1.0) {
            // Assume pulse width is already a fraction
            return getPulseWidthValue();
        } else {
            // Assume pulse width is percentage
            return getPulseWidthValue() / 100.0;
        }
    }

    /**
     * Calculates the actual pulse width in time units.
     *
     * @return Pulse width in time units
     */
    public double getPulseWidthTime() {
        if (getPulseWidthValue() <= 1.0) {
            // Pulse width is fraction of period
            return getPulseWidthValue() * getPeriodValue();
        } else if (getPulseWidthValue() <= 100.0) {
            // Pulse width is percentage of period
            return (getPulseWidthValue() / 100.0) * getPeriodValue();
        } else {
            // Pulse width is absolute time
            return getPulseWidthValue();
        }
    }

    // === Helper Methods ===

    @Override
    public PulseDto copy() {
        return PulseDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .position(getPosition())
                .dimension(getDimension())
                .amplitude(amplitude != null ? amplitude.copy() : null)
                .period(period != null ? period.copy() : null)
                .pulseWidth(pulseWidth != null ? pulseWidth.copy() : null)
                .phaseDelay(phaseDelay != null ? phaseDelay.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
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
                .put("Period", period)
                .put("PulseWidth", pulseWidth)
                .put("PhaseDelay", phaseDelay)
                .put("SampleTime", sampleTime)
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
            "Amplitude", "Pulse amplitude (height of the pulse)",
            "Period", "Period of the pulse train (positive value)",
            "PulseWidth", "Width of the pulse (% of period or absolute time)",
            "PhaseDelay", "Phase delay (time offset for pulse start)",
            "SampleTime", "Sample time for discrete operation (0 for continuous)",
            "OutDataTypeStr", "Output data type specification",
            "SaturateOnIntegerOverflow", "Handle integer overflow behavior (on/off)"
        );
    }

    @Override
    public String toString() {
        return String.format("PulseDto{blockName='%s', amplitude=%.3f, period=%.3f, pulseWidth=%.1f%%, phaseDelay=%.3f}",
                           getBlockName(), getAmplitudeValue(), getPeriodValue(), getPulseWidthValue(), getPhaseDelayValue());
    }
}