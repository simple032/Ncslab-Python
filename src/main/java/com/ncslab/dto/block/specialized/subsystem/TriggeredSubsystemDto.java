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
 * DTO for Triggered Subsystem block - Event-driven subsystem with automatic trigger port.
 *
 * <p>This block represents a pre-configured subsystem with an integrated Trigger port
 * that controls execution based on signal transitions. The Triggered Subsystem combines
 * standard subsystem functionality with automatic trigger event detection.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li><b>Automatic Trigger Block</b>: Internal Trigger block automatically created and configured</li>
 *   <li><b>Event-Driven Execution</b>: Subsystem executes only when trigger event occurs</li>
 *   <li><b>Edge Detection</b>: Rising, falling, either edge, or function-call triggering</li>
 *   <li><b>Zero-Crossing</b>: Optional precise edge detection using zero-crossing algorithm</li>
 * </ul>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>TriggerType</b>: Type of trigger detection ("rising", "falling", "either", "function-call")</li>
 *   <li><b>ShowOutputPort</b>: Create optional output port from trigger block ("on", "off")</li>
 *   <li><b>ZeroCross</b>: Enable zero-crossing detection for precise edges ("on", "off")</li>
 *   <li><b>InitialCondition</b>: Initial output value before first trigger (default: 0.0)</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 trigger input port (on top edge of subsystem boundary)</li>
 *   <li>N data input ports (from Inport blocks)</li>
 *   <li>M data output ports (from Outport blocks)</li>
 *   <li>Optional trigger output port (if ShowOutputPort="on")</li>
 * </ul>
 *
 * <p><b>Trigger Types:</b></p>
 * <ul>
 *   <li><b>rising</b>: Execute on rising edge (0 to positive transition)</li>
 *   <li><b>falling</b>: Execute on falling edge (positive to 0 transition)</li>
 *   <li><b>either</b>: Execute on any edge (rising or falling)</li>
 *   <li><b>function-call</b>: Execute via explicit function call mechanism</li>
 * </ul>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>TriggerType must be one of: "rising", "falling", "either", "function-call"</li>
 *   <li>ShowOutputPort must be "on" or "off"</li>
 *   <li>ZeroCross must be "on" or "off"</li>
 *   <li>InitialCondition must be finite (not NaN or Infinity)</li>
 *   <li>Must contain at least one block (besides auto-created Trigger block)</li>
 * </ul>
 *
 * <p><b>Internal Structure:</b></p>
 * <ul>
 *   <li>Automatic Trigger block creation on subsystem initialization</li>
 *   <li>Trigger block parameters match subsystem trigger parameters</li>
 *   <li>Trigger port exposed on subsystem boundary (typically top edge)</li>
 *   <li>Regular In/Out blocks for data flow (inherited from Subsystem)</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-03
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("TriggeredSubsystem")
public class TriggeredSubsystemDto extends SubsystemDto {

    /**
     * Trigger type for edge detection.
     * Controls which type of signal transition triggers subsystem execution.
     */
    private TypedParameter triggerType = TypedParameter.of("rising");

    /**
     * Whether to show output port for trigger signal passthrough.
     * When enabled, the trigger signal is available as an output from subsystem.
     */
    private TypedParameter showOutputPort = TypedParameter.of("off");

    /**
     * Zero-crossing detection enable flag.
     * When enabled, provides more precise edge detection by detecting zero crossings.
     */
    private TypedParameter zeroCross = TypedParameter.of("on");

    /**
     * Initial condition for subsystem outputs before first trigger.
     * Specifies the output value before the first trigger event occurs.
     */
    private TypedParameter initialCondition = TypedParameter.of(0.0);

    /**
     * Constructs TriggeredSubsystemDto with individual parameters.
     *
     * @param blockName        Name of the triggered subsystem
     * @param blockPath        Path of the block in the model hierarchy
     * @param triggerType      Trigger type parameter ("rising", "falling", "either", "function-call")
     * @param showOutputPort   Show output port flag parameter ("on", "off")
     * @param zeroCross        Zero-crossing detection parameter ("on", "off")
     * @param initialCondition Initial output value parameter
     * @param sampleTime       Sample time parameter
     */
    public TriggeredSubsystemDto(String blockName, String blockPath,
                                TypedParameter triggerType,
                                TypedParameter showOutputPort,
                                TypedParameter zeroCross,
                                TypedParameter initialCondition,
                                TypedParameter sampleTime) {
        super(blockName, blockPath);
        this.triggerType = triggerType;
        this.showOutputPort = showOutputPort;
        this.zeroCross = zeroCross;
        this.initialCondition = initialCondition;
        this.sampleTime = sampleTime;
    }

    /**
     * Constructs TriggeredSubsystemDto with typed parameter map.
     *
     * @param blockName  Name of the triggered subsystem
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public TriggeredSubsystemDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath, parameters);
        this.triggerType = parameters.getTypedParameter("TriggerType", String.class, "rising");
        this.showOutputPort = parameters.getTypedParameter("ShowOutputPort", String.class, "off");
        this.zeroCross = parameters.getTypedParameter("ZeroCross", String.class, "on");
        this.initialCondition = parameters.getTypedParameter("InitialCondition", Double.class, 0.0);
    }

    /**
     * Creates TriggeredSubsystemDto with specified block metadata and trigger parameters.
     *
     * @param blockName   Block instance name
     * @param blockPath   Hierarchical path in model
     * @param position    Block position in diagram
     * @param dimension   Block visual dimensions
     * @param triggerType Type of trigger detection
     */
    public TriggeredSubsystemDto(String blockName, String blockPath, BlockPositionDto position,
                                 BlockDimensionDto dimension, String triggerType) {
        super(blockName, blockPath, position, dimension);
        this.triggerType = TypedParameter.of(triggerType);
        this.showOutputPort = TypedParameter.of("off");
        this.zeroCross = TypedParameter.of("on");
        this.initialCondition = TypedParameter.of(0.0);
        this.sampleTime = TypedParameter.of(-1.0);
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

        // Validate initial condition
        if (initialCondition == null || initialCondition.getAsDouble() == null) {
            addValidationError("Initial condition cannot be null");
            return false;
        }

        double initialConditionValue = getInitialConditionValue();
        if (Double.isNaN(initialConditionValue) || Double.isInfinite(initialConditionValue)) {
            addValidationError("Initial condition must be finite (not NaN or Infinity)");
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

        // Validate initial condition
        if (initialCondition != null && initialCondition.getAsDouble() != null) {
            double icValue = getInitialConditionValue();
            if (Double.isNaN(icValue) || Double.isInfinite(icValue)) {
                errors.add("Initial condition must be finite");
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
     * Gets the initial condition value.
     *
     * @return Initial output value before first trigger
     */
    public double getInitialConditionValue() {
        if (initialCondition != null && initialCondition.getAsDouble() != null) {
            return initialCondition.getAsDouble();
        }
        return 0.0; // Default initial condition
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
    public TriggeredSubsystemDto copy() {
        TriggeredSubsystemDto copy = new TriggeredSubsystemDto();

        // Copy base fields from SubsystemDto
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());
        copy.setSubsystemDescription(getSubsystemDescription());
        copy.setShowPortLabels(getShowPortLabels());
        copy.setReadOnly(getReadOnly());
        copy.setMaskType(getMaskType());
        copy.setNumInputPorts(getNumInputPorts());
        copy.setNumOutputPorts(getNumOutputPorts());

        // Copy DTO-specific fields
        copy.triggerType = triggerType != null ? triggerType.copy() : null;
        copy.showOutputPort = showOutputPort != null ? showOutputPort.copy() : null;
        copy.zeroCross = zeroCross != null ? zeroCross.copy() : null;
        copy.initialCondition = initialCondition != null ? initialCondition.copy() : null;

        return copy;
    }

    /**
     * Creates a TypedParameterMap from this DTO's parameters.
     *
     * @return TypedParameterMap containing all block parameters
     */
    @Override
    public TypedParameterMap toParameterMap() {
        TypedParameterMap baseParams = super.toParameterMap();

        return TypedParameterMap.builder()
                .putAll(baseParams)
                .put("TriggerType", triggerType)
                .put("ShowOutputPort", showOutputPort)
                .put("ZeroCross", zeroCross)
                .put("InitialCondition", initialCondition)
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
            "InitialCondition", "Initial output value before first trigger event",
            "SampleTime", "Sample time for subsystem (-1 for inherited, 0 for continuous)"
        );
    }

    @Override
    public String toString() {
        return String.format("TriggeredSubsystemDto{blockName='%s', triggerType='%s', showOutputPort='%s', " +
                           "zeroCross='%s', initialCondition=%.3f, sampleTime=%.3f}",
                           getBlockName(), getTriggerTypeValue(), getShowOutputPortValue(),
                           getZeroCrossValue(), getInitialConditionValue(), getSampleTimeValue());
    }
}
