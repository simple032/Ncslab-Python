package com.ncslab.dto.block.specialized.sink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

/**
 * Data Transfer Object for Display sink block.
 * Display blocks show signal values at the end of simulation.
 *
 * @author NCSLab
 * @version 2.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Display")
public class DisplayDto extends BlockDto {
    
    // Constructor to set blockType for Jackson deserialization

    /**
     * Sample time for the display block.
     * Typically -1 for inherited timing from input signal
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);

    /**
     * Output data type specification.
     * Usually "Inherit: Same as input" for display blocks
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");

    /**
     * Constructs DisplayDto with individual parameters.
     *
     * @param blockName      Name of the block
     * @param blockPath      Path of the block in the model hierarchy
     * @param sampleTime     Sample time parameter
     * @param outDataTypeStr Output data type parameter
     */
    public DisplayDto(String blockName, String blockPath,
                      TypedParameter sampleTime,
                      TypedParameter outDataTypeStr) {
        super();
        this.blockName = blockName;
        this.blockPath = blockPath;
        this.sampleTime = sampleTime;
        this.outDataTypeStr = outDataTypeStr;
    }

    /**
     * Constructs DisplayDto with typed parameter map.
     *
     * @param blockName  Name of the block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public DisplayDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super();
        this.blockName = blockName;
        this.blockPath = blockPath;
        this.sampleTime = parameters.get("SampleTime");
        this.outDataTypeStr = parameters.get("OutDataTypeStr");
    }

    // === Validation Methods ===

    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();

        // Validate sample time
        if (sampleTime == null || sampleTime.getAsDouble() == null) {
            result.addError("sampleTime", "Sample time cannot be null");
        } else {
            Double sampleTimeValue = sampleTime.getAsDouble();
            if (sampleTimeValue < -1.0 || sampleTimeValue.isNaN() || sampleTimeValue.isInfinite()) {
                result.addError("sampleTime", "Sample time must be >= -1.0 and finite");
            }
        }

        // Validate output data type
        if (outDataTypeStr == null || outDataTypeStr.getAsString() == null) {
            result.addError("outDataTypeStr", "Output data type cannot be null");
        } else if (outDataTypeStr.getAsString().trim().isEmpty()) {
            result.addError("outDataTypeStr", "Output data type cannot be empty");
        }

        return result;
    }

    // === Helper Methods ===

    /**
     * Gets the sample time value with validation.
     *
     * @return Sample time value
     * @throws IllegalStateException if sample time is invalid
     */
    public double getSampleTimeValue() {
        if (sampleTime == null || sampleTime.getAsDouble() == null) {
            throw new IllegalStateException("Sample time is not properly initialized");
        }
        Double value = sampleTime.getAsDouble();
        if (value == null) {
            throw new IllegalStateException("Sample time is not properly initialized or cannot be converted to double");
        }
        return value;
    }

    /**
     * Gets the output data type string with validation.
     *
     * @return Output data type string
     * @throws IllegalStateException if output data type is invalid
     */
    public String getOutDataTypeString() {
        if (outDataTypeStr == null || outDataTypeStr.getAsString() == null) {
            throw new IllegalStateException("Output data type is not properly initialized");
        }
        String value = outDataTypeStr.getAsString();
        if (value == null) {
            throw new IllegalStateException("Output data type is not properly initialized or cannot be converted to string");
        }
        return value;
    }

    /**
     * Checks if the display inherits its sample time from the input signal.
     *
     * @return true if inherited (sample time = -1), false otherwise
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Checks if the display is configured for continuous time.
     *
     * @return true if continuous time (sample time = 0), false otherwise
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if the display is configured for discrete time.
     *
     * @return true if discrete time (sample time > 0), false otherwise
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }

    /**
     * Checks if the display inherits data type from input.
     *
     * @return true if data type is inherited, false otherwise
     */
    public boolean isDataTypeInherited() {
        String dataType = getOutDataTypeString();
        return dataType.toLowerCase().contains("inherit") || dataType.toLowerCase().contains("same as input");
    }

    // === Factory Methods ===

    @Override
    public String toString() {
        return String.format("DisplayDto{blockName='%s', blockPath='%s', sampleTime=%s, outDataType='%s'}",
                getBlockName(), getBlockPath(),
                sampleTime != null ? sampleTime.getAsDouble() : "null",
                outDataTypeStr != null ? outDataTypeStr.getAsString() : "null");
    }

    @Override
    public BlockDto copy() {
        DisplayDto copy = new DisplayDto();
        copy.blockId = this.blockId;
        copy.blockName = this.blockName;
        copy.blockPath = this.blockPath;
        copy.blockUUID = this.blockUUID;
        copy.srcBlock = this.srcBlock;        
        copy.appearance = this.appearance;
        copy.inputPorts = this.inputPorts != null ? new ArrayList<>(this.inputPorts) : null;
        copy.outputPorts = this.outputPorts != null ? new ArrayList<>(this.outputPorts) : null;
        copy.parameters = this.parameters != null ? new HashMap<>(this.parameters) : null;
        copy.paramValues = this.paramValues != null ? new HashMap<>(this.paramValues) : null;
        copy.sampleTime = this.sampleTime != null ? this.sampleTime.copy() : null;
        copy.outDataTypeStr = this.outDataTypeStr != null ? this.outDataTypeStr.copy() : null;
        copy.priority = this.priority;
        copy.description = this.description;
        copy.tags = this.tags != null ? new HashSet<>(this.tags) : null;
        copy.customProperties = this.customProperties != null ? new HashMap<>(this.customProperties) : null;
        copy.createdAt = this.createdAt;
        copy.modifiedAt = this.modifiedAt;
        copy.version = this.version;
        return copy;
    }
}