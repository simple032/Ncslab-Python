package com.ncslab.circuit.validation;

import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.element.*;
import com.ncslab.circuit.builder.CircuitAssembly;

import java.util.*;

/**
 * Validates circuit topology for common errors and issues.
 * Detects short circuits, open circuits, floating nodes, and invalid configurations.
 */
public class CircuitTopologyValidator {

    /**
     * Validate a circuit assembly
     * @param assembly Circuit assembly to validate
     * @return Validation result
     */
    public CircuitValidationResult validate(CircuitAssembly assembly) {
        long startTime = System.nanoTime();
        CircuitValidationResult result = new CircuitValidationResult();

        // Run all validation checks
        validateComponentCount(assembly, result);
        validateVoltageSourceConfiguration(assembly, result);
        validateGroundConnections(assembly, result);
        validateShortCircuits(assembly, result);
        validateFloatingNodes(assembly, result);
        validateCompatibility(assembly, result);

        long endTime = System.nanoTime();
        result.setValidationTimeMs((endTime - startTime) / 1_000_000);

        return result;
    }

    /**
     * Validate component count and basic configuration
     * @param assembly Circuit assembly
     * @param result Validation result
     */
    private void validateComponentCount(CircuitAssembly assembly, CircuitValidationResult result) {
        int componentCount = assembly.getComponentCount();

        if (componentCount == 0) {
            result.addError(ValidationIssue.error(
                ValidationIssue.Category.INVALID_CONFIGURATION,
                "Circuit has no components",
                assembly.getName()
            ));
        } else if (componentCount == 1) {
            result.addWarning(ValidationIssue.warning(
                ValidationIssue.Category.INVALID_CONFIGURATION,
                "Circuit has only one component - may not be functional",
                assembly.getName()
            ));
        }
    }

    /**
     * Validate voltage source configuration
     * @param assembly Circuit assembly
     * @param result Validation result
     */
    private void validateVoltageSourceConfiguration(CircuitAssembly assembly, CircuitValidationResult result) {
        int dcSourceCount = assembly.getDcSources().size();
        int acSourceCount = assembly.getAcSources().size();
        int totalSources = dcSourceCount + acSourceCount;

        if (totalSources == 0) {
            result.addWarning(ValidationIssue.warning(
                ValidationIssue.Category.INVALID_CONFIGURATION,
                "Circuit has no voltage sources - circuit will not be powered",
                assembly.getName(),
                "Add at least one DC or AC voltage source"
            ));
        }

        // Detect potential short circuit: multiple voltage sources in series
        if (totalSources > 1) {
            result.addWarning(ValidationIssue.warning(
                ValidationIssue.Category.SHORT_CIRCUIT,
                "Circuit has multiple voltage sources - verify they are not in series",
                assembly.getName(),
                "Ensure voltage sources are properly isolated or in parallel with adequate impedance"
            ));
        }

        // Check for mixed AC and DC sources
        if (dcSourceCount > 0 && acSourceCount > 0) {
            result.addWarning(ValidationIssue.warning(
                ValidationIssue.Category.INVALID_CONFIGURATION,
                "Circuit mixes DC and AC voltage sources - verify intended behavior",
                assembly.getName()
            ));
        }
    }

    /**
     * Validate ground connections
     * @param assembly Circuit assembly
     * @param result Validation result
     */
    private void validateGroundConnections(CircuitAssembly assembly, CircuitValidationResult result) {
        // Check if circuit has a proper reference point
        boolean hasGroundReference = false;

        // In circuit simulation, at least one node should be grounded (reference point)
        // This is a simplified check - in real implementation, we'd analyze the circuit graph

        List<Object> allComponents = assembly.getAllComponents();
        if (allComponents.isEmpty()) {
            return;
        }

        // For now, we'll just warn if there's no explicit ground
        // In a full implementation, we'd check the circuit connectivity graph
        result.addWarning(ValidationIssue.warning(
            ValidationIssue.Category.MISSING_GROUND,
            "Ground reference point should be explicitly defined for simulation stability",
            assembly.getName(),
            "Ensure circuit has a proper ground reference"
        ));
    }

    /**
     * Detect short circuits in the circuit
     * @param assembly Circuit assembly
     * @param result Validation result
     */
    private void validateShortCircuits(CircuitAssembly assembly, CircuitValidationResult result) {
        // Check for direct voltage source to voltage source connections
        List<DCVoltageSource> dcSources = assembly.getDcSources();
        List<ACVoltageSource> acSources = assembly.getAcSources();

        if (dcSources.size() > 1) {
            // Multiple DC sources - check if they're directly connected
            result.addWarning(ValidationIssue.warning(
                ValidationIssue.Category.SHORT_CIRCUIT,
                String.format("Circuit has %d DC voltage sources - ensure they're not directly connected",
                    dcSources.size()),
                assembly.getName(),
                "Add series impedance (resistors) between voltage sources"
            ));
        }

        // Check for voltage source with zero total impedance path
        validateZeroImpedancePath(assembly, result);
    }

    /**
     * Check for zero impedance paths in circuit
     * @param assembly Circuit assembly
     * @param result Validation result
     */
    private void validateZeroImpedancePath(CircuitAssembly assembly, CircuitValidationResult result) {
        List<Resistor> resistors = assembly.getResistors();
        List<Inductor> inductors = assembly.getInductors();
        List<Capacitor> capacitors = assembly.getCapacitors();

        int totalPassiveElements = resistors.size() + inductors.size() + capacitors.size();

        // If we have voltage sources but no passive elements, that's a potential short circuit
        int totalSources = assembly.getDcSources().size() + assembly.getAcSources().size();
        if (totalSources > 0 && totalPassiveElements == 0) {
            result.addError(ValidationIssue.error(
                ValidationIssue.Category.SHORT_CIRCUIT,
                "Circuit has voltage sources but no passive elements - creates direct short circuit",
                assembly.getName(),
                "Add at least one resistor, capacitor, or inductor to limit current"
            ));
        }

        // Check for resistors with very low resistance (< 0.001 Ω)
        for (Resistor resistor : resistors) {
            // In a full implementation, we'd check the actual resistance value
            // For now, this is a placeholder for the concept
        }
    }

    /**
     * Detect floating nodes (disconnected components)
     * @param assembly Circuit assembly
     * @param result Validation result
     */
    private void validateFloatingNodes(CircuitAssembly assembly, CircuitValidationResult result) {
        // In a real implementation, we'd build a connectivity graph and check for disconnected subgraphs
        // For now, we'll do a simplified check

        List<Object> components = assembly.getAllComponents();
        if (components.isEmpty()) {
            return;
        }

        // Simplified check: if component count is high but no clear circuit structure
        // In reality, we'd need the actual circuit connection information
        int componentCount = components.size();
        if (componentCount > 10) {
            result.addWarning(ValidationIssue.warning(
                ValidationIssue.Category.FLOATING_NODE,
                "Large circuit - verify all components are properly connected",
                assembly.getName(),
                "Review circuit connectivity to ensure no floating nodes"
            ));
        }
    }

    /**
     * Validate component compatibility and configuration
     * @param assembly Circuit assembly
     * @param result Validation result
     */
    private void validateCompatibility(CircuitAssembly assembly, CircuitValidationResult result) {
        // Check capacitor and inductor configurations
        List<Capacitor> capacitors = assembly.getCapacitors();
        List<Inductor> inductors = assembly.getInductors();

        // Check for series capacitors (DC blocking) without DC path
        if (capacitors.size() > 0 && assembly.getDcSources().size() > 0 && assembly.getResistors().isEmpty()) {
            result.addWarning(ValidationIssue.warning(
                ValidationIssue.Category.INVALID_CONFIGURATION,
                "Circuit has capacitors and DC source but no resistive path - DC current will be blocked",
                assembly.getName(),
                "Add a resistor to provide DC path if needed"
            ));
        }

        // Check for resonant circuits (LC combination)
        if (inductors.size() > 0 && capacitors.size() > 0) {
            result.addWarning(ValidationIssue.warning(
                ValidationIssue.Category.TOPOLOGY_ERROR,
                "Circuit contains LC combination - may exhibit resonance",
                assembly.getName(),
                "Verify resonant frequency is within intended operating range"
            ));
        }
    }

    /**
     * Quick validation check (basic checks only)
     * @param assembly Circuit assembly
     * @return true if basic validation passes
     */
    public boolean quickValidate(CircuitAssembly assembly) {
        if (assembly.getComponentCount() == 0) {
            return false;
        }

        int totalSources = assembly.getDcSources().size() + assembly.getAcSources().size();
        int totalPassive = assembly.getResistors().size() +
                          assembly.getInductors().size() +
                          assembly.getCapacitors().size();

        // Must have at least one source and one passive element
        return totalSources > 0 && totalPassive > 0;
    }

    /**
     * Validate individual circuit block parameters
     * @param block Circuit block to validate
     * @return Validation result
     */
    public CircuitValidationResult validateBlock(CircuitBlock block) {
        CircuitValidationResult result = new CircuitValidationResult();

        if (block == null) {
            result.addError(ValidationIssue.error(
                ValidationIssue.Category.PARAMETER_ERROR,
                "Circuit block is null",
                "Unknown"
            ));
            return result;
        }

        // Validate block-specific parameters
        String blockName = block.getBlockName();

        // Check for common issues
        if (blockName == null || blockName.trim().isEmpty()) {
            result.addError(ValidationIssue.error(
                ValidationIssue.Category.PARAMETER_ERROR,
                "Block name is null or empty",
                "Unknown block"
            ));
        }

        return result;
    }
}
