package com.ncslab.block.discontinuous;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.data.Data;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all discontinuous blocks (Saturation, Relay, DeadZone, etc.).
 * Provides common implementations for nonlinear discontinuous operations.
 */
public abstract class DiscontinuousBlock extends Block {

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    /**
     * DTO-NATIVE Constructor
     */
    protected DiscontinuousBlock(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
    }

    /**
     * Legacy JSON Constructor
     */
    protected DiscontinuousBlock(JSONObject blockJson, NCSLabModel model) {
        super(blockJson, model);
    }

        public void calculateInit() {
        // Initialize discontinuous system
        initializeDiscontinuousSystem();
        
        // Set initial output
        if (!outputPortList.isEmpty()) {
            OutputPort output = outputPortList.get(0);
            output.setData(getInitialOutput());
        }
    }

    @Override
    public abstract void calculateOutput(double t);

        public void calculateDerivative(double t) {
        // Discontinuous blocks typically don't have continuous derivatives
        // Some may have conditional derivatives - override if needed
    }

        public void calculateUpdate(double t) {
        // Default implementation - discontinuous blocks can override if needed
    }

        public void calculateDiscreteUpdate(double t) {
        // Default implementation - discontinuous blocks can override if needed
    }

        public void calculateTerminate(double t) {
        // Clean up discontinuous system resources
        cleanupDiscontinuousResources();
    }

    /**
     * Initialize discontinuous system parameters (default implementation)
     */
    protected void initializeDiscontinuousSystem() {
        // Default: no special initialization needed
    }

    /**
     * Get initial output value (default implementation)
     * @return Initial output data
     */
    protected Data getInitialOutput() {
        return new Data(0.0);
    }

    /**
     * Update internal states for discontinuous behavior (default implementation)
     * @param t Current simulation time
     */
    protected void updateDiscontinuousState(double t) {
        // Default: no state updates needed
    }

    /**
     * Update switching states for hysteresis and other switching behaviors
     * @param t Current simulation time
     */
    protected void updateSwitchingState(double t) {
        // Default: no switching state updates needed
    }

    /**
     * Clean up discontinuous system resources (default implementation)
     */
    protected void cleanupDiscontinuousResources() {
        // Default: no cleanup needed
    }

    /**
     * Helper method to implement saturation logic
     * @param value Input value
     * @param lowerLimit Lower saturation limit
     * @param upperLimit Upper saturation limit
     * @return Saturated value
     */
    protected double applySaturation(double value, double lowerLimit, double upperLimit) {
        if (value < lowerLimit) return lowerLimit;
        if (value > upperLimit) return upperLimit;
        return value;
    }

    /**
     * Helper method to implement dead zone logic
     * @param value Input value
     * @param lowerThreshold Lower dead zone threshold
     * @param upperThreshold Upper dead zone threshold
     * @return Value with dead zone applied
     */
    protected double applyDeadZone(double value, double lowerThreshold, double upperThreshold) {
        if (value >= lowerThreshold && value <= upperThreshold) {
            return 0.0; // Inside dead zone
        }
        
        if (value > upperThreshold) {
            return value - upperThreshold;
        } else {
            return value - lowerThreshold;
        }
    }

    /**
     * Helper method to implement relay logic
     * @param value Input value
     * @param threshold Switching threshold
     * @param onValue Output value when input > threshold
     * @param offValue Output value when input <= threshold
     * @return Relay output
     */
    protected double applyRelay(double value, double threshold, double onValue, double offValue) {
        return value > threshold ? onValue : offValue;
    }
}