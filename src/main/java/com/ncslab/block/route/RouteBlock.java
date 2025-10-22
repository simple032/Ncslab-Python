package com.ncslab.block.route;

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
 * Base class for all signal routing blocks (Mux, Demux, Switch, etc.).
 * Provides common implementations for signal routing operations.
 */
public abstract class RouteBlock extends Block {

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Note: Input/output names are defined by specific routing blocks

    /**
     * DTO-NATIVE Constructor
     */
    protected RouteBlock(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
    }

    /**
     * Legacy JSON Constructor
     * @deprecated Use DTO-native constructor for new development
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    protected RouteBlock(JSONObject blockJson, NCSLabModel model) {
        super(blockJson, model);
    }

    public void calculateInit() {
        // Initialize routing: set all outputs to zero/default values
        initializeRouting();
        
        for (OutputPort output : outputPortList) {
            output.setData(new Data(0.0));
        }
    }

    @Override
    public abstract void calculateOutput(double t);

    public void calculateDerivative(double t) {
        // Routing blocks are typically stateless - no derivatives
    }

    public void calculateUpdate(double t) {
        // Routing blocks typically don't maintain internal state
        // Override if the routing block has switching logic that needs state updates
    }

        public void calculateDiscreteUpdate(double t) {
        // Default implementation - routing blocks can override if needed
    }

        public void calculateTerminate(double t) {
        // Routing blocks typically don't need cleanup
        cleanupRoutingResources();
    }


    /**
     * Initialize routing-specific parameters (default implementation)
     */
    protected void initializeRouting() {
        // Default: no special routing initialization needed
    }

    /**
     * Update discrete switching logic for blocks with switching behavior
     * @param t Current simulation time
     */
    protected void updateDiscreteSwitchingLogic(double t) {
        // Default: no discrete switching logic
    }

    /**
     * Clean up routing resources (default implementation)
     */
    protected void cleanupRoutingResources() {
        // Default: no cleanup needed
    }

    /**
     * Helper method to route input to output directly
     */
    protected void routeInputToOutput(int inputIndex, int outputIndex) {
        if (inputIndex >= 0 && inputIndex < inputPortList.size() &&
            outputIndex >= 0 && outputIndex < outputPortList.size()) {
            
            Data inputData = inputPortList.get(inputIndex).getData();
            outputPortList.get(outputIndex).setData(inputData);
        }
    }

    /**
     * Helper method to route multiple inputs to single output (multiplexing)
     */
    protected void routeInputsToOutput(List<Integer> inputIndices, int outputIndex) {
        if (outputIndex >= 0 && outputIndex < outputPortList.size()) {
            // This is a simplified implementation - subclasses should override
            // with proper multiplexing logic based on their specific requirements
            
            if (!inputIndices.isEmpty() && inputIndices.get(0) < inputPortList.size()) {
                Data inputData = inputPortList.get(inputIndices.get(0)).getData();
                outputPortList.get(outputIndex).setData(inputData);
            }
        }
    }

    /**
     * Helper method to route single input to multiple outputs (demultiplexing)
     */
    protected void routeInputToOutputs(int inputIndex, List<Integer> outputIndices) {
        if (inputIndex >= 0 && inputIndex < inputPortList.size()) {
            Data inputData = inputPortList.get(inputIndex).getData();
            
            for (Integer outputIndex : outputIndices) {
                if (outputIndex >= 0 && outputIndex < outputPortList.size()) {
                    outputPortList.get(outputIndex).setData(inputData);
                }
            }
        }
    }
}