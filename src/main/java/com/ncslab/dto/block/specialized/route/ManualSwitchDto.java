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
 * DTO representation of Manual Switch route block.
 *
 * The Manual Switch block provides user-controlled routing between two inputs.
 * Unlike automatic switches controlled by signals, this block's switching state
 * is manually set through a parameter that can be changed interactively.
 *
 * Port Configuration:
 * - Input 0: First data input (routed when SwitchControl = "0")
 * - Input 1: Second data input (routed when SwitchControl = "1")
 * - Output: Selected input signal
 *
 * Selection Logic:
 * - When SwitchControl = "0": Output = Input 0
 * - When SwitchControl = "1": Output = Input 1
 * - Control can be changed during simulation (interactive control)
 *
 * Key Features:
 * - Manual, user-controlled switching
 * - Two input ports with single output
 * - Feedthrough behavior
 * - Supports scalar and matrix signals
 *
 * @author NCSLab
 * @version 1.0
 * @since Manual Switch Implementation 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ManualSwitch")
@MigrationCompatible(originalClass = "com.ncslab.block.route.ManualSwitch")
public class ManualSwitchDto extends BlockDto {

    /**
     * Switch control state determining which input to route
     * Valid values: "0" (route input 0) or "1" (route input 1)
     * Default: "0"
     */
    @Builder.Default
    private TypedParameter switchControl = TypedParameter.of("0");

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

    // ===== PARAMETER ACCESS HELPERS =====

    public String getSwitchControlValue() {
        return switchControl != null ? switchControl.getAsString() : "0";
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Inherit via internal rule";
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate switch control
        if (switchControl != null) {
            String controlValue = switchControl.getAsString();
            if (controlValue != null && !controlValue.equals("0") && !controlValue.equals("1")) {
                result.addError("Switch control must be either '0' or '1', got: " + controlValue);
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
     * Get the number of input ports (always 2 for Manual Switch)
     */
    public int getInputPortCount() {
        return 2;
    }

    /**
     * Get the number of output ports (always 1 for Manual Switch)
     */
    public int getOutputPortCount() {
        return 1;
    }

    /**
     * Check if input 0 is selected (control = "0")
     */
    public boolean isInput0Selected() {
        return "0".equals(getSwitchControlValue());
    }

    /**
     * Check if input 1 is selected (control = "1")
     */
    public boolean isInput1Selected() {
        return "1".equals(getSwitchControlValue());
    }

    /**
     * Get the selected input index (0 or 1)
     */
    public int getSelectedInputIndex() {
        return isInput1Selected() ? 1 : 0;
    }

    /**
     * Create a Manual Switch block with default settings
     */
    public static ManualSwitchDto createDefault(String name, String path) {
        return ManualSwitchDto.builder()
                .blockName(name)
                .blockPath(path)
                .blockUUID("null")
                .switchControl(TypedParameter.of("0"))
                .sampleTime(TypedParameter.of(-1.0))
                .outDataTypeStr(TypedParameter.of("Inherit: Inherit via internal rule"))
                .build();
    }

    /**
     * Create a Manual Switch block with specified control state
     */
    public static ManualSwitchDto create(String name, String path, String switchControl) {
        return ManualSwitchDto.builder()
                .blockName(name)
                .blockPath(path)
                .blockUUID("null")
                .switchControl(TypedParameter.of(switchControl))
                .sampleTime(TypedParameter.of(-1.0))
                .outDataTypeStr(TypedParameter.of("Inherit: Inherit via internal rule"))
                .build();
    }

    /**
     * Create a Manual Switch with input 0 selected
     */
    public static ManualSwitchDto createWithInput0(String name, String path) {
        return create(name, path, "0");
    }

    /**
     * Create a Manual Switch with input 1 selected
     */
    public static ManualSwitchDto createWithInput1(String name, String path) {
        return create(name, path, "1");
    }

    @Override
    public ManualSwitchDto copy() {
        return ManualSwitchDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .switchControl(switchControl != null ? switchControl.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("SwitchControl", switchControl)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();

        if (switchControl != null) {
            params.put("SwitchControl", switchControl.getAsString());
        }
        if (sampleTime != null) {
            params.put("SampleTime", sampleTime.getAsString());
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", String.valueOf(outDataTypeStr.getValue()));
        }

        return params;
    }

    @Override
    public String toString() {
        return String.format("ManualSwitchDto{id=%d, name='%s', type='%s', control='%s', selectedInput=%d}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getSwitchControlValue(),
                           getSelectedInputIndex());
    }
}
