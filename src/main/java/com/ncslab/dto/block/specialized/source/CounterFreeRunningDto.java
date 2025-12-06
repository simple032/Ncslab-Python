package com.ncslab.dto.block.specialized.source;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object for CounterFreeRunning source block.
 * CounterFreeRunning blocks count up or down indefinitely without limits.
 *
 * @author NCSLab
 * @version 2.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("CounterFreeRunning")
public class CounterFreeRunningDto extends BlockDto {

    /**
     * Count direction: "Up" or "Down"
     */
    private TypedParameter countDirection;

    /**
     * Initial count value (starting count)
     */
    private TypedParameter initialCount;

    /**
     * Sample time for the counter block.
     * -1 = inherited, >0 = discrete with specified sample time
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification.
     * Common values: "double", "single", "int32", "int16", "int8", "uint32", "uint16", "uint8"
     */
    private TypedParameter outDataTypeStr;

    /**
     * Whether to saturate on integer overflow.
     */
    private TypedParameter saturateOnIntegerOverflow;

    // Override getters to provide defaults if null
    public TypedParameter getCountDirection() {
        if (countDirection == null) {
            countDirection = TypedParameter.of("Up");
        }
        return countDirection;
    }

    public TypedParameter getInitialCount() {
        if (initialCount == null) {
            initialCount = TypedParameter.of(0.0);
        }
        return initialCount;
    }

    public TypedParameter getSampleTime() {
        if (sampleTime == null) {
            sampleTime = TypedParameter.of(-1.0);  // Inherited by default
        }
        return sampleTime;
    }

    public TypedParameter getOutDataTypeStr() {
        if (outDataTypeStr == null) {
            outDataTypeStr = TypedParameter.of("double");
        }
        return outDataTypeStr;
    }

    public TypedParameter getSaturateOnIntegerOverflow() {
        if (saturateOnIntegerOverflow == null) {
            saturateOnIntegerOverflow = TypedParameter.of("off");
        }
        return saturateOnIntegerOverflow;
    }

    /**
     * Initialize default values for parameters
     */
    public CounterFreeRunningDto(String blockName, String blockPath) {
        super(blockName, blockPath);
        initializeDefaults();
    }

    private void initializeDefaults() {
        if (countDirection == null) {
            countDirection = TypedParameter.of("Up");
        }
        if (initialCount == null) {
            initialCount = TypedParameter.of(0.0);
        }
        if (sampleTime == null) {
            sampleTime = TypedParameter.of(-1.0);
        }
        if (outDataTypeStr == null) {
            outDataTypeStr = TypedParameter.of("double");
        }
        if (saturateOnIntegerOverflow == null) {
            saturateOnIntegerOverflow = TypedParameter.of("off");
        }
    }

    /**
     * Constructs CounterFreeRunningDto with individual parameters.
     */
    public CounterFreeRunningDto(String blockName, String blockPath,
                                TypedParameter countDirection,
                                TypedParameter initialCount,
                                TypedParameter sampleTime,
                                TypedParameter outDataTypeStr,
                                TypedParameter saturateOnIntegerOverflow) {
        super(blockName, blockPath);
        this.countDirection = countDirection;
        this.initialCount = initialCount;
        this.sampleTime = sampleTime;
        this.outDataTypeStr = outDataTypeStr;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
    }

    /**
     * Constructs CounterFreeRunningDto with typed parameter map.
     */
    public CounterFreeRunningDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.countDirection = parameters.getTypedParameter("CountDirection", String.class, "Up");
        this.initialCount = parameters.getTypedParameter("InitialCount", Double.class, 0.0);
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
        this.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class, "double");
        this.saturateOnIntegerOverflow = parameters.getTypedParameter("SaturateOnIntegerOverflow", Boolean.class, false);
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate count direction
        if (countDirection == null || countDirection.getAsString() == null) {
            addValidationError("Count direction cannot be null");
            return false;
        }
        String direction = countDirection.getAsString();
        if (!direction.equals("Up") && !direction.equals("Down")) {
            addValidationError("Count direction must be 'Up' or 'Down'");
            return false;
        }

        // Validate initial count
        if (initialCount == null || initialCount.getAsDouble() == null) {
            addValidationError("Initial count cannot be null");
            return false;
        }

        // Validate sample time
        if (sampleTime == null || sampleTime.getAsDouble() == null) {
            addValidationError("Sample time cannot be null");
            return false;
        }
        Double sampleTimeValue = sampleTime.getAsDouble();
        if (sampleTimeValue < -1.0 || sampleTimeValue == 0.0 || sampleTimeValue.isNaN() || sampleTimeValue.isInfinite()) {
            addValidationError("Sample time must be > 0 or -1 (inherited)");
            return false;
        }

        return true;
    }

    // === Helper Methods ===

    /**
     * Gets the count direction with validation.
     */
    public String getCountDirectionValue() {
        if (countDirection == null || countDirection.getAsString() == null) {
            throw new IllegalStateException("Count direction is not properly initialized");
        }
        return countDirection.getAsString();
    }

    /**
     * Gets the initial count value with validation.
     */
    public double getInitialCountValue() {
        if (initialCount == null || initialCount.getAsDouble() == null) {
            throw new IllegalStateException("Initial count is not properly initialized");
        }
        return initialCount.getAsDouble();
    }

    /**
     * Gets the sample time value with validation.
     */
    public double getSampleTimeValue() {
        if (sampleTime == null || sampleTime.getAsDouble() == null) {
            throw new IllegalStateException("Sample time is not properly initialized");
        }
        return sampleTime.getAsDouble();
    }

    /**
     * Checks if counter counts upward.
     */
    public boolean isCountingUp() {
        return "Up".equals(getCountDirectionValue());
    }

    /**
     * Checks if counter counts downward.
     */
    public boolean isCountingDown() {
        return "Down".equals(getCountDirectionValue());
    }

    @Override
    public String toString() {
        return String.format("CounterFreeRunningDto{blockName='%s', blockPath='%s', direction='%s', initial=%s, sampleTime=%s}",
                getBlockName(), getBlockPath(),
                countDirection != null ? countDirection.getAsString() : "null",
                initialCount != null ? initialCount.getAsDouble() : "null",
                sampleTime != null ? sampleTime.getAsDouble() : "null");
    }
}
