package com.ncslab.ncslablink;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating appropriate simulation strategies
 * Uses the Factory pattern to select the right strategy based on simulation context
 */
public class SimulationStrategyFactory {
    
    private static final List<SimulationStrategy> AVAILABLE_STRATEGIES;
    
    static {
        AVAILABLE_STRATEGIES = new ArrayList<>();
        AVAILABLE_STRATEGIES.add(new VariableStepSimulationStrategy());
        AVAILABLE_STRATEGIES.add(new FixedStepSimulationStrategy());
        AVAILABLE_STRATEGIES.add(new StatelessSimulationStrategy());
    }
    
    /**
     * Create the appropriate simulation strategy based on the context
     * @param context The simulation context containing parameters and solver info
     * @return The most appropriate strategy for the given context
     * @throws ModelException if no suitable strategy is found
     */
    public static SimulationStrategy createStrategy(SimulationModel.SimulationContext context) throws ModelException {
        for (SimulationStrategy strategy : AVAILABLE_STRATEGIES) {
            if (strategy.isApplicable(context)) {
                System.out.printf("Selected simulation strategy: %s%n", strategy.getDescription());
                return strategy;
            }
        }
        
        throw new ModelException(String.format(
            "No suitable simulation strategy found for context: hasStates=%s, isVariableStep=%s, solver=%s",
            context.hasStates(), context.isVariableStep(), context.getSolverName()
        ));
    }
    
    /**
     * Get all available strategies for testing or debugging
     * @return List of all available simulation strategies
     */
    public static List<SimulationStrategy> getAllStrategies() {
        return new ArrayList<>(AVAILABLE_STRATEGIES);
    }
    
    /**
     * Register a custom simulation strategy
     * @param strategy The custom strategy to register
     */
    public static void registerStrategy(SimulationStrategy strategy) {
        if (!AVAILABLE_STRATEGIES.contains(strategy)) {
            AVAILABLE_STRATEGIES.add(0, strategy); // Add at beginning for priority
            System.out.printf("Registered custom simulation strategy: %s%n", strategy.getDescription());
        }
    }
}