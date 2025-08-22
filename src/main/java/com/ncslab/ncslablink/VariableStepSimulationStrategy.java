package com.ncslab.ncslablink;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import jakarta.websocket.Session;
import java.io.IOException;

/**
 * Strategy for variable-step simulation execution
 * Uses adaptive step size integration with step interpolation
 */
public class VariableStepSimulationStrategy implements SimulationStrategy {

    @Override
    public void execute(SimulationModel.SimulationContext context,
                       FirstOrderDifferentialEquations systemODE,
                       FirstOrderIntegrator integrator,
                       Session session,
                       SimulationModel model) throws IOException, ModelException {
        
        System.out.println("Using variable-step integration with adaptive step handler");
        
        StepHandler stepHandler = createStepHandler(context, session, model);
        integrator.addStepHandler(stepHandler);
        
        double tEndActual = integrator.integrate(systemODE, context.getTStart(), 
                                               model.getStates(), context.getTEnd(), model.getStates());
        
        System.out.printf("Variable-step simulation completed: actual_end=%.6f (target=%.6f)%n", 
                         tEndActual, context.getTEnd());
    }

    @Override
    public String getDescription() {
        return "Variable-step integration with adaptive step control";
    }

    @Override
    public boolean isApplicable(SimulationModel.SimulationContext context) {
        return context.hasStates() && context.isVariableStep();
    }
    
    /**
     * Create step handler for variable-step simulation
     */
    private StepHandler createStepHandler(SimulationModel.SimulationContext context, 
                                        Session session, SimulationModel model) {
        return new StepHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
                // Initialization if needed
            }

            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                double currentTime = interpolator.getCurrentTime();
                double[] state = interpolator.getInterpolatedState();

                try {
                    model.calculateDiscreteUpdates(currentTime);

                    // Send time series message with throttling
                    if (shouldSendMessage(currentTime, context.getMinStep())) {
                        model.sendSimulatingMessage(session, currentTime);
                    }
                    
                    if (isLast) {
                        model.calculateTerminates(currentTime);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to send simulation message", e);
                }
            }
        };
    }
    
    /**
     * Determine if a simulation message should be sent based on time throttling
     */
    private boolean shouldSendMessage(double currentTime, double minStep) {
        return currentTime - Math.floor(currentTime) < minStep;
    }
}