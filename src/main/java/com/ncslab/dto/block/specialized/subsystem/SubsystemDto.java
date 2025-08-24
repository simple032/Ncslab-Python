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
import java.util.ArrayList;

/**
 * DTO for Subsystem block - Hierarchical block grouping container.
 * 
 * <p>This block represents a subsystem that contains other blocks and lines,
 * providing hierarchical modeling capabilities. Subsystems can contain:</p>
 * <ul>
 *   <li><b>Internal Blocks</b>: Any block types within the subsystem boundary</li>
 *   <li><b>Internal Lines</b>: Connections between blocks within the subsystem</li>
 *   <li><b>Inport Blocks</b>: Input interfaces to the subsystem</li>
 *   <li><b>Outport Blocks</b>: Output interfaces from the subsystem</li>
 * </ul>
 * 
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>Name</b>: Subsystem name for identification</li>
 *   <li><b>Description</b>: Optional description of subsystem functionality</li>
 *   <li><b>ShowPortLabels</b>: Display port labels on subsystem block</li>
 *   <li><b>SampleTime</b>: Sample time inheritance mode (-1 for inherited)</li>
 * </ul>
 * 
 * <p><b>Hierarchical Features:</b></p>
 * <ul>
 *   <li>Encapsulates complex functionality as reusable components</li>
 *   <li>Supports nested subsystems for multi-level hierarchy</li>
 *   <li>Maintains boundary interface through Inport/Outport blocks</li>
 *   <li>Enables modular design and organization of large models</li>
 * </ul>
 * 
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Must contain at least one block or be marked as placeholder</li>
 *   <li>Inport/Outport blocks must have valid port numbers</li>
 *   <li>Internal connections must be valid and complete</li>
 *   <li>No circular dependencies in hierarchical structure</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-22
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Subsystem")
public class SubsystemDto extends BlockDto {
    
    // Constructor to set blockType for Jackson deserialization

    /**
     * Subsystem description parameter.
     * Optional textual description of the subsystem's purpose and functionality.
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
     * Number of input ports.
     * Dynamically determined by contained Inport blocks.
     */
    private TypedParameter numInputPorts = TypedParameter.of(0);

    /**
     * Number of output ports.
     * Dynamically determined by contained Outport blocks.
     */
    private TypedParameter numOutputPorts = TypedParameter.of(0);

    /**
     * Constructs SubsystemDto with individual parameters.
     *
     * @param blockName     Name of the subsystem
     * @param blockPath     Path of the subsystem in the model hierarchy
     * @param subsystemDescription   Optional description
     * @param showPortLabels Flag to show port labels
     * @param readOnly      Flag for read-only mode
     * @param sampleTime    Sample time parameter
     */
    public SubsystemDto(String blockName, String blockPath,
                       TypedParameter subsystemDescription,
                       TypedParameter showPortLabels,
                       TypedParameter readOnly,
                       TypedParameter sampleTime) {
        super(blockName,blockPath);
        this.subsystemDescription = subsystemDescription;
        this.showPortLabels = showPortLabels;
        this.readOnly = readOnly;
        this.sampleTime = sampleTime;
        this.numInputPorts = TypedParameter.of(0);
        this.numOutputPorts = TypedParameter.of(0);
    }

    /**
     * Constructs SubsystemDto with typed parameter map.
     *
     * @param blockName  Name of the subsystem
     * @param blockPath  Path of the subsystem in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public SubsystemDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super(blockName,blockPath);
        this.subsystemDescription = parameters.getTypedParameter("Description", String.class, "");
        this.showPortLabels = parameters.getTypedParameter("ShowPortLabels", Boolean.class, true);
        this.readOnly = parameters.getTypedParameter("ReadOnly", Boolean.class, false);
        this.maskType = parameters.getTypedParameter("MaskType", String.class, "");
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
        this.numInputPorts = parameters.getTypedParameter("NumInputPorts", Integer.class, 0);
        this.numOutputPorts = parameters.getTypedParameter("NumOutputPorts", Integer.class, 0);
    }

    /**
     * Creates SubsystemDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     */
    public SubsystemDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName,blockPath, position, dimension);
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
            if (stValue < -1.0 || Double.isNaN(stValue) || Double.isInfinite(stValue)) {
                errors.add("Sample time must be >= -1.0 and finite");
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
     * Gets the number of input ports.
     *
     * @return Number of input ports
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
     * Checks if the subsystem is configured for continuous time operation.
     *
     * @return true if sample time is 0 (continuous)
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if the subsystem inherits its sample time.
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
     * Checks if the subsystem has any inputs.
     *
     * @return true if there are input ports
     */
    public boolean hasInputs() {
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
     *
     * @return true if subsystem is a sink
     */
    public boolean isSink() {
        return hasInputs() && !hasOutputs();
    }

    /**
     * Checks if the subsystem is a source (has outputs but no inputs).
     *
     * @return true if subsystem is a source
     */
    public boolean isSource() {
        return !hasInputs() && hasOutputs();
    }

    /**
     * Updates the number of input ports.
     *
     * @param count New input port count
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
    public SubsystemDto copy() {
        SubsystemDto copy = new SubsystemDto();
        
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
            "Description", "Optional description of subsystem functionality",
            "ShowPortLabels", "Display port labels on subsystem block",
            "ReadOnly", "Subsystem contents cannot be modified when true",
            "MaskType", "Optional mask type for custom appearance",
            "SampleTime", "Sample time for subsystem (-1 for inherited, 0 for continuous)",
            "NumInputPorts", "Number of input ports (determined by Inport blocks)",
            "NumOutputPorts", "Number of output ports (determined by Outport blocks)"
        );
    }

    @Override
    public String toString() {
        return String.format("SubsystemDto{blockName='%s', inputs=%d, outputs=%d, description='%s', readOnly=%s, sampleTime=%.3f}",
                           getBlockName(), getNumInputPortsValue(), getNumOutputPortsValue(), 
                           getSubsystemDescriptionValue(), getReadOnlyValue(), getSampleTimeValue());
    }
}