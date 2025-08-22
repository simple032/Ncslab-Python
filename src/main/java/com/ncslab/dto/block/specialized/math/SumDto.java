package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;
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
    
    /**
     * Create DTO from legacy parameters map (for migration support)
     */
    public static SumDto fromLegacyParameters(java.util.Map<String, String> params) {
        SumDto.SumDtoBuilder builder = SumDto.builder()
                .blockName(params.getOrDefault("blockName", "Sum"))
                .blockPath(params.getOrDefault("blockPath", ""))
                .blockUUID(params.getOrDefault("blockUUID", ""))
                .blockType("Sum");
        
        SumDto dto = builder.build();
        
        // Map parameters to TypedParameter
        if (params.containsKey("Inputs")) {
            dto.inputs = TypedParameter.of(params.get("Inputs"));
        }
        
        if (params.containsKey("InputSameDT")) {
            dto.inputSameDT = TypedParameter.of("on".equals(params.get("InputSameDT")));
        }
        
        if (params.containsKey("OutDataTypeStr")) {
            dto.outDataTypeStr = TypedParameter.of(params.get("OutDataTypeStr"));
        }
        
        if (params.containsKey("SaturateOnIntegerOverflow")) {
            dto.saturateOnIntegerOverflow = TypedParameter.of("on".equals(params.get("SaturateOnIntegerOverflow")));
        }
        
        if (params.containsKey("Icon")) {
            dto.icon = TypedParameter.of(params.get("Icon"));
        }
        
        return dto;
    }
    
    /**
     * Create builder with SIMULINK-compatible defaults
     */
    public static SumDtoBuilder builderWithDefaults() {
        SumDto dto = SumDto.builder()
                .blockType("Sum")
                .sampleTime(-1.0)
                .build();
        
        // Set default typed parameters
        dto.inputs = TypedParameter.of("++++");
        dto.inputSameDT = TypedParameter.of(true);
        dto.outDataTypeStr = TypedParameter.of("Inherit: Same as first input");
        dto.saturateOnIntegerOverflow = TypedParameter.of(false);
        dto.icon = TypedParameter.of("round");
        
        return SumDto.builder()
                .blockId(dto.getBlockId())
                .blockType(dto.getBlockType())
                .blockName(dto.getBlockName())
                .blockPath(dto.getBlockPath())
                .blockUUID(dto.getBlockUUID())
                .sampleTime(dto.getSampleTime())
                .inputs(dto.inputs)
                .inputSameDT(dto.inputSameDT)
                .outDataTypeStr(dto.outDataTypeStr)
                .saturateOnIntegerOverflow(dto.saturateOnIntegerOverflow)
                .icon(dto.icon);
    }
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getInputsValue() {
        return inputs != null ? inputs.getValue(String.class) : "++";
    }
    
    public Boolean getInputSameDTValue() {
        return inputSameDT != null ? inputSameDT.getValue(Boolean.class) : true;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getValue(String.class) : "Inherit: Same as first input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getValue(Boolean.class) : false;
    }
    
    public String getIconValue() {
        return icon != null ? icon.getValue(String.class) : "round";
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation
        
        // Validate inputs parameter
        if (inputs == null) {
            result.addError("Input sequence parameter is required");
        } else {
            String inputSeq = inputs.getValue(String.class);
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
            String iconValue = icon.getValue(String.class);
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
                .blockType(getBlockType())
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
    
    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();
        
        if (inputs != null) {
            params.put("Inputs", String.valueOf(inputs.getValue()));
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", String.valueOf(getSampleTime()));
        }
        if (inputSameDT != null) {
            Boolean sameDT = inputSameDT.getValue(Boolean.class);
            params.put("InputSameDT", Boolean.TRUE.equals(sameDT) ? "on" : "off");
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", String.valueOf(outDataTypeStr.getValue()));
        }
        if (saturateOnIntegerOverflow != null) {
            Boolean satVal = saturateOnIntegerOverflow.getValue(Boolean.class);
            params.put("SaturateOnIntegerOverflow", Boolean.TRUE.equals(satVal) ? "on" : "off");
        }
        if (icon != null) {
            params.put("Icon", String.valueOf(icon.getValue()));
        }
        
        return params;
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