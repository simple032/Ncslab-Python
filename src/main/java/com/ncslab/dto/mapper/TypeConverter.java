package com.ncslab.dto.mapper;

import com.ncslab.dto.mapper.exception.MappingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

/**
 * Service for handling type conversions between different object types.
 * Provides safe, configurable type conversion with extensible strategy pattern.
 */
public class TypeConverter {
    
    private final Logger logger = LoggerFactory.getLogger(TypeConverter.class);
    private final Map<ConversionKey, TypeConversionStrategy<?, ?>> conversionStrategies = new HashMap<>();
    
    public TypeConverter() {
        initializeConversionStrategies();
    }
    
    public void initializeConversionStrategies() {
        // String conversions
        registerStrategy(String.class, Double.class, new StringToDoubleStrategy());
        registerStrategy(String.class, Float.class, new StringToFloatStrategy());
        registerStrategy(String.class, Integer.class, new StringToIntegerStrategy());
        registerStrategy(String.class, Boolean.class, new StringToBooleanStrategy());
        registerStrategy(String.class, Long.class, new StringToLongStrategy());
        
        // Number conversions
        registerStrategy(Double.class, Float.class, new DoubleToFloatStrategy());
        registerStrategy(Float.class, Double.class, new FloatToDoubleStrategy());
        registerStrategy(Integer.class, Double.class, new IntegerToDoubleStrategy());
        registerStrategy(Double.class, Integer.class, new DoubleToIntegerStrategy());
        registerStrategy(Long.class, Double.class, new LongToDoubleStrategy());
        registerStrategy(Double.class, Long.class, new DoubleToLongStrategy());
        
        // Collection conversions
        registerStrategy(String.class, List.class, new StringToListStrategy());
        registerStrategy(String.class, double[].class, new StringToDoubleArrayStrategy());
        
        // Object to String conversions
        registerStrategy(Object.class, String.class, new ObjectToStringStrategy());
        
        logger.info("Initialized {} type conversion strategies", conversionStrategies.size());
    }
    
    /**
     * Register a type conversion strategy
     * @param fromType Source type
     * @param toType Target type
     * @param strategy Conversion strategy
     */
    public <F, T> void registerStrategy(Class<F> fromType, Class<T> toType, 
                                       TypeConversionStrategy<F, T> strategy) {
        ConversionKey key = new ConversionKey(fromType, toType);
        conversionStrategies.put(key, strategy);
        logger.debug("Registered conversion strategy: {} -> {}", fromType.getSimpleName(), toType.getSimpleName());
    }
    
    /**
     * Convert a value from one type to another
     * @param value The value to convert
     * @param fromType The source type
     * @param toType The target type
     * @return The converted value
     * @throws TypeConversionException if conversion fails
     */
    @SuppressWarnings("unchecked")
    public <F, T> T convert(F value, Class<F> fromType, Class<T> toType) throws TypeConversionException {
        if (value == null) {
            return null;
        }
        
        // Direct assignment if types are compatible
        if (toType.isAssignableFrom(fromType)) {
            return (T) value;
        }
        
        // Look for exact conversion strategy
        ConversionKey key = new ConversionKey(fromType, toType);
        TypeConversionStrategy<F, T> strategy = (TypeConversionStrategy<F, T>) conversionStrategies.get(key);
        
        // Try with Object as source type if exact match not found
        if (strategy == null && !fromType.equals(Object.class)) {
            ConversionKey objectKey = new ConversionKey(Object.class, toType);
            strategy = (TypeConversionStrategy<F, T>) conversionStrategies.get(objectKey);
        }
        
        if (strategy == null) {
            throw new TypeConversionException("No conversion strategy found", fromType, toType, value);
        }
        
        try {
            T convertedValue = strategy.convert(value);
            logger.debug("Converted {} from {} to {}: {} -> {}", 
                        value, fromType.getSimpleName(), toType.getSimpleName(), value, convertedValue);
            return convertedValue;
        } catch (Exception e) {
            throw new TypeConversionException("Conversion failed", fromType, toType, value, e);
        }
    }
    
    /**
     * Check if conversion is possible between two types
     * @param fromType Source type
     * @param toType Target type
     * @return true if conversion is supported
     */
    public boolean canConvert(Class<?> fromType, Class<?> toType) {
        if (toType.isAssignableFrom(fromType)) {
            return true;
        }
        
        ConversionKey key = new ConversionKey(fromType, toType);
        return conversionStrategies.containsKey(key) || 
               conversionStrategies.containsKey(new ConversionKey(Object.class, toType));
    }
    
    /**
     * Get all supported conversion operations
     * @return Set of supported conversions
     */
    public Set<ConversionKey> getSupportedConversions() {
        return Collections.unmodifiableSet(conversionStrategies.keySet());
    }
    
    // Conversion strategy interface
    public interface TypeConversionStrategy<F, T> {
        T convert(F value) throws ConversionException;
    }
    
    // Conversion strategies
    private static class StringToDoubleStrategy implements TypeConversionStrategy<String, Double> {
        @Override
        public Double convert(String value) throws ConversionException {
            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                throw new ConversionException("Invalid number format: " + value, e);
            }
        }
    }
    
    private static class StringToFloatStrategy implements TypeConversionStrategy<String, Float> {
        @Override
        public Float convert(String value) throws ConversionException {
            try {
                return Float.parseFloat(value.trim());
            } catch (NumberFormatException e) {
                throw new ConversionException("Invalid number format: " + value, e);
            }
        }
    }
    
    private static class StringToIntegerStrategy implements TypeConversionStrategy<String, Integer> {
        @Override
        public Integer convert(String value) throws ConversionException {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                throw new ConversionException("Invalid integer format: " + value, e);
            }
        }
    }
    
    private static class StringToBooleanStrategy implements TypeConversionStrategy<String, Boolean> {
        @Override
        public Boolean convert(String value) throws ConversionException {
            String trimmed = value.trim().toLowerCase();
            if ("true".equals(trimmed) || "1".equals(trimmed) || "yes".equals(trimmed) || "on".equals(trimmed)) {
                return true;
            } else if ("false".equals(trimmed) || "0".equals(trimmed) || "no".equals(trimmed) || "off".equals(trimmed)) {
                return false;
            } else {
                throw new ConversionException("Invalid boolean format: " + value);
            }
        }
    }
    
    private static class StringToLongStrategy implements TypeConversionStrategy<String, Long> {
        @Override
        public Long convert(String value) throws ConversionException {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                throw new ConversionException("Invalid long format: " + value, e);
            }
        }
    }
    
    private static class DoubleToFloatStrategy implements TypeConversionStrategy<Double, Float> {
        @Override
        public Float convert(Double value) throws ConversionException {
            if (value > Float.MAX_VALUE || value < -Float.MAX_VALUE) {
                throw new ConversionException("Value out of float range: " + value);
            }
            return value.floatValue();
        }
    }
    
    private static class FloatToDoubleStrategy implements TypeConversionStrategy<Float, Double> {
        @Override
        public Double convert(Float value) throws ConversionException {
            return value.doubleValue();
        }
    }
    
    private static class IntegerToDoubleStrategy implements TypeConversionStrategy<Integer, Double> {
        @Override
        public Double convert(Integer value) throws ConversionException {
            return value.doubleValue();
        }
    }
    
    private static class DoubleToIntegerStrategy implements TypeConversionStrategy<Double, Integer> {
        @Override
        public Integer convert(Double value) throws ConversionException {
            if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
                throw new ConversionException("Value out of integer range: " + value);
            }
            return value.intValue();
        }
    }
    
    private static class LongToDoubleStrategy implements TypeConversionStrategy<Long, Double> {
        @Override
        public Double convert(Long value) throws ConversionException {
            return value.doubleValue();
        }
    }
    
    private static class DoubleToLongStrategy implements TypeConversionStrategy<Double, Long> {
        @Override
        public Long convert(Double value) throws ConversionException {
            if (value > Long.MAX_VALUE || value < Long.MIN_VALUE) {
                throw new ConversionException("Value out of long range: " + value);
            }
            return value.longValue();
        }
    }
    
    private static class StringToListStrategy implements TypeConversionStrategy<String, List> {
        @Override
        @SuppressWarnings("rawtypes")
        public List convert(String value) throws ConversionException {
            try {
                // Handle array formats like "[1, 2, 3]" or "1, 2, 3"
                String trimmed = value.trim();
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    trimmed = trimmed.substring(1, trimmed.length() - 1);
                }
                
                if (trimmed.isEmpty()) {
                    return new ArrayList();
                }
                
                String[] parts = trimmed.split(",");
                List<String> result = new ArrayList<>();
                
                for (String part : parts) {
                    result.add(part.trim());
                }
                
                return result;
            } catch (Exception e) {
                throw new ConversionException("Invalid list format: " + value, e);
            }
        }
    }
    
    private static class StringToDoubleArrayStrategy implements TypeConversionStrategy<String, double[]> {
        @Override
        public double[] convert(String value) throws ConversionException {
            try {
                // Handle array formats like "[1.0, 2.0, 3.0]" or "1.0 2.0 3.0"
                String trimmed = value.trim();
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    trimmed = trimmed.substring(1, trimmed.length() - 1);
                }
                
                if (trimmed.isEmpty()) {
                    return new double[0];
                }
                
                String[] parts = trimmed.split("[,\\s]+");
                double[] result = new double[parts.length];
                
                for (int i = 0; i < parts.length; i++) {
                    result[i] = Double.parseDouble(parts[i].trim());
                }
                
                return result;
            } catch (Exception e) {
                throw new ConversionException("Invalid double array format: " + value, e);
            }
        }
    }
    
    private static class ObjectToStringStrategy implements TypeConversionStrategy<Object, String> {
        @Override
        public String convert(Object value) throws ConversionException {
            return value.toString();
        }
    }
    
    // Supporting classes
    public static class ConversionKey {
        private final Class<?> fromType;
        private final Class<?> toType;
        
        public ConversionKey(Class<?> fromType, Class<?> toType) {
            this.fromType = fromType;
            this.toType = toType;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ConversionKey)) return false;
            ConversionKey that = (ConversionKey) o;
            return Objects.equals(fromType, that.fromType) && Objects.equals(toType, that.toType);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(fromType, toType);
        }
        
        @Override
        public String toString() {
            return fromType.getSimpleName() + " -> " + toType.getSimpleName();
        }
        
        public Class<?> getFromType() { return fromType; }
        public Class<?> getToType() { return toType; }
    }
    
    public static class ConversionException extends Exception {
        public ConversionException(String message) {
            super(message);
        }
        
        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class TypeConversionException extends MappingException {
        public TypeConversionException(String message, Class<?> fromType, Class<?> toType, Object value) {
            super(message, fromType.getName(), toType.getName(), "value", value);
        }
        
        public TypeConversionException(String message, Class<?> fromType, Class<?> toType, Object value, Throwable cause) {
            super(message, fromType.getName(), toType.getName(), "value", value, cause);
        }
    }
}