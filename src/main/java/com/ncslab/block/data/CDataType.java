package com.ncslab.block.data;

/**
 * C/C++ data types for code generation and byte-level operations.
 *
 * Represents actual data types used in generated C++ code, matching
 * Simulink/MATLAB data type conventions.
 */
public enum CDataType {
    // Floating point types
    DOUBLE,     // 8 bytes - default type
    SINGLE,     // 4 bytes - float

    // Signed integer types
    INT8,       // 1 byte - signed char
    INT16,      // 2 bytes - short
    INT32,      // 4 bytes - int
    INT64,      // 8 bytes - long long

    // Unsigned integer types
    UINT8,      // 1 byte - unsigned char
    UINT16,     // 2 bytes - unsigned short
    UINT32,     // 4 bytes - unsigned int
    UINT64,     // 8 bytes - unsigned long long

    // Boolean type
    BOOLEAN;    // 1 byte - bool

    /**
     * Get size in bytes for this data type
     */
    public int getByteSize() {
        switch (this) {
            case INT8:
            case UINT8:
            case BOOLEAN:
                return 1;
            case INT16:
            case UINT16:
                return 2;
            case INT32:
            case UINT32:
            case SINGLE:
                return 4;
            case INT64:
            case UINT64:
            case DOUBLE:
                return 8;
            default:
                return 8; // Default to double
        }
    }

    /**
     * Get C++ type string for code generation
     */
    public String getCppType() {
        switch (this) {
            case DOUBLE:
                return "double";
            case SINGLE:
                return "float";
            case INT8:
                return "int8_t";
            case INT16:
                return "int16_t";
            case INT32:
                return "int32_t";
            case INT64:
                return "int64_t";
            case UINT8:
                return "uint8_t";
            case UINT16:
                return "uint16_t";
            case UINT32:
                return "uint32_t";
            case UINT64:
                return "uint64_t";
            case BOOLEAN:
                return "bool";
            default:
                return "double";
        }
    }

    /**
     * Parse from Simulink data type string
     */
    public static CDataType fromString(String typeStr) {
        if (typeStr == null || typeStr.isEmpty()) {
            return DOUBLE;
        }

        // Normalize string
        typeStr = typeStr.toLowerCase().trim();

        // Handle common variations
        switch (typeStr) {
            case "double":
                return DOUBLE;
            case "single":
            case "float":
                return SINGLE;
            case "int8":
                return INT8;
            case "int16":
                return INT16;
            case "int32":
                return INT32;
            case "int64":
                return INT64;
            case "uint8":
                return UINT8;
            case "uint16":
                return UINT16;
            case "uint32":
                return UINT32;
            case "uint64":
                return UINT64;
            case "boolean":
            case "bool":
                return BOOLEAN;
            default:
                // If not recognized, default to double
                return DOUBLE;
        }
    }

    /**
     * Check if this is a signed integer type
     */
    public boolean isSignedInteger() {
        return this == INT8 || this == INT16 || this == INT32 || this == INT64;
    }

    /**
     * Check if this is an unsigned integer type
     */
    public boolean isUnsignedInteger() {
        return this == UINT8 || this == UINT16 || this == UINT32 || this == UINT64;
    }

    /**
     * Check if this is any integer type
     */
    public boolean isInteger() {
        return isSignedInteger() || isUnsignedInteger();
    }

    /**
     * Check if this is a floating point type
     */
    public boolean isFloatingPoint() {
        return this == DOUBLE || this == SINGLE;
    }
}
