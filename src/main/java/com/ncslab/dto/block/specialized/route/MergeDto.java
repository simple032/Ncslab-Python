package com.ncslab.dto.block.specialized.route;

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
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;

/**
 * DTO representation of Merge route block.
 *
 * The Merge block combines multiple input signals into a single output by selecting
 * the most recently updated input. This is critical for signal routing in complex models
 * where timing and signal priority matter.
 *
 * Port Configuration:
 * - Multiple inputs (dynamic based on connections)
 * - Single output (selected input signal)
 *
 * Selection Logic:
 * - The Merge block selects the input with the most recent update timestamp
 * - If no input has been updated, it outputs the initialOutput value or last known value
 * - Time-based priority resolution for simultaneous updates
 *
 * Key Features:
 * - Time-based input priority
 * - Dynamic number of inputs
 * - Supports scalar and matrix signals
 * - Configurable initial output value
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
@JsonTypeName("Merge")
@MigrationCompatible(originalClass = "com.ncslab.block.route.Merge")
public class MergeDto extends BlockDto {

    /**
     * Number of input ports
     * Default: 2
     * Validation: Must be >= 2
     */
    @Builder.Default
    private TypedParameter numberOfInputs = TypedParameter.of(2);

    /**
     * Initial output value when no input has been updated
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter initialOutput = TypedParameter.of(0.0);

    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    /**
     * Output data type specification
     * Default: "Inherit: Inherit via internal rule"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Inherit via internal rule");

    /**
     * Allow unconnected inputs (if false, all inputs must be connected)
     * Default: false
     */
    @Builder.Default
    private TypedParameter allowUnconnectedInputs = TypedParameter.of(false);

    // ===== PARAMETER ACCESS HELPERS =====

    public Integer getNumberOfInputsValue() {
        return numberOfInputs != null ? numberOfInputs.getValue(Integer.class) : 2;
    }

    public Double getInitialOutputValue() {
        return initialOutput != null ? initialOutput.getValue(Double.class) : 0.0;
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Inherit via internal rule";
    }

    public Boolean getAllowUnconnectedInputsValue() {
        return allowUnconnectedInputs != null ? allowUnconnectedInputs.getValue(Boolean.class) : false;
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate number of inputs
        if (numberOfInputs != null) {
            Integer numInputs = numberOfInputs.getValue(Integer.class);
            if (numInputs != null && numInputs < 2) {
                result.addError("Number of inputs must be at least 2");
            }
            if (numInputs != null && numInputs > 100) {
                result.addWarning(new com.ncslab.dto.mapper.validation.ValidationWarning("NumberOfInputs", "Number of inputs is very large (" + numInputs + "), which may impact performance"));
            }
        }

        // Validate initial output
        if (initialOutput != null) {
            try {
                Double initVal = initialOutput.getValue(Double.class);
                if (initVal != null && (Double.isNaN(initVal) || Double.isInfinite(initVal))) {
                    result.addError("Initial output value must be finite");
                }
            } catch (Exception e) {
                result.addError("Invalid initial output format: " + e.getMessage());
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
     * Get the number of input ports
     */
    public int getInputPortCount() {
        return getNumberOfInputsValue();
    }

    /**
     * Get the number of output ports (always 1 for Merge block)
     */
    public int getOutputPortCount() {
        return 1; // Always 1 output
    }

    /**
     * Check if the merge block allows unconnected inputs
     */
    public boolean allowsUnconnectedInputs() {
        return getAllowUnconnectedInputsValue();
    }

    /**
     * Create a Merge block with specific number of inputs
     */
    public static MergeDto create(String name, String path, int numberOfInputs) {
        return MergeDto.builder()
                .blockName(name)
                .blockPath(path)
                .blockUUID("null")
                .numberOfInputs(TypedParameter.of(numberOfInputs))
                .initialOutput(TypedParameter.of(0.0))
                .sampleTime(TypedParameter.of(-1.0))
                .outDataTypeStr(TypedParameter.of("Inherit: Inherit via internal rule"))
                .allowUnconnectedInputs(TypedParameter.of(false))
                .build();
    }

    /**
     * Create a Merge block with default 2 inputs
     */
    public static MergeDto createDefault(String name, String path) {
        return create(name, path, 2);
    }

    @Override
    public MergeDto copy() {
        return MergeDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .numberOfInputs(numberOfInputs != null ? numberOfInputs.copy() : null)
                .initialOutput(initialOutput != null ? initialOutput.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .allowUnconnectedInputs(allowUnconnectedInputs != null ? allowUnconnectedInputs.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("NumberOfInputs", numberOfInputs)
                .put("InitialOutput", initialOutput)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("AllowUnconnectedInputs", allowUnconnectedInputs)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();

        if (numberOfInputs != null) {
            params.put("NumberOfInputs", String.valueOf(numberOfInputs.getValue()));
        }
        if (initialOutput != null) {
            params.put("InitialOutput", String.valueOf(initialOutput.getValue()));
        }
        if (sampleTime != null) {
            params.put("SampleTime", sampleTime.getAsString());
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", String.valueOf(outDataTypeStr.getValue()));
        }
        if (allowUnconnectedInputs != null) {
            Boolean allowVal = allowUnconnectedInputs.getValue(Boolean.class);
            params.put("AllowUnconnectedInputs", Boolean.TRUE.equals(allowVal) ? "on" : "off");
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("MergeDto{id=%d, name='%s', type='%s', numberOfInputs=%d, initialOutput=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getNumberOfInputsValue(),
                           getInitialOutputValue());
    }
}
