package com.ncslab.dto.mapper;

import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.core.BlockDto;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for converting between TypedParameter (DTO) and Parameter (Block) objects.
 *
 * This converter bridges the gap between the DTO architecture and the legacy Block parameter system,
 * enabling DTO-based factory methods to work seamlessly with existing Block constructors.
 *
 * Key Responsibilities:
 * - Convert TypedParameter objects to Parameter objects
 * - Extract TypedParameter fields from DTO objects using reflection
 * - Convert Parameter objects back to TypedParameter for serialization
 *
 * @author NCSLab Team
 * @version 1.0
 * @since DTO Migration Phase 2
 */
public class TypedParameterConverter {

    /**
     * Converts a TypedParameter to a Parameter object.
     *
     * @param typedParam The TypedParameter to convert
     * @param block The parent block (can be null during construction)
     * @param paramId The parameter ID for indexing
     * @param paramName The parameter name (local name)
     * @return Parameter object with the converted value
     */
    public static Parameter toParameter(TypedParameter typedParam, Block block, int paramId, String paramName) {
        if (typedParam == null) {
            throw new IllegalArgumentException("TypedParameter cannot be null");
        }

        // Convert TypedParameter value to string representation
        String valueString = convertToString(typedParam);

        // Create Parameter object
        return new Parameter(block, paramId, paramName, valueString);
    }

    /**
     * Converts a TypedParameter value to its string representation.
     * Handles various data types (double, boolean, string, arrays, matrices).
     *
     * @param typedParam The TypedParameter to convert
     * @return String representation of the value
     */
    private static String convertToString(TypedParameter typedParam) {
        Object value = typedParam.getValue();

        if (value == null) {
            return "0"; // Default value for null
        }

        // Handle different value types
        if (value instanceof Number) {
            return String.valueOf(value);
        } else if (value instanceof Boolean) {
            // Convert boolean to string format expected by Parameter
            return ((Boolean) value) ? "on" : "off";
        } else if (value instanceof String) {
            return (String) value;
        } else if (value.getClass().isArray()) {
            // Handle array types - convert to MATLAB-style matrix notation
            return convertArrayToString(value);
        } else {
            return value.toString();
        }
    }

    /**
     * Converts an array to MATLAB-style matrix notation string.
     *
     * @param array The array object
     * @return String representation in matrix notation
     */
    private static String convertArrayToString(Object array) {
        if (array instanceof double[]) {
            double[] arr = (double[]) array;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(arr[i]);
            }
            sb.append("]");
            return sb.toString();
        } else if (array instanceof double[][]) {
            double[][] matrix = (double[][]) array;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < matrix.length; i++) {
                if (i > 0) sb.append("; ");
                for (int j = 0; j < matrix[i].length; j++) {
                    if (j > 0) sb.append(" ");
                    sb.append(matrix[i][j]);
                }
            }
            sb.append("]");
            return sb.toString();
        }

        return array.toString();
    }

    /**
     * Converts a Parameter back to a TypedParameter.
     * Useful for serialization and DTO export.
     *
     * @param param The Parameter to convert
     * @return TypedParameter with the parameter's value
     */
    public static TypedParameter fromParameter(Parameter param) {
        if (param == null) {
            return null;
        }

        String initString = param.getInitString();
        return TypedParameter.of(initString);
    }

    /**
     * Extracts all TypedParameter fields from a DTO object using reflection.
     * This method scans the DTO class hierarchy to find all fields of type TypedParameter
     * and creates a map of parameter names to TypedParameter objects.
     *
     * @param dto The BlockDto object to extract parameters from
     * @return Map of parameter names to TypedParameter objects
     */
    public static Map<String, TypedParameter> extractTypedParameters(BlockDto dto) {
        Map<String, TypedParameter> params = new HashMap<>();

        if (dto == null) {
            return params;
        }

        // Scan class hierarchy for TypedParameter fields
        Class<?> clazz = dto.getClass();
        while (clazz != null && BlockDto.class.isAssignableFrom(clazz)) {
            for (Field field : clazz.getDeclaredFields()) {
                // Check if field is TypedParameter
                if (TypedParameter.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        TypedParameter value = (TypedParameter) field.get(dto);

                        if (value != null) {
                            // Convert field name to parameter name
                            String paramName = convertFieldNameToParameterName(field.getName());
                            params.put(paramName, value);
                        }
                    } catch (IllegalAccessException e) {
                        System.err.println("Cannot access field: " + field.getName() + " in " + clazz.getName());
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }

        return params;
    }

    /**
     * Converts a DTO field name to a SIMULINK-style parameter name.
     *
     * Examples:
     * - "value" → "Value"
     * - "sampleTime" → "SampleTime"
     * - "outDataTypeStr" → "OutDataTypeStr"
     * - "saturateOnIntegerOverflow" → "SaturateOnIntegerOverflow"
     *
     * @param fieldName The DTO field name (camelCase)
     * @return The parameter name (PascalCase)
     */
    private static String convertFieldNameToParameterName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }

        // Convert first character to uppercase
        return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /**
     * Converts all TypedParameters from a DTO to Parameter objects and adds them to a block's parameter list.
     * This is the main integration point for DTO-based block construction.
     *
     * @param dto The BlockDto containing TypedParameter fields
     * @param block The target block to add parameters to
     * @param startingParamId The starting parameter ID (usually 1)
     * @return The next available parameter ID
     */
    public static int convertAndAddParameters(BlockDto dto, Block block, int startingParamId) {
        Map<String, TypedParameter> typedParams = extractTypedParameters(dto);

        int paramId = startingParamId;
        for (Map.Entry<String, TypedParameter> entry : typedParams.entrySet()) {
            String paramName = entry.getKey();
            TypedParameter typedParam = entry.getValue();

            // Convert TypedParameter to Parameter
            Parameter param = toParameter(typedParam, block, paramId++, paramName);

            // Add to block's parameter list
            block.getParameterList().add(param);
        }

        return paramId;
    }
}
