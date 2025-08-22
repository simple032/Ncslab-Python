package com.ncslab.ncslablink;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;

import jakarta.websocket.Session;
import java.io.IOException;

/**
 * Strategy interface for different simulation execution strategies
 * Implements the Strategy pattern to handle different types of simulations
 */
public interface SimulationStrategy {
    
    /**
     * Execute the simulation strategy
     * @param context The simulation context containing all parameters
     * @param systemODE The system of differential equations (may be null for stateless)
     * @param integrator The numerical integrator (may be null for stateless)
     * @param session WebSocket session for real-time updates
     * @param model Reference to the simulation model for callbacks
     * @throws IOException if WebSocket communication fails
     * @throws ModelException if simulation execution fails
     */
    void execute(SimulationModel.SimulationContext context,
                FirstOrderDifferentialEquations systemODE,
                FirstOrderIntegrator integrator,
                Session session,
                SimulationModel model) throws IOException, ModelException;
    
    /**
     * Get a human-readable description of this strategy
     * @return strategy description
     */
    String getDescription();
    
    /**
     * Check if this strategy is applicable for the given context
     * @param context The simulation context
     * @return true if this strategy can handle the given context
     */
    boolean isApplicable(SimulationModel.SimulationContext context);
}