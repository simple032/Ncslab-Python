package com.ncslab.dto.block.specialized.continuous;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO representation of StateSpace block for linear state-space system modeling.
 * 
 * The StateSpace block implements a linear state-space system of the form:
 * dx/dt = Ax + Bu (continuous time) or x[n+1] = Ax[n] + Bu[n] (discrete time)
 * y = Cx + Du
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("StateSpace")
@MigrationCompatible(originalClass = "com.ncslab.block.continuous.StateSpace")
public class StateSpaceDto extends BlockDto {
    
    /**
     * State matrix A (n x n)
     * Default: "[1]"
     * Format: "[a11 a12; a21 a22]" for 2x2 matrix
     */
    @Builder.Default
    private TypedParameter stateMatrix = TypedParameter.of("[1]");
    
    /**
     * Input matrix B (n x m)
     * Default: "[1]"
     * Format: "[b11 b12; b21 b22]"
     */
    @Builder.Default
    private TypedParameter inputMatrix = TypedParameter.of("[1]");
    
    /**
     * Output matrix C (p x n)
     * Default: "[1]"
     * Format: "[c11 c12]"
     */
    @Builder.Default
    private TypedParameter outputMatrix = TypedParameter.of("[1]");
    
    /**
     * Feedthrough matrix D (p x m)
     * Default: "[0]"
     * Format: "[d11 d12]"
     */
    @Builder.Default
    private TypedParameter feedthroughMatrix = TypedParameter.of("[0]");
    
    /**
     * Initial state X0 (n x 1)
     * Default: "[0]"
     * Format: "[x1; x2]"
     */
    @Builder.Default
    private TypedParameter initialState = TypedParameter.of("[0]");
    
    /**
     * Sample time for the block operation
     * Default: 0.0 (continuous time)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(0.0);
    
    /**
     * Absolute tolerance for solver
     * Default: "auto"
     */
    @Builder.Default
    private TypedParameter absoluteTolerance = TypedParameter.of("auto");
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    
    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);
    
    /**
     * Continuous state attributes
     * Default: "[]"
     */
    @Builder.Default
    private TypedParameter continuousStateAttributes = TypedParameter.of("[]");
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getStateMatrixValue() {
        return stateMatrix != null ? stateMatrix.getAsString() : "[1]";
    }
    
    public String getInputMatrixValue() {
        return inputMatrix != null ? inputMatrix.getAsString() : "[1]";
    }
    
    public String getOutputMatrixValue() {
        return outputMatrix != null ? outputMatrix.getAsString() : "[1]";
    }
    
    public String getFeedthroughMatrixValue() {
        return feedthroughMatrix != null ? feedthroughMatrix.getAsString() : "[0]";
    }
    
    public String getInitialStateValue() {
        return initialState != null ? initialState.getAsString() : "[0]";
    }
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : 0.0;
    }
    
    public String getAbsoluteToleranceValue() {
        return absoluteTolerance != null ? absoluteTolerance.getAsString() : "auto";
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    public String getContinuousStateAttributesValue() {
        return continuousStateAttributes != null ? continuousStateAttributes.getAsString() : "[]";
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < 0.0) {
                result.addError("Sample time must be >= 0.0 for state space systems");
            }
        }
        
        // Basic matrix format validation
        if (stateMatrix == null || stateMatrix.getAsString() == null || stateMatrix.getAsString().trim().isEmpty()) {
            result.addError("State matrix A cannot be empty");
        }
        
        if (inputMatrix == null || inputMatrix.getAsString() == null || inputMatrix.getAsString().trim().isEmpty()) {
            result.addError("Input matrix B cannot be empty");
        }
        
        if (outputMatrix == null || outputMatrix.getAsString() == null || outputMatrix.getAsString().trim().isEmpty()) {
            result.addError("Output matrix C cannot be empty");
        }
        
        if (feedthroughMatrix == null || feedthroughMatrix.getAsString() == null || feedthroughMatrix.getAsString().trim().isEmpty()) {
            result.addError("Feedthrough matrix D cannot be empty");
        }
        
        if (initialState == null || initialState.getAsString() == null || initialState.getAsString().trim().isEmpty()) {
            result.addError("Initial state X0 cannot be empty");
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if this is a continuous-time system
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }
    
    /**
     * Check if this is a discrete-time system
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }
    
    @Override
    public StateSpaceDto copy() {
        return StateSpaceDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .stateMatrix(stateMatrix != null ? stateMatrix.copy() : null)
                .inputMatrix(inputMatrix != null ? inputMatrix.copy() : null)
                .outputMatrix(outputMatrix != null ? outputMatrix.copy() : null)
                .feedthroughMatrix(feedthroughMatrix != null ? feedthroughMatrix.copy() : null)
                .initialState(initialState != null ? initialState.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .absoluteTolerance(absoluteTolerance != null ? absoluteTolerance.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .continuousStateAttributes(continuousStateAttributes != null ? continuousStateAttributes.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("A", stateMatrix)
                .put("B", inputMatrix)
                .put("C", outputMatrix)
                .put("D", feedthroughMatrix)
                .put("X0", initialState)
                .put("SampleTime", sampleTime)
                .put("AbsoluteTolerance", absoluteTolerance)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .put("ContinuousStateAttributes", continuousStateAttributes)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("StateSpaceDto{id=%d, name='%s', type='%s', continuous=%s}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           isContinuous());
    }
}