package com.ncslab.ncslablink;

/**
 * Exception thrown to signal simulation should stop due to a stop condition.
 *
 * This exception is used by verification blocks (e.g., StopSimulation, Assert)
 * to halt simulation execution when a specified condition is met or violated.
 * It is a controlled exception used for normal simulation termination, not an error.
 *
 * @author NCSLab Team
 * @version 2025
 */
public class SimulationStopException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** The block that requested the stop */
    private final String blockName;

    /** The simulation time when stop was requested */
    private final double simulationTime;

    /** Whether this is a normal stop (true) or error stop (false) */
    private final boolean normalStop;

    /**
     * Constructs a new SimulationStopException with the specified message.
     *
     * @param message the detail message explaining why simulation is stopping
     */
    public SimulationStopException(String message) {
        super(message);
        this.blockName = null;
        this.simulationTime = -1.0;
        this.normalStop = true;
    }

    /**
     * Constructs a new SimulationStopException with full context.
     *
     * @param message the detail message explaining why simulation is stopping
     * @param blockName the name of the block that requested the stop
     * @param simulationTime the simulation time when stop was requested
     * @param normalStop true if this is a normal stop, false if error stop
     */
    public SimulationStopException(String message, String blockName, double simulationTime, boolean normalStop) {
        super(String.format("%s (Block: %s, Time: %.6f, Normal: %s)",
                          message, blockName, simulationTime, normalStop));
        this.blockName = blockName;
        this.simulationTime = simulationTime;
        this.normalStop = normalStop;
    }

    /**
     * Constructs a new SimulationStopException for normal stop condition.
     *
     * @param message the detail message explaining why simulation is stopping
     * @param blockName the name of the block that requested the stop
     * @param simulationTime the simulation time when stop was requested
     */
    public SimulationStopException(String message, String blockName, double simulationTime) {
        this(message, blockName, simulationTime, true);
    }

    /**
     * Gets the name of the block that requested the stop.
     *
     * @return the block name, or null if not set
     */
    public String getBlockName() {
        return blockName;
    }

    /**
     * Gets the simulation time when stop was requested.
     *
     * @return the simulation time, or -1.0 if not set
     */
    public double getSimulationTime() {
        return simulationTime;
    }

    /**
     * Checks if this is a normal simulation stop.
     *
     * @return true if normal stop, false if error stop
     */
    public boolean isNormalStop() {
        return normalStop;
    }
}
