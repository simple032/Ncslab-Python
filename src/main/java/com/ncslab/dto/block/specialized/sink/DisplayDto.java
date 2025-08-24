package com.ncslab.dto.block.specialized.sink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.sink.SinkDto;
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
public class DisplayDto extends SinkDto {
    
    // Constructor to set blockType for Jackson deserialization

    // Display blocks inherit sampleTime from BlockDto (via SinkDto)
    // No additional parameters needed - displays just show input values

    /**
     * Constructs DisplayDto with individual parameters.
     *
     * @param blockName      Name of the block
     * @param blockPath      Path of the block in the model hierarchy
     * @param sampleTime     Sample time parameter
     */
    public DisplayDto(String blockName, String blockPath, TypedParameter sampleTime) {
        super();
        this.blockName = blockName;
        this.blockPath = blockPath;
        this.sampleTime = sampleTime;
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
        // Display blocks don't need outDataTypeStr - they just display input values
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

        // Display blocks don't have outputs, so no outDataTypeStr validation needed

        return result;
    }

    // === Helper Methods ===

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

    // === Factory Methods ===

    @Override
    public String toString() {
        return String.format("DisplayDto{blockName='%s', blockPath='%s', sampleTime=%s}",
                getBlockName(), getBlockPath(),
                sampleTime != null ? sampleTime.getAsDouble() : "null");
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