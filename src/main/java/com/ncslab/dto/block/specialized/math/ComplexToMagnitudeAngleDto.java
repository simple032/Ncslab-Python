package com.ncslab.dto.block.specialized.math;

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
import lombok.AllArgsConstructor;

/**
 * DTO representation of ComplexToMagnitudeAngle math block.
 *
 * Converts complex numbers from rectangular form (real + imaginary) to polar form (magnitude + angle).
 *
 * SIMULINK Parameters:
 * - AngleUnits: Output angle units ("radians" or "degrees")
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 *
 * Mathematical Behavior:
 * - magnitude = sqrt(real^2 + imag^2)
 * - angle = atan2(imag, real)
 * - Supports both scalar and matrix inputs (element-wise)
 *
 * Inputs:
 * - Input 1 (Re): Real part of complex number
 * - Input 2 (Im): Imaginary part of complex number
 *
 * Outputs:
 * - Output 1 (Mag): Magnitude (amplitude)
 * - Output 2 (Angle): Phase angle (in radians or degrees)
 *
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ComplexToMagnitudeAngle")
@MigrationCompatible(originalClass = "com.ncslab.block.math.ComplexToMagnitudeAngle")
public class ComplexToMagnitudeAngleDto extends BlockDto {

    // ===== COMPLEX TO MAGNITUDE-ANGLE BLOCK SPECIFIC PARAMETERS =====

    /**
     * Output angle units ("radians" or "degrees")
     * Default: "radians"
     * Validation: Must be either "radians" or "degrees"
     */
    private TypedParameter angleUnits;

    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    private TypedParameter outDataTypeStr;

    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    private TypedParameter saturateOnIntegerOverflow;

    /**
     * Post-deserialization hook to populate typed fields from paramValues Map.
     * This method is called automatically after Jackson finishes deserializing the JSON.
     * It extracts values from the legacy paramValues Map and converts them to TypedParameters.
     */
    @com.fasterxml.jackson.annotation.JsonSetter("paramValues")
    public void populateFromParamValues(java.util.Map<String, Object> paramValues) {
        super.setParamValues(paramValues);  // Call parent setter to maintain compatibility

        if (paramValues == null) {
            return;
        }

        // Extract and convert each parameter from paramValues
        if (paramValues.containsKey("AngleUnits")) {
            Object angleUnitsValue = paramValues.get("AngleUnits");
            this.angleUnits = TypedParameter.of(angleUnitsValue);
        }

        if (paramValues.containsKey("SampleTime")) {
            Object stValue = paramValues.get("SampleTime");
            this.sampleTime = TypedParameter.of(stValue);
        }

        if (paramValues.containsKey("OutDataTypeStr")) {
            Object outDtValue = paramValues.get("OutDataTypeStr");
            this.outDataTypeStr = TypedParameter.of(outDtValue);
        }

        if (paramValues.containsKey("SaturateOnIntegerOverflow")) {
            Object satValue = paramValues.get("SaturateOnIntegerOverflow");
            this.saturateOnIntegerOverflow = TypedParameter.of(satValue);
        }
    }

    // ===== PARAMETER ACCESS HELPERS =====

    public String getAngleUnitsValue() {
        return angleUnits != null ? angleUnits.getAsString() : "radians";
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

    // ===== VALIDATION =====

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation

        // Validate angle units parameter
        if (angleUnits != null) {
            String angleUnitsValue = angleUnits.getAsString();
            if (angleUnitsValue != null &&
                !angleUnitsValue.equals("radians") &&
                !angleUnitsValue.equals("degrees")) {
                result.addError("AngleUnits must be 'radians' or 'degrees'");
            }
        }

        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st != -1.0 && st <= 0.0) {
                result.addError("Sample time must be positive or -1 (inherited)");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if output angle is in degrees
     */
    public boolean isAngleInDegrees() {
        return "degrees".equals(getAngleUnitsValue());
    }

    /**
     * Check if output angle is in radians
     */
    public boolean isAngleInRadians() {
        return "radians".equals(getAngleUnitsValue());
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

    @Override
    public ComplexToMagnitudeAngleDto copy() {
        return ComplexToMagnitudeAngleDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .angleUnits(angleUnits != null ? angleUnits.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("AngleUnits", angleUnits)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }

    @Override
    public String toString() {
        return String.format("ComplexToMagnitudeAngleDto{id=%d, name='%s', type='%s', angleUnits='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getAngleUnitsValue());
    }
}
