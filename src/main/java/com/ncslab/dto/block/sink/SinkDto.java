package com.ncslab.dto.block.sink;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.block.BlockDimensionDto;
import com.ncslab.dto.block.BlockPositionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;

/**
 * Base DTO class for all sink blocks in the NCSLabLink system.
 * 
 * Sink blocks are terminal blocks that consume data but don't produce outputs.
 * They are used for visualization, data storage, termination, and monitoring.
 * 
 * Common sink block types:
 * - Scope: Data visualization and storage
 * - Display: Numerical display
 * - Terminator: Signal termination
 * - Matplotlib: Advanced plotting
 * 
 * SIMULINK Parameters:
 * - SampleTime: Sample time for data collection (-1 for inherited, 0 for continuous, >0 for discrete)
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MigrationCompatible(originalClass = "com.ncslab.block.sink.SinkBlock")
public class SinkDto extends BlockDto {

    // === Core Sink Block Parameters ===
    // Sample time is inherited from BlockDto - no additional parameters needed for base sink blocks
        
    // === Constructor ===
    
    public SinkDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
    }

    // === Type-Safe Getters ===
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }


    // === DTO Methods ===
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate sample time
        if (sampleTime != null && sampleTime.isNumericType()) {
            double st = sampleTime.getAsDouble();
            // Sample time must be -1 (inherited), 0 (continuous), or positive
            if (st < -1.0 || (st > -1.0 && st < 0.0)) {
                result.addError("Sample time must be -1 (inherited), 0 (continuous), or positive");
            }
        }
        
        return result;
    }
    
    // === Timing Helper Methods ===
    
    /**
     * Check if sample time is inherited (-1)
     */
    public boolean isInheritedSampleTime() {
        return getSampleTimeValue() == -1.0;
    }
    
    /**
     * Check if sample time is continuous (0)
     */
    public boolean isContinuousSampleTime() {
        return getSampleTimeValue() == 0.0;
    }
    
    /**
     * Check if sample time is discrete (positive value)
     */
    public boolean isDiscreteSampleTime() {
        return getSampleTimeValue() > 0.0;
    }
        
    // === Parameter Access Helpers ===
    
    /**
     * Get sample time as string for code generation
     */
    public String getSampleTimeString() {
        double st = getSampleTimeValue();
        if (st == -1.0) {
            return "-1"; // Inherited
        } else if (st == 0.0) {
            return "0"; // Continuous
        } else {
            return String.valueOf(st);
        }
    }
    
    /**
     * Check if this sink block processes data in real-time
     */
    public boolean isRealTimeProcessing() {
        return isContinuousSampleTime() || isDiscreteSampleTime();
    }
    
    /**
     * Get display-friendly description of sample time behavior
     */
    public String getSampleTimeDescription() {
        if (isInheritedSampleTime()) {
            return "Inherited from input";
        } else if (isContinuousSampleTime()) {
            return "Continuous time";
        } else {
            return String.format("Discrete time (%.6f s)", getSampleTimeValue());
        }
    }
}