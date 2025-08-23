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
 * DTO for Outport (Out) block - Subsystem output port interface.
 * 
 * <p>This block represents an output port within a subsystem, providing
 * an interface for data to flow from internal blocks to the parent level.
 * Outport blocks define the output boundary of a subsystem and establish
 * the connection points for internal signals to external recipients.</p>
 * 
 * <p><b>Parameters:</b></p>
 * <ul>
 *   <li><b>Port</b>: Port number for subsystem output ordering (1, 2, 3, ...)</li>
 *   <li><b>PortDimensions</b>: Dimension specification (-1 for inherited)</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 *   <li><b>OutputDataTypeStr</b>: Output data type specification</li>
 * </ul>
 * 
 * <p><b>Subsystem Interface:</b></p>
 * <ul>
 *   <li>Receives data from internal blocks within the subsystem</li>
 *   <li>Provides data to external connections from the subsystem</li>
 *   <li>Establishes the output interface of the subsystem boundary</li>
 *   <li>Supports dimension inheritance and propagation</li>
 * </ul>
 * 
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>Port number must be positive integer</li>
 *   <li>Port number must be unique within the subsystem</li>
 *   <li>SampleTime must be >= -1.0 and finite</li>
 *   <li>Must be contained within a subsystem block</li>
 *   <li>Must have exactly one input connection from internal blocks</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-22
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Outport")
public class OutportDto extends BlockDto {
    
    // Constructor to set blockType for Jackson deserialization

    /**
     * Port number for subsystem output ordering.
     * Determines the order and position of this output in the subsystem interface.
     */
    private TypedParameter port = TypedParameter.of(1);

    /**
     * Port dimensions specification.
     * Controls the dimensional properties of the output signal (-1 for inherited).
     */
    private TypedParameter portDimensions = TypedParameter.of("-1");

    /**
     * Output data type specification.
     * Controls the data type of the signal passed from the subsystem.
     */
    private TypedParameter outputDataTypeStr = TypedParameter.of("Inherit: auto");

    /**
     * Constructs OutportDto with individual parameters.
     *
     * @param blockName        Name of the outport block
     * @param blockPath        Path of the block in the model hierarchy
     * @param port             Port number parameter
     * @param portDimensions   Port dimensions parameter
     * @param sampleTime       Sample time parameter
     * @param outputDataTypeStr Output data type parameter
     */
    public OutportDto(String blockName, String blockPath,
                     TypedParameter port,
                     TypedParameter portDimensions,
                     TypedParameter sampleTime,
                     TypedParameter outputDataTypeStr) {
        super("Outport", blockName, blockPath);
        this.port = port;
        this.portDimensions = portDimensions;
        this.sampleTime = sampleTime;
        this.outputDataTypeStr = outputDataTypeStr;
    }

    /**
     * Constructs OutportDto with typed parameter map.
     *
     * @param blockName  Name of the outport block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public OutportDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super("Outport", blockName, blockPath);
        this.port = parameters.getTypedParameter("Port", Integer.class, 1);
        this.portDimensions = parameters.getTypedParameter("PortDimensions", String.class, "-1");
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
        this.outputDataTypeStr = parameters.getTypedParameter("OutputDataTypeStr", String.class, "Inherit: auto");
    }

    /**
     * Creates OutportDto with specified block metadata and port number.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     * @param portNumber Port number for this output
     */
    public OutportDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension, int portNumber) {
        super("Outport", blockName, blockPath, position, dimension);
        this.port = TypedParameter.of(portNumber);
        this.portDimensions = TypedParameter.of("-1");
        this.sampleTime = TypedParameter.of(-1.0);
        this.outputDataTypeStr = TypedParameter.of("Inherit: auto");
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate port number
        if (port == null || port.getAsInteger() == null) {
            addValidationError("Port number cannot be null");
            return false;
        }

        int portValue = getPortValue();
        if (portValue < 1) {
            addValidationError("Port number must be positive");
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

        // Validate port dimensions
        if (portDimensions == null || portDimensions.getAsString() == null) {
            addValidationError("Port dimensions cannot be null");
            return false;
        }

        // Validate output data type
        if (outputDataTypeStr == null || outputDataTypeStr.getAsString() == null || 
            outputDataTypeStr.getAsString().trim().isEmpty()) {
            addValidationError("Output data type cannot be null or empty");
            return false;
        }

        return true;
    }

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();
        
        // Validate port number
        if (port != null && port.getAsInteger() != null) {
            int portValue = getPortValue();
            if (portValue < 1) {
                errors.add("Port number must be positive");
            }
            if (portValue > 1000) { // Reasonable upper limit
                errors.add("Port number exceeds reasonable limit (1000)");
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
     * Gets the port number value.
     *
     * @return Port number for subsystem output ordering
     */
    public int getPortValue() {
        if (port != null && port.getAsInteger() != null) {
            return port.getAsInteger();
        }
        return 1; // Default port number
    }

    /**
     * Gets the port dimensions specification.
     *
     * @return Port dimensions string (-1 for inherited)
     */
    public String getPortDimensionsValue() {
        if (portDimensions != null && portDimensions.getAsString() != null) {
            return portDimensions.getAsString();
        }
        return "-1"; // Default inherited
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
    public String getOutputDataTypeStrValue() {
        if (outputDataTypeStr != null && outputDataTypeStr.getAsString() != null) {
            return outputDataTypeStr.getAsString();
        }
        return "Inherit: auto";
    }

    // === Helper Methods ===

    /**
     * Checks if the outport is configured for continuous time operation.
     *
     * @return true if sample time is 0 (continuous)
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if the outport inherits its sample time.
     *
     * @return true if sample time is -1 (inherited)
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Checks if the outport is configured for discrete time operation.
     *
     * @return true if sample time is positive (discrete)
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }

    /**
     * Checks if port dimensions are inherited.
     *
     * @return true if port dimensions are inherited from input
     */
    public boolean isDimensionsInherited() {
        return "-1".equals(getPortDimensionsValue());
    }

    /**
     * Checks if the data type is inherited.
     *
     * @return true if output data type is inherited
     */
    public boolean isDataTypeInherited() {
        String dataType = getOutputDataTypeStrValue();
        return dataType.contains("Inherit") || dataType.contains("auto");
    }

    /**
     * Updates the port number.
     *
     * @param portNumber New port number (must be positive)
     */
    public void setPortValue(int portNumber) {
        if (portNumber > 0) {
            this.port = TypedParameter.of(portNumber);
        }
    }

    // === Factory Methods ===
    
    @Override
    public OutportDto copy() {
        OutportDto copy = new OutportDto();
        
        // Copy base fields
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());
        
        // Copy DTO-specific fields
        copy.port = port != null ? port.copy() : null;
        copy.portDimensions = portDimensions != null ? portDimensions.copy() : null;
        copy.outputDataTypeStr = outputDataTypeStr != null ? outputDataTypeStr.copy() : null;
        
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
                .put("Port", port)
                .put("PortDimensions", portDimensions)
                .put("SampleTime", sampleTime)
                .put("OutputDataTypeStr", outputDataTypeStr)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "Port", "Port number for subsystem output ordering (1, 2, 3, ...)",
            "PortDimensions", "Dimension specification (-1 for inherited)",
            "SampleTime", "Sample time for discrete operation (-1 for inherited, 0 for continuous)",
            "OutputDataTypeStr", "Output data type specification"
        );
    }

    @Override
    public String toString() {
        return String.format("OutportDto{blockName='%s', port=%d, dimensions='%s', dataType='%s', sampleTime=%.3f}",
                           getBlockName(), getPortValue(), getPortDimensionsValue(), 
                           getOutputDataTypeStrValue(), getSampleTimeValue());
    }
}