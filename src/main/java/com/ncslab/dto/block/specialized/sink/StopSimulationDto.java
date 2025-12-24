package com.ncslab.dto.block.specialized.sink;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.block.sink.SinkDto;
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
 * DTO representation of StopSimulation sink block.
 *
 * The StopSimulation block stops simulation when the input signal
 * becomes nonzero or meets a specified stop condition.
 *
 * SIMULINK Parameters:
 * - StopCondition: Condition for stopping ("nonzero", ">0", "<0", "==value")
 * - StopMessage: Custom message when simulation stops
 * - SampleTime: Sample time for condition checking (-1 for inherited)
 *
 * @author NCSLab Verification Framework
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("StopSimulation")
@MigrationCompatible(originalClass = "com.ncslab.block.sink.StopSimulation")
public class StopSimulationDto extends SinkDto {

    /**
     * Stop condition: "nonzero", ">0", "<0", "==value"
     * Default: "nonzero"
     */
    @Builder.Default
    private TypedParameter stopCondition = TypedParameter.of("nonzero");

    /**
     * Comparison value for "==value" condition
     * Default: 1.0
     */
    @Builder.Default
    private TypedParameter comparisonValue = TypedParameter.of(1.0);

    /**
     * Custom message to display when simulation stops
     * Default: "Simulation stopped by StopSimulation block"
     */
    @Builder.Default
    private TypedParameter stopMessage = TypedParameter.of("Simulation stopped by StopSimulation block");

    /**
     * Sample time for condition checking
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    // ===== PARAMETER ACCESS HELPERS =====

    public String getStopConditionValue() {
        return stopCondition != null ? stopCondition.getAsString() : "nonzero";
    }

    public Double getComparisonValueValue() {
        return comparisonValue != null ? comparisonValue.getAsDouble() : 1.0;
    }

    public String getStopMessageValue() {
        return stopMessage != null ? stopMessage.getAsString() : "Simulation stopped by StopSimulation block";
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate stop condition
        String condition = getStopConditionValue();
        if (!"nonzero".equals(condition) &&
            !">0".equals(condition) &&
            !"<0".equals(condition) &&
            !"==value".equals(condition)) {
            result.addError("Stop condition must be 'nonzero', '>0', '<0', or '==value'");
        }

        // Validate stop message is not empty (optional - will use default)
        String msg = getStopMessageValue();
        if (msg == null || msg.trim().isEmpty()) {
            // Stop message can be empty, just use default
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
     * Check if stop condition uses comparison value
     */
    public boolean usesComparisonValue() {
        return "==value".equals(getStopConditionValue());
    }

    /**
     * Evaluate if input meets stop condition
     *
     * @param inputValue the input signal value to check
     * @return true if stop condition is met
     */
    public boolean meetsStopCondition(double inputValue) {
        String condition = getStopConditionValue();
        switch (condition) {
            case "nonzero":
                return inputValue != 0.0;
            case ">0":
                return inputValue > 0.0;
            case "<0":
                return inputValue < 0.0;
            case "==value":
                return Math.abs(inputValue - getComparisonValueValue()) < 1e-10;
            default:
                return inputValue != 0.0;
        }
    }

    @Override
    public StopSimulationDto copy() {
        return StopSimulationDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .stopCondition(stopCondition != null ? stopCondition.copy() : null)
                .comparisonValue(comparisonValue != null ? comparisonValue.copy() : null)
                .stopMessage(stopMessage != null ? stopMessage.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("StopCondition", stopCondition)
                .put("ComparisonValue", comparisonValue)
                .put("StopMessage", stopMessage)
                .put("SampleTime", sampleTime)
                .build();
    }

    @Override
    public String toString() {
        return String.format("StopSimulationDto{id=%d, name='%s', type='%s', condition='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getStopConditionValue());
    }
}
