package com.ncslab.dto.block.specialized.hardware;

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
 * DTO for DigitalWrite block - Digital output control hardware interface.
 * 
 * <p>This block interfaces with digital output hardware, providing configurable
 * digital signal output with the following parameters:
 * <ul>
 *   <li><b>Pin</b>: Digital output pin number</li>
 *   <li><b>InitialValue</b>: Initial output state on startup</li>
 *   <li><b>ActiveLow</b>: Invert output logic (active low)</li>
 *   <li><b>SampleTime</b>: Sample time for output updates (-1 for inherited)</li>
 * </ul>
 * </p>
 * 
 * <p><b>Hardware Interface:</b></p>
 * <ul>
 *   <li>Controls digital output pin state (HIGH/LOW)</li>
 *   <li>Configurable initial state for predictable startup behavior</li>
 *   <li>Supports active high/low logic configuration</li>
 * </ul>
 * 
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Pin must be non-negative integer</li>
 *   <li>InitialValue must be boolean or binary (0/1)</li>
 *   <li>SampleTime must be >= 0 or -1 (inherited)</li>
 *   <li>ActiveLow must be boolean value</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonTypeName("DigitalWrite")
public class DigitalWriteDto extends BlockDto {

    /**
     * Digital output pin number.
     * Specifies which hardware pin to control for digital output.
     */
    private TypedParameter pin;

    /**
     * Initial output state on startup.
     * Defines the pin state when the system starts (before input processing).
     */
    private TypedParameter initialValue;

    /**
     * Invert output logic (active low).
     * When true, logical HIGH input results in LOW voltage output.
     */
    private TypedParameter activeLow;

    /**
     * Sample time for output updates.
     * -1 for inherited, 0 for continuous, >0 for discrete
     */
    private TypedParameter sampleTime;

    /**
     * Constructs DigitalWriteDto with individual parameters.
     *
     * @param blockName    Name of the block
     * @param blockPath    Path of the block in the model hierarchy
     * @param pin          Digital output pin number
     * @param initialValue Initial output state
     * @param activeLow    Invert output logic
     * @param sampleTime   Sample time parameter
     */
    public DigitalWriteDto(String blockName, String blockPath,
                          TypedParameter pin,
                          TypedParameter initialValue,
                          TypedParameter activeLow,
                          TypedParameter sampleTime) {
        super(blockName,blockPath);
        this.pin = pin;
        this.initialValue = initialValue;
        this.activeLow = activeLow;
        this.sampleTime = sampleTime;
    }

    /**
     * Constructs DigitalWriteDto with typed parameter map.
     *
     * @param blockName  Name of the block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public DigitalWriteDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName,blockPath);
        this.pin = parameters.getTypedParameter("Pin", Integer.class, 13);
        this.initialValue = parameters.getTypedParameter("InitialValue", Boolean.class, false);
        this.activeLow = parameters.getTypedParameter("ActiveLow", Boolean.class, false);
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, 0.0);
    }

    /**
     * Creates DigitalWriteDto with specified block metadata and hardware configuration.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param pin Digital output pin number
     */
    public DigitalWriteDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension, int pin) {
        super(blockName,blockPath, position, dimension);
        this.pin = TypedParameter.of(pin);
        this.initialValue = TypedParameter.of(false);
        this.activeLow = TypedParameter.of(false);
        this.sampleTime = TypedParameter.of(0.0);
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate pin number
        if (pin == null || pin.getAsInteger() == null) {
            addValidationError("Pin number cannot be null");
            return false;
        }

        int pinValue = getPinValue();
        if (pinValue < 0) {
            addValidationError("Pin number must be non-negative");
            return false;
        }

        // Validate sample time
        if (sampleTime == null || sampleTime.getAsDouble() == null) {
            addValidationError("Sample time cannot be null");
            return false;
        }

        double sampleTimeValue = getSampleTimeValue();
        if (sampleTimeValue < -1.0 || Double.isNaN(sampleTimeValue) || Double.isInfinite(sampleTimeValue)) {
            addValidationError("Sample time must be >= -1.0 and finite");
            return false;
        }

        // Validate boolean parameters
        if (initialValue == null || initialValue.getAsBoolean() == null) {
            addValidationError("InitialValue parameter cannot be null");
            return false;
        }

        if (activeLow == null || activeLow.getAsBoolean() == null) {
            addValidationError("ActiveLow parameter cannot be null");
            return false;
        }

        return true;
    }

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();
        
        // Validate pin number
        if (pin != null && pin.getAsInteger() != null) {
            int pinValue = getPinValue();
            if (pinValue < 0) {
                errors.add("Pin number must be non-negative");
            }
            if (pinValue > 255) { // Reasonable upper limit for most microcontrollers
                errors.add("Pin number exceeds reasonable limit (255)");
            }
        }
        
        // Validate sample time
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            double stValue = getSampleTimeValue();
            if (stValue < -1.0 || Double.isNaN(stValue) || Double.isInfinite(stValue)) {
                errors.add("Sample time must be >= -1.0 and finite");
            }
        }
        
        return errors;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the pin number value.
     *
     * @return Digital output pin number
     */
    public int getPinValue() {
        if (pin != null && pin.getAsInteger() != null) {
            Object value = pin.getAsInteger();
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        return 13; // Default LED pin on many Arduino boards
    }

    /**
     * Gets the initial value setting.
     *
     * @return Initial output state on startup
     */
    public boolean getInitialValueValue() {
        if (initialValue != null && initialValue.getAsBoolean() != null) {
            Object value = initialValue.getAsBoolean();
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            }
        }
        return false; // Default LOW
    }

    /**
     * Gets the active low setting.
     *
     * @return true if output logic is inverted (active low)
     */
    public boolean getActiveLowValue() {
        if (activeLow != null && activeLow.getAsBoolean() != null) {
            Object value = activeLow.getAsBoolean();
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return false; // Default active high
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time for output updates
     */
    public double getSampleTimeValue() {
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            Object value = sampleTime.getAsDouble();
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return 0.0; // Default continuous
    }

    // === Helper Methods ===

    /**
     * Checks if the digital write is configured for continuous time operation.
     *
     * @return true if sample time is 0 (continuous)
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if the digital write inherits its sample time.
     *
     * @return true if sample time is -1 (inherited)
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Checks if the digital write is configured for discrete time operation.
     *
     * @return true if sample time is positive (discrete)
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }

    /**
     * Determines the actual hardware output state given a logical input.
     *
     * @param logicalState The logical input state (true/false)
     * @return The physical output state considering active low setting
     */
    public boolean getPhysicalOutput(boolean logicalState) {
        return getActiveLowValue() ? !logicalState : logicalState;
    }

    // === Factory Methods ===
    
    @Override
    public DigitalWriteDto copy() {
        DigitalWriteDto copy = new DigitalWriteDto();
        
        // Copy base fields
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());
        
        // Copy DTO-specific fields
        copy.pin = pin != null ? pin.copy() : null;
        copy.initialValue = initialValue != null ? initialValue.copy() : null;
        copy.activeLow = activeLow != null ? activeLow.copy() : null;
        
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
                .put("Pin", pin)
                .put("InitialValue", initialValue)
                .put("ActiveLow", activeLow)
                .put("SampleTime", sampleTime)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "Pin", "Digital output pin number (0-255)",
            "InitialValue", "Initial output state on startup (true=HIGH, false=LOW)",
            "ActiveLow", "Invert output logic (active low when true)",
            "SampleTime", "Sample time for output updates (-1 for inherited, 0 for continuous)"
        );
    }

    @Override
    public String toString() {
        return String.format("DigitalWriteDto{blockName='%s', pin=%d, initialValue=%s, activeLow=%s, sampleTime=%.3f}",
                           getBlockName(), getPinValue(), getInitialValueValue(), getActiveLowValue(), getSampleTimeValue());
    }
}