package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO representation of Sum block with SIMULINK-compatible parameters.
 *
 * This DTO provides a modern, type-safe interface for the Sum block
 * and supports migration from the legacy JSONObject-based approach.
 *
 * SIMULINK Parameters:
 * - Inputs: String sequence defining input signs (e.g., "++", "+-", "++--")
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - InputSameDT: Require inputs to have same data type
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * - Icon: Icon shape representation
 *
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 5
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Sum")
@MigrationCompatible(originalClass = "com.ncslab.block.math.Sum")
public class SumDto extends BlockDto {
    
    // ===== SUM BLOCK SPECIFIC PARAMETERS =====
    
    /**
     * String sequence defining input signs (e.g., "++", "+-", "++--")
     * Default: "++"
     * Validation: Must contain only '+' and '-' characters, at least one character
     */
    private TypedParameter inputs;
    
    /**
     * Require inputs to have same data type
     * Default: true (on)
     */
    private TypedParameter inputSameDT;
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as first input"
     */
    private TypedParameter outDataTypeStr;
    
    /**
     * Handle integer overflow
     * Default: false (off)
     */
    private TypedParameter saturateOnIntegerOverflow;
    
    /**
     * Icon shape representation
     * Default: "round"
     * Options: "round", "rectangular"
     */
    private TypedParameter icon;
    
    // ===== FACTORY METHODS =====
    
    
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getInputsValue() {
        return inputs != null ? inputs.getAsString() : "++";
    }
    
    public Boolean getInputSameDTValue() {
        return inputSameDT != null ? inputSameDT.getAsBoolean() : true;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as first input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    public String getIconValue() {
        return icon != null ? icon.getAsString() : "round";
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation
        
        // Validate inputs parameter
        if (inputs == null) {
            result.addError("Input sequence parameter is required");
        } else {
            String inputSeq = inputs.getAsString();
            if (inputSeq == null || inputSeq.trim().isEmpty()) {
                result.addError("Input sequence cannot be empty");
            } else {
                // Validate sequence contains only + and - characters
                for (char c : inputSeq.toCharArray()) {
                    if (c != '+' && c != '-') {
                        result.addError("Input sequence must contain only '+' and '-' characters");
                        break;
                    }
                }
                
                if (inputSeq.length() < 1) {
                    result.addError("Input sequence must have at least one input");
                }
            }
        }
        
        // Validate icon shape
        if (icon != null) {
            String iconValue = icon.getAsString();
            if (iconValue != null && !iconValue.equals("round") && !iconValue.equals("rectangular")) {
                result.addError("Icon must be 'round' or 'rectangular'");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Get the number of input ports based on input sequence
     */
    public int getInputPortCount() {
        String inputSeq = getInputsValue();
        return inputSeq != null ? inputSeq.length() : 0;
    }
    
    /**
     * Get input port names dynamically generated from sequence
     */
    public List<String> getInputPortNames() {
        List<String> portNames = new ArrayList<>();
        String inputSeq = getInputsValue();
        if (inputSeq != null) {
            for (int i = 0; i < inputSeq.length(); i++) {
                portNames.add("in" + (i + 1));
            }
        }
        return portNames;
    }
    
    /**
     * Check if input at given index is positive ('+')
     */
    public boolean isInputPositive(int index) {
        String inputSeq = getInputsValue();
        if (inputSeq != null && index >= 0 && index < inputSeq.length()) {
            return inputSeq.charAt(index) == '+';
        }
        return false;
    }
    
    @Override
    public SumDto copy() {
        return SumDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .inputs(inputs != null ? inputs.copy() : null)
                .inputSameDT(inputSameDT != null ? inputSameDT.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .icon(icon != null ? icon.copy() : null)
                .build();
    }
    
    
    @Override
    public String toString() {
        return String.format("SumDto{id=%d, name='%s', type='%s', inputs='%s', ports=%d}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getInputsValue(), 
                           getInputPortCount());
    }
}