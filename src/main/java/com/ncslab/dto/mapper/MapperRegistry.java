package com.ncslab.dto.mapper;

import com.ncslab.dto.core.BaseDto;
import com.ncslab.entity.Entity;
import com.ncslab.dto.mapper.exception.MappingException;
import com.ncslab.dto.mapper.exception.MapperNotFoundException;
import com.ncslab.dto.mapper.exception.BatchMappingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * Centralized registry for managing all BlockMapper instances in the system.
 * Provides factory methods for entity/DTO creation and batch operations.
 */
public class MapperRegistry {
    
    private final Map<Class<?>, BlockMapper<?, ?>> mappersByDtoClass = new ConcurrentHashMap<>();
    private final Map<Class<?>, BlockMapper<?, ?>> mappersByEntityClass = new ConcurrentHashMap<>();
    private final Map<String, BlockMapper<?, ?>> mappersByName = new ConcurrentHashMap<>();
    
    private final Logger logger = LoggerFactory.getLogger(MapperRegistry.class);
    
    /**
     * Constructor that auto-registers all available mappers
     */
    public MapperRegistry(List<BlockMapper<?, ?>> mappers) {
        registerMappers(mappers);
    }
    
    /**
     * Default constructor for testing
     */
    public MapperRegistry() {}
    
    /**
     * Register multiple mappers at once
     */
    private void registerMappers(List<BlockMapper<?, ?>> mappers) {
        if (mappers == null || mappers.isEmpty()) {
            logger.warn("No mappers found for registration");
            return;
        }
        
        for (BlockMapper<?, ?> mapper : mappers) {
            try {
                registerMapper(mapper);
            } catch (Exception e) {
                logger.error("Failed to register mapper: {}", mapper.getClass().getSimpleName(), e);
            }
        }
        
        logger.info("Registered {} mappers", mappers.size());
    }
    
    /**
     * Register a single mapper
     */
    public void registerMapper(BlockMapper<?, ?> mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("Mapper cannot be null");
        }
        
        Class<?> dtoClass = mapper.getDtoClass();
        Class<?> entityClass = mapper.getEntityClass();
        String mapperName = mapper.getMapperId();
        
        if (dtoClass == null || entityClass == null) {
            throw new IllegalArgumentException("Mapper must specify both DTO and Entity classes");
        }
        
        mappersByDtoClass.put(dtoClass, mapper);
        mappersByEntityClass.put(entityClass, mapper);
        mappersByName.put(mapperName, mapper);
        
        logger.debug("Registered mapper: {} for DTO: {} and Entity: {}", 
                    mapperName, dtoClass.getSimpleName(), entityClass.getSimpleName());
    }
    
    /**
     * Unregister a mapper
     */
    public void unregisterMapper(BlockMapper<?, ?> mapper) {
        if (mapper == null) {
            return;
        }
        
        Class<?> dtoClass = mapper.getDtoClass();
        Class<?> entityClass = mapper.getEntityClass();
        String mapperName = mapper.getMapperId();
        
        mappersByDtoClass.remove(dtoClass);
        mappersByEntityClass.remove(entityClass);
        mappersByName.remove(mapperName);
        
        logger.debug("Unregistered mapper: {}", mapperName);
    }
    
    /**
     * Get mapper by DTO class
     */
    @SuppressWarnings("unchecked")
    public <D extends BaseDto, E extends Entity> BlockMapper<D, E> getMapperForDto(Class<D> dtoClass) {
        BlockMapper<?, ?> mapper = mappersByDtoClass.get(dtoClass);
        if (mapper == null) {
            // Try to find by superclass or interface
            mapper = findMapperByDtoHierarchy(dtoClass);
        }
        
        if (mapper == null) {
            throw new MapperNotFoundException("No mapper found for DTO class: " + dtoClass.getName());
        }
        
        return (BlockMapper<D, E>) mapper;
    }
    
    /**
     * Get mapper by Entity class
     */
    @SuppressWarnings("unchecked")
    public <D extends BaseDto, E extends Entity> BlockMapper<D, E> getMapperForEntity(Class<E> entityClass) {
        BlockMapper<?, ?> mapper = mappersByEntityClass.get(entityClass);
        if (mapper == null) {
            // Try to find by superclass or interface
            mapper = findMapperByEntityHierarchy(entityClass);
        }
        
        if (mapper == null) {
            throw new MapperNotFoundException("No mapper found for Entity class: " + entityClass.getName());
        }
        
        return (BlockMapper<D, E>) mapper;
    }
    
    /**
     * Get mapper by name
     */
    @SuppressWarnings("unchecked")
    public <D extends BaseDto, E extends Entity> BlockMapper<D, E> getMapperByName(String mapperName) {
        BlockMapper<?, ?> mapper = mappersByName.get(mapperName);
        if (mapper == null) {
            throw new MapperNotFoundException("No mapper found with name: " + mapperName);
        }
        return (BlockMapper<D, E>) mapper;
    }
    
    /**
     * Check if mapper exists for DTO class
     */
    public boolean hasMapperForDto(Class<?> dtoClass) {
        return mappersByDtoClass.containsKey(dtoClass) || findMapperByDtoHierarchy(dtoClass) != null;
    }
    
    /**
     * Check if mapper exists for Entity class
     */
    public boolean hasMapperForEntity(Class<?> entityClass) {
        return mappersByEntityClass.containsKey(entityClass) || findMapperByEntityHierarchy(entityClass) != null;
    }
    
    /**
     * Get all registered DTO classes
     */
    public Set<Class<?>> getRegisteredDtoClasses() {
        return Collections.unmodifiableSet(mappersByDtoClass.keySet());
    }
    
    /**
     * Get all registered Entity classes
     */
    public Set<Class<?>> getRegisteredEntityClasses() {
        return Collections.unmodifiableSet(mappersByEntityClass.keySet());
    }
    
    /**
     * Get all registered mapper names
     */
    public Set<String> getRegisteredMapperNames() {
        return Collections.unmodifiableSet(mappersByName.keySet());
    }
    
    /**
     * Factory method for creating entities from DTOs
     */
    @SuppressWarnings("unchecked")
    public <E extends Entity> E createEntityFromDto(BaseDto dto) throws MappingException {
        if (dto == null) {
            throw new MappingException("Cannot create entity from null DTO");
        }
        
        BlockMapper<BaseDto, E> mapper = (BlockMapper<BaseDto, E>) getMapperForDto(dto.getClass());
        return mapper.toEntity(dto);
    }
    
    /**
     * Factory method for creating DTOs from entities
     */
    @SuppressWarnings("unchecked")
    public <D extends BaseDto> D createDtoFromEntity(Entity entity) throws MappingException {
        if (entity == null) {
            throw new MappingException("Cannot create DTO from null entity");
        }
        
        BlockMapper<D, Entity> mapper = (BlockMapper<D, Entity>) getMapperForEntity(entity.getClass());
        return mapper.toDto(entity);
    }
    
    /**
     * Update entity from DTO
     */
    @SuppressWarnings("unchecked")
    public <E extends Entity> void updateEntityFromDto(E entity, BaseDto dto) throws MappingException {
        if (entity == null) {
            throw new MappingException("Cannot update null entity");
        }
        if (dto == null) {
            throw new MappingException("Cannot update from null DTO");
        }
        
        BlockMapper<BaseDto, E> mapper = (BlockMapper<BaseDto, E>) getMapperForDto(dto.getClass());
        mapper.updateEntity(entity, dto);
    }
    
    /**
     * Update DTO from entity
     */
    @SuppressWarnings("unchecked")
    public <D extends BaseDto> void updateDtoFromEntity(D dto, Entity entity) throws MappingException {
        if (dto == null) {
            throw new MappingException("Cannot update null DTO");
        }
        if (entity == null) {
            throw new MappingException("Cannot update from null entity");
        }
        
        BlockMapper<D, Entity> mapper = (BlockMapper<D, Entity>) getMapperForEntity(entity.getClass());
        mapper.updateDto(dto, entity);
    }
    
    /**
     * Batch operation: create entities from DTOs
     */
    public <E extends Entity> List<E> createEntitiesFromDtos(List<? extends BaseDto> dtos) throws MappingException {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<E> entities = new ArrayList<>();
        List<MappingException> errors = new ArrayList<>();
        
        for (int i = 0; i < dtos.size(); i++) {
            BaseDto dto = dtos.get(i);
            try {
                E entity = createEntityFromDto(dto);
                entities.add(entity);
            } catch (MappingException e) {
                logger.error("Failed to create entity from DTO at index {}: {}", i, dto, e);
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
    
    /**
     * Batch operation: create DTOs from entities
     */
    public <D extends BaseDto> List<D> createDtosFromEntities(List<? extends Entity> entities) throws MappingException {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<D> dtos = new ArrayList<>();
        List<MappingException> errors = new ArrayList<>();
        
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            try {
                D dto = createDtoFromEntity(entity);
                dtos.add(dto);
            } catch (MappingException e) {
                logger.error("Failed to create DTO from entity at index {}: {}", i, entity, e);
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
    
    /**
     * Get registry statistics
     */
    public RegistryStatistics getStatistics() {
        return new RegistryStatistics(
            mappersByDtoClass.size(),
            mappersByEntityClass.size(),
            mappersByName.size()
        );
    }
    
    /**
     * Clear all registered mappers
     */
    public void clear() {
        mappersByDtoClass.clear();
        mappersByEntityClass.clear();
        mappersByName.clear();
        logger.info("Cleared all registered mappers");
    }
    
    /**
     * Find mapper by DTO class hierarchy (check superclasses and interfaces)
     */
    private BlockMapper<?, ?> findMapperByDtoHierarchy(Class<?> dtoClass) {
        // Check superclasses
        Class<?> currentClass = dtoClass.getSuperclass();
        while (currentClass != null && !currentClass.equals(Object.class)) {
            BlockMapper<?, ?> mapper = mappersByDtoClass.get(currentClass);
            if (mapper != null) {
                logger.debug("Found mapper for DTO superclass: {} -> {}", dtoClass.getSimpleName(), currentClass.getSimpleName());
                return mapper;
            }
            currentClass = currentClass.getSuperclass();
        }
        
        // Check interfaces
        for (Class<?> interfaceClass : dtoClass.getInterfaces()) {
            BlockMapper<?, ?> mapper = mappersByDtoClass.get(interfaceClass);
            if (mapper != null) {
                logger.debug("Found mapper for DTO interface: {} -> {}", dtoClass.getSimpleName(), interfaceClass.getSimpleName());
                return mapper;
            }
        }
        
        return null;
    }
    
    /**
     * Find mapper by Entity class hierarchy (check superclasses and interfaces)
     */
    private BlockMapper<?, ?> findMapperByEntityHierarchy(Class<?> entityClass) {
        // Check superclasses
        Class<?> currentClass = entityClass.getSuperclass();
        while (currentClass != null && !currentClass.equals(Object.class)) {
            BlockMapper<?, ?> mapper = mappersByEntityClass.get(currentClass);
            if (mapper != null) {
                logger.debug("Found mapper for Entity superclass: {} -> {}", entityClass.getSimpleName(), currentClass.getSimpleName());
                return mapper;
            }
            currentClass = currentClass.getSuperclass();
        }
        
        // Check interfaces
        for (Class<?> interfaceClass : entityClass.getInterfaces()) {
            BlockMapper<?, ?> mapper = mappersByEntityClass.get(interfaceClass);
            if (mapper != null) {
                logger.debug("Found mapper for Entity interface: {} -> {}", entityClass.getSimpleName(), interfaceClass.getSimpleName());
                return mapper;
            }
        }
        
        return null;
    }
    
    /**
     * Registry statistics holder
     */
    public static class RegistryStatistics {
        private final int dtoMapperCount;
        private final int entityMapperCount;
        private final int namedMapperCount;
        
        public RegistryStatistics(int dtoMapperCount, int entityMapperCount, int namedMapperCount) {
            this.dtoMapperCount = dtoMapperCount;
            this.entityMapperCount = entityMapperCount;
            this.namedMapperCount = namedMapperCount;
        }
        
        public int getDtoMapperCount() { return dtoMapperCount; }
        public int getEntityMapperCount() { return entityMapperCount; }
        public int getNamedMapperCount() { return namedMapperCount; }
        
        @Override
        public String toString() {
            return String.format("RegistryStatistics{dtoMappers=%d, entityMappers=%d, namedMappers=%d}", 
                               dtoMapperCount, entityMapperCount, namedMapperCount);
        }
    }
}