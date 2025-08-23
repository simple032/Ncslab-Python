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
 * DTO for AnalogRead block - Analog input reading hardware interface.
 * 
 * <p>This block interfaces with analog input hardware, providing configurable
 * analog-to-digital conversion with the following parameters:
 * <ul>
 *   <li><b>Pin</b>: Analog input pin number</li>
 *   <li><b>VoltageRange</b>: Input voltage range (0-3.3V, 0-5V, etc.)</li>
 *   <li><b>Resolution</b>: ADC resolution in bits (8, 10, 12, 16)</li>
 *   <li><b>SampleTime</b>: Sample time for ADC readings (-1 for inherited)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type specification</li>
 *   <li><b>Scaling</b>: Output scaling mode (Raw, Voltage, Percentage)</li>
 * </ul>
 * </p>
 * 
 * <p><b>Hardware Interface:</b></p>
 * <ul>
 *   <li>Reads analog voltage from specified pin using ADC</li>
 *   <li>Configurable voltage reference and resolution</li>
 *   <li>Multiple output scaling options for different applications</li>
 * </ul>
 * 
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Pin must be valid analog input pin number</li>
 *   <li>VoltageRange must be positive and finite</li>
 *   <li>Resolution must be between 1 and 32 bits</li>
 *   <li>SampleTime must be >= 0 or -1 (inherited)</li>
 *   <li>Scaling must be valid scaling mode</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonTypeName("AnalogRead")
public class AnalogReadDto extends BlockDto {

    /**
     * Analog input pin number.
     * Specifies which ADC channel/pin to read from.
     */
    private TypedParameter pin;

    /**
     * Input voltage range.
     * Maximum input voltage that corresponds to full scale reading.
     */
    private TypedParameter voltageRange;

    /**
     * ADC resolution in bits.
     * Determines the number of discrete levels in the conversion.
     */
    private TypedParameter resolution;

    /**
     * Output data type specification.
     * Controls the data type of the block output.
     */
    private TypedParameter outDataTypeStr;

    /**
     * Output scaling mode.
     * Determines how raw ADC values are converted to output.
     */
    private TypedParameter scaling;

    /**
     * Constructs AnalogReadDto with individual parameters.
     *
     * @param blockName      Name of the block
     * @param blockPath      Path of the block in the model hierarchy
     * @param pin            Analog input pin number
     * @param voltageRange   Input voltage range
     * @param resolution     ADC resolution in bits
     * @param sampleTime     Sample time parameter
     * @param outDataTypeStr Output data type parameter
     * @param scaling        Output scaling mode
     */
    public AnalogReadDto(String blockName, String blockPath,
                        TypedParameter pin,
                        TypedParameter voltageRange,
                        TypedParameter resolution,
                        TypedParameter sampleTime,
                        TypedParameter outDataTypeStr,
                        TypedParameter scaling) {
        super("AnalogRead", blockName, blockPath);
        this.pin = pin;
        this.voltageRange = voltageRange;
        this.resolution = resolution;
        this.sampleTime = sampleTime;
        this.outDataTypeStr = outDataTypeStr;
        this.scaling = scaling;
    }

    /**
     * Constructs AnalogReadDto with typed parameter map.
     *
     * @param blockName  Name of the block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public AnalogReadDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super("AnalogRead", blockName, blockPath);
        this.pin = parameters.getTypedParameter("Pin", Integer.class, 0);
        this.voltageRange = parameters.getTypedParameter("VoltageRange", Double.class, 3.3);
        this.resolution = parameters.getTypedParameter("Resolution", Integer.class, 10);
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, 0.01);
        this.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class, "double");
        this.scaling = parameters.getTypedParameter("Scaling", String.class, "Voltage");
    }

    /**
     * Creates AnalogReadDto with specified block metadata and hardware configuration.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param pin Analog input pin number
     */
    public AnalogReadDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension, int pin) {
        super("AnalogRead", blockName, blockPath, position, dimension);
        this.pin = TypedParameter.of(pin);
        this.voltageRange = TypedParameter.of(3.3);
        this.resolution = TypedParameter.of(10);
        this.sampleTime = TypedParameter.of(0.01);
        this.outDataTypeStr = TypedParameter.of("double");
        this.scaling = TypedParameter.of("Voltage");
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

        // Validate voltage range
        if (voltageRange == null || voltageRange.getAsString() == null) {
            addValidationError("Voltage range cannot be null");
            return false;
        }

        double voltageValue = getVoltageRangeValue();
        if (voltageValue <= 0.0 || Double.isNaN(voltageValue) || Double.isInfinite(voltageValue)) {
            addValidationError("Voltage range must be positive and finite");
            return false;
        }

        // Validate resolution
        if (resolution == null || resolution.getAsInteger() == null) {
            addValidationError("Resolution cannot be null");
            return false;
        }

        int resolutionValue = getResolutionValue();
        if (resolutionValue < 1 || resolutionValue > 32) {
            addValidationError("Resolution must be between 1 and 32 bits");
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

        // Validate output data type
        if (outDataTypeStr == null || outDataTypeStr.getAsString() == null || 
            outDataTypeStr.getAsString().toString().trim().isEmpty()) {
            addValidationError("Output data type cannot be null or empty");
            return false;
        }

        // Validate scaling mode
        if (scaling == null || scaling.getAsString() == null) {
            addValidationError("Scaling mode cannot be null");
            return false;
        }

        String scalingValue = getScalingValue();
        if (!isValidScalingMode(scalingValue)) {
            addValidationError("Invalid scaling mode: " + scalingValue);
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
            // Most microcontrollers have limited ADC pins
            if (pinValue > 31) {
                errors.add("Pin number exceeds typical ADC pin range (0-31)");
            }
        }
        
        // Validate voltage range
        if (voltageRange != null && voltageRange.getAsString() != null) {
            double vRange = getVoltageRangeValue();
            if (vRange <= 0.0 || Double.isNaN(vRange) || Double.isInfinite(vRange)) {
                errors.add("Voltage range must be positive and finite");
            }
            if (vRange > 50.0) { // Reasonable safety limit
                errors.add("Voltage range exceeds safety limit (50V)");
            }
        }
        
        // Validate resolution
        if (resolution != null && resolution.getAsInteger() != null) {
            int res = getResolutionValue();
            if (res < 1 || res > 32) {
                errors.add("Resolution must be between 1 and 32 bits");
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
     * @return Analog input pin number
     */
    public int getPinValue() {
        if (pin != null && pin.getAsInteger() != null) {
            Object value = pin.getAsInteger();
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        return 0; // Default A0 pin
    }

    /**
     * Gets the voltage range value.
     *
     * @return Input voltage range in volts
     */
    public double getVoltageRangeValue() {
        if (voltageRange != null && voltageRange.getAsString() != null) {
            Object value = voltageRange.getAsString();
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return 3.3; // Default 3.3V
    }

    /**
     * Gets the ADC resolution value.
     *
     * @return ADC resolution in bits
     */
    public int getResolutionValue() {
        if (resolution != null && resolution.getAsInteger() != null) {
            Object value = resolution.getAsInteger();
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        return 10; // Default 10-bit (Arduino standard)
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time for ADC readings
     */
    public double getSampleTimeValue() {
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            Object value = sampleTime.getAsDouble();
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return 0.01; // Default 10ms
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
        return "double";
    }

    /**
     * Gets the scaling mode string.
     *
     * @return Output scaling mode
     */
    public String getScalingValue() {
        if (scaling != null && scaling.getAsString() != null) {
            return scaling.getAsString().toString();
        }
        return "Voltage";
    }

    // === Helper Methods ===

    /**
     * Validates if the scaling mode is supported.
     *
     * @param scalingMode The scaling mode to validate
     * @return true if the scaling mode is valid
     */
    private boolean isValidScalingMode(String scalingMode) {
        return scalingMode != null && (
            scalingMode.equals("Raw") ||
            scalingMode.equals("Voltage") ||
            scalingMode.equals("Percentage")
        );
    }

    /**
     * Calculates the maximum raw ADC value based on resolution.
     *
     * @return Maximum raw ADC value
     */
    public int getMaxRawValue() {
        return (1 << getResolutionValue()) - 1;
    }

    /**
     * Converts raw ADC value to voltage.
     *
     * @param rawValue The raw ADC value (0 to max)
     * @return Voltage value
     */
    public double rawToVoltage(int rawValue) {
        return (rawValue / (double) getMaxRawValue()) * getVoltageRangeValue();
    }

    /**
     * Converts raw ADC value to percentage.
     *
     * @param rawValue The raw ADC value (0 to max)
     * @return Percentage value (0.0 to 100.0)
     */
    public double rawToPercentage(int rawValue) {
        return (rawValue / (double) getMaxRawValue()) * 100.0;
    }

    /**
     * Checks if the analog read is configured for continuous time operation.
     *
     * @return true if sample time is 0 (continuous)
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if the analog read inherits its sample time.
     *
     * @return true if sample time is -1 (inherited)
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Checks if the analog read is configured for discrete time operation.
     *
     * @return true if sample time is positive (discrete)
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }

    // === Factory Methods ===
    
    @Override
    public AnalogReadDto copy() {
        AnalogReadDto copy = new AnalogReadDto();
        
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
        copy.voltageRange = voltageRange != null ? voltageRange.copy() : null;
        copy.resolution = resolution != null ? resolution.copy() : null;
        copy.outDataTypeStr = outDataTypeStr != null ? outDataTypeStr.copy() : null;
        copy.scaling = scaling != null ? scaling.copy() : null;
        
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
                .put("VoltageRange", voltageRange)
                .put("Resolution", resolution)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("Scaling", scaling)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "Pin", "Analog input pin number (ADC channel)",
            "VoltageRange", "Input voltage range in volts (e.g., 3.3, 5.0)",
            "Resolution", "ADC resolution in bits (8, 10, 12, 16)",
            "SampleTime", "Sample time for ADC readings (-1 for inherited, 0 for continuous)",
            "OutDataTypeStr", "Output data type specification",
            "Scaling", "Output scaling mode (Raw, Voltage, Percentage)"
        );
    }

    @Override
    public String toString() {
        return String.format("AnalogReadDto{blockName='%s', pin=A%d, voltage=%.1fV, resolution=%d-bit, scaling=%s, sampleTime=%.3f}",
                           getBlockName(), getPinValue(), getVoltageRangeValue(), getResolutionValue(), getScalingValue(), getSampleTimeValue());
    }
}