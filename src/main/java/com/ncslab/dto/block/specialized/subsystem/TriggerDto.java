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
 * DTO for Trigger block - Event-driven subsystem execution control.
 *
 * <p>This block represents a trigger port within a subsystem, providing
 * event-driven execution control based on signal transitions. Trigger blocks
 * control when a subsystem executes based on rising edges, falling edges,
 * either edge, or function-call events.</p>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>TriggerType</b>: Type of trigger detection ("rising", "falling", "either", "function-call")</li>
 *   <li><b>ShowOutputPort</b>: Whether to create output port for trigger signal passthrough ("on", "off")</li>
 *   <li><b>ZeroCross</b>: Enable zero-crossing detection for precise edge detection ("on", "off")</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 *   <li><b>OutputDataType</b>: Output data type specification ("auto" or specific type)</li>
 * </ul>
 *
 * <p><b>Trigger Types:</b></p>
 * <ul>
 *   <li><b>rising</b>: Trigger on positive edge (0 to non-zero transition)</li>
 *   <li><b>falling</b>: Trigger on negative edge (non-zero to 0 transition)</li>
 *   <li><b>either</b>: Trigger on any edge (rising or falling)</li>
 *   <li><b>function-call</b>: Trigger via explicit function call mechanism</li>
 * </ul>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>TriggerType must be one of: "rising", "falling", "either", "function-call"</li>
 *   <li>ShowOutputPort must be "on" or "off"</li>
 *   <li>ZeroCross must be "on" or "off"</li>
 *   <li>SampleTime must be >= -1.0 and finite</li>
 *   <li>Must be contained within a subsystem block</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-03
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Trigger")
public class TriggerDto extends BlockDto {

    /**
     * Trigger type for edge detection.
     * Controls which type of signal transition triggers subsystem execution.
     */
    private TypedParameter triggerType = TypedParameter.of("rising");

    /**
     * Whether to show output port for trigger signal passthrough.
     * When enabled, the trigger signal is available as an output.
     */
    private TypedParameter showOutputPort = TypedParameter.of("off");

    /**
     * Zero-crossing detection enable flag.
     * When enabled, provides more precise edge detection by detecting zero crossings.
     */
    private TypedParameter zeroCross = TypedParameter.of("on");

    /**
     * Output data type specification.
     * Controls the data type of the trigger output signal when ShowOutputPort is enabled.
     */
    private TypedParameter outputDataType = TypedParameter.of("auto");

    /**
     * Constructs TriggerDto with individual parameters.
     *
     * @param blockName        Name of the trigger block
     * @param blockPath        Path of the block in the model hierarchy
     * @param triggerType      Trigger type parameter ("rising", "falling", "either", "function-call")
     * @param showOutputPort   Show output port flag parameter ("on", "off")
     * @param zeroCross        Zero-crossing detection parameter ("on", "off")
     * @param sampleTime       Sample time parameter
     * @param outputDataType   Output data type parameter
     */
    public TriggerDto(String blockName, String blockPath,
                     TypedParameter triggerType,
                     TypedParameter showOutputPort,
                     TypedParameter zeroCross,
                     TypedParameter sampleTime,
                     TypedParameter outputDataType) {
        super(blockName, blockPath);
        this.triggerType = triggerType;
        this.showOutputPort = showOutputPort;
        this.zeroCross = zeroCross;
        this.sampleTime = sampleTime;
        this.outputDataType = outputDataType;
    }

    /**
     * Constructs TriggerDto with typed parameter map.
     *
     * @param blockName  Name of the trigger block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public TriggerDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.triggerType = parameters.getTypedParameter("TriggerType", String.class, "rising");
        this.showOutputPort = parameters.getTypedParameter("ShowOutputPort", String.class, "off");
        this.zeroCross = parameters.getTypedParameter("ZeroCross", String.class, "on");
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
        this.outputDataType = parameters.getTypedParameter("OutputDataType", String.class, "auto");
    }

    /**
     * Creates TriggerDto with specified block metadata and trigger type.
     *
     * @param blockName   Block instance name
     * @param blockPath   Hierarchical path in model
     * @param position    Block position in diagram
     * @param dimension   Block visual dimensions
     * @param triggerType Type of trigger detection
     */
    public TriggerDto(String blockName, String blockPath, BlockPositionDto position,
                     BlockDimensionDto dimension, String triggerType) {
        super(blockName, blockPath, position, dimension);
        this.triggerType = TypedParameter.of(triggerType);
        this.showOutputPort = TypedParameter.of("off");
        this.zeroCross = TypedParameter.of("on");
        this.sampleTime = TypedParameter.of(-1.0);
        this.outputDataType = TypedParameter.of("auto");
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate trigger type
        if (triggerType == null || triggerType.getAsString() == null) {
            addValidationError("Trigger type cannot be null");
            return false;
        }

        String triggerTypeValue = getTriggerTypeValue();
        if (!isValidTriggerType(triggerTypeValue)) {
            addValidationError("Trigger type must be 'rising', 'falling', 'either', or 'function-call'");
            return false;
        }

        // Validate showOutputPort
        if (showOutputPort == null || showOutputPort.getAsString() == null) {
            addValidationError("ShowOutputPort cannot be null");
            return false;
        }

        String showOutputValue = getShowOutputPortValue();
        if (!showOutputValue.equals("on") && !showOutputValue.equals("off")) {
            addValidationError("ShowOutputPort must be 'on' or 'off'");
            return false;
        }

        // Validate zeroCross
        if (zeroCross == null || zeroCross.getAsString() == null) {
            addValidationError("ZeroCross cannot be null");
            return false;
        }

        String zeroCrossValue = getZeroCrossValue();
        if (!zeroCrossValue.equals("on") && !zeroCrossValue.equals("off")) {
            addValidationError("ZeroCross must be 'on' or 'off'");
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
        if (outputDataType == null || outputDataType.getAsString() == null ||
            outputDataType.getAsString().trim().isEmpty()) {
            addValidationError("Output data type cannot be null or empty");
            return false;
        }

        return true;
    }

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();

        // Validate trigger type
        if (triggerType != null && triggerType.getAsString() != null) {
            String typeValue = getTriggerTypeValue();
            if (!isValidTriggerType(typeValue)) {
                errors.add("Invalid trigger type: " + typeValue);
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
     * Gets the trigger type value.
     *
     * @return Trigger type string ("rising", "falling", "either", "function-call")
     */
    public String getTriggerTypeValue() {
        if (triggerType != null && triggerType.getAsString() != null) {
            return triggerType.getAsString();
        }
        return "rising"; // Default trigger type
    }

    /**
     * Gets the show output port flag value.
     *
     * @return Show output port flag ("on" or "off")
     */
    public String getShowOutputPortValue() {
        if (showOutputPort != null && showOutputPort.getAsString() != null) {
            return showOutputPort.getAsString();
        }
        return "off"; // Default no output port
    }

    /**
     * Gets the zero-crossing detection flag value.
     *
     * @return Zero-crossing detection flag ("on" or "off")
     */
    public String getZeroCrossValue() {
        if (zeroCross != null && zeroCross.getAsString() != null) {
            return zeroCross.getAsString();
        }
        return "on"; // Default zero-crossing enabled
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time for discrete operation
     */
    public double getSampleTimeValue() {
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            return sampleTime.getAsDouble();
        }
        return -1.0; // Default inherited
    }

    /**
     * Gets the output data type string.
     *
     * @return Output data type specification
     */
    public String getOutputDataTypeValue() {
        if (outputDataType != null && outputDataType.getAsString() != null) {
            return outputDataType.getAsString();
        }
        return "auto";
    }

    // === Helper Methods ===

    /**
     * Checks if the trigger type is valid.
     *
     * @param type Trigger type string to validate
     * @return true if trigger type is valid
     */
    private boolean isValidTriggerType(String type) {
        return "rising".equals(type) || "falling".equals(type) ||
               "either".equals(type) || "function-call".equals(type);
    }

    /**
     * Checks if the trigger is configured for continuous time operation.
     *
     * @return true if sample time is 0 (continuous)
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if the trigger inherits its sample time.
     *
     * @return true if sample time is -1 (inherited)
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Checks if the trigger is configured for discrete time operation.
     *
     * @return true if sample time is positive (discrete)
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }

    /**
     * Checks if output port is enabled.
     *
     * @return true if ShowOutputPort is "on"
     */
    public boolean hasOutputPort() {
        return "on".equals(getShowOutputPortValue());
    }

    /**
     * Checks if zero-crossing detection is enabled.
     *
     * @return true if ZeroCross is "on"
     */
    public boolean isZeroCrossingEnabled() {
        return "on".equals(getZeroCrossValue());
    }

    /**
     * Checks if trigger detects rising edges.
     *
     * @return true if trigger type is "rising" or "either"
     */
    public boolean detectsRisingEdge() {
        String type = getTriggerTypeValue();
        return "rising".equals(type) || "either".equals(type);
    }

    /**
     * Checks if trigger detects falling edges.
     *
     * @return true if trigger type is "falling" or "either"
     */
    public boolean detectsFallingEdge() {
        String type = getTriggerTypeValue();
        return "falling".equals(type) || "either".equals(type);
    }

    /**
     * Checks if trigger is function-call based.
     *
     * @return true if trigger type is "function-call"
     */
    public boolean isFunctionCall() {
        return "function-call".equals(getTriggerTypeValue());
    }

    /**
     * Updates the trigger type.
     *
     * @param type New trigger type ("rising", "falling", "either", "function-call")
     */
    public void setTriggerTypeValue(String type) {
        if (isValidTriggerType(type)) {
            this.triggerType = TypedParameter.of(type);
        }
    }

    // === Factory Methods ===

    @Override
    public TriggerDto copy() {
        TriggerDto copy = new TriggerDto();

        // Copy base fields
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());

        // Copy DTO-specific fields
        copy.triggerType = triggerType != null ? triggerType.copy() : null;
        copy.showOutputPort = showOutputPort != null ? showOutputPort.copy() : null;
        copy.zeroCross = zeroCross != null ? zeroCross.copy() : null;
        copy.outputDataType = outputDataType != null ? outputDataType.copy() : null;

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
                .put("TriggerType", triggerType)
                .put("ShowOutputPort", showOutputPort)
                .put("ZeroCross", zeroCross)
                .put("SampleTime", sampleTime)
                .put("OutputDataType", outputDataType)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "TriggerType", "Type of trigger detection (rising, falling, either, function-call)",
            "ShowOutputPort", "Whether to create output port for trigger signal passthrough (on/off)",
            "ZeroCross", "Enable zero-crossing detection for precise edge detection (on/off)",
            "SampleTime", "Sample time for discrete operation (-1 for inherited, 0 for continuous)",
            "OutputDataType", "Output data type specification (auto or specific type)"
        );
    }

    @Override
    public String toString() {
        return String.format("TriggerDto{blockName='%s', triggerType='%s', showOutputPort='%s', zeroCross='%s', sampleTime=%.3f}",
                           getBlockName(), getTriggerTypeValue(), getShowOutputPortValue(),
                           getZeroCrossValue(), getSampleTimeValue());
    }
}
