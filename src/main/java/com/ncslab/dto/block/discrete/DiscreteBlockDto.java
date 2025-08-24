package com.ncslab.dto.block.discrete;

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
 * Abstract DTO representation of DiscreteBlock with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for discrete-time blocks
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - SampleTime: Discrete sample time (-1 for inherited, 0 for fixed step, positive for discrete)
 * - Feedthrough: Whether block has direct feedthrough from input to output
 * 
 * All discrete blocks inherit timing precision and update logic from this base class.
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.DiscreteBlock")
public class DiscreteBlockDto extends BlockDto {

    // === Core Discrete Block Parameters ===    
    
    /**
     * Whether block has feedthrough from input to output
     * Default: false (most discrete blocks don't have feedthrough)
     */
    @Builder.Default
    private TypedParameter feedthrough = TypedParameter.of(false);

    public DiscreteBlockDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super(blockName, blockPath, position, dimension);
    }
    // === Type-Safe Getters ===
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }
    
    public Boolean getFeedthroughValue() {
        return feedthrough != null ? feedthrough.getAsBoolean() : false;
    }

    // === DTO Methods ===
    
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate sample time
        if (sampleTime != null && sampleTime.isNumericType()) {
            double st = sampleTime.getAsDouble();
            // Sample time must be -1 (inherited), 0 (fixed step), or positive
            if (st < -1.0 || (st > -1.0 && st < 0.0)) {
                result.addError("Sample time must be -1 (inherited), 0 (fixed step), or positive");
            }
        }
        
        // Validate feedthrough
        if (feedthrough != null && !feedthrough.isBooleanType()) {
            result.addError("Feedthrough must be a boolean value");
        }
        
        return result;
    }
    
    // === Timing Helper Methods ===
    
    /**
     * Check if block has feedthrough
     */
    public boolean hasFeedthrough() {
        return getFeedthroughValue();
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
            return "0"; // Fixed step
        } else {
            return String.valueOf(st);
        }
    }
    
    /**
     * Get feedthrough as string for code generation
     */
    public String getFeedthroughString() {
        return getFeedthroughValue() ? "true" : "false";
    }
}