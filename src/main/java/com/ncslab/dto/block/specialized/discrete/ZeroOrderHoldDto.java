package com.ncslab.dto.block.specialized.discrete;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.block.discrete.DiscreteBlock;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.block.discrete.DiscreteBlockDto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;

/**
 * DTO representation of Zero_Order_Hold block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the Zero_Order_Hold block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - SampleTime: Sample time for discrete operation (must be positive or -1 for inherited)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * Zero-Order Hold Behavior:
 * The zero-order hold block samples the input at discrete intervals and holds
 * the output constant until the next sample time. This is commonly used for
 * digital-to-analog conversion and discrete-time control systems.
 * 
 * y(t) = u[k] for kT ≤ t < (k+1)T
 * 
 * where T is the sample time and k is the sample index.
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 8
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonTypeName("ZeroOrderHold")
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.Zero_Order_Hold")
public class ZeroOrderHoldDto extends DiscreteBlockDto {

    // ===== ZERO ORDER HOLD SPECIFIC PARAMETERS =====
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    private TypedParameter outDataTypeStr;
    
    /**
     * Handle integer overflow
     * Default: false (off)
     */
    private TypedParameter saturateOnIntegerOverflow;
    
    // ===== FACTORY METHODS =====    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // ===== DISCRETE-TIME VALIDATION =====
    
    
    // ===== UTILITY METHODS =====

    /**
     * Get the hold duration in seconds
     */
    public double getHoldDuration() {
        if (isDiscreteSampleTime()) {
            return sampleTime.getAsDouble(); // Hold for one sample period
        }
        return Double.NaN; // Cannot determine without knowing inherited sample time
    }
    
    /**
     * Calculate the Nyquist frequency for anti-aliasing considerations
     */
    public double getNyquistFrequency() {
        if (isDiscreteSampleTime()) {
            return 1.0 / (2.0 * getSampleTime().getAsDouble()); // f_nyquist = 1/(2*T)
        }
        return Double.NaN;
    }
    
    /**
     * Create zero-order hold with specific sample time
     */
    public static ZeroOrderHoldDto create(String name, String path, double sampleTime) {
        ZeroOrderHoldDto dto = new ZeroOrderHoldDto();
        dto.setBlockName(name);
        dto.setBlockPath(path);
        dto.setSampleTime(TypedParameter.of(sampleTime));
        
        // Set default parameters
        dto.setOutDataTypeStr(TypedParameter.of("Inherit: Same as input"));
        dto.setSaturateOnIntegerOverflow(TypedParameter.of(false));
        
        return dto;
    }
    
    /**
     * Create zero-order hold with inherited sample time
     */
    public static ZeroOrderHoldDto createInherited(String name, String path) {
        return create(name, path, -1.0);
    }
    
    /**
     * Create zero-order hold for common control system frequencies
     */
    public static ZeroOrderHoldDto createForFrequency(String name, String path, double frequency) {
        // Sample at 10x the signal frequency for good reconstruction
        double sampleTime = 1.0 / (10.0 * frequency);
        return create(name, path, sampleTime);
    }
    
    @Override
    public ZeroOrderHoldDto copy() {
        return ZeroOrderHoldDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("ZeroOrderHoldDto{id=%d, name='%s', type='%s', sampleTime=%s, nyquist=%.2fHz}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getSampleTime(),
                           isDiscreteSampleTime() ? getNyquistFrequency() : Double.NaN);
    }
}