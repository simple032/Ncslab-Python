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

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * DTO representation of Bus Selector route block.
 *
 * The Bus Selector block extracts specified elements from a bus signal and creates
 * individual output signals. It is critical for decomposing bus signals into their
 * constituent elements for further processing.
 *
 * Key Features:
 * - Extracts specific named elements from bus
 * - Creates individual output signals
 * - Supports multiple element selection
 * - Validates element names against bus definition
 *
 * Port Configuration:
 * - Input: Bus signal (single input port)
 * - Outputs: Individual signals (one per selected element)
 *
 * Parameters:
 * - numberOfOutputs: Number of outputs/elements to extract (default: 1, range: 1-100)
 * - selectedSignals: Comma-separated element names ("signal1,signal2,signal3")
 * - sampleTime: Sample time for discrete operation (default: -1.0)
 * - outDataTypeStr: Output data type specification (default: "Inherit: Inherit via back propagation")
 *
 * Example Usage:
 * <pre>
 * BusSelectorDto dto = BusSelectorDto.builder()
 *     .blockName("Bus Selector1")
 *     .blockPath("model")
 *     .numberOfOutputs(TypedParameter.of(3))
 *     .selectedSignals(TypedParameter.of("speed,position,temperature"))
 *     .build();
 * </pre>
 *
 * @author NCSLab Bus Architecture Implementation
 * @version 1.0
 * @since 2025-01-15
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("BusSelector")
@MigrationCompatible(originalClass = "com.ncslab.block.route.BusSelector")
public class BusSelectorDto extends BlockDto {

    /**
     * Number of output ports (must match number of selected signals)
     * Default: 1
     * Range: 1-100
     */
    @Builder.Default
    private TypedParameter numberOfOutputs = TypedParameter.of(1);

    /**
     * Comma-separated list of bus element names to extract
     * Example: "signal1,signal2,signal3"
     * Must be non-empty and count must match numberOfOutputs
     */
    @Builder.Default
    private TypedParameter selectedSignals = TypedParameter.of("signal1");

    /**
     * Sample time for the block operation
     * Default: -1.0 (inherited)
     * -1: Inherited, 0: Continuous, >0: Discrete
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    /**
     * Output data type specification
     * Default: "Inherit: Inherit via back propagation"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Inherit via back propagation");

    // ===== PARAMETER ACCESS HELPERS =====

    /**
     * Gets the number of output ports.
     * @return Number of outputs (1-100)
     */
    public int getNumberOfOutputsValue() {
        return numberOfOutputs != null ? numberOfOutputs.getAsInteger() : 1;
    }

    /**
     * Gets the selected signals string.
     * @return Comma-separated signal names
     */
    public String getSelectedSignalsValue() {
        return selectedSignals != null ? selectedSignals.getAsString() : "signal1";
    }

    /**
     * Gets the sample time value.
     * @return Sample time (-1 for inherited, 0 for continuous, >0 for discrete)
     */
    public double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    /**
     * Gets the output data type string.
     * @return Output data type specification
     */
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Inherit via back propagation";
    }

    /**
     * Parses the selected signals string into an array of signal names.
     * @return Array of trimmed signal names
     */
    public String[] getSelectedSignalsArray() {
        String signalsStr = getSelectedSignalsValue();
        if (signalsStr == null || signalsStr.trim().isEmpty()) {
            return new String[0];
        }

        return Arrays.stream(signalsStr.split(","))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .toArray(String[]::new);
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate number of outputs
        if (numberOfOutputs != null) {
            Integer numOutputs = numberOfOutputs.getAsInteger();
            if (numOutputs != null) {
                if (numOutputs < 1) {
                    result.addError("Number of outputs must be at least 1");
                }
                if (numOutputs > 100) {
                    result.addError("Number of outputs cannot exceed 100");
                }
            }
        }

        // Validate selected signals is non-empty
        if (selectedSignals != null) {
            String signals = selectedSignals.getAsString();
            if (signals == null || signals.trim().isEmpty()) {
                result.addError("Selected signals cannot be empty");
            } else {
                // Parse signal names
                String[] signalNames = getSelectedSignalsArray();

                // Check that count matches numberOfOutputs
                if (numberOfOutputs != null && numberOfOutputs.getAsInteger() != null) {
                    int expected = numberOfOutputs.getAsInteger();
                    if (signalNames.length != expected) {
                        result.addError(String.format(
                            "Number of selected signals (%d) must match numberOfOutputs (%d)",
                            signalNames.length, expected
                        ));
                    }
                }

                // Check for empty signal names
                for (String name : signalNames) {
                    if (name.isEmpty()) {
                        result.addError("Selected signals list contains empty names");
                        break;
                    }
                }

                // Check for duplicate signal names
                List<String> nameList = Arrays.asList(signalNames);
                if (nameList.stream().distinct().count() != nameList.size()) {
                    result.addError("Selected signals list contains duplicate names");
                }
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
     * Gets the number of input ports (always 1 for Bus Selector)
     */
    public int getInputPortCount() {
        return 1;
    }

    /**
     * Gets the number of output ports (based on numberOfOutputs parameter)
     */
    public int getOutputPortCount() {
        return getNumberOfOutputsValue();
    }

    /**
     * Checks if the specified signal name is in the selected list
     * @param signalName Name to check
     * @return true if signal is selected
     */
    public boolean isSignalSelected(String signalName) {
        if (signalName == null) return false;
        String[] selected = getSelectedSignalsArray();
        return Arrays.asList(selected).contains(signalName);
    }

    @Override
    public BusSelectorDto copy() {
        return BusSelectorDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .numberOfOutputs(numberOfOutputs != null ? numberOfOutputs.copy() : null)
                .selectedSignals(selectedSignals != null ? selectedSignals.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("NumberOfOutputs", numberOfOutputs)
                .put("SelectedSignals", selectedSignals)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    @Override
    public String toString() {
        return String.format("BusSelectorDto{id=%d, name='%s', type='%s', outputs=%d, signals='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getNumberOfOutputsValue(),
                           getSelectedSignalsValue());
    }
}
