package com.ncslab.dto.block.specialized.verification;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of Assert verification block.
 *
 * The Assert block verifies that an input signal meets a specified condition
 * during simulation. If the assertion fails, it can log an error and optionally
 * stop the simulation.
 *
 * SIMULINK Parameters:
 * - Assertion: Condition to verify (boolean or >0)
 * - ErrorMessage: Custom error message when assertion fails
 * - StopWhenAssertionFail: Stop simulation on failure (default: true)
 * - SampleTime: Sample time for assertion checking (-1 for inherited)
 *
 * @author NCSLab Verification Framework
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Assert")
@MigrationCompatible(originalClass = "com.ncslab.block.verification.Assert")
public class AssertDto extends BlockDto {

    /**
     * Error message to display when assertion fails
     * Default: "Assertion failed"
     */
    @Builder.Default
    private TypedParameter errorMessage = TypedParameter.of("Assertion failed");

    /**
     * Whether to stop simulation when assertion fails
     * Default: true
     */
    @Builder.Default
    private TypedParameter stopWhenAssertionFail = TypedParameter.of(true);

    /**
     * Sample time for assertion checking
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    /**
     * Enabled status of the assertion
     * Default: true
     */
    @Builder.Default
    private TypedParameter enabled = TypedParameter.of(true);

    // ===== PARAMETER ACCESS HELPERS =====

    public String getErrorMessageValue() {
        return errorMessage != null ? errorMessage.getAsString() : "Assertion failed";
    }

    public Boolean getStopWhenAssertionFailValue() {
        return stopWhenAssertionFail != null ? stopWhenAssertionFail.getAsBoolean() : true;
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public Boolean getEnabledValue() {
        return enabled != null ? enabled.getAsBoolean() : true;
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate error message is not empty (optional warning)
        String msg = getErrorMessageValue();
        if (msg == null || msg.trim().isEmpty()) {
            // Error message can be empty, just use default - this is informational only
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
     * Check if this block inherits its sample time
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Check if this block operates in continuous time
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Check if assertions are enabled
     */
    public boolean isEnabled() {
        return getEnabledValue();
    }

    /**
     * Check if simulation should stop on failure
     */
    public boolean shouldStopOnFailure() {
        return getStopWhenAssertionFailValue();
    }

    @Override
    public AssertDto copy() {
        return AssertDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .errorMessage(errorMessage != null ? errorMessage.copy() : null)
                .stopWhenAssertionFail(stopWhenAssertionFail != null ? stopWhenAssertionFail.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .enabled(enabled != null ? enabled.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("ErrorMessage", errorMessage)
                .put("StopWhenAssertionFail", stopWhenAssertionFail)
                .put("SampleTime", sampleTime)
                .put("Enabled", enabled)
                .build();
    }

    @Override
    public String toString() {
        return String.format("AssertDto{id=%d, name='%s', type='%s', stopOnFail=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getStopWhenAssertionFailValue());
    }
}
