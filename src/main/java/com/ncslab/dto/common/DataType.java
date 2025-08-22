package com.ncslab.dto.common;

/**
 * Enumeration of supported data types for block ports and parameters.
 * Provides mapping between DTO representation and internal data types.
 */
public enum DataType {
    DOUBLE("double", 8),
    FLOAT("float", 4),
    INTEGER("int", 4),
    BOOLEAN("boolean", 1),
    COMPLEX_DOUBLE("complex<double>", 16),
    COMPLEX_FLOAT("complex<float>", 8);
    
    private final String cTypeName;
    private final int sizeInBytes;
    
    DataType(String cTypeName, int sizeInBytes) {
        this.cTypeName = cTypeName;
        this.sizeInBytes = sizeInBytes;
    }
    
    /**
     * Get the C type name for code generation
     * @return C type name
     */
    public String getCTypeName() {
        return cTypeName;
    }
    
    /**
     * Get the size in bytes for memory calculations
     * @return Size in bytes
     */
    public int getSizeInBytes() {
        return sizeInBytes;
    }
    
    /**
     * Check if this is a complex data type
     * @return true if complex, false otherwise
     */
    public boolean isComplex() {
        return this == COMPLEX_DOUBLE || this == COMPLEX_FLOAT;
    }
    
    /**
     * Check if this is a numeric data type
     * @return true if numeric, false otherwise
     */
    public boolean isNumeric() {
        return this == DOUBLE || this == FLOAT || this == INTEGER || isComplex();
    }
    
    /**
     * Get the corresponding real data type for complex types
     * @return Real data type, or self if already real
     */
    public DataType getRealType() {
        switch (this) {
            case COMPLEX_DOUBLE: return DOUBLE;
            case COMPLEX_FLOAT: return FLOAT;
            default: return this;
        }
    }
}