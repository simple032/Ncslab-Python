package com.ncslab.dto.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.ncslab.dto.annotations.FlexibleNumber;

import java.io.IOException;

/**
 * Custom deserializer for flexible number conversion
 * Handles string->number conversion for web form compatibility
 */
public class FlexibleNumberDeserializer extends JsonDeserializer<Object> {
    
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.getCurrentToken();
        
        // Get annotation info for defaults
        FlexibleNumber annotation = null;
        try {
            // Try to get the annotation from current field
            annotation = ctxt.getContextualType() != null ? 
                ctxt.getContextualType().getRawClass().getAnnotation(FlexibleNumber.class) : null;
        } catch (Exception e) {
            // Ignore annotation retrieval errors
        }
        
        double defaultValue = annotation != null ? annotation.defaultValue() : 0.0;
        boolean allowNull = annotation == null || annotation.allowNull();
        
        if (token == JsonToken.VALUE_NULL) {
            return allowNull ? null : defaultValue;
        }
        
        if (token == JsonToken.VALUE_STRING) {
            String text = p.getText().trim();
            if (text.isEmpty()) {
                return allowNull ? null : defaultValue;
            }
            
            try {
                // Try integer first for whole numbers
                if (!text.contains(".") && !text.toLowerCase().contains("e")) {
                    long longValue = Long.parseLong(text);
                    if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                        return (int) longValue;
                    }
                    return longValue;
                }
                
                // Parse as double for decimal numbers
                return Double.parseDouble(text);
                
            } catch (NumberFormatException e) {
                // If parsing fails, return default or null
                return allowNull ? null : defaultValue;
            }
        }
        
        if (token == JsonToken.VALUE_NUMBER_INT) {
            return p.getIntValue();
        }
        
        if (token == JsonToken.VALUE_NUMBER_FLOAT) {
            return p.getDoubleValue();
        }
        
        // For any other token type, return default
        return allowNull ? null : defaultValue;
    }
}