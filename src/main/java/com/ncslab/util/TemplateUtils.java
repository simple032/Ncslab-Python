package com.ncslab.util;

import org.apache.velocity.VelocityContext;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.ncslablink.ModelMode;

/**
 * Utility class for standardizing Velocity template context population.
 * Provides common variables and helper methods for template rendering.
 */
public class TemplateUtils {

    /**
     * Populates the VelocityContext with all available block variables including
     * parameters, states, ports, and comprehensive context information.
     *
     * @param context The VelocityContext to populate
     * @param block The block instance for context-specific information
     */
    public static void populateAllContext(VelocityContext context, Block block) {
        // Use standard context as base
        populateStandardContext(context, block);

        // Add all parameter defaults if available
//        if (block.getParameterDefaults() != null) {
//            context.put("parameterDefaults", block.getParameterDefaults());
//
//            // Add individual parameter values for easy template access
//            java.util.Map<String, Object> paramDefaults = block.getParameterDefaults();
//            for (java.util.Map.Entry<String, Object> entry : paramDefaults.entrySet()) {
//                context.put(entry.getKey(), entry.getValue());
//            }
//        }

        // Add detailed parameter information - both C variable names and values
        for (com.ncslab.block.io.Parameter param : block.getParameterList()) {
            context.put(param.getName(), param.getName()); // C variable name as string
            context.put(param.getName() + "Name", param.getName()); // Explicit C variable name
            context.put(param.getName() + "Value", param.getData().getInitValue());
            context.put(param.getName() + "Object", param); // Keep object for advanced access if needed
        }

        // Add detailed state information - both C variable names and values
        for (com.ncslab.block.io.State state : block.getStateList()) {
            context.put(state.getName(), state.getName()); // C variable name as string
            context.put(state.getName() + "Name", state.getName()); // Explicit C variable name
            context.put(state.getName() + "Value", state.getData().getInitValue());
            context.put(state.getName() + "Object", state); // Keep object for advanced access if needed
        }

        // Add input/output signal variable names (C variable names as strings)
        if (!block.getInputPortList().isEmpty()) {
            context.put("inputSignal", block.getInputPortVariable(0));
        }
        if (!block.getOutputPortList().isEmpty()) {
            context.put("outputSignal", block.getOutputPortVariable(0));
        }

        // Add block-specific configuration
//        context.put("sampleTime", block.getSampleTime());
//        context.put("isDiscrete", block.isDiscrete());
//        context.put("isContinuous", block.isContinuous());
//        context.put("hasDirectFeedthrough", block.hasDirectFeedthrough());

        // Add dimension information for template loops
        context.put("inputWidth", !block.getInputPortList().isEmpty() ? block.getInputPortList().get(0).getWidth() : 0);
        context.put("inputHeight", !block.getInputPortList().isEmpty() ? block.getInputPortList().get(0).getHeight() : 0);
        context.put("outputWidth", !block.getOutputPortList().isEmpty() ? block.getOutputPortList().get(0).getWidth() : 0);
        context.put("outputHeight", !block.getOutputPortList().isEmpty() ? block.getOutputPortList().get(0).getHeight() : 0);

        // Add parameter dimension information (for blocks like Gain that need gainHeight/gainWidth)
        for (com.ncslab.block.io.Parameter param : block.getParameterList()) {
            context.put(param.getName() + "Height", param.getHeight());
            context.put(param.getName() + "Width", param.getWidth());
        }

        // Add DataType constants for template comparisons
        context.put("DataType", com.ncslab.block.data.DataType.class);

        // Add block information object for template access
        context.put("blockInfo", block);

        // Add common template variables
        context.put("matrixMultiplication", false); // Default for most blocks
    }

    /**
     * Populates the VelocityContext with standard variables that are commonly
     * used across all block templates.
     *
     * @param context The VelocityContext to populate
     * @param block The block instance for context-specific information
     */
    public static void populateStandardContext(VelocityContext context, Block block) {
        // Basic block information
        context.put("block", block);
        context.put("blockId", block.getBlockId());
        context.put("blockName", block.getBlockName());
        context.put("blockType", block.getClass().getSimpleName());

        // Data types for template comparisons
        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);

        // Model information
        if (block.getModel() != null) {
            context.put("model", block.getModel());
            context.put("modelMode", block.getModel().getModelMode());
            context.put("solver", block.getModel().getConfig().getSolver());
        }

        // Common mode constants
        context.put("compilationMode", ModelMode.Compilation);
        context.put("simulationMode", ModelMode.Simulation);

        // Block ports and parameters
        context.put("inputPorts", block.getInputPortList());
        context.put("outputPorts", block.getOutputPortList());
        context.put("parameters", block.getParameterList());
        context.put("states", block.getStateList());

        // Input/Output variables for easy access
        if (!block.getInputPortList().isEmpty()) {
            java.util.List<String> inputs = new java.util.ArrayList<>();
            for (int i = 0; i < block.getInputPortList().size(); i++) {
                InputPort inputPort = block.getInputPortList().get(i);
                if (inputPort.getLinkedLine() != null && inputPort.getLinkedLine().getLinkedOutputPort() != null &&
                    inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC() != null) {
                    inputs.add(block.getInputPortVariable(i));
                } else {
                    // For unconnected inputs, add a default value of 0.0
                    inputs.add("0.0");
                }
            }
            context.put("inputs", inputs);
        }

        if (!block.getOutputPortList().isEmpty()) {
            java.util.List<String> outputs = new java.util.ArrayList<>();
            for (int i = 0; i < block.getOutputPortList().size(); i++) {
                outputs.add(block.getOutputPortVariable(i));
            }
            context.put("outputs", outputs);
        }

        // Utility functions for templates
        context.put("math", new MathUtils());
        context.put("string", new StringUtils());
    }

    /**
     * Math utility functions for use in templates
     */
    public static class MathUtils {
        public int add(int a, int b) { return a + b; }
        public int sub(int a, int b) { return a - b; }
        public int mul(int a, int b) { return a * b; }
        public int div(int a, int b) { return b != 0 ? a / b : 0; }
        public int max(int a, int b) { return Math.max(a, b); }
        public int min(int a, int b) { return Math.min(a, b); }
        public int abs(int a) { return Math.abs(a); }
    }

    /**
     * String utility functions for use in templates
     */
    public static class StringUtils {
        public String capitalize(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }

        public String toLowerCase(String str) {
            return str != null ? str.toLowerCase() : "";
        }

        public String toUpperCase(String str) {
            return str != null ? str.toUpperCase() : "";
        }

        public boolean isEmpty(String str) {
            return str == null || str.trim().isEmpty();
        }

        public String replace(String str, String target, String replacement) {
            return str != null ? str.replace(target, replacement) : "";
        }
    }
}
