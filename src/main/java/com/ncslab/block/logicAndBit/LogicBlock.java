package com.ncslab.block.logicAndBit;

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
 * Base class for all logic and bit manipulation blocks.
 * Provides common implementations for logical operations and bit manipulations.
 */
public abstract class LogicBlock extends Block {

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    /**
     * DTO-NATIVE Constructor
     */
    protected LogicBlock(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
    }

    /**
     * Legacy JSON Constructor
     */
    protected LogicBlock(JSONObject blockJson, NCSLabModel model) {
        super(blockJson, model);
    }

        public void calculateInit() {
        // Initialize logic operation
        initializeLogicOperation();
        
        // Set initial output to false/0
        if (!outputPortList.isEmpty()) {
            OutputPort output = outputPortList.get(0);
            output.setData(new Data(0.0)); // Logic false
        }
    }

    @Override
    public abstract void calculateOutput(double t);

        public void calculateDerivative(double t) {
        // Logic blocks are discrete - no derivatives
    }

        public void calculateUpdate(double t) {
        // Logic blocks typically don't maintain state
        // Override if the logic block has memory or latching behavior
    }

        public void calculateDiscreteUpdate(double t) {
        // Default implementation - logic blocks can override if needed
    }

        public void calculateTerminate(double t) {
        // Logic blocks typically don't need cleanup
        cleanupLogicResources();
    }


    /**
     * Initialize logic operation parameters (default implementation)
     */
    protected void initializeLogicOperation() {
        // Default: no special initialization needed
    }

    /**
     * Update discrete logic state for clocked operations (default implementation)
     * @param t Current simulation time
     */
    protected void updateDiscreteLogicState(double t) {
        // Default: no discrete state updates needed
    }

    /**
     * Clean up logic resources (default implementation)
     */
    protected void cleanupLogicResources() {
        // Default: no cleanup needed
    }

    /**
     * Convert double value to boolean (logic interpretation)
     * @param value Input value
     * @return true if value > 0, false otherwise
     */
    protected boolean toBoolean(double value) {
        return value > 0.0;
    }

    /**
     * Convert boolean to double (logic representation)
     * @param value Boolean value
     * @return 1.0 for true, 0.0 for false
     */
    protected double fromBoolean(boolean value) {
        return value ? 1.0 : 0.0;
    }

    /**
     * Get boolean input from specified port
     * @param portIndex Input port index
     * @return Boolean interpretation of input
     */
    protected boolean getBooleanInput(int portIndex) {
        if (portIndex >= 0 && portIndex < inputPortList.size()) {
            return toBoolean(inputPortList.get(portIndex).getData().getInitValue());
        }
        return false; // Default to false if port doesn't exist
    }

    /**
     * Set boolean output to specified port
     * @param portIndex Output port index
     * @param value Boolean value to set
     */
    protected void setBooleanOutput(int portIndex, boolean value) {
        if (portIndex >= 0 && portIndex < outputPortList.size()) {
            outputPortList.get(portIndex).setData(new Data(fromBoolean(value)));
        }
    }

    /**
     * Perform logical AND operation
     * @param a First operand
     * @param b Second operand
     * @return Logical AND result
     */
    protected boolean logicalAnd(boolean a, boolean b) {
        return a && b;
    }

    /**
     * Perform logical OR operation
     * @param a First operand
     * @param b Second operand
     * @return Logical OR result
     */
    protected boolean logicalOr(boolean a, boolean b) {
        return a || b;
    }

    /**
     * Perform logical NOT operation
     * @param a Operand
     * @return Logical NOT result
     */
    protected boolean logicalNot(boolean a) {
        return !a;
    }

    /**
     * Perform logical XOR operation
     * @param a First operand
     * @param b Second operand
     * @return Logical XOR result
     */
    protected boolean logicalXor(boolean a, boolean b) {
        return a ^ b;
    }

    /**
     * Perform comparison operation
     * @param a First value
     * @param b Second value
     * @param operation Comparison operation ("==", "!=", "<", "<=", ">", ">=")
     * @return Comparison result
     */
    protected boolean compare(double a, double b, String operation) {
        switch (operation) {
            case "==": return Math.abs(a - b) < 1e-12; // Account for floating point precision
            case "!=": return Math.abs(a - b) >= 1e-12;
            case "<": return a < b;
            case "<=": return a <= b;
            case ">": return a > b;
            case ">=": return a >= b;
            default: return false;
        }
    }
}