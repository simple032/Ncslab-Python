package com.ncslab.dto.block.specialized.discontinuous;

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
 * DTO representation of HitCrossing discontinuous block.
 *
 * The HitCrossing block detects when a signal crosses a specified threshold.
 * It outputs 1 when a crossing is detected, and 0 otherwise.
 *
 * Crossing Direction:
 * - "rising": detect when signal goes from below to above threshold
 * - "falling": detect when signal goes from above to below threshold
 * - "either": detect both rising and falling crossings
 *
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("HitCrossing")
@MigrationCompatible(originalClass = "com.ncslab.block.discontinuous.HitCrossing")
public class HitCrossingDto extends BlockDto {

    /**
     * Crossing level/threshold
     * Default: 0.0
     */
    @Builder.Default
    private TypedParameter hitCrossingOffset = TypedParameter.of(0.0);

    /**
     * Crossing direction: "rising", "falling", or "either"
     * Default: "either"
     */
    @Builder.Default
    private TypedParameter hitCrossingDirection = TypedParameter.of("either");

    /**
     * Whether to show output port
     * Default: true
     */
    @Builder.Default
    private TypedParameter showOutputPort = TypedParameter.of(true);

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

    // ===== PARAMETER ACCESS HELPERS =====

    public Double getHitCrossingOffsetValue() {
        return hitCrossingOffset != null ? hitCrossingOffset.getAsDouble() : 0.0;
    }

    public String getHitCrossingDirectionValue() {
        return hitCrossingDirection != null ? hitCrossingDirection.getAsString() : "either";
    }

    public Boolean getShowOutputPortValue() {
        return showOutputPort != null ? showOutputPort.getAsBoolean() : true;
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

        // Validate crossing offset is finite
        Double offset = getHitCrossingOffsetValue();
        if (offset != null && (offset.isNaN() || offset.isInfinite())) {
            result.addError("Hit crossing offset must be finite");
        }

        // Validate crossing direction
        String direction = getHitCrossingDirectionValue();
        if (direction != null) {
            if (!direction.equals("rising") && !direction.equals("falling") && !direction.equals("either")) {
                result.addError("Hit crossing direction must be 'rising', 'falling', or 'either'");
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
     * Check if the block detects rising crossings
     */
    public boolean detectsRising() {
        String dir = getHitCrossingDirectionValue();
        return "rising".equals(dir) || "either".equals(dir);
    }

    /**
     * Check if the block detects falling crossings
     */
    public boolean detectsFalling() {
        String dir = getHitCrossingDirectionValue();
        return "falling".equals(dir) || "either".equals(dir);
    }

    /**
     * Detect if a crossing occurred given previous and current values
     */
    public boolean detectCrossing(double previousValue, double currentValue) {
        double threshold = getHitCrossingOffsetValue();
        String direction = getHitCrossingDirectionValue();

        boolean risingCrossing = previousValue < threshold && currentValue >= threshold;
        boolean fallingCrossing = previousValue > threshold && currentValue <= threshold;

        switch (direction) {
            case "rising":
                return risingCrossing;
            case "falling":
                return fallingCrossing;
            case "either":
                return risingCrossing || fallingCrossing;
            default:
                return false;
        }
    }

    @Override
    public HitCrossingDto copy() {
        return HitCrossingDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .hitCrossingOffset(hitCrossingOffset != null ? hitCrossingOffset.copy() : null)
                .hitCrossingDirection(hitCrossingDirection != null ? hitCrossingDirection.copy() : null)
                .showOutputPort(showOutputPort != null ? showOutputPort.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }

    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("HitCrossingOffset", hitCrossingOffset)
                .put("HitCrossingDirection", hitCrossingDirection)
                .put("ShowOutputPort", showOutputPort)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    @Override
    public String toString() {
        return String.format("HitCrossingDto{id=%d, name='%s', type='%s', threshold=%.3f, direction='%s'}",
                           getBlockId(),
                           getBlockName(),
                           getBlockType(),
                           getHitCrossingOffsetValue(),
                           getHitCrossingDirectionValue());
    }
}
