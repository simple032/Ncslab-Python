package com.ncslab.ncslablink;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;

import jakarta.websocket.Session;
import java.io.IOException;

/**
 * Strategy for stateless simulation execution
 * Handles systems without state variables (algebraic equations only)
 */
public class StatelessSimulationStrategy implements SimulationStrategy {

    @Override
    public void execute(SimulationModel.SimulationContext context,
                       FirstOrderDifferentialEquations systemODE,
                       FirstOrderIntegrator integrator,
                       Session session,
                       SimulationModel model) throws IOException, ModelException {
        
        System.out.println("Using stateless simulation (algebraic equations only)");
        
        double t = context.getTStart();
        boolean finalTimeProcessed = false;
        
        while (t < context.getTEnd()) {
            model.calculateOutputs(t);
            model.calculateDiscreteUpdates(t);
            
            // Send time series message with throttling
            if (shouldSendMessage(t, context.getMinStep())) {
                model.sendSimulatingMessage(session, t);
            }
            
            t += context.getStep();
            
            // Check if next step would overshoot the end time
            if (t >= context.getTEnd()) {
                processStatelessFinalTimePoint(context, session, model);
                finalTimeProcessed = true;
                break;
            }
        }
        
        // Safety check: ensure final time is always processed
        if (!finalTimeProcessed) {
            System.out.printf("Warning: Processing final time point as safety measure: t=%.6f, tEnd=%.6f%n", 
                             t, context.getTEnd());
            processStatelessFinalTimePoint(context, session, model);
        }
        
        System.out.printf("Stateless simulation completed: final_time=%.6f%n", context.getTEnd());
    }

    @Override
    public String getDescription() {
        return "Stateless simulation for algebraic equations";
    }

    @Override
    public boolean isApplicable(SimulationModel.SimulationContext context) {
        return !context.hasStates();
    }
    
    /**
     * Process the final time point for stateless simulation
     */
    private void processStatelessFinalTimePoint(SimulationModel.SimulationContext context, 
                                              Session session, SimulationModel model) throws IOException, ModelException {
        model.calculateOutputs(context.getTEnd());
        model.calculateDiscreteUpdates(context.getTEnd());
        model.sendSimulatingMessage(session, context.getTEnd());
    }
    
    /**
     * Determine if a simulation message should be sent based on time throttling
     */
    private boolean shouldSendMessage(double currentTime, double minStep) {
        return currentTime - Math.floor(currentTime) < minStep;
    }
}