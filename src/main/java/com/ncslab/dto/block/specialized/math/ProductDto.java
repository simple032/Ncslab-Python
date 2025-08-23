package com.ncslab.dto.block.specialized.math;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * DTO representation of Product math block.
 * 
 * The Product block performs element-wise or matrix multiplication/division
 * operations on multiple inputs based on the input sequence specification.
 * The input sequence defines which operations to perform: '*' for multiplication
 * and '/' for division.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Product")
@MigrationCompatible(originalClass = "com.ncslab.block.math.Product")
public class ProductDto extends BlockDto {
    
    /**
     * Input sequence defining operations
     * Default: "**" (two multiplication inputs)
     * Format: String of '*' and '/' characters
     * Each character represents one input port's operation
     */
    @Builder.Default
    private TypedParameter inputs = TypedParameter.of("**");
    
    /**
     * Multiplication mode
     * Default: "Element-wise(.*)"
     * Options: "Element-wise(.*)", "Matrix(*)"
     */
    @Builder.Default
    private TypedParameter multiplication = TypedParameter.of("Element-wise(.*)");
    
    /**
     * Require inputs to have same data type
     * Default: true
     */
    @Builder.Default
    private TypedParameter inputSameDT = TypedParameter.of(true);
    
    /**
     * Sample time for the block operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as first input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as first input");
    
    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getInputsValue() {
        return inputs != null ? inputs.getAsString() : "**";
    }
    
    public String getMultiplicationValue() {
        return multiplication != null ? multiplication.getAsString() : "Element-wise(.*)";
    }
    
    public Boolean getInputSameDTValue() {
        return inputSameDT != null ? inputSameDT.getAsBoolean() : true;
    }
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as first input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate inputs parameter
        if (inputs == null) {
            result.addError("Input sequence parameter is required");
        } else {
            String inputSeq = inputs.getAsString();
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
            String multValue = multiplication.getAsString();
            if (multValue != null && 
                !multValue.equals("Element-wise(.*)") && 
                !multValue.equals("Matrix(*)")) {
                result.addError("Multiplication mode must be 'Element-wise(.*)' or 'Matrix(*)'");
            }
        }
        
        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if this block operates in continuous time
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }
    
    /**
     * Check if this block inherits its sample time
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }
    
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
    
    /**
     * Check if all operations are multiplication
     */
    public boolean isAllMultiplication() {
        String inputSeq = getInputsValue();
        if (inputSeq == null || inputSeq.isEmpty()) {
            return false;
        }
        for (char c : inputSeq.toCharArray()) {
            if (c != '*') {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if any operations are division
     */
    public boolean hasAnyDivision() {
        String inputSeq = getInputsValue();
        if (inputSeq == null) {
            return false;
        }
        return inputSeq.contains("/");
    }
    
    /**
     * Count number of multiplication operations
     */
    public int getMultiplicationCount() {
        String inputSeq = getInputsValue();
        if (inputSeq == null) {
            return 0;
        }
        return (int) inputSeq.chars().filter(c -> c == '*').count();
    }
    
    /**
     * Count number of division operations
     */
    public int getDivisionCount() {
        String inputSeq = getInputsValue();
        if (inputSeq == null) {
            return 0;
        }
        return (int) inputSeq.chars().filter(c -> c == '/').count();
    }
    
    @Override
    public ProductDto copy() {
        return ProductDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .inputs(inputs != null ? inputs.copy() : null)
                .multiplication(multiplication != null ? multiplication.copy() : null)
                .inputSameDT(inputSameDT != null ? inputSameDT.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Inputs", inputs)
                .put("Multiplication", multiplication)
                .put("InputSameDT", inputSameDT)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
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