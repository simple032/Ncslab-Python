package com.ncslab.dto.block.specialized.signal;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;

/**
 * DTO representation of Rate Transition block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the Rate Transition block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * SIMULINK Parameters:
 * - InputSampleTime: Input block sample rate (-1 for inherited)
 * - OutputSampleTime: Output block sample rate (-1 for inherited)
 * - InitialCondition: Initial output value (default: 0)
 * - OutDataTypeStr: Output data type specification
 * - EnsureDataIntegrity: Ensure deterministic output for code generation
 * - X0: Initial condition for internal state
 *
 * Rate Transition Behavior:
 * The Rate Transition block handles sample rate transitions between blocks
 * with different rates, providing buffering and interpolation as needed.
 *
 * Fast-to-Slow (downsampling):
 * - Samples the fast input at the slower output rate
 * - Uses zero-order hold (holds last value)
 *
 * Slow-to-Fast (upsampling):
 * - Repeats the slow input value at the faster output rate
 * - Uses zero-order hold interpolation
 *
 * Implementation Modes:
 * 1. "Ensure data integrity during data transfer" (default) - standard rate transition
 * 2. "Ensure deterministic data transfer (maximum delay)" - deterministic for code generation
 * 3. "Specify explicit sample time" - explicit sample time specification
 *
 * @author NCSLab Team
 * @version 1.0
 * @since Rate Transition Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("RateTransition")
@MigrationCompatible(originalClass = "com.ncslab.block.signal.RateTransition")
public class RateTransitionDto extends BlockDto {

    // ===== RATE TRANSITION SPECIFIC PARAMETERS =====

    /**
     * Input sample time
     * Default: -1 (inherited from input block)
     * Validation: Must be positive or -1
     */
    private TypedParameter inputSampleTime;

    /**
     * Output sample time
     * Default: -1 (inherited from output block)
     * Validation: Must be positive or -1
     */
    private TypedParameter outputSampleTime;

    /**
     * Initial condition for output
     * Default: 0
     * Validation: Must be finite number
     */
    private TypedParameter initialCondition;

    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    private TypedParameter outDataTypeStr;

    /**
     * Ensure data integrity during data transfer
     * Default: true (on)
     */
    private TypedParameter ensureDataIntegrity;

    /**
     * Initial condition for internal state
     * Default: 0
     * Validation: Must be finite number
     */
    private TypedParameter x0;

    // ===== PARAMETER ACCESS HELPERS =====

    public Double getInputSampleTimeValue() {
        return inputSampleTime != null ? inputSampleTime.getValue(Double.class) : -1.0;
    }

    public Double getOutputSampleTimeValue() {
        return outputSampleTime != null ? outputSampleTime.getValue(Double.class) : -1.0;
    }

    public Double getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getValue(Double.class) : 0.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }

    public Boolean getEnsureDataIntegrityValue() {
        return ensureDataIntegrity != null ? ensureDataIntegrity.getValue(Boolean.class) : true;
    }

    public Double getX0Value() {
        return x0 != null ? x0.getValue(Double.class) : 0.0;
    }

    // ===== RATE TRANSITION ANALYSIS =====

    /**
     * Determine if this is a fast-to-slow rate transition (downsampling)
     */
    public boolean isFastToSlow() {
        if (inputSampleTime == null || outputSampleTime == null) {
            return false;
        }

        double inRate = inputSampleTime.getAsDouble();
        double outRate = outputSampleTime.getAsDouble();

        // Both must be explicit (not inherited)
        if (inRate <= 0 || outRate <= 0) {
            return false;
        }

        // Fast-to-slow: input rate is smaller (faster) than output rate
        return inRate < outRate;
    }

    /**
     * Determine if this is a slow-to-fast rate transition (upsampling)
     */
    public boolean isSlowToFast() {
        if (inputSampleTime == null || outputSampleTime == null) {
            return false;
        }

        double inRate = inputSampleTime.getAsDouble();
        double outRate = outputSampleTime.getAsDouble();

        // Both must be explicit (not inherited)
        if (inRate <= 0 || outRate <= 0) {
            return false;
        }

        // Slow-to-fast: input rate is larger (slower) than output rate
        return inRate > outRate;
    }

    /**
     * Determine if this is a same-rate transition (pass-through)
     */
    public boolean isSameRate() {
        if (inputSampleTime == null || outputSampleTime == null) {
            return true; // Assume same rate if not specified
        }

        double inRate = inputSampleTime.getAsDouble();
        double outRate = outputSampleTime.getAsDouble();

        // Both inherited or both equal
        if (inRate == -1.0 && outRate == -1.0) {
            return true;
        }

        if (inRate <= 0 || outRate <= 0) {
            return true; // Cannot determine, assume same
        }

        return Math.abs(inRate - outRate) < 1e-9;
    }

    /**
     * Calculate the rate conversion ratio
     * Returns output_rate / input_rate
     */
    public double getRateRatio() {
        if (inputSampleTime == null || outputSampleTime == null) {
            return 1.0;
        }

        double inRate = inputSampleTime.getAsDouble();
        double outRate = outputSampleTime.getAsDouble();

        if (inRate <= 0 || outRate <= 0) {
            return 1.0;
        }

        return outRate / inRate;
    }

    /**
     * Get the downsampling factor (for fast-to-slow)
     * Returns how many input samples per output sample
     */
    public int getDownsamplingFactor() {
        if (!isFastToSlow()) {
            return 1;
        }

        return (int) Math.round(getRateRatio());
    }

    /**
     * Get the upsampling factor (for slow-to-fast)
     * Returns how many output samples per input sample
     */
    public int getUpsamplingFactor() {
        if (!isSlowToFast()) {
            return 1;
        }

        return (int) Math.round(1.0 / getRateRatio());
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate initial condition
        if (initialCondition != null) {
            try {
                Double icVal = initialCondition.getValue(Double.class);
                if (icVal != null && (Double.isNaN(icVal) || Double.isInfinite(icVal))) {
                    result.addError("Initial condition must be finite");
                }
            } catch (Exception e) {
                result.addError("Invalid initial condition format: " + e.getMessage());
            }
        }

        // Validate x0
        if (x0 != null) {
            try {
                Double x0Val = x0.getValue(Double.class);
                if (x0Val != null && (Double.isNaN(x0Val) || Double.isInfinite(x0Val))) {
                    result.addError("X0 initial state must be finite");
                }
            } catch (Exception e) {
                result.addError("Invalid X0 format: " + e.getMessage());
            }
        }

        // Validate input sample time
        if (inputSampleTime != null) {
            double inSampleTime = inputSampleTime.getAsDouble();
            if (inSampleTime != -1.0 && inSampleTime <= 0.0) {
                result.addError("Input sample time must be positive or -1 (inherited)");
            }
            if (Double.isNaN(inSampleTime) || Double.isInfinite(inSampleTime)) {
                result.addError("Input sample time must be finite");
            }
        }

        // Validate output sample time
        if (outputSampleTime != null) {
            double outSampleTime = outputSampleTime.getAsDouble();
            if (outSampleTime != -1.0 && outSampleTime <= 0.0) {
                result.addError("Output sample time must be positive or -1 (inherited)");
            }
            if (Double.isNaN(outSampleTime) || Double.isInfinite(outSampleTime)) {
                result.addError("Output sample time must be finite");
            }
        }

        // Validate rate transition consistency
        if (inputSampleTime != null && outputSampleTime != null) {
            double inRate = inputSampleTime.getAsDouble();
            double outRate = outputSampleTime.getAsDouble();

            // Both cannot be inherited
            if (inRate == -1.0 && outRate == -1.0) {
                result.addWarning("Both input and output sample times are inherited - rate transition may not function correctly");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Create rate transition with explicit sample times
     */
    public static RateTransitionDto create(String name, String path, double inputSampleTime,
                                          double outputSampleTime, double initialCondition) {
        RateTransitionDto dto = new RateTransitionDto();
        dto.setBlockName(name);
        dto.setBlockPath(path);

        // Set parameters
        dto.setInputSampleTime(TypedParameter.of(inputSampleTime));
        dto.setOutputSampleTime(TypedParameter.of(outputSampleTime));
        dto.setInitialCondition(TypedParameter.of(initialCondition));
        dto.setX0(TypedParameter.of(initialCondition));

        // Set defaults
        dto.setOutDataTypeStr(TypedParameter.of("Inherit: Same as input"));
        dto.setEnsureDataIntegrity(TypedParameter.of(true));

        return dto;
    }

    /**
     * Create rate transition with inherited sample times
     */
    public static RateTransitionDto createInherited(String name, String path) {
        return create(name, path, -1.0, -1.0, 0.0);
    }

    /**
     * Create fast-to-slow rate transition (downsampling)
     */
    public static RateTransitionDto createFastToSlow(String name, String path,
                                                     double fastRate, double slowRate) {
        if (fastRate >= slowRate) {
            throw new IllegalArgumentException("Fast rate must be less than slow rate");
        }
        return create(name, path, fastRate, slowRate, 0.0);
    }

    /**
     * Create slow-to-fast rate transition (upsampling)
     */
    public static RateTransitionDto createSlowToFast(String name, String path,
                                                     double slowRate, double fastRate) {
        if (slowRate <= fastRate) {
            throw new IllegalArgumentException("Slow rate must be greater than fast rate");
        }
        return create(name, path, slowRate, fastRate, 0.0);
    }

    @Override
    public RateTransitionDto copy() {
        return RateTransitionDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .inputSampleTime(inputSampleTime != null ? inputSampleTime.copy() : null)
                .outputSampleTime(outputSampleTime != null ? outputSampleTime.copy() : null)
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .ensureDataIntegrity(ensureDataIntegrity != null ? ensureDataIntegrity.copy() : null)
                .x0(x0 != null ? x0.copy() : null)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();

        if (inputSampleTime != null) {
            params.put("InputSampleTime", String.valueOf(inputSampleTime.getValue()));
        }
        if (outputSampleTime != null) {
            params.put("OutputSampleTime", String.valueOf(outputSampleTime.getValue()));
        }
        if (initialCondition != null) {
            params.put("InitialCondition", String.valueOf(initialCondition.getValue()));
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", String.valueOf(outDataTypeStr.getValue()));
        }
        if (ensureDataIntegrity != null) {
            Boolean integrity = ensureDataIntegrity.getValue(Boolean.class);
            params.put("EnsureDataIntegrity", Boolean.TRUE.equals(integrity) ? "on" : "off");
        }
        if (x0 != null) {
            params.put("X0", String.valueOf(x0.getValue()));
        }

        return params;
    }

    @Override
    public String toString() {
        String transitionType = "unknown";
        if (isFastToSlow()) {
            transitionType = String.format("fast-to-slow (%dx downsample)", getDownsamplingFactor());
        } else if (isSlowToFast()) {
            transitionType = String.format("slow-to-fast (%dx upsample)", getUpsamplingFactor());
        } else if (isSameRate()) {
            transitionType = "same-rate (pass-through)";
        }

        return String.format("RateTransitionDto{id=%d, name='%s', type='%s', transition=%s, inRate=%s, outRate=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           transitionType,
                           getInputSampleTimeValue(),
                           getOutputSampleTimeValue());
    }
}
