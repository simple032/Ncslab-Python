package com.ncslab.dto.block;

import com.ncslab.dto.core.BaseDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ncslab.dto.mapper.validation.ValidationResult;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.HashMap;

/**
 * DTO for block configuration settings with validation support.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class BlockConfigurationDto implements BaseDto {
    
    @JsonProperty("enabled")
    private Boolean enabled = true;
    
    @JsonProperty("visible")
    private Boolean visible = true;
    
    @JsonProperty("editable")
    private Boolean editable = true;
    
    @JsonProperty("priority")
    private Integer priority = 0;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("tags")
    private Map<String, String> tags;
    
    @JsonProperty("properties")
    private Map<String, Object> properties;
    
    private Map<String, Object> metadata = new HashMap<>();
    
    public BlockConfigurationDto() {}
    
    // Builder pattern
    public static class Builder {
        private final BlockConfigurationDto dto = new BlockConfigurationDto();
        
        public Builder enabled(Boolean enabled) {
            dto.enabled = enabled;
            return this;
        }
        
        public Builder visible(Boolean visible) {
            dto.visible = visible;
            return this;
        }
        
        public Builder editable(Boolean editable) {
            dto.editable = editable;
            return this;
        }
        
        public Builder priority(Integer priority) {
            dto.priority = priority;
            return this;
        }
        
        public Builder category(String category) {
            dto.category = category;
            return this;
        }
        
        public Builder tags(Map<String, String> tags) {
            dto.tags = tags;
            return this;
        }
        
        public Builder addTag(String key, String value) {
            if (dto.tags == null) {
                dto.tags = new HashMap<>();
            }
            dto.tags.put(key, value);
            return this;
        }
        
        public Builder properties(Map<String, Object> properties) {
            dto.properties = properties;
            return this;
        }
        
        public Builder addProperty(String key, Object value) {
            if (dto.properties == null) {
                dto.properties = new HashMap<>();
            }
            dto.properties.put(key, value);
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            dto.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            return this;
        }
        
        public BlockConfigurationDto build() {
            return dto;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Convenience methods
    public boolean isEnabled() {
        return enabled != null ? enabled : true;
    }
    
    public boolean isVisible() {
        return visible != null ? visible : true;
    }
    
    public boolean isEditable() {
        return editable != null ? editable : true;
    }
    
    public int getPriorityValue() {
        return priority != null ? priority : 0;
    }
    
    public String getTag(String key) {
        return tags != null ? tags.get(key) : null;
    }
    
    public boolean hasTag(String key) {
        return tags != null && tags.containsKey(key);
    }
    
    public Object getProperty(String key) {
        return properties != null ? properties.get(key) : null;
    }
    
    public boolean hasProperty(String key) {
        return properties != null && properties.containsKey(key);
    }
    
    public void addTag(String key, String value) {
        if (tags == null) {
            tags = new HashMap<>();
        }
        tags.put(key, value);
    }
    
    public void addProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
    }
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Priority should be within reasonable bounds
        if (priority != null && (priority < -1000 || priority > 1000)) {
            result.addError("priority", "Priority must be between -1000 and 1000");
        }
        
        // Category should be valid if specified
        if (category != null && !category.trim().isEmpty()) {
            if (!category.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                result.addError("category", "Category must start with a letter and contain only alphanumeric characters and underscores");
            }
        }
        
        // Validate tag keys and values
        if (tags != null) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                if (key == null || key.trim().isEmpty()) {
                    result.addError("tags", "Tag key cannot be null or empty");
                    continue;
                }
                
                if (!key.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                    result.addError("tags." + key, "Tag key must start with a letter and contain only alphanumeric characters and underscores");
                }
                
                if (value != null && value.length() > 1000) {
                    result.addError("tags." + key, "Tag value cannot exceed 1000 characters");
                }
            }
        }
        
        // Validate property keys
        if (properties != null) {
            for (String key : properties.keySet()) {
                if (key == null || key.trim().isEmpty()) {
                    result.addError("properties", "Property key cannot be null or empty");
                    continue;
                }
                
                if (!key.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                    result.addError("properties." + key, "Property key must start with a letter and contain only alphanumeric characters and underscores");
                }
            }
        }
        
        return result;
    }
    
    @Override
    public String getDtoType() {
        return "BlockConfiguration";
    }
    
    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
    
    @Override
    public String toString() {
        return String.format("BlockConfigurationDto{enabled=%s, visible=%s, editable=%s, priority=%d, category='%s', tagCount=%d, propertyCount=%d, valid=%s}", 
                           enabled, visible, editable, getPriorityValue(), category, 
                           tags != null ? tags.size() : 0, 
                           properties != null ? properties.size() : 0, 
                           isValid());
    }
}