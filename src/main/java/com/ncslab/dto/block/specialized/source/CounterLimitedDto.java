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
 * Data Transfer Object for CounterLimited source block.
 * CounterLimited blocks count up or down with wraparound limits.
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
@JsonTypeName("CounterLimited")
public class CounterLimitedDto extends BlockDto {

    /**
     * Count direction: "Up" or "Down"
     */
    private TypedParameter countDirection;

    /**
     * Initial count value (starting count)
     */
    private TypedParameter initialCount;

    /**
     * Maximum count before wrapping (for up) or minimum before wrapping (for down)
     */
    private TypedParameter maxCount;

    /**
     * Output at terminal count: "Hit" or "Wrap"
     */
    private TypedParameter outputAtTerminalCount;

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

    public TypedParameter getMaxCount() {
        if (maxCount == null) {
            maxCount = TypedParameter.of(255.0);
        }
        return maxCount;
    }

    public TypedParameter getOutputAtTerminalCount() {
        if (outputAtTerminalCount == null) {
            outputAtTerminalCount = TypedParameter.of("Wrap");
        }
        return outputAtTerminalCount;
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
    public CounterLimitedDto(String blockName, String blockPath) {
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
        if (maxCount == null) {
            maxCount = TypedParameter.of(255.0);
        }
        if (outputAtTerminalCount == null) {
            outputAtTerminalCount = TypedParameter.of("Wrap");
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
     * Constructs CounterLimitedDto with individual parameters.
     */
    public CounterLimitedDto(String blockName, String blockPath,
                            TypedParameter countDirection,
                            TypedParameter initialCount,
                            TypedParameter maxCount,
                            TypedParameter outputAtTerminalCount,
                            TypedParameter sampleTime,
                            TypedParameter outDataTypeStr,
                            TypedParameter saturateOnIntegerOverflow) {
        super(blockName, blockPath);
        this.countDirection = countDirection;
        this.initialCount = initialCount;
        this.maxCount = maxCount;
        this.outputAtTerminalCount = outputAtTerminalCount;
        this.sampleTime = sampleTime;
        this.outDataTypeStr = outDataTypeStr;
        this.saturateOnIntegerOverflow = saturateOnIntegerOverflow;
    }

    /**
     * Constructs CounterLimitedDto with typed parameter map.
     */
    public CounterLimitedDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.countDirection = parameters.getTypedParameter("CountDirection", String.class, "Up");
        this.initialCount = parameters.getTypedParameter("InitialCount", Double.class, 0.0);
        this.maxCount = parameters.getTypedParameter("MaxCount", Double.class, 255.0);
        this.outputAtTerminalCount = parameters.getTypedParameter("OutputAtTerminalCount", String.class, "Wrap");
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

        // Validate max count
        if (maxCount == null || maxCount.getAsDouble() == null) {
            addValidationError("Max count cannot be null");
            return false;
        }

        // Validate output at terminal count
        if (outputAtTerminalCount == null || outputAtTerminalCount.getAsString() == null) {
            addValidationError("Output at terminal count cannot be null");
            return false;
        }
        String terminalBehavior = outputAtTerminalCount.getAsString();
        if (!terminalBehavior.equals("Hit") && !terminalBehavior.equals("Wrap")) {
            addValidationError("Output at terminal count must be 'Hit' or 'Wrap'");
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
     * Gets the max count value with validation.
     */
    public double getMaxCountValue() {
        if (maxCount == null || maxCount.getAsDouble() == null) {
            throw new IllegalStateException("Max count is not properly initialized");
        }
        return maxCount.getAsDouble();
    }

    /**
     * Gets the output at terminal count value with validation.
     */
    public String getOutputAtTerminalCountValue() {
        if (outputAtTerminalCount == null || outputAtTerminalCount.getAsString() == null) {
            throw new IllegalStateException("Output at terminal count is not properly initialized");
        }
        return outputAtTerminalCount.getAsString();
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

    /**
     * Checks if counter wraps at terminal count.
     */
    public boolean isWrapMode() {
        return "Wrap".equals(getOutputAtTerminalCountValue());
    }

    /**
     * Checks if counter holds at terminal count.
     */
    public boolean isHitMode() {
        return "Hit".equals(getOutputAtTerminalCountValue());
    }

    @Override
    public String toString() {
        return String.format("CounterLimitedDto{blockName='%s', blockPath='%s', direction='%s', initial=%s, max=%s, terminal='%s', sampleTime=%s}",
                getBlockName(), getBlockPath(),
                countDirection != null ? countDirection.getAsString() : "null",
                initialCount != null ? initialCount.getAsDouble() : "null",
                maxCount != null ? maxCount.getAsDouble() : "null",
                outputAtTerminalCount != null ? outputAtTerminalCount.getAsString() : "null",
                sampleTime != null ? sampleTime.getAsDouble() : "null");
    }
}
