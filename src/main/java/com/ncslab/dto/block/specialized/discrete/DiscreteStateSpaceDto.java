package com.ncslab.dto.block.specialized.discrete;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.versioning.VersionedDto;
import com.ncslab.dto.versioning.VersionInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of DiscreteStateSpace block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the Discrete State-Space block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - A: State matrix (n x n)
 * - B: Input matrix (n x m)
 * - C: Output matrix (p x n)
 * - D: Feedthrough matrix (p x m)
 * - InitialCondition: Initial condition for state vector
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * State-Space Representation:
 * x[k+1] = A*x[k] + B*u[k]
 * y[k]   = C*x[k] + D*u[k]
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 5
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("DiscreteStateSpace")
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.DiscreteStateSpace")
public class DiscreteStateSpaceDto extends BlockDto implements VersionedDto {
    
    // Constructor for Jackson deserialization
    
    // ===== DISCRETE STATE SPACE SPECIFIC PARAMETERS =====
    
    /**
     * State matrix A (n x n)
     * Default: "1"
     * Defines state transition dynamics
     */
    @Builder.Default
    private TypedParameter A = TypedParameter.of("1");
    
    /**
     * Input matrix B (n x m)
     * Default: "1"
     * Defines input influence on state
     */
    @Builder.Default
    private TypedParameter B = TypedParameter.of("1");
    
    /**
     * Output matrix C (p x n)
     * Default: "1"
     * Defines state to output mapping
     */
    @Builder.Default
    private TypedParameter C = TypedParameter.of("1");
    
    /**
     * Feedthrough matrix D (p x m)
     * Default: "0"
     * Defines direct input to output mapping
     */
    @Builder.Default
    private TypedParameter D = TypedParameter.of("0");
    
    /**
     * Initial condition for state vector
     * Default: "0"
     * Initial state values
     */
    @Builder.Default
    private TypedParameter initialCondition = TypedParameter.of("0");
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    
    /**
     * Handle integer overflow
     * Default: false
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getAMatrixValue() {
        return A != null ? A.getAsString() : "1";
    }
    
    public String getBMatrixValue() {
        return B != null ? B.getAsString() : "1";
    }
    
    public String getCMatrixValue() {
        return C != null ? C.getAsString() : "1";
    }
    
    public String getDMatrixValue() {
        return D != null ? D.getAsString() : "0";
    }
    
    public String getInitialConditionValue() {
        return initialCondition != null ? initialCondition.getAsString() : "0";
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate required matrices are present
        if (A == null || A.getAsString() == null || A.getAsString().trim().isEmpty()) {
            result.addError("A matrix is required");
        }
        
        if (B == null || B.getAsString() == null || B.getAsString().trim().isEmpty()) {
            result.addError("B matrix is required");
        }
        
        if (C == null || C.getAsString() == null || C.getAsString().trim().isEmpty()) {
            result.addError("C matrix is required");
        }
        
        if (D == null || D.getAsString() == null || D.getAsString().trim().isEmpty()) {
            result.addError("D matrix is required");
        }
        
        // Validate sample time is positive for discrete systems
        if (getSampleTime() != null && getSampleTime().getAsDouble() <= 0.0) {
            result.addError("Sample time must be positive for discrete systems");
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if the system has feedthrough (D matrix is non-zero)
     */
    public boolean hasFeedthrough() {
        String dMatrix = getDMatrixValue();
        return !"0".equals(dMatrix) && !"[0]".equals(dMatrix);
    }
    
    /**
     * Check if system has initial conditions
     */
    public boolean hasInitialConditions() {
        String ic = getInitialConditionValue();
        return !"0".equals(ic) && !"[0]".equals(ic);
    }
    
    @Override
    public DiscreteStateSpaceDto copy() {
        return DiscreteStateSpaceDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .A(A != null ? A.copy() : null)
                .B(B != null ? B.copy() : null)
                .C(C != null ? C.copy() : null)
                .D(D != null ? D.copy() : null)
                .initialCondition(initialCondition != null ? initialCondition.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("A", A)
                .put("B", B)
                .put("C", C)
                .put("D", D)
                .put("InitialCondition", initialCondition)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .put("SampleTime", getSampleTime() != null ? TypedParameter.of(getSampleTime()) : null)
                .build();
    }
    
    @Override
    public boolean isValidConfiguration() {
        ValidationResult result = validate();
        return result.isValid() && 
               A != null && B != null && C != null && D != null &&
               getSampleTime() != null && getSampleTime().getAsDouble() > 0.0;
    }
    
    // ===== VERSIONING SUPPORT =====
    
    @Override
    public VersionInfo getVersionInfo() {
        return VersionInfo.of("1.0", "Initial DTO implementation");
    }
    
    @Override
    public String toString() {
        return String.format("DiscreteStateSpaceDto{id=%d, name='%s', sampleTime=%.3f, feedthrough=%s}", 
                           getBlockId(), 
                           getBlockName(), 
                           getSampleTime(),
                           hasFeedthrough());
    }
}