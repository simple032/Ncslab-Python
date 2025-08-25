package com.ncslab.block.hardware;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Base class for all hardware interface blocks (GPIO, ADC, DAC, PWM, EtherCAT, etc.).
 * Provides common implementations for hardware communication and real-time operations.
 */
public abstract class HardwareBlock extends Block {

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Hardware connection state
    protected boolean hardwareConnected = false;
    protected Map<String, Object> hardwareConfig = new HashMap<>();
    
    // Real-time constraints
    protected double maxExecutionTime = 0.001; // 1ms default
    protected long lastExecutionTime = 0;

    /**
     * DTO-NATIVE Constructor
     */
    protected HardwareBlock(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
    }

    /**
     * Legacy JSON Constructor
     */
    protected HardwareBlock(JSONObject blockJson, NCSLabModel model) {
        super(blockJson, model);
    }

        public void calculateInit() {
        // Initialize hardware connection
        if (initializeHardware()) {
            hardwareConnected = true;
            initializeHardwareSpecificParameters();
        } else {
            System.err.println("Warning: Hardware initialization failed for block " + blockName);
            hardwareConnected = false;
        }
        
        // Set initial output
        setInitialHardwareOutput();
    }

        public void calculateOutput(double t) {
        // Default implementation - hardware blocks should override this method
        // with their specific hardware I/O operations
    }

        public void calculateDerivative(double t) {
        // Hardware blocks are typically discrete - no derivatives
        // Override if the hardware block models continuous dynamics
    }

        public void calculateUpdate(double t) {
        // Default implementation - hardware blocks can override if needed
    }

        public void calculateDiscreteUpdate(double t) {
        // Default implementation - hardware blocks can override if needed
    }

        public void calculateTerminate(double t) {
        // Clean up hardware resources and connections
        cleanupHardware();
        hardwareConnected = false;
    }

    /**
     * Initialize hardware-specific connections (default implementation)
     * @return true if hardware initialized successfully, false otherwise
     */
    protected boolean initializeHardware() {
        // Default implementation - hardware blocks should override this method
        return true;
    }

    /**
     * Perform hardware-specific output operations (default implementation)
     * @param t Current simulation time
     */
    protected void performHardwareOutput(double t) {
        // Default implementation - hardware blocks should override this method
    }

    /**
     * Clean up hardware resources (default implementation)
     */
    protected void cleanupHardware() {
        // Default implementation - hardware blocks can override if needed
    }

    /**
     * Initialize hardware-specific parameters (default implementation)
     */
    protected void initializeHardwareSpecificParameters() {
        // Default: no special parameter initialization needed
    }

    /**
     * Set initial hardware output values (default implementation)
     */
    protected void setInitialHardwareOutput() {
        // Default: set all outputs to zero
        for (int i = 0; i < outputPortList.size(); i++) {
            outputPortList.get(i).setData(new Data(0.0));
        }
    }

    /**
     * Provide safe output when hardware is not connected (default implementation)
     * @param t Current simulation time
     */
    protected void provideSafeOutput(double t) {
        // Default: maintain last known safe values (zero)
        for (int i = 0; i < outputPortList.size(); i++) {
            outputPortList.get(i).setData(new Data(0.0));
        }
    }

    /**
     * Update hardware state (default implementation)
     * @param t Current simulation time
     */
    protected void updateHardwareState(double t) {
        // Default: no state updates needed
    }

    /**
     * Perform discrete hardware updates (default implementation)
     * @param t Current simulation time
     */
    protected void performDiscreteHardwareUpdate(double t) {
        // Default: no discrete updates needed
    }

    /**
     * Handle hardware errors (default implementation)
     * @param e Exception that occurred
     * @param t Current simulation time
     */
    protected void handleHardwareError(Exception e, double t) {
        System.err.println("Hardware error in block " + blockName + " at time " + t + ": " + e.getMessage());
        // Attempt to reconnect or provide safe fallback
        hardwareConnected = false;
    }

    /**
     * Check if hardware is connected and operational
     * @return true if hardware is connected
     */
    public boolean isHardwareConnected() {
        return hardwareConnected;
    }

    /**
     * Get last execution time in nanoseconds
     * @return Last execution time
     */
    public long getLastExecutionTime() {
        return lastExecutionTime;
    }

    /**
     * Set maximum allowed execution time for real-time constraints
     * @param maxTimeSeconds Maximum execution time in seconds
     */
    public void setMaxExecutionTime(double maxTimeSeconds) {
        this.maxExecutionTime = maxTimeSeconds;
    }

    /**
     * Set hardware configuration parameters
     * @param config Configuration map
     */
    public void setHardwareConfig(Map<String, Object> config) {
        this.hardwareConfig.putAll(config);
    }

    /**
     * Get hardware configuration parameter
     * @param key Parameter key
     * @return Parameter value or null if not found
     */
    protected Object getHardwareConfigValue(String key) {
        return hardwareConfig.get(key);
    }
}