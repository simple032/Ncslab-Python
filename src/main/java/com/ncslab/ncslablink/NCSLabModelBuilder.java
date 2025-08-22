package com.ncslab.ncslablink;

import com.ncslab.dto.core.ModelDto;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.util.JsonUtils;
import org.json.JSONObject;

/**
 * Builder pattern implementation for NCSLabModel construction
 * Provides a clean, flexible way to create NCSLabModel instances with validation
 */
public class NCSLabModelBuilder {
    
    private String jsonString;
    private JSONObject jsonObject;
    private ModelDto modelDto;
    private ModelMode mode;
    private boolean validateStructure = true;
    private boolean enableFallback = false;
    
    /**
     * Private constructor - use static factory methods
     */
    private NCSLabModelBuilder() {
    }
    
    /**
     * Create builder from JSON string
     */
    public static NCSLabModelBuilder fromJsonString(String jsonString) {
        NCSLabModelBuilder builder = new NCSLabModelBuilder();
        builder.jsonString = jsonString;
        return builder;
    }
    
    /**
     * Create builder from JSONObject
     */
    public static NCSLabModelBuilder fromJsonObject(JSONObject jsonObject) {
        NCSLabModelBuilder builder = new NCSLabModelBuilder();
        builder.jsonObject = jsonObject;
        return builder;
    }
    
    /**
     * Create builder from ModelDto
     */
    public static NCSLabModelBuilder fromDto(ModelDto modelDto) {
        NCSLabModelBuilder builder = new NCSLabModelBuilder();
        builder.modelDto = modelDto;
        return builder;
    }
    
    /**
     * Set the model mode
     */
    public NCSLabModelBuilder withMode(ModelMode mode) {
        this.mode = mode;
        return this;
    }
    
    /**
     * Enable or disable JSON structure validation
     */
    public NCSLabModelBuilder withValidation(boolean validateStructure) {
        this.validateStructure = validateStructure;
        return this;
    }
    
    /**
     * Enable fallback to JSONObject parsing if DTO parsing fails
     */
    public NCSLabModelBuilder withFallback(boolean enableFallback) {
        this.enableFallback = enableFallback;
        return this;
    }
    
    /**
     * Build the NCSLabModel instance
     * @param modelClass The specific model class to instantiate (SimulationModel, etc.)
     * @return Configured NCSLabModel instance
     * @throws ModelException if construction fails
     */
    public <T extends NCSLabModel> T build(Class<T> modelClass) throws ModelException {
        validateBuildParameters();
        
        try {
            // Prepare the final DTO for construction
            ModelDto finalDto = prepareDto();
            
            // Create the model instance using reflection
            return createModelInstance(modelClass, finalDto);
            
        } catch (Exception e) {
            throw new ModelException("Failed to build NCSLabModel: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build a generic NCSLabModel (for backward compatibility)
     */
    public NCSLabModel build() throws ModelException {
        return build(NCSLabModel.class);
    }
    
    /**
     * Validate build parameters
     */
    private void validateBuildParameters() throws ModelException {
        if (mode == null) {
            throw new ModelException("ModelMode must be specified");
        }
        
        if (jsonString == null && jsonObject == null && modelDto == null) {
            throw new ModelException("No input data provided - must specify JSON string, JSONObject, or ModelDto");
        }
        
        // Ensure only one input type is provided
        int inputCount = 0;
        if (jsonString != null) inputCount++;
        if (jsonObject != null) inputCount++;
        if (modelDto != null) inputCount++;
        
        if (inputCount > 1) {
            throw new ModelException("Only one input type should be provided");
        }
    }
    
    /**
     * Prepare the final DTO for construction
     */
    private ModelDto prepareDto() throws ModelException {
        if (modelDto != null) {
            // Direct DTO provided
            return validateDto(modelDto);
        }
        
        if (jsonString != null) {
            // Convert JSON string to DTO
            return convertJsonStringToDto();
        }
        
        if (jsonObject != null) {
            // Convert JSONObject to DTO
            return convertJsonObjectToDto();
        }
        
        throw new ModelException("No valid input data available");
    }
    
    /**
     * Convert JSON string to DTO with validation and fallback
     */
    private ModelDto convertJsonStringToDto() throws ModelException {
        try {
            // Validate JSON structure if requested
            if (validateStructure) {
                String validationError = JsonUtils.validateJsonStructure(jsonString);
                if (validationError != null) {
                    throw new ModelException("Invalid JSON structure: " + validationError);
                }
            }
            
            // Primary: Use Jackson DTO parsing
            ModelDto dto = JsonUtils.parseModelDto(jsonString);
            if (dto != null && dto.isValid()) {
                System.out.println("Builder: Successfully parsed JSON string to DTO");
                return dto;
            }
            
            // Fallback to JSONObject if enabled
            if (enableFallback) {
                System.out.println("Builder: DTO parsing failed, attempting JSONObject fallback");
                JSONObject jsonObj = new JSONObject(jsonString);
                return convertJsonObjectToDto(jsonObj);
            }
            
            String error = (dto != null) ? dto.getValidationError() : "Failed to parse JSON to DTO";
            throw new ModelException("DTO parsing failed: " + error);
            
        } catch (Exception e) {
            if (e instanceof ModelException) {
                throw e;
            }
            throw new ModelException("Failed to convert JSON string to DTO: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convert JSONObject to DTO
     */
    private ModelDto convertJsonObjectToDto() throws ModelException {
        return convertJsonObjectToDto(this.jsonObject);
    }
    
    /**
     * Convert JSONObject to DTO (helper method)
     */
    private ModelDto convertJsonObjectToDto(JSONObject jsonObj) throws ModelException {
        try {
            // For now, convert JSONObject to string and parse with Jackson
            // This could be optimized in the future with direct JSONObject to DTO mapping
            String jsonString = jsonObj.toString();
            ModelDto dto = JsonUtils.parseModelDto(jsonString);
            
            if (dto != null && dto.isValid()) {
                System.out.println("Builder: Successfully converted JSONObject to DTO");
                return dto;
            }
            
            String error = (dto != null) ? dto.getValidationError() : "Failed to convert JSONObject to DTO";
            throw new ModelException("JSONObject to DTO conversion failed: " + error);
            
        } catch (Exception e) {
            if (e instanceof ModelException) {
                throw e;
            }
            throw new ModelException("Failed to convert JSONObject to DTO: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate DTO
     */
    private ModelDto validateDto(ModelDto dto) throws ModelException {
        if (dto == null) {
            throw new ModelException("ModelDto is null");
        }
        
        if (!dto.isValid()) {
            throw new ModelException("ModelDto validation failed: " + dto.getValidationError());
        }
        
        System.out.println("Builder: DTO validation passed for model: " + dto.getModelName());
        return dto;
    }
    
    /**
     * Create model instance using reflection
     */
    @SuppressWarnings("unchecked")
    private <T extends NCSLabModel> T createModelInstance(Class<T> modelClass, ModelDto dto) throws ModelException {
        try {
            // For now, we'll use a factory method approach instead of direct reflection
            // This is safer and more maintainable
            return (T) createModelFromDto(dto, mode);
            
        } catch (Exception e) {
            throw new ModelException("Failed to create model instance: " + e.getMessage(), e);
        }
    }
    
    /**
     * Factory method to create appropriate model type from DTO
     */
    private NCSLabModel createModelFromDto(ModelDto dto, ModelMode mode) throws ModelException {
        return NCSLabModelFactory.createModelAuto(dto, mode);
    }
}