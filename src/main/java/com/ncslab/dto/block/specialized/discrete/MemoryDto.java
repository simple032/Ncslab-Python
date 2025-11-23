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

import java.util.Map;
import java.util.HashMap;

/**
 * DTO representation of Memory block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the Memory block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * SIMULINK Parameters:
 * - InitialCondition: Initial condition for the delay (default: 0)
 * - SampleTime: Sample time for discrete operation (must be positive or -1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * Memory Block Behavior:
 * y[k] = u[k-1]
 *
 * The Memory block is specifically designed to break algebraic loops in feedback systems.
 * It stores the input from the previous time step and outputs it at the current time step.
 * Similar to UnitDelay but optimized for algebraic loop resolution.
 *
 * @author NCSLab
 * @version 1.0
 * @since Quick-Win Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Memory")
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.Memory")
public class MemoryDto extends DiscreteBlockDto {

    // ===== MEMORY SPECIFIC PARAMETERS =====

    /**
     * Initial condition for the memory block
     * Default: 0
     * Validation: Must be finite number
     */
    @Builder.Default
    private TypedParameter initialCondition = TypedParameter.of(0.0);

    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");

    /**
     * Handle integer overflow
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);

    // ===== PARAMETER ACCESS HELPERS =====

    public Double getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getValue(Double.class) : 0.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }

    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getValue(Boolean.class) : false;
    }

    // ===== DISCRETE-TIME VALIDATION =====

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
     * Create Memory block with specific initial condition and sample time
     */
    public static MemoryDto create(String name, String path, double initialCondition, double sampleTime) {
        return MemoryDto.builder()
                .blockName(name)
                .blockPath(path)
                .blockUUID("null")
                .sampleTime(TypedParameter.of(sampleTime))
                .initialCondition(TypedParameter.of(initialCondition))
                .outDataTypeStr(TypedParameter.of("Inherit: Same as input"))
                .saturateOnIntegerOverflow(TypedParameter.of(false))
                .build();
    }

    /**
     * Create Memory block with inherited sample time
     */
    public static MemoryDto createInherited(String name, String path, double initialCondition) {
        return create(name, path, initialCondition, -1.0);
    }

    /**
     * Check if this block is suitable for breaking algebraic loops
     * Memory blocks are specifically designed for this purpose
     */
    public boolean isAlgebraicLoopBreaker() {
        return true; // Memory blocks always break algebraic loops
    }

    @Override
    public MemoryDto copy() {
        return MemoryDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();

        if (initialCondition != null) {
            params.put("InitialCondition", String.valueOf(initialCondition.getValue()));
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", getSampleTime().getAsString());
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", String.valueOf(outDataTypeStr.getValue()));
        }
        if (saturateOnIntegerOverflow != null) {
            Boolean satVal = saturateOnIntegerOverflow.getValue(Boolean.class);
            params.put("SaturateOnIntegerOverflow", Boolean.TRUE.equals(satVal) ? "on" : "off");
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("MemoryDto{id=%d, name='%s', type='%s', initialCondition=%s, sampleTime=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getInitialConditionValue(),
                           getSampleTime());
    }
}
