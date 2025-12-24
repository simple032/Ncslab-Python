package com.ncslab.circuit.validation;

import com.ncslab.circuit.builder.CircuitAssembly;
import com.ncslab.circuit.builder.CircuitBuilder;
import com.ncslab.ncslablink.NCSLabModel;

/**
 * Example usage of circuit topology validation.
 * Demonstrates how to validate circuits and handle validation results.
 */
public class ValidationExample {

    /**
     * Example: Validate a simple RC circuit
     * @param model Parent model
     */
    public static void validateRCCircuit(NCSLabModel model) {
        // Create a simple RC circuit
        CircuitAssembly circuit = CircuitBuilder.createRCLowPassFilter(
            "TestRC", "/test", model, 1000.0, 1e-6, 5.0);

        // Create validator
        CircuitTopologyValidator validator = new CircuitTopologyValidator();

        // Perform validation
        CircuitValidationResult result = validator.validate(circuit);

        // Check results
        if (result.isValid()) {
            System.out.println("Circuit validation passed!");
            System.out.println(result.getSummary());
        } else {
            System.err.println("Circuit validation failed!");
            System.err.println(result.getDetailedReport());
        }

        // Print warnings even if valid
        if (result.hasWarnings()) {
            System.out.println("\nWarnings:");
            for (ValidationIssue warning : result.getWarnings()) {
                System.out.println("  " + warning);
            }
        }
    }

    /**
     * Example: Validate a circuit with potential issues
     * @param model Parent model
     */
    public static void validateProblematicCircuit(NCSLabModel model) {
        // Create a circuit with potential short circuit (voltage source only)
        CircuitBuilder builder = new CircuitBuilder("BadCircuit", "/test", model);
        builder.addDCSource("V1", 10.0);
        builder.addDCSource("V2", 5.0);  // Two sources without impedance
        CircuitAssembly circuit = builder.build();

        // Validate
        CircuitTopologyValidator validator = new CircuitTopologyValidator();
        CircuitValidationResult result = validator.validate(circuit);

        // Display results
        System.out.println(result.getDetailedReport());

        // Check specific issues
        if (result.hasErrors()) {
            System.out.println("\nCircuit has " + result.getErrors().size() + " critical error(s):");
            for (ValidationIssue error : result.getErrors()) {
                System.out.println("  - " + error.getMessage());
                if (error.getSuggestion() != null) {
                    System.out.println("    Fix: " + error.getSuggestion());
                }
            }
        }
    }

    /**
     * Example: Quick validation for performance-critical scenarios
     * @param model Parent model
     * @return true if circuit passes basic validation
     */
    public static boolean quickValidateCircuit(NCSLabModel model) {
        CircuitAssembly circuit = CircuitBuilder.createRLCSeriesCircuit(
            "QuickTest", "/test", model, 100.0, 0.1, 1e-6, 12.0);

        CircuitTopologyValidator validator = new CircuitTopologyValidator();
        return validator.quickValidate(circuit);
    }

    /**
     * Example: Validate multiple circuits and aggregate results
     * @param model Parent model
     */
    public static void validateMultipleCircuits(NCSLabModel model) {
        CircuitTopologyValidator validator = new CircuitTopologyValidator();
        CircuitValidationResult aggregateResult = new CircuitValidationResult();

        // Create multiple circuits
        CircuitAssembly[] circuits = {
            CircuitBuilder.createRCLowPassFilter("RC1", "/test", model, 1000.0, 1e-6, 5.0),
            CircuitBuilder.createRLCSeriesCircuit("RLC1", "/test", model, 100.0, 0.1, 1e-6, 12.0),
            CircuitBuilder.createVoltageDivider("VDiv1", "/test", model, 1000.0, 2000.0, 9.0)
        };

        // Validate each and aggregate
        for (CircuitAssembly circuit : circuits) {
            CircuitValidationResult result = validator.validate(circuit);
            aggregateResult.merge(result);
            System.out.println(circuit.getName() + ": " + result.getSummary());
        }

        // Display aggregate results
        System.out.println("\n=== Aggregate Results ===");
        System.out.println("Total errors: " + aggregateResult.getErrors().size());
        System.out.println("Total warnings: " + aggregateResult.getWarnings().size());
        System.out.println("Overall valid: " + aggregateResult.isValid());
    }

    /**
     * Example: Custom validation workflow
     * @param model Parent model
     */
    public static void customValidationWorkflow(NCSLabModel model) {
        CircuitBuilder builder = new CircuitBuilder("CustomCircuit", "/test", model);

        // Build circuit step by step
        builder.addDCSource("V1", 12.0);
        builder.addResistor("R1", 1000.0);

        CircuitAssembly circuit = builder.build();
        CircuitTopologyValidator validator = new CircuitTopologyValidator();

        // Validate after each major change
        System.out.println("Initial validation:");
        CircuitValidationResult result1 = validator.validate(circuit);
        System.out.println(result1.getSummary());

        // Add more components
        builder.addCapacitor("C1", 1e-6);
        circuit = builder.build();

        System.out.println("\nAfter adding capacitor:");
        CircuitValidationResult result2 = validator.validate(circuit);
        System.out.println(result2.getSummary());

        // Compare validation times
        System.out.println("\nValidation performance:");
        System.out.println("  First: " + result1.getValidationTimeMs() + " ms");
        System.out.println("  Second: " + result2.getValidationTimeMs() + " ms");
    }

    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        // Note: In real usage, you'd have an actual NCSLabModel instance
        // This is just for demonstration of the API
        System.out.println("Circuit Topology Validation Examples");
        System.out.println("====================================");
        System.out.println("\nThese examples demonstrate the validation API.");
        System.out.println("In actual use, pass a valid NCSLabModel instance.");
    }
}
