package com.ncslab.dto.mapper;

import com.ncslab.dto.block.BlockCreationDto;
import com.ncslab.dto.block.BlockParametersDto;
import com.ncslab.dto.block.BlockPortDto;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.exception.MappingException;
import com.ncslab.dto.mapper.validation.MappingValidationResult;
import com.ncslab.block.Block;
import com.ncslab.block.BlockEntity;
import com.ncslab.block.BlockType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.ncslablink.NCSLabModel;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Mapper for converting between BlockCreationDto and Block entities.
 * Handles comprehensive mapping with validation, error handling, and type safety.
 */
public class BlockCreationMapper extends AbstractBlockMapper<BlockCreationDto, BlockEntity> {
    
    private final Logger logger = LoggerFactory.getLogger(BlockCreationMapper.class);
    
    private PortMapper portMapper;
    private ParameterMapper parameterMapper;
    
    // Default constructor for Spring
    public BlockCreationMapper() {}
    
    // Constructor for testing
    public BlockCreationMapper(MappingContext mappingContext, 
                             ValidationService validationService,
                             PortMapper portMapper,
                             ParameterMapper parameterMapper) {
        super(mappingContext, validationService);
        this.portMapper = portMapper;
        this.parameterMapper = parameterMapper;
    }
    
    @Override
    public BlockEntity toEntity(BlockCreationDto dto) throws MappingException {
        if (mappingContext != null) {
            mappingContext.setCurrentOperation("DTO_TO_ENTITY");
            mappingContext.setCurrentBlockType(dto.getBlockType());
        }
        
        try {
            // Validate DTO before mapping
            MappingValidationResult validation = validateForMapping(dto);
            if (!validation.isValid()) {
                throw new MappingException("DTO validation failed: " + validation.getErrors());
            }
            
            // Create Block using factory pattern
            Block block = createBlockFromDto(dto);
            
            // Wrap in BlockEntity
            BlockEntity blockEntity = BlockEntity.fromBlock(block);
            
            // Post-creation validation
            validateCreatedBlock(block, dto);
            
            logger.debug("Successfully mapped DTO to BlockEntity: {}", block.getBlockName());
            return blockEntity;
            
        } catch (Exception e) {
            logger.error("Failed to map BlockCreationDto to BlockEntity", e);
            throw new MappingException("Failed to map DTO to entity", "BlockCreationDto", "BlockEntity", null, dto, e);
        } finally {
            if (mappingContext != null) {
                mappingContext.clearContext();
            }
        }
    }
    
    @Override
    public BlockCreationDto toDto(BlockEntity entity) throws MappingException {
        if (mappingContext != null) {
            mappingContext.setCurrentOperation("ENTITY_TO_DTO");
            mappingContext.setCurrentBlockType(entity.getBlock().getBlockType());
        }
        
        try {
            Block block = entity.getBlock();
            
            BlockCreationDto dto = new BlockCreationDto();
            
            // Map basic properties
            dto.setBlockType(block.getBlockType());
            dto.setBlockName(block.getBlockName());
            dto.setBlockPath(block.getBlockPath());
            dto.setBlockUUID(block.getBlockUUID());
            
            // Map parameters
            if (block.getParamValues() != null) {
                TypedParameterMap parameterMap = parameterMapper.mapFromJsonObject(block.getParamValues());
                BlockParametersDto parametersDto = parameterMapper.mapToDto(parameterMap);
                dto.setParameters(parametersDto);
            }
            
            // Map input ports
            if (block.getInputPortList() != null && !block.getInputPortList().isEmpty()) {
                List<BlockPortDto> inputPortDtos = portMapper.mapInputPortsToDto(block.getInputPortList());
                dto.setInputPorts(inputPortDtos);
            }
            
            // Map output ports
            if (block.getOutputPortList() != null && !block.getOutputPortList().isEmpty()) {
                List<BlockPortDto> outputPortDtos = portMapper.mapOutputPortsToDto(block.getOutputPortList());
                dto.setOutputPorts(outputPortDtos);
            }
            
            // Map metadata
            if (entity.getMetadata() != null && !entity.getMetadata().isEmpty()) {
                Map<String, Object> metadata = new HashMap<>(entity.getMetadata());
                dto.setMetadata(metadata);
            }
            
            logger.debug("Successfully mapped BlockEntity to DTO: {}", dto.getBlockName());
            return dto;
            
        } catch (Exception e) {
            logger.error("Failed to map BlockEntity to BlockCreationDto", e);
            throw new MappingException("Failed to map entity to DTO", "BlockEntity", "BlockCreationDto", null, entity, e);
        } finally {
            if (mappingContext != null) {
                mappingContext.clearContext();
            }
        }
    }
    
    @Override
    public void updateEntity(BlockEntity entity, BlockCreationDto dto) throws MappingException {
        if (mappingContext != null) {
            mappingContext.setCurrentOperation("UPDATE_ENTITY");
            mappingContext.setCurrentBlockType(dto.getBlockType());
        }
        
        try {
            // Validate DTO
            MappingValidationResult validation = validateForMapping(dto);
            if (!validation.isValid()) {
                throw new MappingException("DTO validation failed for update: " + validation.getErrors());
            }
            
            Block block = entity.getBlock();
            
            // Ensure block types match
            if (!block.getBlockType().equals(dto.getBlockType())) {
                throw new MappingException("Cannot change block type during update");
            }
            
            // Update basic properties (limited update capability due to Block structure)
            // Most Block properties are final or protected, so we focus on parameters
            
            // Update parameters
            if (dto.getParameters() != null) {
                TypedParameterMap newParameters = parameterMapper.mapFromDto(dto.getParameters());
                JSONObject newParamValues = parameterMapper.mapToJsonObject(newParameters);
                
                // Note: Block.paramValues is protected, so we would need to use reflection
                // or add setter methods to Block class for full update support
                updateBlockParameters(block, newParamValues);
            }
            
            // Sync entity metadata
            entity.syncMetadata();
            
            logger.debug("Successfully updated BlockEntity from DTO: {}", entity.getBlock().getBlockName());
            
        } catch (Exception e) {
            logger.error("Failed to update BlockEntity from BlockCreationDto", e);
            throw new MappingException("Failed to update entity from DTO", "BlockCreationDto", "BlockEntity", null, dto, e);
        } finally {
            if (mappingContext != null) {
                mappingContext.clearContext();
            }
        }
    }
    
    @Override
    public void updateDto(BlockCreationDto dto, BlockEntity entity) throws MappingException {
        if (mappingContext != null) {
            mappingContext.setCurrentOperation("UPDATE_DTO");
            mappingContext.setCurrentBlockType(entity.getBlock().getBlockType());
        }
        
        try {
            Block block = entity.getBlock();
            
            // Update DTO properties from entity
            dto.setBlockType(block.getBlockType());
            dto.setBlockName(block.getBlockName());
            dto.setBlockPath(block.getBlockPath());
            dto.setBlockUUID(block.getBlockUUID());
            
            // Update parameters
            if (block.getParamValues() != null) {
                TypedParameterMap parameterMap = parameterMapper.mapFromJsonObject(block.getParamValues());
                BlockParametersDto parametersDto = parameterMapper.mapToDto(parameterMap);
                dto.setParameters(parametersDto);
            }
            
            // Update ports
            if (block.getInputPortList() != null && !block.getInputPortList().isEmpty()) {
                List<BlockPortDto> inputPortDtos = portMapper.mapInputPortsToDto(block.getInputPortList());
                dto.setInputPorts(inputPortDtos);
            }
            
            if (block.getOutputPortList() != null && !block.getOutputPortList().isEmpty()) {
                List<BlockPortDto> outputPortDtos = portMapper.mapOutputPortsToDto(block.getOutputPortList());
                dto.setOutputPorts(outputPortDtos);
            }
            
            // Update metadata
            if (entity.getMetadata() != null && !entity.getMetadata().isEmpty()) {
                Map<String, Object> metadata = new HashMap<>(entity.getMetadata());
                dto.setMetadata(metadata);
            }
            
            logger.debug("Successfully updated DTO from BlockEntity: {}", dto.getBlockName());
            
        } catch (Exception e) {
            logger.error("Failed to update DTO from BlockEntity", e);
            throw new MappingException("Failed to update DTO from entity", "BlockEntity", "BlockCreationDto", null, entity, e);
        } finally {
            if (mappingContext != null) {
                mappingContext.clearContext();
            }
        }
    }
    
    @Override
    protected BlockCreationDto createPartialDto(BlockCreationDto originalDto, Set<String> fieldNames) {
        BlockCreationDto partialDto = new BlockCreationDto();
        
        // Always include block type and name for identity
        partialDto.setBlockType(originalDto.getBlockType());
        partialDto.setBlockName(originalDto.getBlockName());
        
        for (String fieldName : fieldNames) {
            switch (fieldName) {
                case "blockPath":
                    partialDto.setBlockPath(originalDto.getBlockPath());
                    break;
                case "blockUUID":
                    partialDto.setBlockUUID(originalDto.getBlockUUID());
                    break;
                case "parameters":
                    partialDto.setParameters(originalDto.getParameters());
                    break;
                case "inputPorts":
                    partialDto.setInputPorts(originalDto.getInputPorts());
                    break;
                case "outputPorts":
                    partialDto.setOutputPorts(originalDto.getOutputPorts());
                    break;
                case "dimensions":
                    partialDto.setDimensions(originalDto.getDimensions());
                    break;
                case "position":
                    partialDto.setPosition(originalDto.getPosition());
                    break;
                case "configuration":
                    partialDto.setConfiguration(originalDto.getConfiguration());
                    break;
                case "metadata":
                    partialDto.setMetadata(originalDto.getMetadata());
                    break;
            }
        }
        
        return partialDto;
    }
    
    @Override
    protected void validateMappingConstraints(BlockCreationDto dto, MappingValidationResult result) {
        // Validate block type exists
        if (dto.getBlockType() == null || dto.getBlockType().trim().isEmpty()) {
            result.addError("blockType", "Block type is required");
            return;
        }
        
        // Validate block name
        if (dto.getBlockName() == null || dto.getBlockName().trim().isEmpty()) {
            result.addError("blockName", "Block name is required");
        }
        
        // Validate that BlockType can create this type of block
        try {
            if (!isValidBlockType(dto.getBlockType())) {
                result.addError("blockType", "Invalid or unsupported block type: " + dto.getBlockType());
            }
        } catch (Exception e) {
            result.addError("blockType", "Failed to validate block type: " + e.getMessage());
        }
        
        // Validate parameters if present
        if (dto.getParameters() != null) {
            try {
                var paramValidation = dto.getParameters().validate();
                if (!paramValidation.isValid()) {
                    // Convert ValidationResult to MappingValidationResult
                    for (var error : paramValidation.getErrors()) {
                        result.addError("parameters." + error.getField(), error.getMessage());
                    }
                    for (var warning : paramValidation.getWarnings()) {
                        result.addWarning("parameters." + warning.getField(), warning.getMessage());
                    }
                }
            } catch (Exception e) {
                result.addError("parameters", "Parameter validation failed: " + e.getMessage());
            }
        }
        
        // Validate ports if present
        if (dto.getInputPorts() != null) {
            validatePortList(dto.getInputPorts(), "inputPorts", result);
        }
        
        if (dto.getOutputPorts() != null) {
            validatePortList(dto.getOutputPorts(), "outputPorts", result);
        }
    }
    
    @Override
    public Class<BlockCreationDto> getDtoClass() {
        return BlockCreationDto.class;
    }
    
    @Override
    public Class<BlockEntity> getEntityClass() {
        return BlockEntity.class;
    }
    
    /**
     * Create a Block instance from DTO using the BlockType factory
     */
    private Block createBlockFromDto(BlockCreationDto dto) throws MappingException {
        try {
            // Create JSON object for Block constructor (for backward compatibility)
            JSONObject blockJson = new JSONObject();
            blockJson.put("blockType", dto.getBlockType());
            blockJson.put("blockName", dto.getBlockName());
            blockJson.put("blockPath", dto.getBlockPath() != null ? dto.getBlockPath() : "");
            blockJson.put("blockUUID", dto.getBlockUUID() != null ? dto.getBlockUUID() : "null");
            
            // Add parameters as JSONObject
            if (dto.getParameters() != null) {
                TypedParameterMap parameterMap = parameterMapper.mapFromDto(dto.getParameters());
                JSONObject paramValues = parameterMapper.mapToJsonObject(parameterMap);
                blockJson.put("paramValues", paramValues);
            }
            
            // Create temporary model (this would need to be injected in real usage)
            // For now, we'll use null and let the Block handle it
            NCSLabModel model = null; // Would be injected or passed as parameter
            
            // Use BlockType factory to create the block (using int ID and correct signature)
            int blockId = 1; // Default ID for DTO-created blocks
            Block block = BlockType.createBlock(blockId, blockJson, model);
            
            if (block == null) {
                throw new MappingException("BlockType factory returned null for type: " + dto.getBlockType());
            }
            
            return block;
            
        } catch (Exception e) {
            throw new MappingException("Failed to create Block from DTO", e);
        }
    }
    
    /**
     * Validate that a block type is supported
     */
    private boolean isValidBlockType(String blockType) {
        try {
            // This would ideally check against a registry of valid block types
            // For now, we'll do a simple check that it's not null/empty
            return blockType != null && !blockType.trim().isEmpty();
        } catch (Exception e) {
            logger.warn("Failed to validate block type {}: {}", blockType, e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate a list of ports
     */
    private void validatePortList(List<BlockPortDto> ports, String fieldName, MappingValidationResult result) {
        for (int i = 0; i < ports.size(); i++) {
            BlockPortDto port = ports.get(i);
            try {
                var portValidation = port.validate();
                if (!portValidation.isValid()) {
                    for (var error : portValidation.getErrors()) {
                        result.addError(fieldName + "[" + i + "]." + error.getField(), error.getMessage());
                    }
                }
            } catch (Exception e) {
                result.addError(fieldName + "[" + i + "]", "Port validation failed: " + e.getMessage());
            }
        }
    }
    
    /**
     * Validate the created block matches the DTO
     */
    private void validateCreatedBlock(Block block, BlockCreationDto dto) throws MappingException {
        try {
            if (!block.getBlockType().equals(dto.getBlockType())) {
                throw new MappingException("Block type mismatch after creation");
            }
            
            if (!block.getBlockName().equals(dto.getBlockName())) {
                throw new MappingException("Block name mismatch after creation");
            }
            
        } catch (Exception e) {
            throw new MappingException("Block validation failed after creation", e);
        }
    }
    
    /**
     * Update block parameters using reflection (since paramValues is protected)
     */
    private void updateBlockParameters(Block block, JSONObject newParamValues) throws MappingException {
        try {
            // Use reflection to update protected paramValues field
            java.lang.reflect.Field paramField = Block.class.getDeclaredField("paramValues");
            paramField.setAccessible(true);
            paramField.set(block, newParamValues);
            
            // Re-parse parameter list
            java.lang.reflect.Method parseMethod = Block.class.getDeclaredMethod("parseParameterList");
            parseMethod.setAccessible(true);
            parseMethod.invoke(block);
            
        } catch (Exception e) {
            throw new MappingException("Failed to update block parameters", e);
        }
    }
}