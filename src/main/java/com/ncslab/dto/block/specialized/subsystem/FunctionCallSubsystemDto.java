package com.ncslab.dto.block.specialized.subsystem;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.greenpineyu.fel.function.operator.Sub;
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
 * DTO for Function-Call Subsystem block - Event-driven subsystem execution.
 *
 * <p>This block represents a specialized subsystem that executes only when
 * triggered by a function-call event from a connected block. Unlike regular
 * subsystems that execute continuously, function-call subsystems are
 * event-driven and execute atomically when triggered.</p>
 *
 * <p><b>Key Characteristics:</b></p>
 * <ul>
 *   <li><b>Event-Driven</b>: Executes only when function-call signal is active</li>
 *   <li><b>Atomic Execution</b>: Completes execution within single time step</li>
 *   <li><b>Function-Call Port</b>: Special input port for function-call events</li>
 *   <li><b>No Continuous States</b>: Cannot contain continuous-time blocks</li>
 * </ul>
 *
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li>Inherits all parameters from Subsystem base class</li>
 *   <li>No additional specialized parameters (function-call behavior is implicit)</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 special function-call input port (not a regular data port)</li>
 *   <li>Additional data input/output ports via In/Out blocks</li>
 *   <li>Function-call port triggers subsystem execution</li>
 * </ul>
 *
 * <p><b>Execution Model:</b></p>
 * <ul>
 *   <li>Subsystem remains inactive until function-call received</li>
 *   <li>Upon function-call: Execute all internal blocks once</li>
 *   <li>Complete execution atomically within caller's time step</li>
 *   <li>Reset function-call flag after execution</li>
 * </ul>
 *
 * <p><b>Common Use Cases:</b></p>
 * <ul>
 *   <li>Interrupt service routines (ISR) modeling</li>
 *   <li>Event-driven state machines</li>
 *   <li>Callback function implementations</li>
 *   <li>Scheduled task execution</li>
 *   <li>Function-call generator triggered operations</li>
 * </ul>
 *
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Must have valid function-call input connection</li>
 *   <li>Cannot contain continuous-time blocks (integrators, derivatives)</li>
 *   <li>Sample time must be inherited or discrete</li>
 *   <li>All internal blocks must support discrete execution</li>
 * </ul>
 *
 * <p><b>Simulink Compatibility:</b></p>
 * <p>This block is compatible with MATLAB/Simulink Function-Call Subsystem
 * block, supporting the same execution semantics and port configuration.</p>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("FunctionCallSubsystem")
public class FunctionCallSubsystemDto extends SubsystemDto {

    /**
     * Subsystem description parameter.
     * Optional textual description of the function-call subsystem's purpose.
     */
    private TypedParameter subsystemDescription = TypedParameter.of("");

    /**
     * Show port labels flag.
     * Controls whether port labels are displayed on the subsystem block.
     */
    private TypedParameter showPortLabels = TypedParameter.of(true);

    /**
     * Read-only flag.
     * Indicates if the subsystem contents can be modified.
     */
    private TypedParameter readOnly = TypedParameter.of(false);

    /**
     * Mask type specification.
     * Optional mask type for custom subsystem appearance and behavior.
     */
    private TypedParameter maskType = TypedParameter.of("");

    /**
     * Number of data input ports (excluding function-call port).
     * Dynamically determined by contained Inport blocks.
     */
    private TypedParameter numInputPorts = TypedParameter.of(0);

    /**
     * Number of output ports.
     * Dynamically determined by contained Outport blocks.
     */
    private TypedParameter numOutputPorts = TypedParameter.of(0);

    /**
     * Constructs FunctionCallSubsystemDto with individual parameters.
     *
     * @param blockName              Name of the function-call subsystem
     * @param blockPath              Path of the subsystem in the model hierarchy
     * @param subsystemDescription   Optional description
     * @param showPortLabels         Flag to show port labels
     * @param readOnly               Flag for read-only mode
     * @param sampleTime             Sample time parameter
     */
    public FunctionCallSubsystemDto(String blockName, String blockPath,
                                   TypedParameter subsystemDescription,
                                   TypedParameter showPortLabels,
                                   TypedParameter readOnly,
                                   TypedParameter sampleTime) {
        super(blockName, blockPath);
        this.subsystemDescription = subsystemDescription;
        this.showPortLabels = showPortLabels;
        this.readOnly = readOnly;
        this.sampleTime = sampleTime;
        this.numInputPorts = TypedParameter.of(0);
        this.numOutputPorts = TypedParameter.of(0);
    }

    /**
     * Constructs FunctionCallSubsystemDto with typed parameter map.
     *
     * @param blockName  Name of the function-call subsystem
     * @param blockPath  Path of the subsystem in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public FunctionCallSubsystemDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName, blockPath);
        this.subsystemDescription = parameters.getTypedParameter("Description", String.class, "");
        this.showPortLabels = parameters.getTypedParameter("ShowPortLabels", Boolean.class, true);
        this.readOnly = parameters.getTypedParameter("ReadOnly", Boolean.class, false);
        this.maskType = parameters.getTypedParameter("MaskType", String.class, "");
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
        this.numInputPorts = parameters.getTypedParameter("NumInputPorts", Integer.class, 0);
        this.numOutputPorts = parameters.getTypedParameter("NumOutputPorts", Integer.class, 0);
    }

    /**
     * Creates FunctionCallSubsystemDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position  Block position in diagram
     * @param dimension Block visual dimensions
     */
    public FunctionCallSubsystemDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
        this.subsystemDescription = TypedParameter.of("");
        this.showPortLabels = TypedParameter.of(true);
        this.readOnly = TypedParameter.of(false);
        this.maskType = TypedParameter.of("");
        this.sampleTime = TypedParameter.of(-1.0);
        this.numInputPorts = TypedParameter.of(0);
        this.numOutputPorts = TypedParameter.of(0);
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate sample time - function-call subsystems should not be continuous
        if (sampleTime == null || sampleTime.getAsDouble() == null) {
            addValidationError("Sample time cannot be null");
            return false;
        }

        double sampleTimeValue = getSampleTimeValue();
        if (Double.isNaN(sampleTimeValue) || Double.isInfinite(sampleTimeValue)) {
            addValidationError("Sample time must be finite");
            return false;
        }

        // Function-call subsystems should be inherited (-1) or discrete (>0)
        if (sampleTimeValue == 0.0) {
            addValidationError("Function-call subsystems cannot be continuous (sample time = 0)");
            return false;
        }

        if (sampleTimeValue < -1.0) {
            addValidationError("Sample time must be >= -1.0");
            return false;
        }

        // Validate port counts
        if (numInputPorts != null && numInputPorts.getAsInteger() != null) {
            int inputCount = getNumInputPortsValue();
            if (inputCount < 0) {
                addValidationError("Number of input ports cannot be negative");
                return false;
            }
        }

        if (numOutputPorts != null && numOutputPorts.getAsInteger() != null) {
            int outputCount = getNumOutputPortsValue();
            if (outputCount < 0) {
                addValidationError("Number of output ports cannot be negative");
                return false;
            }
        }

        // Validate boolean flags
        if (showPortLabels != null && showPortLabels.getAsBoolean() == null) {
            addValidationError("ShowPortLabels must be a valid boolean value");
            return false;
        }

        if (readOnly != null && readOnly.getAsBoolean() == null) {
            addValidationError("ReadOnly must be a valid boolean value");
            return false;
        }

        return true;
    }

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();

        // Validate sample time
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            double stValue = getSampleTimeValue();
            if (Double.isNaN(stValue) || Double.isInfinite(stValue)) {
                errors.add("Sample time must be finite");
            }
            if (stValue == 0.0) {
                errors.add("Function-call subsystems cannot be continuous (sample time = 0)");
            }
            if (stValue < -1.0) {
                errors.add("Sample time must be >= -1.0");
            }
        }

        // Validate port counts
        if (numInputPorts != null && numInputPorts.getAsInteger() != null) {
            int inputCount = getNumInputPortsValue();
            if (inputCount < 0) {
                errors.add("Number of input ports cannot be negative");
            }
            if (inputCount > 100) { // Reasonable limit
                errors.add("Number of input ports exceeds reasonable limit (100)");
            }
        }

        if (numOutputPorts != null && numOutputPorts.getAsInteger() != null) {
            int outputCount = getNumOutputPortsValue();
            if (outputCount < 0) {
                errors.add("Number of output ports cannot be negative");
            }
            if (outputCount > 100) { // Reasonable limit
                errors.add("Number of output ports exceeds reasonable limit (100)");
            }
        }

        return errors;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the subsystem description value.
     *
     * @return Subsystem description
     */
    public String getSubsystemDescriptionValue() {
        if (subsystemDescription != null && subsystemDescription.getAsString() != null) {
            return subsystemDescription.getAsString();
        }
        return "";
    }

    /**
     * Gets the show port labels setting.
     *
     * @return true if port labels should be shown
     */
    public boolean getShowPortLabelsValue() {
        if (showPortLabels != null && showPortLabels.getAsBoolean() != null) {
            return showPortLabels.getAsBoolean();
        }
        return true; // Default show labels
    }

    /**
     * Gets the read-only setting.
     *
     * @return true if subsystem is read-only
     */
    public boolean getReadOnlyValue() {
        if (readOnly != null && readOnly.getAsBoolean() != null) {
            return readOnly.getAsBoolean();
        }
        return false; // Default editable
    }

    /**
     * Gets the mask type value.
     *
     * @return Mask type specification
     */
    public String getMaskTypeValue() {
        if (maskType != null && maskType.getAsString() != null) {
            return maskType.getAsString();
        }
        return "";
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time for subsystem
     */
    public double getSampleTimeValue() {
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            return sampleTime.getAsDouble();
        }
        return -1.0; // Default inherited
    }

    /**
     * Gets the number of data input ports (excluding function-call port).
     *
     * @return Number of data input ports
     */
    public int getNumInputPortsValue() {
        if (numInputPorts != null && numInputPorts.getAsInteger() != null) {
            return numInputPorts.getAsInteger();
        }
        return 0;
    }

    /**
     * Gets the number of output ports.
     *
     * @return Number of output ports
     */
    public int getNumOutputPortsValue() {
        if (numOutputPorts != null && numOutputPorts.getAsInteger() != null) {
            return numOutputPorts.getAsInteger();
        }
        return 0;
    }

    // === Helper Methods ===

    /**
     * Checks if the subsystem inherits its sample time.
     * This is the typical configuration for function-call subsystems.
     *
     * @return true if sample time is -1 (inherited)
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Checks if the subsystem is configured for discrete time operation.
     *
     * @return true if sample time is positive (discrete)
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }

    /**
     * Checks if the subsystem has any data inputs (excluding function-call port).
     *
     * @return true if there are data input ports
     */
    public boolean hasDataInputs() {
        return getNumInputPortsValue() > 0;
    }

    /**
     * Checks if the subsystem has any outputs.
     *
     * @return true if there are output ports
     */
    public boolean hasOutputs() {
        return getNumOutputPortsValue() > 0;
    }

    /**
     * Checks if the subsystem is a sink (has inputs but no outputs).
     * For function-call subsystems, this means it only consumes function calls and data.
     *
     * @return true if subsystem is a sink
     */
    public boolean isSink() {
        return !hasOutputs();
    }

    /**
     * Checks if the subsystem is a source (has outputs but no data inputs).
     * For function-call subsystems, this means it only produces outputs when called.
     *
     * @return true if subsystem is a source
     */
    public boolean isSource() {
        return !hasDataInputs() && hasOutputs();
    }

    /**
     * Checks if the subsystem is purely event-driven (no data inputs or outputs).
     * This is useful for modeling ISRs or pure event handlers.
     *
     * @return true if subsystem has no data ports
     */
    public boolean isPureEventDriven() {
        return !hasDataInputs() && !hasOutputs();
    }

    /**
     * Updates the number of data input ports.
     *
     * @param count New data input port count
     */
    public void setNumInputPortsValue(int count) {
        this.numInputPorts = TypedParameter.of(Math.max(0, count));
    }

    /**
     * Updates the number of output ports.
     *
     * @param count New output port count
     */
    public void setNumOutputPortsValue(int count) {
        this.numOutputPorts = TypedParameter.of(Math.max(0, count));
    }

    // === Factory Methods ===

    @Override
    public FunctionCallSubsystemDto copy() {
        FunctionCallSubsystemDto copy = new FunctionCallSubsystemDto();

        // Copy base fields
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());

        // Copy DTO-specific fields
        copy.subsystemDescription = subsystemDescription != null ? subsystemDescription.copy() : null;
        copy.showPortLabels = showPortLabels != null ? showPortLabels.copy() : null;
        copy.readOnly = readOnly != null ? readOnly.copy() : null;
        copy.maskType = maskType != null ? maskType.copy() : null;
        copy.numInputPorts = numInputPorts != null ? numInputPorts.copy() : null;
        copy.numOutputPorts = numOutputPorts != null ? numOutputPorts.copy() : null;

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
                .put("Description", subsystemDescription)
                .put("ShowPortLabels", showPortLabels)
                .put("ReadOnly", readOnly)
                .put("MaskType", maskType)
                .put("SampleTime", sampleTime)
                .put("NumInputPorts", numInputPorts)
                .put("NumOutputPorts", numOutputPorts)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "Description", "Optional description of function-call subsystem functionality",
            "ShowPortLabels", "Display port labels on subsystem block",
            "ReadOnly", "Subsystem contents cannot be modified when true",
            "MaskType", "Optional mask type for custom appearance",
            "SampleTime", "Sample time for subsystem (-1 for inherited, >0 for discrete, NOT 0 for function-call)",
            "NumInputPorts", "Number of data input ports (determined by Inport blocks, excludes function-call port)",
            "NumOutputPorts", "Number of output ports (determined by Outport blocks)"
        );
    }

    @Override
    public String toString() {
        return String.format("FunctionCallSubsystemDto{blockName='%s', dataInputs=%d, outputs=%d, description='%s', readOnly=%s, sampleTime=%.3f}",
                           getBlockName(), getNumInputPortsValue(), getNumOutputPortsValue(),
                           getSubsystemDescriptionValue(), getReadOnlyValue(), getSampleTimeValue());
    }
}
