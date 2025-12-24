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
 * DTO representation of Signal Specification block for signal property enforcement.
 *
 * The Signal Specification block specifies and enforces signal properties:
 * - Dimensions: Expected signal dimensions (e.g., "1", "[3]", "[2 3]")
 * - Data Type: Expected data type
 * - Sample Time: Expected sample time
 * - Complexity: Expected complexity (real, complex, auto)
 * - Enforcement flags for each property
 *
 * SIMULINK Equivalent: Signal Specification block
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("SignalSpecification")
@MigrationCompatible(originalClass = "com.ncslab.block.signal.SignalSpecification")
public class SignalSpecificationDto extends BlockDto {

    /**
     * Expected signal dimensions
     * Valid values: "-1" (inherit), "N" (scalar), "[N]" (vector), "[M N]" (matrix)
     * Default: "-1" (inherit)
     */
    @Builder.Default
    private TypedParameter dimensions = TypedParameter.of("-1");

    /**
     * Dimensions mode
     * Valid values: "Inherit", "Fixed"
     * Default: "Inherit"
     */
    @Builder.Default
    private TypedParameter dimensionsMode = TypedParameter.of("Inherit");

    /**
     * Expected data type
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter dataType = TypedParameter.of("Inherit: Same as input");

    /**
     * Expected sample time
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    /**
     * Expected complexity
     * Valid values: "real", "complex", "auto"
     * Default: "auto"
     */
    @Builder.Default
    private TypedParameter complexity = TypedParameter.of("auto");

    /**
     * Enforce dimension check
     * Valid values: "on", "off"
     * Default: "on"
     */
    @Builder.Default
    private TypedParameter enforceDimensions = TypedParameter.of("on");

    /**
     * Enforce data type check
     * Valid values: "on", "off"
     * Default: "off"
     */
    @Builder.Default
    private TypedParameter enforceDataType = TypedParameter.of("off");

    /**
     * Enforce sample time check
     * Valid values: "on", "off"
     * Default: "off"
     */
    @Builder.Default
    private TypedParameter enforceSampleTime = TypedParameter.of("off");

    /**
     * Enforce complexity check
     * Valid values: "on", "off"
     * Default: "off"
     */
    @Builder.Default
    private TypedParameter enforceComplexity = TypedParameter.of("off");

    // ===== PARAMETER ACCESS HELPERS =====

    public String getDimensionsValue() {
        return dimensions != null ? dimensions.getAsString() : "-1";
    }

    public String getDimensionsModeValue() {
        return dimensionsMode != null ? dimensionsMode.getAsString() : "Inherit";
    }

    public String getDataTypeValue() {
        return dataType != null ? dataType.getAsString() : "Inherit: Same as input";
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getComplexityValue() {
        return complexity != null ? complexity.getAsString() : "auto";
    }

    public String getEnforceDimensionsValue() {
        return enforceDimensions != null ? enforceDimensions.getAsString() : "on";
    }

    public String getEnforceDataTypeValue() {
        return enforceDataType != null ? enforceDataType.getAsString() : "off";
    }

    public String getEnforceSampleTimeValue() {
        return enforceSampleTime != null ? enforceSampleTime.getAsString() : "off";
    }

    public String getEnforceComplexityValue() {
        return enforceComplexity != null ? enforceComplexity.getAsString() : "off";
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate dimensions mode
        if (dimensionsMode != null) {
            String mode = dimensionsMode.getAsString();
            if (mode != null && !isValidDimensionsMode(mode)) {
                result.addError("Dimensions mode must be one of: Inherit, Fixed");
            }
        }

        // Validate complexity
        if (complexity != null) {
            String comp = complexity.getAsString();
            if (comp != null && !isValidComplexity(comp)) {
                result.addError("Complexity must be one of: real, complex, auto");
            }
        }

        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }

        // Validate dimensions format (basic validation)
        if (dimensions != null) {
            String dims = dimensions.getAsString();
            if (dims != null && !isValidDimensionFormat(dims)) {
                result.addError("Invalid dimension format. Use: '-1', 'N', '[N]', or '[M N]'");
            }
        }

        // Validate enforcement flags
        validateEnforcementFlag("EnforceDimensions", enforceDimensions, result);
        validateEnforcementFlag("EnforceDataType", enforceDataType, result);
        validateEnforcementFlag("EnforceSampleTime", enforceSampleTime, result);
        validateEnforcementFlag("EnforceComplexity", enforceComplexity, result);

        return result;
    }

    /**
     * Check if the dimensions mode is valid
     */
    private boolean isValidDimensionsMode(String mode) {
        return mode.equals("Inherit") || mode.equals("Fixed");
    }

    /**
     * Check if the complexity is valid
     */
    private boolean isValidComplexity(String comp) {
        return comp.equals("real") || comp.equals("complex") || comp.equals("auto");
    }

    /**
     * Basic validation of dimension format
     */
    private boolean isValidDimensionFormat(String dims) {
        if (dims == null || dims.isEmpty()) {
            return false;
        }

        // Allow "-1" (inherit)
        if ("-1".equals(dims)) {
            return true;
        }

        // Allow "N" (scalar)
        if (dims.matches("^\\d+$")) {
            return true;
        }

        // Allow "[N]" (vector) or "[M N]" (matrix)
        if (dims.matches("^\\[\\s*\\d+\\s*\\]$") ||
            dims.matches("^\\[\\s*\\d+\\s+\\d+\\s*\\]$")) {
            return true;
        }

        return false;
    }

    /**
     * Validate enforcement flag (must be "on" or "off")
     */
    private void validateEnforcementFlag(String flagName, TypedParameter flag,
                                         ValidationResult result) {
        if (flag != null) {
            String value = flag.getAsString();
            if (value != null && !value.equals("on") && !value.equals("off")) {
                result.addError(flagName + " must be 'on' or 'off'");
            }
        }
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if dimension enforcement is enabled
     */
    public boolean isDimensionsEnforced() {
        return "on".equalsIgnoreCase(getEnforceDimensionsValue());
    }

    /**
     * Check if data type enforcement is enabled
     */
    public boolean isDataTypeEnforced() {
        return "on".equalsIgnoreCase(getEnforceDataTypeValue());
    }

    /**
     * Check if sample time enforcement is enabled
     */
    public boolean isSampleTimeEnforced() {
        return "on".equalsIgnoreCase(getEnforceSampleTimeValue());
    }

    /**
     * Check if complexity enforcement is enabled
     */
    public boolean isComplexityEnforced() {
        return "on".equalsIgnoreCase(getEnforceComplexityValue());
    }

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
     * Check if dimensions mode is fixed
     */
    public boolean isFixedDimensions() {
        return "Fixed".equalsIgnoreCase(getDimensionsModeValue());
    }

    @Override
    public SignalSpecificationDto copy() {
        return SignalSpecificationDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .dimensions(dimensions != null ? dimensions.copy() : null)
                .dimensionsMode(dimensionsMode != null ? dimensionsMode.copy() : null)
                .dataType(dataType != null ? dataType.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .complexity(complexity != null ? complexity.copy() : null)
                .enforceDimensions(enforceDimensions != null ? enforceDimensions.copy() : null)
                .enforceDataType(enforceDataType != null ? enforceDataType.copy() : null)
                .enforceSampleTime(enforceSampleTime != null ? enforceSampleTime.copy() : null)
                .enforceComplexity(enforceComplexity != null ? enforceComplexity.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Dimensions", dimensions)
                .put("DimensionsMode", dimensionsMode)
                .put("DataType", dataType)
                .put("SampleTime", sampleTime)
                .put("Complexity", complexity)
                .put("EnforceDimensions", enforceDimensions)
                .put("EnforceDataType", enforceDataType)
                .put("EnforceSampleTime", enforceSampleTime)
                .put("EnforceComplexity", enforceComplexity)
                .build();
    }

    @Override
    public String toString() {
        return String.format("SignalSpecificationDto{id=%d, name='%s', type='%s', " +
                           "dimensions='%s', enforced=[dims:%s,type:%s,time:%s,comp:%s]}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getDimensionsValue(),
                           isDimensionsEnforced(),
                           isDataTypeEnforced(),
                           isSampleTimeEnforced(),
                           isComplexityEnforced());
    }
}
