package com.ncslab.dto.block.specialized.discrete;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.block.discrete.DiscreteBlockDto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * DTO representation of TappedDelay block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the TappedDelay block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * SIMULINK Parameters:
 * - NumDelays: Number of delay taps (default: 2, minimum: 1)
 * - DelayOrder: Output ordering - "Oldest first" or "Newest first" (default: "Oldest first")
 * - SampleTime: Sample time for discrete operation (must be positive or -1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - InitialCondition: Initial values for delay buffer (default: 0.0)
 *
 * Tapped Delay Block Behavior:
 * - Oldest first: output = [x[k], x[k-1], x[k-2], ..., x[k-NumDelays]]
 * - Newest first: output = [x[k-NumDelays], ..., x[k-2], x[k-1], x[k]]
 *
 * The Tapped Delay block implements a multi-tap delay line commonly used in FIR filter
 * structures. It maintains a delay buffer and outputs the current input plus NumDelays
 * previous input values as a vector.
 *
 * Key Features:
 * - Multi-tap delay line with configurable number of taps
 * - Vector output containing current + NumDelays delayed samples
 * - Configurable output ordering (oldest-first or newest-first)
 * - No feedthrough - breaks algebraic loops
 *
 * @author NCSLab
 * @version 1.0
 * @since Tapped Delay Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("TappedDelay")
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.TappedDelay")
public class TappedDelayDto extends DiscreteBlockDto {

    // ===== TAPPED DELAY SPECIFIC PARAMETERS =====

    /**
     * Number of delay taps
     * Default: 2
     * Validation: Must be >= 1
     * This determines the size of the delay buffer and output vector size (NumDelays + 1)
     */
    @Builder.Default
    private TypedParameter numDelays = TypedParameter.of(2);

    /**
     * Delay output ordering
     * Default: "Oldest first"
     * Valid values: "Oldest first", "Newest first"
     * - "Oldest first": [x[k], x[k-1], ..., x[k-N]]
     * - "Newest first": [x[k-N], ..., x[k-1], x[k]]
     */
    @Builder.Default
    private TypedParameter delayOrder = TypedParameter.of("Oldest first");

    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");

    /**
     * Initial values for delay buffer
     * Default: 0.0
     * Validation: Must be finite number
     */
    @Builder.Default
    private TypedParameter initialCondition = TypedParameter.of(0.0);

    // ===== VALID PARAMETER VALUES =====

    private static final List<String> VALID_DELAY_ORDERS = Arrays.asList(
        "Oldest first", "Newest first"
    );

    // ===== PARAMETER ACCESS HELPERS =====

    public Integer getNumDelaysValue() {
        return numDelays != null ? numDelays.getValue(Integer.class) : 2;
    }

    public String getDelayOrderValue() {
        return delayOrder != null ? delayOrder.getAsString() : "Oldest first";
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }

    public Double getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getValue(Double.class) : 0.0;
    }

    // ===== DISCRETE-TIME VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate numDelays
        if (numDelays != null) {
            try {
                Integer numDelaysVal = numDelays.getValue(Integer.class);
                if (numDelaysVal != null && numDelaysVal < 1) {
                    result.addError("NumDelays must be at least 1");
                }
            } catch (Exception e) {
                result.addError("Invalid NumDelays format: " + e.getMessage());
            }
        } else {
            result.addError("NumDelays parameter is required");
        }

        // Validate delayOrder
        if (delayOrder != null) {
            String order = delayOrder.getAsString();
            if (order != null && !VALID_DELAY_ORDERS.contains(order)) {
                result.addError("DelayOrder must be 'Oldest first' or 'Newest first', got: " + order);
            }
        } else {
            result.addError("DelayOrder parameter is required");
        }

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

        // Validate discrete sample time
        if (getSampleTime() != null) {
            double sampleTime = getSampleTime().getAsDouble();
            if (sampleTime != -1.0 && sampleTime <= 0.0) {
                result.addError("Sample time must be positive or -1 (inherited)");
            }
            if (Double.isNaN(sampleTime) || Double.isInfinite(sampleTime)) {
                result.addError("Sample time must be finite");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if delay order is "Oldest first"
     */
    public boolean isOldestFirst() {
        return "Oldest first".equals(getDelayOrderValue());
    }

    /**
     * Check if delay order is "Newest first"
     */
    public boolean isNewestFirst() {
        return "Newest first".equals(getDelayOrderValue());
    }

    /**
     * Get the output vector width (NumDelays + 1)
     */
    public int getOutputWidth() {
        return getNumDelaysValue() + 1;
    }

    /**
     * Create TappedDelay block with specific parameters
     */
    public static TappedDelayDto create(String name, String path, int numDelays, String delayOrder, double sampleTime) {
        return TappedDelayDto.builder()
                .blockName(name)
                .blockPath(path)
                .blockUUID("null")
                .numDelays(TypedParameter.of(numDelays))
                .delayOrder(TypedParameter.of(delayOrder))
                .sampleTime(TypedParameter.of(sampleTime))
                .outDataTypeStr(TypedParameter.of("Inherit: Same as input"))
                .initialCondition(TypedParameter.of(0.0))
                .build();
    }

    /**
     * Create TappedDelay block with inherited sample time
     */
    public static TappedDelayDto createInherited(String name, String path, int numDelays, String delayOrder) {
        return create(name, path, numDelays, delayOrder, -1.0);
    }

    /**
     * Create TappedDelay block with default parameters (2 taps, oldest first)
     */
    public static TappedDelayDto createDefault(String name, String path, double sampleTime) {
        return create(name, path, 2, "Oldest first", sampleTime);
    }

    /**
     * Check if this block is suitable for breaking algebraic loops
     * Tapped delay blocks always break algebraic loops (no feedthrough)
     */
    public boolean isAlgebraicLoopBreaker() {
        return true; // Tapped delay blocks always break algebraic loops
    }

    @Override
    public TappedDelayDto copy() {
        return TappedDelayDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .numDelays(numDelays != null ? numDelays.copy() : null)
                .delayOrder(delayOrder != null ? delayOrder.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();

        if (numDelays != null) {
            params.put("NumDelays", String.valueOf(numDelays.getValue()));
        }
        if (delayOrder != null) {
            params.put("DelayOrder", String.valueOf(delayOrder.getValue()));
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", String.valueOf(outDataTypeStr.getValue()));
        }
        if (initialCondition != null) {
            params.put("InitialCondition", String.valueOf(initialCondition.getValue()));
        }

        return params;
    }

    /**
     * Convert to parameter map for Block constructor
     */
    @Override
    public com.ncslab.dto.common.TypedParameterMap toParameterMap() {
        com.ncslab.dto.common.TypedParameterMap params = new com.ncslab.dto.common.TypedParameterMap();

        params.put("NumDelays", numDelays);
        params.put("DelayOrder", delayOrder);
        params.put("SampleTime", getSampleTime());
        params.put("OutDataTypeStr", outDataTypeStr);
        params.put("InitialCondition", initialCondition);

        return params;
    }

    @Override
    public String toString() {
        return String.format("TappedDelayDto{id=%d, name='%s', type='%s', numDelays=%d, delayOrder='%s', sampleTime=%s, initialCondition=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getNumDelaysValue(),
                           getDelayOrderValue(),
                           getSampleTime(),
                           getInitialConditionValue());
    }
}
