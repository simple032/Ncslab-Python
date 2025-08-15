package com.ncslab.dto;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ncslab.dto.annotations.DtoValidation;
import com.ncslab.dto.annotations.FlexibleNumber;
import com.ncslab.dto.deserializers.FlexibleNumberDeserializer;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

// Jakarta validation constraints removed - not available in current dependencies
// import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.NotEmpty;
// import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Optimized ModelJson DTO with enhanced Jackson annotations for direct string->DTO conversion.
 * Eliminates need for intermediate JSONObject processing.
 */
@DtoValidation(validateRequired = true, strictTypes = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"userId", "modelId", "modelName", "modelRealName", "config", "blocks", "lines"})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizedModelJson {
    
    @JsonProperty("userId")
    @JsonDeserialize(using = FlexibleNumberDeserializer.class)
    // Validation annotations removed due to missing jakarta.validation dependency
    // @NotNull(message = "User ID is required")
    // @Min(value = 1, message = "User ID must be positive")
    private Integer userId;
    
    @JsonProperty("testRig")
    @JsonDeserialize(using = FlexibleNumberDeserializer.class)
    @Builder.Default
    private Integer testRig = 0;
    
    @JsonProperty("copyNum")
    @JsonDeserialize(using = FlexibleNumberDeserializer.class)
    @Builder.Default
    private Integer copyNum = 0;
    
    @JsonProperty("modelId")
    @JsonDeserialize(using = FlexibleNumberDeserializer.class)
    @Builder.Default
    private Integer modelId = 0;
    
    @JsonProperty("modelName")
    // @NotEmpty(message = "Model name is required")
    @JsonAlias({"name", "model_name"}) // Support alternative field names
    private String modelName;
    
    @JsonProperty("uuid")
    @JsonDeserialize(using = FlexibleNumberDeserializer.class)
    @Builder.Default
    private Long uuid = 0L;
    
    @JsonProperty("modelRealName")
    @JsonAlias({"realName", "real_name", "model_real_name"})
    private String modelRealName;
    
    @JsonProperty("templateName")
    @JsonAlias({"template", "template_name"})
    private String templateName;
    
    @JsonProperty("config")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ConfigJson config;
    
    @JsonProperty("blocks")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    // @NotNull(message = "Blocks list is required")
    private List<BlockJson> blocks;
    
    @JsonProperty("lines") 
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    // @NotNull(message = "Lines list is required")
    private List<LineJson> lines;
    
    @JsonProperty("option")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> option;
    
    @JsonProperty("saveInfo")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private SaveInfoJson saveInfo;
    
    // Computed properties with caching
    @JsonIgnore
    private transient Boolean hasBlocksCache;
    
    @JsonIgnore
    private transient Boolean hasLinesCache;
    
    /**
     * Check if model has blocks (cached for performance)
     */
    @JsonIgnore
    public boolean hasBlocks() {
        if (hasBlocksCache == null) {
            hasBlocksCache = blocks != null && !blocks.isEmpty();
        }
        return hasBlocksCache;
    }
    
    /**
     * Check if model has lines (cached for performance)
     */
    @JsonIgnore
    public boolean hasLines() {
        if (hasLinesCache == null) {
            hasLinesCache = lines != null && !lines.isEmpty();
        }
        return hasLinesCache;
    }
    
    /**
     * Validate the model structure
     * @return Validation error message or null if valid
     */
    @JsonIgnore
    public String validate() {
        if (modelName == null || modelName.trim().isEmpty()) {
            return "Model name is required";
        }
        if (userId == null || userId <= 0) {
            return "Valid user ID is required";
        }
        if (blocks == null) {
            return "Blocks list cannot be null";
        }
        if (lines == null) {
            return "Lines list cannot be null";
        }
        
        // Validate individual blocks
        for (int i = 0; i < blocks.size(); i++) {
            BlockJson block = blocks.get(i);
            if (block == null) {
                return "Block at index " + i + " is null";
            }
            String blockError = block.getValidationError();
            if (blockError != null) {
                return "Block " + i + " validation failed: " + blockError;
            }
        }
        
        // Validate individual lines
        for (int i = 0; i < lines.size(); i++) {
            LineJson line = lines.get(i);
            if (line == null) {
                return "Line at index " + i + " is null";
            }
            String lineError = line.getValidationError();
            if (lineError != null) {
                return "Line " + i + " validation failed: " + lineError;
            }
        }
        
        return null; // Valid
    }
    
    /**
     * Check if model is valid
     */
    @JsonIgnore
    public boolean isValid() {
        return validate() == null;
    }
    
    /**
     * Get block by name (optimized lookup)
     */
    @JsonIgnore
    public BlockJson getBlockByName(String blockName) {
        if (blockName == null || blocks == null) {
            return null;
        }
        
        return blocks.stream()
                .filter(block -> blockName.equals(block.getBlockName()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get blocks by type (optimized filtering)
     */
    @JsonIgnore
    public List<BlockJson> getBlocksByType(String blockType) {
        if (blockType == null || blocks == null) {
            return List.of();
        }
        
        return blocks.stream()
                .filter(block -> blockType.equals(block.getBlockType()))
                .toList();
    }
    
    /**
     * Get statistics about the model
     */
    @JsonIgnore
    public ModelStatistics getStatistics() {
        return ModelStatistics.builder()
                .blockCount(blocks != null ? blocks.size() : 0)
                .lineCount(lines != null ? lines.size() : 0)
                .hasConfig(config != null)
                .hasOptions(option != null && !option.isEmpty())
                .hasSaveInfo(saveInfo != null)
                .build();
    }
    
    @Getter
    @Builder
    public static class ModelStatistics {
        private final int blockCount;
        private final int lineCount;
        private final boolean hasConfig;
        private final boolean hasOptions;
        private final boolean hasSaveInfo;
    }
    
    // Custom serialization methods for debugging
    @JsonIgnore
    public String toDebugString() {
        return String.format("OptimizedModelJson{modelName='%s', userId=%d, blocks=%d, lines=%d, valid=%s}",
                modelName, userId, 
                blocks != null ? blocks.size() : 0,
                lines != null ? lines.size() : 0,
                isValid());
    }
    
    // Equality and hashCode for proper comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptimizedModelJson that = (OptimizedModelJson) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(modelId, that.modelId) &&
               Objects.equals(modelName, that.modelName) &&
               Objects.equals(uuid, that.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, modelId, modelName, uuid);
    }
    
    @Override
    public String toString() {
        return toDebugString();
    }
}