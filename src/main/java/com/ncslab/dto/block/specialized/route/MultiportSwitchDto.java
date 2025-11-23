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

import java.util.Arrays;
import java.util.List;

/**
 * DTO representation of Multiport Switch route block.
 *
 * The Multiport Switch block routes one of many inputs to the output based on
 * a control signal. The first input is the control signal (integer), and the
 * remaining inputs are data inputs that can be selected.
 *
 * Port Configuration:
 * - Input 0: Control signal (integer indicating which data input to select)
 * - Inputs 1 to N: Data inputs (numberOfDataInputs)
 * - Output: Selected data input
 *
 * Selection Logic:
 * - Control signal is converted to integer (cast or round)
 * - If dataPortOrder is "Zero-based contiguous":
 *   - control=0 selects input 1, control=1 selects input 2, etc.
 * - If dataPortOrder is "One-based contiguous":
 *   - control=1 selects input 1, control=2 selects input 2, etc.
 * - If control is out of range: output = defaultOutput
 * - If control is NaN or Inf: output = defaultOutput
 *
 * Key Features:
 * - Multi-way conditional routing
 * - Configurable data port indexing (zero-based or one-based)
 * - Default output for invalid control signals
 * - Supports scalar and matrix signals
 *
 * @author NCSLab
 * @version 1.0
 * @since Multiport Switch Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Multiport Switch")
@MigrationCompatible(originalClass = "com.ncslab.block.route.MultiportSwitch")
public class MultiportSwitchDto extends BlockDto {

    /**
     * Number of data input ports (excluding control input)
     * Default: 3
     * Validation: Must be >= 2 and <= 100
     */
    @Builder.Default
    private TypedParameter numberOfDataInputs = TypedParameter.of(3);

    /**
     * Data port indexing order
     * Default: "Zero-based contiguous"
     * Options: "Zero-based contiguous", "One-based contiguous"
     */
    @Builder.Default
    private TypedParameter dataPortOrder = TypedParameter.of("Zero-based contiguous");

    /**
     * Default output value when control signal is out of range or invalid
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter defaultOutput = TypedParameter.of(0.0);

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

    // Valid data port order options
    private static final List<String> VALID_DATA_PORT_ORDERS = Arrays.asList(
        "Zero-based contiguous",
        "One-based contiguous"
    );

    // ===== PARAMETER ACCESS HELPERS =====

    public Integer getNumberOfDataInputsValue() {
        return numberOfDataInputs != null ? numberOfDataInputs.getValue(Integer.class) : 3;
    }

    public String getDataPortOrderValue() {
        return dataPortOrder != null ? dataPortOrder.getAsString() : "Zero-based contiguous";
    }

    public Double getDefaultOutputValue() {
        return defaultOutput != null ? defaultOutput.getAsDouble() : 0.0;
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

        // Validate number of data inputs
        if (numberOfDataInputs != null) {
            Integer numInputs = numberOfDataInputs.getValue(Integer.class);
            if (numInputs != null && numInputs < 2) {
                result.addError("Number of data inputs must be at least 2");
            }
            if (numInputs != null && numInputs > 100) {
                result.addError("Number of data inputs must not exceed 100");
            }
        }

        // Validate data port order
        if (dataPortOrder != null) {
            String order = dataPortOrder.getAsString();
            if (order != null && !VALID_DATA_PORT_ORDERS.contains(order)) {
                result.addError("Data port order must be one of: " + VALID_DATA_PORT_ORDERS);
            }
        }

        // Validate default output
        if (defaultOutput != null) {
            Double defOut = defaultOutput.getAsDouble();
            if (defOut != null && (Double.isNaN(defOut) || Double.isInfinite(defOut))) {
                result.addError("Default output value must be finite");
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
     * Get the total number of input ports (control + data inputs)
     */
    public int getInputPortCount() {
        return 1 + getNumberOfDataInputsValue(); // 1 control + N data inputs
    }

    /**
     * Get the number of output ports (always 1 for Multiport Switch)
     */
    public int getOutputPortCount() {
        return 1;
    }

    /**
     * Check if data port order is zero-based
     */
    public boolean isZeroBased() {
        return "Zero-based contiguous".equals(getDataPortOrderValue());
    }

    /**
     * Check if data port order is one-based
     */
    public boolean isOneBased() {
        return "One-based contiguous".equals(getDataPortOrderValue());
    }

    /**
     * Convert control value to data input index
     * @param controlValue The control signal value
     * @return The data input index (1-based for inputPortList), or -1 if out of range
     */
    public int getSelectedDataInputIndex(double controlValue) {
        // Handle NaN and Inf
        if (Double.isNaN(controlValue) || Double.isInfinite(controlValue)) {
            return -1; // Invalid control, use default output
        }

        // Convert to integer (round to nearest)
        int controlInt = (int) Math.round(controlValue);

        int selectedInput;
        if (isZeroBased()) {
            // Zero-based: control=0 → input 1, control=1 → input 2, etc.
            selectedInput = controlInt + 1;
        } else {
            // One-based: control=1 → input 1, control=2 → input 2, etc.
            selectedInput = controlInt;
        }

        // Validate range (1 to numberOfDataInputs)
        if (selectedInput < 1 || selectedInput > getNumberOfDataInputsValue()) {
            return -1; // Out of range, use default output
        }

        return selectedInput;
    }

    /**
     * Check if the control value is valid
     * @param controlValue The control signal value
     * @return true if valid, false if out of range or invalid
     */
    public boolean isValidControlValue(double controlValue) {
        return getSelectedDataInputIndex(controlValue) != -1;
    }

    /**
     * Create a Multiport Switch block with specific number of data inputs
     */
    public static MultiportSwitchDto create(String name, String path, int numberOfDataInputs) {
        return MultiportSwitchDto.builder()
                .blockName(name)
                .blockPath(path)
                .blockUUID("null")
                .numberOfDataInputs(TypedParameter.of(numberOfDataInputs))
                .dataPortOrder(TypedParameter.of("Zero-based contiguous"))
                .defaultOutput(TypedParameter.of(0.0))
                .sampleTime(TypedParameter.of(-1.0))
                .outDataTypeStr(TypedParameter.of("Inherit: Same as input"))
                .build();
    }

    /**
     * Create a Multiport Switch block with default 3 data inputs
     */
    public static MultiportSwitchDto createDefault(String name, String path) {
        return create(name, path, 3);
    }

    @Override
    public MultiportSwitchDto copy() {
        return MultiportSwitchDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .numberOfDataInputs(numberOfDataInputs != null ? numberOfDataInputs.copy() : null)
                .dataPortOrder(dataPortOrder != null ? dataPortOrder.copy() : null)
                .defaultOutput(defaultOutput != null ? defaultOutput.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("NumberOfDataInputs", numberOfDataInputs)
                .put("DataPortOrder", dataPortOrder)
                .put("DefaultOutput", defaultOutput)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    @Override
    public String toString() {
        return String.format("MultiportSwitchDto{id=%d, name='%s', type='%s', numberOfDataInputs=%d, dataPortOrder='%s', defaultOutput=%s}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getNumberOfDataInputsValue(),
                           getDataPortOrderValue(),
                           getDefaultOutputValue());
    }
}
