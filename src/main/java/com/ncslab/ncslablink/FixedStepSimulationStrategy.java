package com.ncslab.ncslablink;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;

import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Arrays;

/**
 * Strategy for fixed-step simulation execution
 * Uses fixed step size with precise time control
 */
public class FixedStepSimulationStrategy implements SimulationStrategy {

    @Override
    public void execute(SimulationModel.SimulationContext context,
                       FirstOrderDifferentialEquations systemODE,
                       FirstOrderIntegrator integrator,
                       Session session,
                       SimulationModel model) throws IOException, ModelException {
        
        System.out.println("Using fixed-step integration with precise step control");
        performFixedStepIntegration(context, systemODE, integrator, session, model);
    }

    @Override
    public String getDescription() {
        return "Fixed-step integration with precise time control";
    }

    @Override
    public boolean isApplicable(SimulationModel.SimulationContext context) {
        return context.hasStates() && !context.isVariableStep();
    }
    
    /**
     * Performs fixed-step integration with precise output control
     */
    private void performFixedStepIntegration(SimulationModel.SimulationContext context,
                                           FirstOrderDifferentialEquations systemODE,
                                           FirstOrderIntegrator integrator,
                                           Session session,
                                           SimulationModel model) throws IOException, ModelException {

        double currentTime = context.getTStart();
        double[] currentStates = Arrays.copyOf(model.getStates(), model.getStates().length);

        // Calculate the exact number of steps needed
        int totalSteps = (int) Math.round((context.getTEnd() - context.getTStart()) / context.getStep());
        double actualStep = (context.getTEnd() - context.getTStart()) / totalSteps;

        System.out.printf("Fixed-step integration: start=%.6f, end=%.6f, steps=%d, actualStep=%.6f%n",
            context.getTStart(), context.getTEnd(), totalSteps, actualStep);

        // Initialize and send initial conditions
        model.calculateOutputs(currentTime);
        model.calculateDiscreteUpdates(currentTime);
        model.sendSimulatingMessage(session, currentTime);

        // Perform step-by-step integration
        for (int stepIndex = 1; stepIndex <= totalSteps; stepIndex++) {
            double targetTime = context.getTStart() + stepIndex * actualStep;

            // Clamp to exact end time to avoid floating-point overshoot
            if (stepIndex == totalSteps) {
                targetTime = context.getTEnd();
            }

            // Integrate from current time to target time
            double actualEndTime = integrator.integrate(systemODE, currentTime, currentStates, targetTime, currentStates);

            // Update current time
            currentTime = actualEndTime;

            // Calculate outputs and discrete updates at this precise time point
            model.calculateOutputs(currentTime);
            model.calculateDiscreteUpdates(currentTime);

            // Send simulation message with throttling
            if (shouldSendMessage(currentTime, context.getMinStep())) {
                model.sendSimulatingMessage(session, currentTime);
            }

            System.out.printf("Fixed-step %d/%d: t=%.6f%n", stepIndex, totalSteps, currentTime);
        }

        System.out.printf("Fixed-step integration completed: final_time=%.6f%n", currentTime);
    }
    
    /**
     * Determine if a simulation message should be sent based on time throttling
     */
    private boolean shouldSendMessage(double currentTime, double minStep) {
        return currentTime - Math.floor(currentTime) < minStep;
    }
}