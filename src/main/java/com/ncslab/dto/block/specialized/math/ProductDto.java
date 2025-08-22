package com.ncslab.dto.block.specialized.math;

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
 * DTO representation of Product block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the Product block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - Inputs: String sequence defining input operations (e.g., "**", "*", "/")
 * - Multiplication: Element-wise or Matrix multiplication mode
 * - SampleTime: Sample time for discrete operation (-1 for inherited)
 * - InputSameDT: Require inputs to have same data type
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
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
@MigrationCompatible(originalClass = "com.ncslab.block.math.Product")
public class ProductDto extends BlockDto {
    
    // ===== PRODUCT BLOCK SPECIFIC PARAMETERS =====
    
    /**
     * String sequence defining input operations (e.g., "**", "*", "/")
     * Default: "**"
     * Validation: Must contain only '*' and '/' characters, at least one character
     */
    private TypedParameter inputs;
    
    /**
     * Element-wise or Matrix multiplication mode
     * Default: "Element-wise(.*)"
     * Options: "Element-wise(.*)", "Matrix(*)"
     */
    private TypedParameter multiplication;
    
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
    
    // ===== FACTORY METHODS =====
    
    /**
     * Create DTO from legacy parameters map (for migration support)
     */
    public static ProductDto fromLegacyParameters(Map<String, String> params) {
        ProductDto.ProductDtoBuilder builder = ProductDto.builder()
                .blockName(params.getOrDefault("blockName", "Product"))
                .blockPath(params.getOrDefault("blockPath", ""))
                .blockUUID(params.getOrDefault("blockUUID", ""))
                .blockType("Product");
        
        ProductDto dto = builder.build();
        
        // Map parameters to TypedParameter
        if (params.containsKey("Inputs")) {
            dto.inputs = TypedParameter.of(params.get("Inputs"));
        }
        
        if (params.containsKey("Multiplication")) {
            dto.multiplication = TypedParameter.of(params.get("Multiplication"));
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
        
        return dto;
    }
    
    /**
     * Create builder with SIMULINK-compatible defaults
     */
    public static ProductDtoBuilder builderWithDefaults() {
        ProductDto dto = ProductDto.builder()
                .blockType("Product")
                .sampleTime(-1.0)
                .build();
        
        // Set default typed parameters
        dto.inputs = TypedParameter.of("**");
        dto.multiplication = TypedParameter.of("Element-wise(.*)");
        dto.inputSameDT = TypedParameter.of(true);
        dto.outDataTypeStr = TypedParameter.of("Inherit: Same as first input");
        dto.saturateOnIntegerOverflow = TypedParameter.of(false);
        
        return ProductDto.builder()
                .blockId(dto.getBlockId())
                .blockType(dto.getBlockType())
                .blockName(dto.getBlockName())
                .blockPath(dto.getBlockPath())
                .blockUUID(dto.getBlockUUID())
                .sampleTime(dto.getSampleTime())
                .inputs(dto.inputs)
                .multiplication(dto.multiplication)
                .inputSameDT(dto.inputSameDT)
                .outDataTypeStr(dto.outDataTypeStr)
                .saturateOnIntegerOverflow(dto.saturateOnIntegerOverflow);
    }
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getInputsValue() {
        return inputs != null ? inputs.getValue(String.class) : "**";
    }
    
    public String getMultiplicationValue() {
        return multiplication != null ? multiplication.getValue(String.class) : "Element-wise(.*)";
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
                // Validate sequence contains only * and / characters
                for (char c : inputSeq.toCharArray()) {
                    if (c != '*' && c != '/') {
                        result.addError("Input sequence must contain only '*' and '/' characters");
                        break;
                    }
                }
                
                if (inputSeq.length() < 1) {
                    result.addError("Input sequence must have at least one input");
                }
            }
        }
        
        // Validate multiplication mode
        if (multiplication != null) {
            String multValue = multiplication.getValue(String.class);
            if (multValue != null && !multValue.equals("Element-wise(.*)") && !multValue.equals("Matrix(*)")) {
                result.addError("Multiplication mode must be 'Element-wise(.*)' or 'Matrix(*)'");
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
     * Check if input at given index is multiplication ('*')
     */
    public boolean isInputMultiplication(int index) {
        String inputSeq = getInputsValue();
        if (inputSeq != null && index >= 0 && index < inputSeq.length()) {
            return inputSeq.charAt(index) == '*';
        }
        return false;
    }
    
    /**
     * Check if input at given index is division ('/')
     */
    public boolean isInputDivision(int index) {
        String inputSeq = getInputsValue();
        if (inputSeq != null && index >= 0 && index < inputSeq.length()) {
            return inputSeq.charAt(index) == '/';
        }
        return false;
    }
    
    /**
     * Check if matrix multiplication is enabled
     */
    public boolean isMatrixMultiplication() {
        return "Matrix(*)".equals(getMultiplicationValue());
    }
    
    @Override
    public ProductDto copy() {
        return ProductDto.builder()
                .blockId(getBlockId())
                .blockType(getBlockType())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .inputs(inputs != null ? inputs.copy() : null)
                .multiplication(multiplication != null ? multiplication.copy() : null)
                .inputSameDT(inputSameDT != null ? inputSameDT.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
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
        if (multiplication != null) {
            params.put("Multiplication", String.valueOf(multiplication.getValue()));
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
        
        return params;
    }
    
    @Override
    public String toString() {
        return String.format("ProductDto{id=%d, name='%s', type='%s', inputs='%s', ports=%d, matrix=%s}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getInputsValue(), 
                           getInputPortCount(),
                           isMatrixMultiplication());
    }
}