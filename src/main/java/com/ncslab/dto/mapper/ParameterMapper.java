package com.ncslab.dto.mapper;

import com.ncslab.dto.block.BlockParametersDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.exception.MappingException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * Mapper for converting between parameter DTOs and entity parameter representations.
 * Handles type-safe conversion and validation of block parameters.
 */
public class ParameterMapper {
    
    private final Logger logger = LoggerFactory.getLogger(ParameterMapper.class);
    
    private TypeConverter typeConverter;
    
    public ParameterMapper() {
        this.typeConverter = new TypeConverter();
    }
    
    public ParameterMapper(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }
    
    /**
     * Convert BlockParametersDto to TypedParameterMap
     * @param parametersDto The DTO containing parameters
     * @return TypedParameterMap for entity use
     * @throws MappingException if conversion fails
     */
    public TypedParameterMap mapFromDto(BlockParametersDto parametersDto) throws MappingException {
        if (parametersDto == null) {
            return new TypedParameterMap();
        }
        
        TypedParameterMap parameterMap = new TypedParameterMap();
        
        try {
            // Map each parameter with type safety
            for (Map.Entry<String, TypedParameter> entry : parametersDto.getParameters().entrySet()) {
                String paramName = entry.getKey();
                TypedParameter dtoParam = entry.getValue();
                
                if (dtoParam == null) {
                    logger.warn("Null parameter value for: {}", paramName);
                    continue;
                }
                
                // Create entity parameter from DTO
                TypedParameter entityParam = new TypedParameter(
                    dtoParam.getValue(), 
                    dtoParam.getType(),
                    dtoParam.getConstraints()
                );
                
                // Copy metadata
                if (dtoParam.getMetadata() != null) {
                    entityParam.setMetadata(new HashMap<>(dtoParam.getMetadata()));
                }
                
                parameterMap.put(paramName, entityParam);
            }
            
            return parameterMap;
            
        } catch (Exception e) {
            throw new MappingException("Failed to map parameters from DTO", e);
        }
    }
    
    /**
     * Convert TypedParameterMap to BlockParametersDto
     * @param parameterMap The entity parameter map
     * @return BlockParametersDto for DTO use
     * @throws MappingException if conversion fails
     */
    public BlockParametersDto mapToDto(TypedParameterMap parameterMap) throws MappingException {
        if (parameterMap == null || parameterMap.isEmpty()) {
            return new BlockParametersDto();
        }
        
        BlockParametersDto parametersDto = new BlockParametersDto();
        
        try {
            for (Map.Entry<String, TypedParameter> entry : parameterMap.entrySet()) {
                String paramName = entry.getKey();
                TypedParameter entityParam = entry.getValue();
                
                if (entityParam != null) {
                    // Create DTO parameter from entity
                    TypedParameter dtoParam = new TypedParameter(
                        entityParam.getValue(), 
                        entityParam.getType(),
                        entityParam.getConstraints()
                    );
                    
                    // Copy metadata
                    if (entityParam.getMetadata() != null) {
                        dtoParam.setMetadata(new HashMap<>(entityParam.getMetadata()));
                    }
                    
                    parametersDto.getParameters().put(paramName, dtoParam);
                }
            }
            
            return parametersDto;
            
        } catch (Exception e) {
            throw new MappingException("Failed to map parameters to DTO", e);
        }
    }
    
    /**
     * Convert JSONObject to TypedParameterMap (for backward compatibility)
     * @param paramValues JSONObject containing parameter values
     * @return TypedParameterMap for entity use
     * @throws MappingException if conversion fails
     */
    public TypedParameterMap mapFromJsonObject(JSONObject paramValues) throws MappingException {
        if (paramValues == null || paramValues.isEmpty()) {
            return new TypedParameterMap();
        }
        
        TypedParameterMap parameterMap = new TypedParameterMap();
        
        try {
            for (String key : paramValues.keySet()) {
                Object value = paramValues.get(key);
                
                // Determine type from value
                String type = determineTypeFromValue(value);
                
                TypedParameter param = new TypedParameter(value, type);
                parameterMap.put(key, param);
            }
            
            return parameterMap;
            
        } catch (Exception e) {
            throw new MappingException("Failed to map parameters from JSONObject", e);
        }
    }
    
    /**
     * Convert TypedParameterMap to JSONObject (for backward compatibility)
     * @param parameterMap The parameter map to convert
     * @return JSONObject for legacy use
     * @throws MappingException if conversion fails
     */
    public JSONObject mapToJsonObject(TypedParameterMap parameterMap) throws MappingException {
        if (parameterMap == null || parameterMap.isEmpty()) {
            return new JSONObject();
        }
        
        JSONObject jsonObject = new JSONObject();
        
        try {
            for (Map.Entry<String, TypedParameter> entry : parameterMap.entrySet()) {
                String paramName = entry.getKey();
                TypedParameter param = entry.getValue();
                
                if (param != null) {
                    // Convert typed value to appropriate JSON type
                    Object jsonValue = convertToJsonValue(param);
                    jsonObject.put(paramName, jsonValue);
                }
            }
            
            return jsonObject;
            
        } catch (Exception e) {
            throw new MappingException("Failed to map parameters to JSONObject", e);
        }
    }
    
    /**
     * Determine type from value for automatic type detection
     */
    private String determineTypeFromValue(Object value) {
        if (value == null) {
            return "string";
        }
        
        if (value instanceof Double || value instanceof Float) {
            return "double";
        } else if (value instanceof Integer || value instanceof Long) {
            return "integer";
        } else if (value instanceof Boolean) {
            return "boolean";
        } else if (value.getClass().isArray() || value instanceof java.util.Collection) {
            return "array";
        } else {
            return "string";
        }
    }
    
    /**
     * Convert TypedParameter to appropriate JSON value
     */
    private Object convertToJsonValue(TypedParameter param) {
        Object value = param.getValue();
        
        if (value == null) {
            return null;
        }
        
        // Handle type-specific conversions
        switch (param.getType().toLowerCase()) {
            case "double":
            case "float":
            case "number":
                return param.getAsDouble();
            case "int":
            case "integer":
                return param.getAsInteger();
            case "boolean":
            case "bool":
                return param.getAsBoolean();
            case "string":
            case "text":
            default:
                return param.getAsString();
        }
    }
}