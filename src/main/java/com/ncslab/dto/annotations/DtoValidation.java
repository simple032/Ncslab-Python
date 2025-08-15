package com.ncslab.dto.annotations;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation for standardized DTO validation and serialization behavior
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public @interface DtoValidation {
    
    /**
     * Whether to validate required fields during deserialization
     */
    boolean validateRequired() default true;
    
    /**
     * Whether to perform strict type checking
     */
    boolean strictTypes() default false;
    
    /**
     * Whether to allow null values for non-primitive fields
     */
    boolean allowNulls() default true;
    
    /**
     * Custom validation groups
     */
    String[] validationGroups() default {};
}