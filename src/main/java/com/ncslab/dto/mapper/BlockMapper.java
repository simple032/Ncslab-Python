package com.ncslab.dto.mapper;

import com.ncslab.dto.core.BaseDto;
import com.ncslab.entity.Entity;
import com.ncslab.dto.mapper.exception.MappingException;
import com.ncslab.dto.mapper.validation.MappingValidationResult;

import java.util.List;
import java.util.Set;

/**
 * Generic interface for mapping between DTOs and Entities.
 * Provides comprehensive mapping operations with validation and error handling.
 * 
 * @param <D> DTO type extending BaseDto
 * @param <E> Entity type extending Entity
 */
public interface BlockMapper<D extends BaseDto, E extends Entity> {
    
    /**
     * Convert DTO to Entity
     * @param dto The DTO to convert
     * @return The converted entity
     * @throws MappingException if conversion fails
     */
    E toEntity(D dto) throws MappingException;
    
    /**
     * Convert Entity to DTO
     * @param entity The entity to convert
     * @return The converted DTO
     * @throws MappingException if conversion fails
     */
    D toDto(E entity) throws MappingException;
    
    /**
     * Update existing entity with DTO data
     * @param entity The entity to update
     * @param dto The DTO with new data
     * @throws MappingException if update fails
     */
    void updateEntity(E entity, D dto) throws MappingException;
    
    /**
     * Partial update - only updates specified fields
     * @param entity The entity to update
     * @param dto The DTO with new data
     * @param fieldNames The fields to update
     * @throws MappingException if update fails
     */
    void partialUpdateEntity(E entity, D dto, Set<String> fieldNames) throws MappingException;
    
    /**
     * Update existing DTO with entity data
     * @param dto The DTO to update
     * @param entity The entity with new data
     * @throws MappingException if update fails
     */
    void updateDto(D dto, E entity) throws MappingException;
    
    /**
     * Convert list of DTOs to entities
     * @param dtos List of DTOs
     * @return List of converted entities
     * @throws MappingException if any conversion fails
     */
    List<E> toEntityList(List<D> dtos) throws MappingException;
    
    /**
     * Convert list of entities to DTOs
     * @param entities List of entities
     * @return List of converted DTOs
     * @throws MappingException if any conversion fails
     */
    List<D> toDtoList(List<E> entities) throws MappingException;
    
    /**
     * Get the DTO class this mapper handles
     * @return DTO class
     */
    Class<D> getDtoClass();
    
    /**
     * Get the Entity class this mapper handles
     * @return Entity class
     */
    Class<E> getEntityClass();
    
    /**
     * Validate that a DTO can be mapped to an entity
     * @param dto The DTO to validate
     * @return Validation result with errors and warnings
     */
    MappingValidationResult validateForMapping(D dto);
    
    /**
     * Validate that an entity can be mapped to a DTO
     * @param entity The entity to validate
     * @return Validation result with errors and warnings
     */
    MappingValidationResult validateForReverseMapping(E entity);
    
    /**
     * Check if this mapper can handle the given DTO type
     * @param dto The DTO to check
     * @return true if this mapper can handle the DTO, false otherwise
     */
    default boolean canHandle(BaseDto dto) {
        return dto != null && getDtoClass().isAssignableFrom(dto.getClass());
    }
    
    /**
     * Check if this mapper can handle the given entity type
     * @param entity The entity to check
     * @return true if this mapper can handle the entity, false otherwise
     */
    default boolean canHandle(Entity entity) {
        return entity != null && getEntityClass().isAssignableFrom(entity.getClass());
    }
    
    /**
     * Get the mapper identifier/name
     * @return Unique identifier for this mapper
     */
    default String getMapperId() {
        return getClass().getSimpleName();
    }
    
    /**
     * Get supported mapping operations for this mapper
     * @return Set of supported operations
     */
    default Set<String> getSupportedOperations() {
        return Set.of("TO_ENTITY", "TO_DTO", "UPDATE_ENTITY", "UPDATE_DTO", "PARTIAL_UPDATE", "BATCH_CONVERSION");
    }
}