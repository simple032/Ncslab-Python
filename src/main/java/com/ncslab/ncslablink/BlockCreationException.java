package com.ncslab.ncslablink;

/**
 * Exception thrown when block creation fails due to invalid parameters or configuration.
 * 
 * This exception is used across all block types in the NCSLabLink system to indicate
 * failures during block instantiation, parameter validation, or factory method execution.
 */
public class BlockCreationException extends RuntimeException {
    
    /**
     * Constructs a new BlockCreationException with the specified detail message and cause.
     * 
     * @param message the detail message explaining the cause of the exception
     * @param cause the underlying cause of the exception
     */
    public BlockCreationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new BlockCreationException with the specified detail message.
     * 
     * @param message the detail message explaining the cause of the exception
     */
    public BlockCreationException(String message) {
        super(message);
    }
}