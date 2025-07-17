package com.ncslab.util;

import org.apache.velocity.VelocityContext;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.ncslablink.ModelMode;

/**
 * Utility class for standardizing Velocity template context population.
 * Provides common variables and helper methods for template rendering.
 */
public class TemplateUtils {

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
                inputs.add(block.getInputPortVariable(i));
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
