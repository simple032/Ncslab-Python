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

        // Add pre-computed parameter information with standardized names
        java.util.List<String> paramNames = new java.util.ArrayList<>();
        java.util.List<String> paramVariables = new java.util.ArrayList<>();
        java.util.List<Object> paramValues = new java.util.ArrayList<>();
        
        for (com.ncslab.block.io.Parameter param : block.getParameterList()) {
            String paramName = param.getName(); // Already includes Block{id}_ prefix
            String paramLocalName = param.getLocalName(); // Local name without prefix
            String paramVar = paramName; // Use the full name as-is, already properly prefixed
            Object paramValue = param.getData().getInitValue();

            paramNames.add(paramName);
            paramVariables.add(paramVar);
            paramValues.add(paramValue);

            // Individual parameter access using local name to avoid double prefixing
            context.put(paramLocalName, paramVar); // C variable name as string
            context.put(paramLocalName + "Name", paramVar); // Explicit C variable name
            context.put(paramLocalName + "Value", paramValue);
            context.put(paramLocalName + "Object", param); // Keep object for advanced access if needed

            // Add generic template variable names for backward compatibility using local name
            if ("Gain".equals(paramLocalName)) {
                context.put("parameterName", paramVar); // For Gain block templates
                context.put("gainName", paramVar);
            }
            if ("SampleTime".equals(paramLocalName)) {
                context.put("sampleTimeName", paramVar); // For discrete block templates
            }
            if ("offset".equals(paramLocalName) || "Offset".equals(paramLocalName)) {
                context.put("offsetName", paramVar);
                context.put("offsetInitCodeC", paramVar + " = " + paramValue + ";");
            }

            // PID Controller specific parameter mappings using local name
            if ("P".equals(paramLocalName)) {
                context.put("proportionalGainName", paramVar);
            }
            if ("I".equals(paramLocalName)) {
                context.put("integralGainName", paramVar);
            }
            if ("D".equals(paramLocalName)) {
                context.put("derivativeGainName", paramVar);
            }
            if ("N".equals(paramLocalName)) {
                context.put("filterCoefficientName", paramVar);
            }
        }
        
        // Standardized parameter collections
        context.put("paramNames", paramNames);
        context.put("paramVariables", paramVariables);
        context.put("paramValues", paramValues);

        // Add pre-computed state information with standardized names
        java.util.List<String> stateNames = new java.util.ArrayList<>();
        java.util.List<String> stateVariables = new java.util.ArrayList<>();
        java.util.List<Object> stateValues = new java.util.ArrayList<>();

        for (com.ncslab.block.io.State state : block.getStateList()) {
            String stateName = state.getName(); // Already includes Block{id}_State_ prefix
            String stateVar = stateName; // Use as-is, already properly prefixed
            String stateLocalName = state.getLocalName(); // Local name without prefix
            Object stateValue = state.getData().getInitValue();

            stateNames.add(stateName);
            stateVariables.add(stateVar);
            stateValues.add(stateValue);

            // Individual state access (legacy) - use full state name as key
            context.put(stateName, stateVar); // C variable name as string
            context.put(stateName + "Name", stateVar); // Explicit C variable name
            context.put(stateName + "Value", stateValue);
            context.put(stateName + "Object", state); // Keep object for advanced access if needed

            // Add local name mapping to avoid double prefixing
            context.put(stateLocalName, stateVar); // Map local name to full C variable name
            context.put(stateLocalName + "Name", stateVar); // Explicit mapping for template access
            context.put(stateLocalName + "Value", stateValue);

            // Add generic template variable names for backward compatibility
            if (stateName.contains("stateX") || stateName.contains("stateOutput")) {
                context.put("stateOutputName", stateVar);
            }
        }
        
        // Standardized state collections
        context.put("stateNames", stateNames);
        context.put("stateVariables", stateVariables);
        context.put("stateValues", stateValues);

        // Add standardized input/output signal variable names (C variable names as strings)
        if (!block.getInputPortList().isEmpty()) {
            context.put("inputVar", block.getInputPortVariable(0));
            context.put("inputSignal", block.getInputPortVariable(0));  // Backward compatibility
        }
        if (!block.getOutputPortList().isEmpty()) {
            context.put("outputVar", block.getOutputPortVariable(0));
            context.put("outputSignal", block.getOutputPortVariable(0));  // Backward compatibility
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
        
        // Add signal dimensions for compatibility with existing templates
        if (!block.getInputPortList().isEmpty()) {
            context.put("signalWidth", block.getInputPortList().get(0).getWidth());
            context.put("signalHeight", block.getInputPortList().get(0).getHeight());
            context.put("wMax", block.getInputPortList().get(0).getWidth() - 1);
            context.put("hMax", block.getInputPortList().get(0).getHeight() - 1);
        }
        
        // Add template-compatible signal variable names
        if (!block.getInputPortList().isEmpty()) {
            context.put("inputSignalName", block.getInputPortVariable(0));
        }
        if (!block.getOutputPortList().isEmpty()) {
            context.put("outputSignalName", block.getOutputPortVariable(0));
        }

        // Add parameter dimension information (for blocks like Gain that need gainHeight/gainWidth)
        for (com.ncslab.block.io.Parameter param : block.getParameterList()) {
            String paramLocalName = param.getLocalName(); // Use local name to avoid double prefixing
            context.put(paramLocalName + "Height", param.getHeight());
            context.put(paramLocalName + "Width", param.getWidth());
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

        // Pre-computed Input/Output variables for standardized template access
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
            
            // Add individual input variables with standardized names
            for (int i = 0; i < inputs.size(); i++) {
                context.put("inputVar" + (i + 1), inputs.get(i));
                context.put("input" + (i + 1), inputs.get(i));  // Backward compatibility
            }
        }

        if (!block.getOutputPortList().isEmpty()) {
            java.util.List<String> outputs = new java.util.ArrayList<>();
            for (int i = 0; i < block.getOutputPortList().size(); i++) {
                outputs.add(block.getOutputPortVariable(i));
            }
            context.put("outputs", outputs);
            
            // Add individual output variables with standardized names  
            for (int i = 0; i < outputs.size(); i++) {
                context.put("outputVar" + (i + 1), outputs.get(i));
                context.put("output" + (i + 1), outputs.get(i));  // Backward compatibility
            }
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

        // Support for array-style access: $math.sub[$width][1] should return $width - 1
        // This creates a nested array structure for subtraction operations
        public java.util.Map<Integer, java.util.Map<Integer, Integer>> sub = new java.util.HashMap<Integer, java.util.Map<Integer, Integer>>() {
            @Override
            public java.util.Map<Integer, Integer> get(Object key) {
                if (key instanceof Integer) {
                    final int outerIndex = (Integer) key;
                    return new java.util.HashMap<Integer, Integer>() {
                        @Override
                        public Integer get(Object innerKey) {
                            if (innerKey instanceof Integer) {
                                int innerIndex = (Integer) innerKey;
                                if (innerIndex == 1) {
                                    return outerIndex - 1;  // $math.sub[$width][1] = $width - 1
                                }
                            }
                            return 0;
                        }
                    };
                }
                return new java.util.HashMap<>();
            }
        };
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
