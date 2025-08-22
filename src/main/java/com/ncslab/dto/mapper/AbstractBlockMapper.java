package com.ncslab.dto.mapper;

import com.ncslab.dto.core.BaseDto;
import com.ncslab.entity.Entity;
import com.ncslab.dto.mapper.exception.MappingException;
import com.ncslab.dto.mapper.exception.BatchMappingException;
import com.ncslab.dto.mapper.validation.MappingValidationResult;
import com.ncslab.dto.mapper.validation.ValidationError;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.block.BlockParametersDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Abstract base class for all block mappers in the NCSLabLink system.
 * Provides common functionality for mapping between DTOs and Entities with
 * comprehensive error handling, validation, and context management.
 * 
 * @param <D> DTO type extending BaseDto
 * @param <E> Entity type extending Entity
 */
public abstract class AbstractBlockMapper<D extends BaseDto, E extends Entity> 
        implements BlockMapper<D, E> {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected MappingContext mappingContext;
    protected ValidationService validationService;
    
    /**
     * Default constructor for Spring dependency injection
     */
    protected AbstractBlockMapper() {}
    
    /**
     * Constructor with explicit dependencies (for testing)
     */
    protected AbstractBlockMapper(MappingContext mappingContext, ValidationService validationService) {
        this.mappingContext = mappingContext;
        this.validationService = validationService;
    }
    
    @Override
    public List<E> toEntityList(List<D> dtos) throws MappingException {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<E> entities = new ArrayList<>();
        List<MappingException> errors = new ArrayList<>();
        
        for (int i = 0; i < dtos.size(); i++) {
            D dto = dtos.get(i);
            try {
                E entity = toEntity(dto);
                entities.add(entity);
            } catch (MappingException e) {
                logger.error("Failed to map DTO to entity at index {}: {}", i, dto, e);
                errors.add(e);
            }
        }
        
        if (!errors.isEmpty()) {
            BatchMappingException batchException = new BatchMappingException(
                String.format("Failed to convert %d of %d DTOs to entities", errors.size(), dtos.size()));
            errors.forEach(batchException::addMappingException);
            throw batchException;
        }
        
        return entities;
    }
    
    @Override
    public List<D> toDtoList(List<E> entities) throws MappingException {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<D> dtos = new ArrayList<>();
        List<MappingException> errors = new ArrayList<>();
        
        for (int i = 0; i < entities.size(); i++) {
            E entity = entities.get(i);
            try {
                D dto = toDto(entity);
                dtos.add(dto);
            } catch (MappingException e) {
                logger.error("Failed to map entity to DTO at index {}: {}", i, entity, e);
                errors.add(e);
            }
        }
        
        if (!errors.isEmpty()) {
            BatchMappingException batchException = new BatchMappingException(
                String.format("Failed to convert %d of %d entities to DTOs", errors.size(), entities.size()));
            errors.forEach(batchException::addMappingException);
            throw batchException;
        }
        
        return dtos;
    }
    
    @Override
    public void partialUpdateEntity(E entity, D dto, Set<String> fieldNames) throws MappingException {
        if (entity == null) {
            throw new MappingException("Cannot update null entity");
        }
        if (dto == null) {
            throw new MappingException("Cannot update from null DTO");
        }
        if (fieldNames == null || fieldNames.isEmpty()) {
            logger.warn("No field names specified for partial update, performing full update");
            updateEntity(entity, dto);
            return;
        }
        
        try {
            // Create a partial DTO with only specified fields
            D partialDto = createPartialDto(dto, fieldNames);
            updateEntity(entity, partialDto);
        } catch (Exception e) {
            throw new MappingException("Failed to perform partial update", e);
        }
    }
    
    @Override
    public MappingValidationResult validateForMapping(D dto) {
        MappingValidationResult result = new MappingValidationResult();
        
        if (dto == null) {
            result.addError("dto", "DTO cannot be null");
            return result;
        }
        
        // Basic DTO validation
        try {
            var dtoValidation = dto.validate();
            if (!dtoValidation.isValid()) {
                result.addErrors(dtoValidation.getErrors());
            }
        } catch (Exception e) {
            result.addError("dto", "DTO validation failed: " + e.getMessage());
        }
        
        // Mapping-specific validation
        try {
            validateMappingConstraints(dto, result);
        } catch (Exception e) {
            result.addError("mapping", "Mapping validation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public MappingValidationResult validateForReverseMapping(E entity) {
        MappingValidationResult result = new MappingValidationResult();
        
        if (entity == null) {
            result.addError("entity", "Entity cannot be null");
            return result;
        }
        
        // Entity-specific validation
        try {
            validateReverseMappingConstraints(entity, result);
        } catch (Exception e) {
            result.addError("reverseMapping", "Reverse mapping validation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Create a partial DTO containing only specified fields from the original DTO.
     * Subclasses must implement this method to handle field-specific copying.
     * 
     * @param originalDto The source DTO
     * @param fieldNames The fields to include in the partial DTO
     * @return A new DTO containing only the specified fields
     */
    protected abstract D createPartialDto(D originalDto, Set<String> fieldNames);
    
    /**
     * Validate mapping-specific constraints for DTO to Entity conversion.
     * Subclasses should override this method to add specific validation logic.
     * 
     * @param dto The DTO to validate
     * @param result The validation result to populate with errors/warnings
     */
    protected abstract void validateMappingConstraints(D dto, MappingValidationResult result);
    
    /**
     * Validate reverse mapping constraints for Entity to DTO conversion.
     * Subclasses can override this method to add specific validation logic.
     * 
     * @param entity The entity to validate
     * @param result The validation result to populate with errors/warnings
     */
    protected void validateReverseMappingConstraints(E entity, MappingValidationResult result) {
        // Default implementation - subclasses can override
        if (entity.getId() == null || entity.getId().trim().isEmpty()) {
            result.addWarning("entity.id", "Entity has no ID - may not be persisted");
        }
    }
    
    /**
     * Helper method to safely copy properties between objects with error handling.
     * 
     * @param source Supplier for the source value
     * @param target Consumer for the target value
     * @param propertyName Name of the property being copied (for error reporting)
     * @param result Validation result to add errors to
     */
    protected <T> void safePropertyCopy(Supplier<T> source, Consumer<T> target, 
                                      String propertyName, MappingValidationResult result) {
        try {
            T value = source.get();
            if (value != null) {
                target.accept(value);
            }
        } catch (Exception e) {
            result.addError(propertyName, "Failed to copy property: " + e.getMessage());
            logger.warn("Property copy failed for {}: {}", propertyName, e.getMessage());
        }
    }
    
    /**
     * Helper method to map parameters from DTO to entity with type safety.
     * 
     * @param parametersDto The DTO containing parameters
     * @return TypedParameterMap for the entity
     * @throws MappingException if mapping fails
     */
    protected TypedParameterMap mapParameters(BlockParametersDto parametersDto) throws MappingException {
        if (parametersDto == null) {
            return new TypedParameterMap();
        }
        
        TypedParameterMap parameterMap = new TypedParameterMap();
        
        try {
            for (Map.Entry<String, TypedParameter> entry : parametersDto.getParameters().entrySet()) {
                String paramName = entry.getKey();
                TypedParameter paramValue = entry.getValue();
                
                // Validate parameter before adding
                if (paramValue == null) {
                    logger.warn("Null parameter value for: {}", paramName);
                    continue;
                }
                
                parameterMap.put(paramName, paramValue);
            }
            
            return parameterMap;
        } catch (Exception e) {
            throw new MappingException("Failed to map parameters from DTO", e);
        }
    }
    
    /**
     * Helper method to map parameters from entity to DTO.
     * 
     * @param parameterMap The entity parameter map
     * @return BlockParametersDto for the DTO
     * @throws MappingException if mapping fails
     */
    protected BlockParametersDto mapParametersToDto(TypedParameterMap parameterMap) throws MappingException {
        if (parameterMap == null || parameterMap.isEmpty()) {
            return new BlockParametersDto();
        }
        
        BlockParametersDto parametersDto = new BlockParametersDto();
        
        try {
            for (Map.Entry<String, TypedParameter> entry : parameterMap.entrySet()) {
                String paramName = entry.getKey();
                TypedParameter paramValue = entry.getValue();
                
                if (paramValue != null) {
                    parametersDto.getParameters().put(paramName, paramValue);
                }
            }
            
            return parametersDto;
        } catch (Exception e) {
            throw new MappingException("Failed to map parameters to DTO", e);
        }
    }
    
    /**
     * Helper method to validate required fields in a DTO.
     * 
     * @param dto The DTO to validate
     * @param requiredFields List of required field names
     * @param result Validation result to populate
     */
    protected void validateRequiredFields(D dto, List<String> requiredFields, MappingValidationResult result) {
        if (requiredFields == null || requiredFields.isEmpty()) {
            return;
        }
        
        try {
            Class<?> dtoClass = dto.getClass();
            for (String fieldName : requiredFields) {
                try {
                    java.lang.reflect.Field field = dtoClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(dto);
                    
                    if (value == null) {
                        result.addError(fieldName, "Required field is null: " + fieldName);
                    } else if (value instanceof String && ((String) value).trim().isEmpty()) {
                        result.addError(fieldName, "Required string field is empty: " + fieldName);
                    }
                } catch (NoSuchFieldException e) {
                    result.addWarning(fieldName, "Required field not found in DTO: " + fieldName);
                } catch (IllegalAccessException e) {
                    result.addError(fieldName, "Cannot access required field: " + fieldName);
                }
            }
        } catch (Exception e) {
            result.addError("validation", "Failed to validate required fields: " + e.getMessage());
        }
    }
    
    /**
     * Log mapping operation for debugging purposes.
     * 
     * @param operation The operation being performed
     * @param source The source object
     * @param target The target object
     */
    protected void logMappingOperation(String operation, Object source, Object target) {
        if (logger.isDebugEnabled()) {
            String sourceType = source != null ? source.getClass().getSimpleName() : "null";
            String targetType = target != null ? target.getClass().getSimpleName() : "null";
            logger.debug("Mapping operation: {} from {} to {}", operation, sourceType, targetType);
        }
    }
    
    /**
     * Get the current mapping context information for debugging.
     * 
     * @return String representation of current context
     */
    protected String getCurrentContext() {
        if (mappingContext != null) {
            return mappingContext.getContextInfo();
        }
        return "No mapping context available";
    }
}