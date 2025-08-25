package com.ncslab.block.continuous;

import com.ncslab.block.Block;
import com.ncslab.block.io.State;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all continuous-time blocks.
 * Provides common implementations for continuous system operations.
 */
public abstract class ContinuousBlock extends Block {

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    /**
     * DTO-NATIVE Constructor
     */
    protected ContinuousBlock(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
    }

    /**
     * Legacy JSON Constructor
     */
    protected ContinuousBlock(JSONObject blockJson, NCSLabModel model) {
        super(blockJson, model);
    }

        public void calculateInit() {
        // Initialize continuous system states        
        
        // Set initial output
        if (!outputPortList.isEmpty()) {
            OutputPort output = outputPortList.get(0);
            output.setData(getInitialOutput());
        }
    }

        public void calculateOutput(double t) {
        // Default implementation - continuous blocks should override this method
        // with their specific continuous-time calculations
    }

        public void calculateDerivative(double t) {
        // Calculate state derivatives for continuous-time integration
        
    }

        public void calculateUpdate(double t) {
        // Continuous blocks typically don't need discrete updates
        // But can be overridden for hybrid systems
    }

        public void calculateDiscreteUpdate(double t) {
        // Continuous blocks typically don't have discrete updates
    }

        public void calculateTerminate(double t) {
        // Clean up continuous system resources
        cleanupContinuousResources();
    }

    /**
     * Get initial output value (default implementation)
     * @return Initial output data
     */
    protected com.ncslab.block.data.Data getInitialOutput() {
        return new com.ncslab.block.data.Data(0.0);
    }

    /**
     * Clean up continuous system resources (default implementation)
     */
    protected void cleanupContinuousResources() {
        // Default: no cleanup needed
    }

    /**
     * Helper method to get current input
     */
    protected com.ncslab.block.data.Data getCurrentInput() {
        if (!inputPortList.isEmpty()) {
            return inputPortList.get(0).getData();
        }
        return new com.ncslab.block.data.Data(0.0);
    }

    /**
     * Helper method to set state derivative
     */
    protected void setStateDerivative(State state, com.ncslab.block.data.Data derivative) {
        if (state != null) {
            state.setDerivateData(derivative);
        }
    }
}