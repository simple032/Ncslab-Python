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
 * DTO representation of CheckSignalAttributes verification block.
 *
 * The CheckSignalAttributes block verifies that signal properties match expected
 * specifications including dimensions, data type, sample time, and complexity.
 * It generates warnings or errors when mismatches are detected.
 *
 * SIMULINK Parameters:
 * - Dimensions: Expected signal dimensions [rows, cols] (default: -1 for any)
 * - DimensionsMode: Mode for dimension checking (default: "Ignore")
 * - DataType: Expected data type (default: "Inherit: Same as input")
 * - Complexity: Expected complexity "real" or "complex" (default: "Inherit")
 * - SampleTime: Expected sample time (default: -1 for any)
 * - SeverityLevel: "warning", "error", or "none" (default: "warning")
 * - StopOnError: Stop simulation on error (default: false)
 *
 * @author NCSLab Verification Framework
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("CheckSignalAttributes")
@MigrationCompatible(originalClass = "com.ncslab.block.verification.CheckSignalAttributes")
public class CheckSignalAttributesDto extends BlockDto {

    /**
     * Expected signal dimensions as string "[rows, cols]"
     * Default: "-1" (any dimensions)
     */
    @Builder.Default
    private TypedParameter dimensions = TypedParameter.of("-1");

    /**
     * Dimension checking mode: "Ignore", "Check", "CheckAndReport"
     * Default: "Ignore"
     */
    @Builder.Default
    private TypedParameter dimensionsMode = TypedParameter.of("Ignore");

    /**
     * Expected data type
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter dataType = TypedParameter.of("Inherit: Same as input");

    /**
     * Expected complexity: "real", "complex", or "Inherit"
     * Default: "Inherit"
     */
    @Builder.Default
    private TypedParameter complexity = TypedParameter.of("Inherit");

    /**
     * Expected sample time
     * Default: -1 (any sample time)
     */
    @Builder.Default
    private TypedParameter expectedSampleTime = TypedParameter.of(-1.0);

    /**
     * Severity level: "warning", "error", or "none"
     * Default: "warning"
     */
    @Builder.Default
    private TypedParameter severityLevel = TypedParameter.of("warning");

    /**
     * Stop simulation on error
     * Default: false
     */
    @Builder.Default
    private TypedParameter stopOnError = TypedParameter.of(false);

    /**
     * Sample time for attribute checking
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    // ===== PARAMETER ACCESS HELPERS =====

    public String getDimensionsValue() {
        return dimensions != null ? dimensions.getAsString() : "-1";
    }

    public String getDimensionsModeValue() {
        return dimensionsMode != null ? dimensionsMode.getAsString() : "Ignore";
    }

    public String getDataTypeValue() {
        return dataType != null ? dataType.getAsString() : "Inherit: Same as input";
    }

    public String getComplexityValue() {
        return complexity != null ? complexity.getAsString() : "Inherit";
    }

    public Double getExpectedSampleTimeValue() {
        return expectedSampleTime != null ? expectedSampleTime.getAsDouble() : -1.0;
    }

    public String getSeverityLevelValue() {
        return severityLevel != null ? severityLevel.getAsString() : "warning";
    }

    public Boolean getStopOnErrorValue() {
        return stopOnError != null ? stopOnError.getAsBoolean() : false;
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate severity level
        String severity = getSeverityLevelValue();
        if (!"warning".equalsIgnoreCase(severity) &&
            !"error".equalsIgnoreCase(severity) &&
            !"none".equalsIgnoreCase(severity)) {
            result.addError("Severity level must be 'warning', 'error', or 'none'");
        }

        // Validate dimensions mode
        String dimMode = getDimensionsModeValue();
        if (!"Ignore".equals(dimMode) &&
            !"Check".equals(dimMode) &&
            !"CheckAndReport".equals(dimMode)) {
            result.addError("Dimensions mode must be 'Ignore', 'Check', or 'CheckAndReport'");
        }

        // Validate complexity (informational only - defaults to Inherit)
        String comp = getComplexityValue();
        if (!"real".equalsIgnoreCase(comp) &&
            !"complex".equalsIgnoreCase(comp) &&
            !"Inherit".equals(comp)) {
            // Invalid complexity will use default "Inherit"
        }

        // Validate sample times
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }

        if (expectedSampleTime != null) {
            Double est = expectedSampleTime.getAsDouble();
            if (est != null && est < -1.0) {
                result.addError("Expected sample time must be >= -1.0");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if dimension checking is enabled
     */
    public boolean isDimensionCheckingEnabled() {
        return !"Ignore".equals(getDimensionsModeValue());
    }

    /**
     * Check if this block inherits its sample time
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Check if complexity checking is enabled
     */
    public boolean isComplexityCheckingEnabled() {
        return !"Inherit".equals(getComplexityValue());
    }

    /**
     * Check if data type checking is enabled
     */
    public boolean isDataTypeCheckingEnabled() {
        return !getDataTypeValue().startsWith("Inherit");
    }

    /**
     * Check if this block should generate warnings
     */
    public boolean shouldGenerateWarnings() {
        return "warning".equalsIgnoreCase(getSeverityLevelValue());
    }

    /**
     * Check if this block should generate errors
     */
    public boolean shouldGenerateErrors() {
        return "error".equalsIgnoreCase(getSeverityLevelValue());
    }

    @Override
    public CheckSignalAttributesDto copy() {
        return CheckSignalAttributesDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .dimensions(dimensions != null ? dimensions.copy() : null)
                .dimensionsMode(dimensionsMode != null ? dimensionsMode.copy() : null)
                .dataType(dataType != null ? dataType.copy() : null)
                .complexity(complexity != null ? complexity.copy() : null)
                .expectedSampleTime(expectedSampleTime != null ? expectedSampleTime.copy() : null)
                .severityLevel(severityLevel != null ? severityLevel.copy() : null)
                .stopOnError(stopOnError != null ? stopOnError.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Dimensions", dimensions)
                .put("DimensionsMode", dimensionsMode)
                .put("DataType", dataType)
                .put("Complexity", complexity)
                .put("ExpectedSampleTime", expectedSampleTime)
                .put("SeverityLevel", severityLevel)
                .put("StopOnError", stopOnError)
                .put("SampleTime", sampleTime)
                .build();
    }

    @Override
    public String toString() {
        return String.format("CheckSignalAttributesDto{id=%d, name='%s', type='%s', severity='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getSeverityLevelValue());
    }
}
