package com.ncslab.dto.block.specialized.signal;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO representation of IC (Initial Condition) block for signal initialization.
 *
 * The IC block sets the initial condition for a signal at the start of simulation,
 * then passes the input through unchanged:
 * - At t=0: Outputs the InitialCondition value
 * - At t>0: Transparently passes through the input signal
 *
 * Common use: Initialize integrators, delays, and other stateful blocks.
 *
 * SIMULINK Equivalent: IC block
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("IC")
@MigrationCompatible(originalClass = "com.ncslab.block.signal.IC")
public class ICDto extends BlockDto {

    /**
     * Initial value to output before first input arrives
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter initialCondition = TypedParameter.of(0.0);

    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");

    // ===== PARAMETER ACCESS HELPERS =====

    public Double getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getAsDouble() : 0.0;
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate initial condition (must be finite)
        if (initialCondition != null) {
            Double ic = initialCondition.getAsDouble();
            if (ic != null && !Double.isFinite(ic)) {
                result.addError("Initial condition must be finite (not NaN or Infinite)");
            }
        }

        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if this block operates in continuous time
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Check if this block inherits its sample time
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Get the number of input ports (always 1 for IC block)
     */
    public int getInputPortCount() {
        return 1;
    }

    /**
     * Get the number of output ports (always 1 for IC block)
     */
    public int getOutputPortCount() {
        return 1;
    }

    @Override
    public ICDto copy() {
        return ICDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("InitialCondition", initialCondition)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    @Override
    public String toString() {
        return String.format("ICDto{id=%d, name='%s', type='%s', initialCondition=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getInitialConditionValue());
    }
}
