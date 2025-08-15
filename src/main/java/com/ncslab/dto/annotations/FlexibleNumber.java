package com.ncslab.dto.annotations;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ncslab.dto.deserializers.FlexibleNumberDeserializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for fields that should accept both string and numeric input
 * and convert appropriately (common in web form data)
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = FlexibleNumberDeserializer.class)
public @interface FlexibleNumber {
    
    /**
     * Default value to use if parsing fails
     */
    double defaultValue() default 0.0;
    
    /**
     * Whether to allow null values
     */
    boolean allowNull() default true;
}