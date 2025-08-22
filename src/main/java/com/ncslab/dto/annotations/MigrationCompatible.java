package com.ncslab.dto.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark DTOs that are compatible with legacy block classes.
 * Used for migration tracking and validation.
 * 
 * @author DTO Migration Framework
 * @version 1.0
 * @since DTO Migration Week 5
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MigrationCompatible {
    
    /**
     * The original class that this DTO replaces or is compatible with.
     */
    String originalClass();
    
    /**
     * Version of the migration compatibility.
     */
    String version() default "1.0";
    
    /**
     * Additional notes about the migration compatibility.
     */
    String notes() default "";
    
    /**
     * Whether this DTO is fully compatible (true) or has limitations (false).
     */
    boolean fullyCompatible() default true;
}