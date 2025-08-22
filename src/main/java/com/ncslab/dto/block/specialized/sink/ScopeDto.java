package com.ncslab.dto.block.specialized.sink;

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

/**
 * DTO representation of Scope block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the Scope block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - NumberOfInputs: Number of input ports (default: 1)
 * - SampleTime: Sample time for data collection (-1 for inherited, 0 for continuous)
 * - SaveName: Variable name to save data (default: "ScopeData")
 * - SaveFormat: Data save format (default: "Array")
 * - BufferSize: Size of data buffer (default: 100000)
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
@MigrationCompatible(originalClass = "com.ncslab.block.sink.Scope")
public class ScopeDto extends BlockDto {
    
    // ===== SCOPE BLOCK SPECIFIC PARAMETERS =====
    
    /**
     * Number of input ports
     * Default: 1
     * Validation: Must be positive integer
     */
    private TypedParameter numberOfInputs;
    
    /**
     * Variable name to save data
     * Default: "ScopeData"
     * Validation: Must be valid identifier
     */
    private TypedParameter saveName;
    
    /**
     * Data save format
     * Default: "Array"
     * Options: "Array", "Structure", "Structure with time"
     */
    private TypedParameter saveFormat;
    
    /**
     * Size of data buffer
     * Default: 100000
     * Validation: Must be positive integer
     */
    private TypedParameter bufferSize;
    
    // ===== FACTORY METHODS =====
    
    /**
     * Create DTO from legacy parameters map (for migration support)
     */
    public static ScopeDto fromLegacyParameters(Map<String, String> params) {
        ScopeDto.ScopeDtoBuilder builder = ScopeDto.builder()
                .blockName(params.getOrDefault("blockName", "Scope"))
                .blockPath(params.getOrDefault("blockPath", ""))
                .blockUUID(params.getOrDefault("blockUUID", ""))
                .blockType("Scope");
        
        ScopeDto dto = builder.build();
        
        // Map parameters to TypedParameter
        if (params.containsKey("NumberOfInputs")) {
            dto.numberOfInputs = TypedParameter.of(Integer.parseInt(params.get("NumberOfInputs")));
        } else if (params.containsKey("Inputs")) {
            // Legacy compatibility
            dto.numberOfInputs = TypedParameter.of(Integer.parseInt(params.get("Inputs")));
        }
        
        if (params.containsKey("SaveName")) {
            dto.saveName = TypedParameter.of(params.get("SaveName"));
        }
        
        if (params.containsKey("SaveFormat")) {
            dto.saveFormat = TypedParameter.of(params.get("SaveFormat"));
        }
        
        if (params.containsKey("BufferSize")) {
            dto.bufferSize = TypedParameter.of(Integer.parseInt(params.get("BufferSize")));
        }
        
        return dto;
    }
    
    /**
     * Create builder with SIMULINK-compatible defaults
     */
    public static ScopeDtoBuilder builderWithDefaults() {
        ScopeDto dto = ScopeDto.builder()
                .blockType("Scope")
                .sampleTime(-1.0)
                .build();
        
        // Set default typed parameters
        dto.numberOfInputs = TypedParameter.of(1);
        dto.saveName = TypedParameter.of("ScopeData");
        dto.saveFormat = TypedParameter.of("Array");
        dto.bufferSize = TypedParameter.of(100000);
        
        return ScopeDto.builder()
                .blockId(dto.getBlockId())
                .blockType(dto.getBlockType())
                .blockName(dto.getBlockName())
                .blockPath(dto.getBlockPath())
                .blockUUID(dto.getBlockUUID())
                .sampleTime(dto.getSampleTime())
                .numberOfInputs(dto.numberOfInputs)
                .saveName(dto.saveName)
                .saveFormat(dto.saveFormat)
                .bufferSize(dto.bufferSize);
    }
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Integer getNumberOfInputsValue() {
        return numberOfInputs != null ? numberOfInputs.getValue(Integer.class) : 1;
    }
    
    public String getSaveNameValue() {
        return saveName != null ? saveName.getValue(String.class) : "ScopeData";
    }
    
    public String getSaveFormatValue() {
        return saveFormat != null ? saveFormat.getValue(String.class) : "Array";
    }
    
    public Integer getBufferSizeValue() {
        return bufferSize != null ? bufferSize.getValue(Integer.class) : 100000;
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation
        
        // Validate number of inputs
        if (numberOfInputs != null) {
            Integer inputCount = numberOfInputs.getValue(Integer.class);
            if (inputCount == null || inputCount <= 0) {
                result.addError("Number of inputs must be a positive integer");
            } else if (inputCount > 10) {
                result.addError("Number of inputs should not exceed 10 for performance reasons");
            }
        }
        
        // Validate save name
        if (saveName != null) {
            String saveNameStr = saveName.getValue(String.class);
            if (saveNameStr != null && !saveNameStr.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                result.addError("Save name must be a valid identifier (letters, numbers, underscore, starting with letter or underscore)");
            }
        }
        
        // Validate save format
        if (saveFormat != null) {
            String formatStr = saveFormat.getValue(String.class);
            if (formatStr != null && !formatStr.equals("Array") && 
                !formatStr.equals("Structure") && !formatStr.equals("Structure with time")) {
                result.addError("Save format must be 'Array', 'Structure', or 'Structure with time'");
            }
        }
        
        // Validate buffer size
        if (bufferSize != null) {
            Integer bufferSizeVal = bufferSize.getValue(Integer.class);
            if (bufferSizeVal == null || bufferSizeVal <= 0) {
                result.addError("Buffer size must be a positive integer");
            } else if (bufferSizeVal > 10000000) {
                result.addError("Buffer size should not exceed 10,000,000 for memory reasons");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Get the number of input ports this scope will have
     */
    public int getInputPortCount() {
        return getNumberOfInputsValue();
    }
    
    /**
     * Check if this scope saves data in structure format
     */
    public boolean isSaveStructureFormat() {
        String format = getSaveFormatValue();
        return format != null && (format.equals("Structure") || format.equals("Structure with time"));
    }
    
    /**
     * Check if this scope includes time in saved data
     */
    public boolean includesTimeInSave() {
        String format = getSaveFormatValue();
        return format != null && format.equals("Structure with time");
    }
    
    @Override
    public ScopeDto copy() {
        return ScopeDto.builder()
                .blockId(getBlockId())
                .blockType(getBlockType())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .numberOfInputs(numberOfInputs != null ? numberOfInputs.copy() : null)
                .saveName(saveName != null ? saveName.copy() : null)
                .saveFormat(saveFormat != null ? saveFormat.copy() : null)
                .bufferSize(bufferSize != null ? bufferSize.copy() : null)
                .build();
    }
    
    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();
        
        if (numberOfInputs != null) {
            params.put("NumberOfInputs", String.valueOf(numberOfInputs.getValue()));
            params.put("Inputs", String.valueOf(numberOfInputs.getValue())); // Legacy compatibility
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", String.valueOf(getSampleTime()));
        }
        if (saveName != null) {
            params.put("SaveName", String.valueOf(saveName.getValue()));
        }
        if (saveFormat != null) {
            params.put("SaveFormat", String.valueOf(saveFormat.getValue()));
        }
        if (bufferSize != null) {
            params.put("BufferSize", String.valueOf(bufferSize.getValue()));
        }
        
        return params;
    }
    
    @Override
    public String toString() {
        return String.format("ScopeDto{id=%d, name='%s', type='%s', inputs=%d, saveName='%s', format='%s'}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getNumberOfInputsValue(), 
                           getSaveNameValue(),
                           getSaveFormatValue());
    }
}