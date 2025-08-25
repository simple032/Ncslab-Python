package com.ncslab.block.math;

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
 * Base class for all mathematical operation blocks.
 * Provides common implementations for standard mathematical operations.
 */
public abstract class MathBlock extends Block {

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Most math blocks have one output
        outputNames.add("out1");
        // Most math blocks have one or more inputs (defined by subclasses)
    }

    /**
     * DTO-NATIVE Constructor
     */
    protected MathBlock(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
    }

    /**
     * Legacy JSON Constructor
     */
    protected MathBlock(JSONObject blockJson, NCSLabModel model) {
        super(blockJson, model);
    }

        public void calculateInit() {
        // Initialize output to zero for mathematical operations
        if (!outputPortList.isEmpty()) {
            OutputPort output = outputPortList.get(0);
            output.setData(new Data(0.0));
        }
    }

        public void calculateOutput(double t) {
        // Default implementation - mathematical blocks should override this method
        // with their specific mathematical operations
    }

        public void calculateDerivative(double t) {
        // Mathematical blocks are typically stateless - no derivatives
    }

        public void calculateUpdate(double t) {
        // Mathematical blocks typically don't maintain state
    }

        public void calculateDiscreteUpdate(double t) {
        // Mathematical blocks are typically continuous operations
    }

        public void calculateTerminate(double t) {
        // Mathematical blocks typically don't need cleanup
    }

    /**
     * Helper method to get input data safely
     */
    protected Data getInputData(int portIndex) {
        if (portIndex >= 0 && portIndex < inputPortList.size()) {
            InputPort port = inputPortList.get(portIndex);
            return port.getData();
        }
        return new Data(0.0); // Default to zero if port doesn't exist
    }

    /**
     * Helper method to set output data safely
     */
    protected void setOutputData(int portIndex, Data data) {
        if (portIndex >= 0 && portIndex < outputPortList.size()) {
            OutputPort port = outputPortList.get(portIndex);
            port.setData(data);
        }
    }

    /**
     * Helper method to set primary output (port 0)
     */
    protected void setOutput(Data data) {
        setOutputData(0, data);
    }

    /**
     * Helper method to get primary input (port 0)
     */
    protected Data getInput() {
        return getInputData(0);
    }
}