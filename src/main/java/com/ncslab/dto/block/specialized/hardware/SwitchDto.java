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
 * DTO for Switch block - Digital switch input hardware interface.
 * 
 * <p>This block interfaces with digital switch hardware, providing configurable
 * switch state detection with the following parameters:
 * <ul>
 *   <li><b>Pin</b>: Digital input pin number</li>
 *   <li><b>PullUp</b>: Enable internal pull-up resistor</li>
 *   <li><b>ActiveLow</b>: Invert switch logic (active low)</li>
 *   <li><b>SampleTime</b>: Sample time for reading switch state (-1 for inherited)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type specification</li>
 * </ul>
 * </p>
 * 
 * <p><b>Hardware Interface:</b></p>
 * <ul>
 *   <li>Reads digital switch state from specified pin</li>
 *   <li>Supports internal pull-up resistor configuration</li>
 *   <li>Configurable active high/low logic</li>
 * </ul>
 * 
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Pin must be non-negative integer</li>
 *   <li>SampleTime must be >= 0 or -1 (inherited)</li>
 *   <li>PullUp and ActiveLow must be boolean values</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonTypeName("Switch")
public class SwitchDto extends BlockDto {

    /**
     * Digital input pin number.
     * Specifies which hardware pin to read the switch state from.
     */
    private TypedParameter pin;

    /**
     * Enable internal pull-up resistor.
     * When enabled, activates the microcontroller's internal pull-up resistor.
     */
    private TypedParameter pullUp;

    /**
     * Invert switch logic (active low).
     * When true, treats logic low (0V) as switch pressed.
     */
    private TypedParameter activeLow;

    /**
     * Sample time for reading switch state.
     * -1 for inherited, 0 for continuous, >0 for discrete
     */
    private TypedParameter sampleTime;

    /**
     * Output data type specification.
     * Controls the data type of the block output.
     */
    private TypedParameter outDataTypeStr;

    /**
     * Constructs SwitchDto with individual parameters.
     *
     * @param blockName   Name of the block
     * @param blockPath   Path of the block in the model hierarchy
     * @param pin         Digital input pin number
     * @param pullUp      Enable internal pull-up resistor
     * @param activeLow   Invert switch logic
     * @param sampleTime  Sample time parameter
     * @param outDataTypeStr Output data type parameter
     */
    public SwitchDto(String blockName, String blockPath,
                     TypedParameter pin,
                     TypedParameter pullUp,
                     TypedParameter activeLow,
                     TypedParameter sampleTime,
                     TypedParameter outDataTypeStr) {
        super(blockName, blockPath);
        this.pin = pin;
        this.pullUp = pullUp;
        this.activeLow = activeLow;
        this.sampleTime = sampleTime;
        this.outDataTypeStr = outDataTypeStr;
    }

    /**
     * Constructs SwitchDto with typed parameter map.
     *
     * @param blockName  Name of the block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public SwitchDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.pin = parameters.getTypedParameter("Pin", Integer.class, 2);
        this.pullUp = parameters.getTypedParameter("PullUp", Boolean.class, true);
        this.activeLow = parameters.getTypedParameter("ActiveLow", Boolean.class, false);
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, 0.1);
        this.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class, "boolean");
    }

    /**
     * Creates SwitchDto with specified block metadata and hardware configuration.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param pin Digital input pin number
     */
    public SwitchDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension, int pin) {
        super(blockName, blockPath, position, dimension);
        this.pin = TypedParameter.of(pin);
        this.pullUp = TypedParameter.of(true);
        this.activeLow = TypedParameter.of(false);
        this.sampleTime = TypedParameter.of(0.1);
        this.outDataTypeStr = TypedParameter.of("boolean");
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
        if (pullUp == null || pullUp.getAsBoolean() == null) {
            addValidationError("PullUp parameter cannot be null");
            return false;
        }

        if (activeLow == null || activeLow.getAsBoolean() == null) {
            addValidationError("ActiveLow parameter cannot be null");
            return false;
        }

        // Validate output data type
        if (outDataTypeStr == null || outDataTypeStr.getAsString() == null || 
            outDataTypeStr.getAsString().toString().trim().isEmpty()) {
            addValidationError("Output data type cannot be null or empty");
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
     * @return Digital input pin number
     */
    public int getPinValue() {
        if (pin != null && pin.getAsInteger() != null) {
            Object value = pin.getAsInteger();
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        return 2; // Default pin
    }

    /**
     * Gets the pull-up resistor setting.
     *
     * @return true if pull-up resistor is enabled
     */
    public boolean getPullUpValue() {
        if (pullUp != null && pullUp.getAsBoolean() != null) {
            Object value = pullUp.getAsBoolean();
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return true; // Default enabled
    }

    /**
     * Gets the active low setting.
     *
     * @return true if switch logic is inverted (active low)
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
     * @return Sample time for reading switch state
     */
    public double getSampleTimeValue() {
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            Object value = sampleTime.getAsDouble();
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return 0.1; // Default 100ms
    }

    /**
     * Gets the output data type string.
     *
     * @return Output data type specification
     */
    public String getOutDataTypeStrValue() {
        if (outDataTypeStr != null && outDataTypeStr.getAsString() != null) {
            return outDataTypeStr.getAsString().toString();
        }
        return "boolean";
    }

    // === Helper Methods ===

    /**
     * Checks if the switch is configured for continuous time operation.
     *
     * @return true if sample time is 0 (continuous)
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if the switch inherits its sample time.
     *
     * @return true if sample time is -1 (inherited)
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Checks if the switch is configured for discrete time operation.
     *
     * @return true if sample time is positive (discrete)
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }

    // === Factory Methods ===
    
    @Override
    public SwitchDto copy() {
        SwitchDto copy = new SwitchDto();
        
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
        copy.pullUp = pullUp != null ? pullUp.copy() : null;
        copy.activeLow = activeLow != null ? activeLow.copy() : null;
        copy.outDataTypeStr = outDataTypeStr != null ? outDataTypeStr.copy() : null;
        
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
                .put("PullUp", pullUp)
                .put("ActiveLow", activeLow)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "Pin", "Digital input pin number (0-255)",
            "PullUp", "Enable internal pull-up resistor",
            "ActiveLow", "Invert switch logic (active low when true)",
            "SampleTime", "Sample time for reading switch state (-1 for inherited, 0 for continuous)",
            "OutDataTypeStr", "Output data type specification"
        );
    }

    @Override
    public String toString() {
        return String.format("SwitchDto{blockName='%s', pin=%d, pullUp=%s, activeLow=%s, sampleTime=%.3f}",
                           getBlockName(), getPinValue(), getPullUpValue(), getActiveLowValue(), getSampleTimeValue());
    }
}