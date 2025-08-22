package com.ncslab.dto.mapper.exception;

/**
 * Exception thrown when a requested mapper cannot be found in the registry.
 * This is typically a runtime exception indicating a configuration issue.
 */
public class MapperNotFoundException extends RuntimeException {
    
    private final String mapperType;
    private final String requestedClass;
    
    /**
     * Create exception with simple message
     */
    public MapperNotFoundException(String message) {
        super(message);
        this.mapperType = null;
        this.requestedClass = null;
    }
    
    /**
     * Create exception with message and cause
     */
    public MapperNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.mapperType = null;
        this.requestedClass = null;
    }
    
    /**
     * Create exception with detailed information
     */
    public MapperNotFoundException(String message, String mapperType, String requestedClass) {
        super(message);
        this.mapperType = mapperType;
        this.requestedClass = requestedClass;
    }
    
    /**
     * Create exception with detailed information and cause
     */
    public MapperNotFoundException(String message, String mapperType, String requestedClass, Throwable cause) {
        super(message, cause);
        this.mapperType = mapperType;
        this.requestedClass = requestedClass;
    }
    
    /**
     * Get the type of mapper that was requested
     */
    public String getMapperType() {
        return mapperType;
    }
    
    /**
     * Get the class name that was requested
     */
    public String getRequestedClass() {
        return requestedClass;
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        
        if (mapperType != null) {
            sb.append(" [Type: ").append(mapperType).append("]");
        }
        
        if (requestedClass != null) {
            sb.append(" [Class: ").append(requestedClass).append("]");
        }
        
        return sb.toString();
    }
    
    /**
     * Static factory methods for common scenarios
     */
    public static MapperNotFoundException forDtoClass(String className) {
        return new MapperNotFoundException(
            "No mapper found for DTO class: " + className,
            "DTO",
            className
        );
    }
    
    public static MapperNotFoundException forEntityClass(String className) {
        return new MapperNotFoundException(
            "No mapper found for Entity class: " + className,
            "Entity", 
            className
        );
    }
    
    public static MapperNotFoundException forMapperName(String mapperName) {
        return new MapperNotFoundException(
            "No mapper found with name: " + mapperName,
            "Named",
            mapperName
        );
    }
}