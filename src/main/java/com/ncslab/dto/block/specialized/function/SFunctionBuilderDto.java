package com.ncslab.dto.block.specialized.function;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for S-Function Builder block.
 * Provides a GUI-based approach to create S-Functions.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SFunctionBuilderDto extends BlockDto {
    
    /**
     * S-Function name.
     * Default: "sfun_builder"
     */
    private TypedParameter sFunctionName;
    
    /**
     * Number of input ports.
     * Default: 1
     */
    private TypedParameter numberOfInputPorts;
    
    /**
     * Number of output ports.
     * Default: 1
     */
    private TypedParameter numberOfOutputPorts;
    
    /**
     * Number of discrete states.
     * Default: 0
     */
    private TypedParameter numberOfDiscreteStates;
    
    /**
     * Number of continuous states.
     * Default: 0
     */
    private TypedParameter numberOfContinuousStates;
    
    /**
     * Sample time specification.
     * Default: "-1" (inherited)
     */
    private TypedParameter sampleTime;
    
    /**
     * C source code for the S-Function.
     * Default: ""
     */
    private TypedParameter sourceCode;
    
    public SFunctionBuilderDto(String blockName, String blockPath) {
        super("S-FunctionBuilder", blockName, blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (sFunctionName == null) {
            sFunctionName = TypedParameter.of("sfun_builder");
        }
        if (numberOfInputPorts == null) {
            numberOfInputPorts = TypedParameter.of(1);
        }
        if (numberOfOutputPorts == null) {
            numberOfOutputPorts = TypedParameter.of(1);
        }
        if (numberOfDiscreteStates == null) {
            numberOfDiscreteStates = TypedParameter.of(0);
        }
        if (numberOfContinuousStates == null) {
            numberOfContinuousStates = TypedParameter.of(0);
        }
        if (sampleTime == null) {
            sampleTime = TypedParameter.of("-1");
        }
        if (sourceCode == null) {
            sourceCode = TypedParameter.of("");
        }
    }
}