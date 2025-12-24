package com.ncslab.ncslablink;

/**
 * Exception thrown when a block execution fails during simulation.
 *
 * This exception is used to indicate runtime errors during block calculations,
 * such as assertion failures, invalid input conditions, or constraint violations.
 * It differs from BlockCreationException which is thrown during block instantiation.
 *
 * @author NCSLab Team
 * @version 2025
 */
public class BlockExecutionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** The block that threw this exception */
    private final String blockName;

    /** The simulation time when the exception occurred */
    private final double simulationTime;

    /**
     * Constructs a new BlockExecutionException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public BlockExecutionException(String message) {
        super(message);
        this.blockName = null;
        this.simulationTime = -1.0;
    }

    /**
     * Constructs a new BlockExecutionException with the specified detail message and cause.
     *
     * @param message the detail message explaining the cause of the exception
     * @param cause the underlying cause of the exception
     */
    public BlockExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.blockName = null;
        this.simulationTime = -1.0;
    }

    /**
     * Constructs a new BlockExecutionException with block context.
     *
     * @param message the detail message explaining the cause of the exception
     * @param blockName the name of the block that threw the exception
     * @param simulationTime the simulation time when the exception occurred
     */
    public BlockExecutionException(String message, String blockName, double simulationTime) {
        super(String.format("%s (Block: %s, Time: %.6f)", message, blockName, simulationTime));
        this.blockName = blockName;
        this.simulationTime = simulationTime;
    }

    /**
     * Gets the name of the block that threw this exception.
     *
     * @return the block name, or null if not set
     */
    public String getBlockName() {
        return blockName;
    }

    /**
     * Gets the simulation time when the exception occurred.
     *
     * @return the simulation time, or -1.0 if not set
     */
    public double getSimulationTime() {
        return simulationTime;
    }
}
