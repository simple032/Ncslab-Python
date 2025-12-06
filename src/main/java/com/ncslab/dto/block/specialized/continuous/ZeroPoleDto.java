package com.ncslab.dto.block.specialized.continuous;

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
 * DTO representation of Zero-Pole block (ZeroPole).
 *
 * The Zero-Pole block models a transfer function in zero-pole-gain form:
 * H(s) = K * prod(s - z_i) / prod(s - p_i)
 *
 * Internally converted to transfer function form for state-space realization.
 *
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ZeroPole")
@MigrationCompatible(originalClass = "com.ncslab.block.continuous.ZeroPole")
public class ZeroPoleDto extends BlockDto {

    /**
     * Zero locations [z1, z2, ...]
     * Default: "[]" (no zeros)
     * Format: "[z1 z2 z3]" for real zeros
     * Complex zeros come in conjugate pairs
     */
    @Builder.Default
    private TypedParameter zeros = TypedParameter.of("[]");

    /**
     * Pole locations [p1, p2, ...]
     * Default: "[-1]"
     * Format: "[p1 p2 p3]" for real poles
     * Complex poles come in conjugate pairs
     * Validation: All poles should have negative real parts for stability
     */
    @Builder.Default
    private TypedParameter poles = TypedParameter.of("[-1]");

    /**
     * System gain K
     * Default: 1.0
     * The gain multiplies the entire transfer function
     */
    @Builder.Default
    private TypedParameter gain = TypedParameter.of(1.0);

    /**
     * Absolute tolerance for simulation
     * Default: "auto"
     */
    @Builder.Default
    private TypedParameter absoluteTolerance = TypedParameter.of("auto");

    /**
     * Attributes for continuous states
     * Default: "'''"
     */
    @Builder.Default
    private TypedParameter continuousStateAttributes = TypedParameter.of("'''");

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

    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);

    // ===== PARAMETER ACCESS HELPERS =====

    public String getZerosValue() {
        return zeros != null ? zeros.getAsString() : "[]";
    }

    public String getPolesValue() {
        return poles != null ? poles.getAsString() : "[-1]";
    }

    public Double getGainValue() {
        return gain != null ? gain.getAsDouble() : 1.0;
    }

    public String getAbsoluteToleranceValue() {
        return absoluteTolerance != null ? absoluteTolerance.getAsString() : "auto";
    }

    public String getContinuousStateAttributesValue() {
        return continuousStateAttributes != null ? continuousStateAttributes.getAsString() : "'''";
    }

    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }

    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }

    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }

    // ===== ARRAY PARSING =====

    /**
     * Parse array from string format "[]" or "[z1 z2 z3]"
     */
    public double[] parseArray(String arrayStr) {
        if (arrayStr == null || arrayStr.trim().isEmpty()) {
            return new double[0];
        }

        // Remove brackets and trim
        String cleanStr = arrayStr.trim().replaceAll("^\\[|\\]$", "").trim();

        // Empty array case
        if (cleanStr.isEmpty()) {
            return new double[0];
        }

        // Split by whitespace
        String[] parts = cleanStr.split("\\s+");

        double[] array = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                array[i] = Double.parseDouble(parts[i].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid value '" + parts[i] + "' in array: " + arrayStr);
            }
        }

        return array;
    }

    /**
     * Get zeros as double array
     */
    public double[] getZerosArray() {
        return parseArray(getZerosValue());
    }

    /**
     * Get poles as double array
     */
    public double[] getPolesArray() {
        return parseArray(getPolesValue());
    }

    /**
     * Format array as string "[a1 a2 a3]"
     */
    public static String formatArray(double[] array) {
        if (array == null || array.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate poles
        if (poles != null) {
            try {
                double[] poleArray = getPolesArray();
                if (poleArray.length == 0) {
                    result.addError("Poles array cannot be empty");
                }
                // Check for valid numbers
                for (double pole : poleArray) {
                    if (Double.isNaN(pole) || Double.isInfinite(pole)) {
                        result.addError("Poles must be finite numbers");
                        break;
                    }
                }
            } catch (Exception e) {
                result.addError("Invalid poles format: " + e.getMessage());
            }
        }

        // Validate zeros (can be empty)
        if (zeros != null) {
            try {
                double[] zeroArray = getZerosArray();
                // Check for valid numbers
                for (double zero : zeroArray) {
                    if (Double.isNaN(zero) || Double.isInfinite(zero)) {
                        result.addError("Zeros must be finite numbers");
                        break;
                    }
                }
            } catch (Exception e) {
                result.addError("Invalid zeros format: " + e.getMessage());
            }
        }

        // Validate gain
        if (gain != null) {
            Double gainValue = gain.getAsDouble();
            if (gainValue != null) {
                if (Double.isNaN(gainValue) || Double.isInfinite(gainValue)) {
                    result.addError("Gain must be a finite number");
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
     * Get the order of the transfer function (number of poles)
     */
    public int getTransferFunctionOrder() {
        try {
            return getPolesArray().length;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Check if transfer function has direct feedthrough (more zeros than poles)
     */
    public boolean hasDirectFeedthrough() {
        try {
            double[] zeroArray = getZerosArray();
            double[] poleArray = getPolesArray();
            return zeroArray.length >= poleArray.length;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the number of states required for realization
     */
    public int getRequiredStateCount() {
        return Math.max(0, getTransferFunctionOrder());
    }

    /**
     * Check if this is a simple gain (no poles, no zeros)
     */
    public boolean isSimpleGain() {
        try {
            double[] zeroArray = getZerosArray();
            double[] poleArray = getPolesArray();
            return zeroArray.length == 0 && poleArray.length == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ZeroPoleDto copy() {
        return ZeroPoleDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .zeros(zeros != null ? zeros.copy() : null)
                .poles(poles != null ? poles.copy() : null)
                .gain(gain != null ? gain.copy() : null)
                .absoluteTolerance(absoluteTolerance != null ? absoluteTolerance.copy() : null)
                .continuousStateAttributes(continuousStateAttributes != null ? continuousStateAttributes.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Zeros", zeros)
                .put("Poles", poles)
                .put("Gain", gain)
                .put("AbsoluteTolerance", absoluteTolerance)
                .put("ContinuousStateAttributes", continuousStateAttributes)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }

    @Override
    public String toString() {
        return String.format("ZeroPoleDto{id=%d, name='%s', type='%s', zeros='%s', poles='%s', gain=%.4f, order=%d}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getZerosValue(),
                           getPolesValue(),
                           getGainValue(),
                           getTransferFunctionOrder());
    }
}
