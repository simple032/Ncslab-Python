package com.ncslab.dto.block.specialized.subsystem;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.block.BlockPositionDto;
import com.ncslab.dto.block.BlockDimensionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for Function-Call Generator block - Generates function-call events for subsystems.
 *
 * <p>This block generates function-call events at specified sample intervals to trigger
 * execution of function-call subsystems. Function-call subsystems execute only when triggered
 * by a function-call event, providing precise control over subsystem execution timing.</p>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>SampleTime</b>: Sample time for function-call generation (default: 1.0)</li>
 *   <li><b>InitFcnCallInput</b>: Initial function-call state - "off" (default) or "on"</li>
 * </ul>
 *
 * <p><b>Function-Call Event Generation:</b></p>
 * <ul>
 *   <li>Generates events at regular intervals based on sample time</li>
 *   <li>Each event triggers execution of connected function-call subsystem</li>
 *   <li>InitFcnCallInput controls whether an event is generated at t=0</li>
 *   <li>Events are edge-triggered (rising edge detection)</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>0 input ports (event generator, not driven by signals)</li>
 *   <li>1 output port (function-call signal - special signal type)</li>
 * </ul>
 *
 * <p><b>Use Cases:</b></p>
 * <ul>
 *   <li>Periodic task execution in real-time systems</li>
 *   <li>Multi-rate system simulation with explicit task scheduling</li>
 *   <li>Event-driven control logic implementation</li>
 *   <li>Function-call subsystem triggering and coordination</li>
 * </ul>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>SampleTime must be positive (> 0)</li>
 *   <li>InitFcnCallInput must be "on" or "off"</li>
 *   <li>Output must be connected to function-call subsystem</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("FunctionCallGenerator")
public class FunctionCallGeneratorDto extends BlockDto {

    /**
     * Sample time for function-call event generation.
     * Determines the interval between function-call events (must be positive).
     * Default: 1.0 second
     */
    private TypedParameter sampleTimeParam = TypedParameter.of(1.0);

    /**
     * Initial function-call state at t=0.
     * "off" - No function-call event at initialization (default)
     * "on" - Generate function-call event at initialization
     */
    private TypedParameter initFcnCallInput = TypedParameter.of("off");

    /**
     * Constructs FunctionCallGeneratorDto with individual parameters.
     *
     * @param blockName         Name of the function-call generator block
     * @param blockPath         Path of the block in the model hierarchy
     * @param sampleTimeParam   Sample time for event generation
     * @param initFcnCallInput  Initial function-call state ("on" or "off")
     */
    public FunctionCallGeneratorDto(String blockName, String blockPath,
                                    TypedParameter sampleTimeParam,
                                    TypedParameter initFcnCallInput) {
        super(blockName, blockPath);
        this.sampleTimeParam = sampleTimeParam;
        this.initFcnCallInput = initFcnCallInput;
        // Note: sampleTime in base class is NOT used for this block
        // sampleTimeParam controls the function-call generation rate
    }

    /**
     * Constructs FunctionCallGeneratorDto with typed parameter map.
     *
     * @param blockName  Name of the function-call generator block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public FunctionCallGeneratorDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.sampleTimeParam = parameters.getTypedParameter("SampleTime", Double.class, 1.0);
        this.initFcnCallInput = parameters.getTypedParameter("InitFcnCallInput", String.class, "off");
    }

    /**
     * Creates FunctionCallGeneratorDto with specified block metadata.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position  Block position in diagram
     * @param dimension Block visual dimensions
     */
    public FunctionCallGeneratorDto(String blockName, String blockPath,
                                   BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
        this.sampleTimeParam = TypedParameter.of(1.0);
        this.initFcnCallInput = TypedParameter.of("off");
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate sample time parameter
        if (sampleTimeParam == null || sampleTimeParam.getAsDouble() == null) {
            addValidationError("SampleTime parameter cannot be null");
            return false;
        }

        double sampleTimeValue = getSampleTimeParamValue();
        if (sampleTimeValue <= 0.0 || Double.isNaN(sampleTimeValue) || Double.isInfinite(sampleTimeValue)) {
            addValidationError("SampleTime must be positive and finite");
            return false;
        }

        // Validate InitFcnCallInput
        String initState = getInitFcnCallInputValue();
        if (!isValidOnOffParameter(initState)) {
            addValidationError("InitFcnCallInput must be 'on' or 'off'");
            return false;
        }

        return true;
    }

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();

        // Validate sample time parameter
        if (sampleTimeParam != null && sampleTimeParam.getAsDouble() != null) {
            double stValue = getSampleTimeParamValue();
            if (stValue <= 0.0) {
                errors.add("SampleTime must be positive (> 0)");
            }
            if (Double.isNaN(stValue) || Double.isInfinite(stValue)) {
                errors.add("SampleTime must be finite");
            }
            if (stValue > 1e6) {
                errors.add("SampleTime exceeds reasonable limit (1e6 seconds)");
            }
        }

        // Validate InitFcnCallInput
        String initState = getInitFcnCallInputValue();
        if (!isValidOnOffParameter(initState)) {
            errors.add("InitFcnCallInput must be 'on' or 'off'");
        }

        return errors;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the sample time value for function-call generation.
     *
     * @return Sample time interval (positive value)
     */
    public double getSampleTimeParamValue() {
        if (sampleTimeParam != null && sampleTimeParam.getAsDouble() != null) {
            return sampleTimeParam.getAsDouble();
        }
        return 1.0; // Default sample time
    }

    /**
     * Gets the initial function-call state.
     *
     * @return Initial state ("on" or "off")
     */
    public String getInitFcnCallInputValue() {
        if (initFcnCallInput != null && initFcnCallInput.getAsString() != null) {
            return initFcnCallInput.getAsString();
        }
        return "off"; // Default off
    }

    // === Helper Methods ===

    /**
     * Checks if initial function-call is enabled.
     *
     * @return true if function-call event is generated at t=0
     */
    public boolean isInitialCallEnabled() {
        return "on".equalsIgnoreCase(getInitFcnCallInputValue());
    }

    /**
     * Checks if the generator produces periodic function-calls.
     * Function-call generators are always periodic (discrete-time).
     *
     * @return true (always periodic)
     */
    public boolean isPeriodic() {
        return true;
    }

    /**
     * Gets the period of function-call generation.
     *
     * @return Sample time interval (same as getSampleTimeParamValue)
     */
    public double getPeriod() {
        return getSampleTimeParamValue();
    }

    /**
     * Calculates the number of function-call events in a given time interval.
     *
     * @param duration Time duration to analyze
     * @return Number of function-call events that would occur
     */
    public int getEventCount(double duration) {
        if (duration <= 0.0) {
            return 0;
        }
        double period = getSampleTimeParamValue();
        int count = (int) Math.floor(duration / period) + 1;
        return isInitialCallEnabled() ? count + 1 : count;
    }

    // === Validation Helpers ===

    /**
     * Validates on/off parameter.
     *
     * @param value Parameter value string
     * @return true if value is valid ("on" or "off")
     */
    private boolean isValidOnOffParameter(String value) {
        return value != null && (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("off"));
    }

    // === Factory Methods ===

    @Override
    public FunctionCallGeneratorDto copy() {
        FunctionCallGeneratorDto copy = new FunctionCallGeneratorDto();

        // Copy base fields
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());

        // Copy DTO-specific fields
        copy.sampleTimeParam = sampleTimeParam != null ? sampleTimeParam.copy() : null;
        copy.initFcnCallInput = initFcnCallInput != null ? initFcnCallInput.copy() : null;

        return copy;
    }

    /**
     * Creates a TypedParameterMap from this DTO's parameters.
     *
     * @return TypedParameterMap containing all block parameters
     */
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("SampleTime", sampleTimeParam)
                .put("InitFcnCallInput", initFcnCallInput)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "SampleTime", "Sample time for function-call event generation (must be positive)",
            "InitFcnCallInput", "Initial function-call state at t=0 ('on' or 'off')"
        );
    }

    @Override
    public String toString() {
        return String.format("FunctionCallGeneratorDto{blockName='%s', sampleTime=%.3f, initCall='%s'}",
                           getBlockName(), getSampleTimeParamValue(), getInitFcnCallInputValue());
    }
}
