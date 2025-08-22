package com.ncslab.ncslablink;

import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.communication.WebSocketMessageDto;
import com.ncslab.dto.communication.RealTimeScopeDto;
import com.ncslab.dto.communication.RealTimeScopeUpdateDto;
import com.ncslab.util.JsonUtils;
import jakarta.websocket.Session;

import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.sampling.FixedStepHandler;
import org.apache.commons.math3.ode.sampling.StepNormalizer;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * StepSimulationModel - Extends SimulationModel with precise step control functionality
 * Combines frontend commands with step simulation process for real-time control
 */
public class StepSimulationModel extends SimulationModel {
    
    // Step control state
    private double currentTime = 0.0;
    private double[] currentStates;
    private boolean isPaused = false;
    private boolean stepMode = true;
    private double stepSize;
    private int currentStepCount = 0;
    private int totalSteps = 0;
    
    // Thread safety for step control
    private final ReentrantLock stepLock = new ReentrantLock();
    
    // Current simulation session
    private Session currentSession;

    // Constructors following parent pattern
    StepSimulationModel(JSONObject jsonIn, ModelMode mode) throws ModelException {
        super(jsonIn, mode);
        initializeStepControl();
    }
    
    StepSimulationModel(ModelDto modelDto, ModelMode mode) throws ModelException {
        super(modelDto, mode);
        initializeStepControl();
    }
    
    private void initializeStepControl() {
        this.stepSize = getConfig().getFixedStep();
        this.currentStates = new double[getStateSize()];
        this.currentTime = 0.0;
        this.stepMode = true;
        System.out.println("StepSimulationModel initialized with step size: " + stepSize);
    }
    
    // Factory methods
    public static StepSimulationModel createFromJSON(JSONObject jsonIn, ModelMode mode) throws ModelException {
        return new StepSimulationModel(jsonIn, mode);
    }

    public static StepSimulationModel createFromDto(ModelDto modelDto, ModelMode mode) throws ModelException {
        return new StepSimulationModel(modelDto, mode);
    }
    
    // Get state array size
    private int getStateSize() {
        // Calculate total state size from blocks
        int stateSize = 0;
        if (getBlockList() != null) {
            stateSize = getBlockList().stream()
                .mapToInt(block -> block.getStateList().size())
                .sum();
        }
        return Math.max(stateSize, 1); // At least 1 state
    }
    
    /**
     * Execute step forward command with precise control
     */
    public void executeStepForward(Session session, int steps, Double customStepSize) throws ModelException {
        stepLock.lock();
        try {
            this.currentSession = session;
            this.totalSteps = steps;
            this.currentStepCount = 0;
            
            if (customStepSize != null) {
                this.stepSize = customStepSize;
            }
            
            sendStepMessage(session, "step_forward_started");
            
            // Execute specified number of steps
            for (int i = 0; i < steps; i++) {
                if (isPaused) {
                    sendStepMessage(session, "step_forward_paused");
                    break;
                }
                
                currentStepCount = i + 1;
                executeOneStep();
                
                // Send progress update
                sendStepProgressMessage(session, currentStepCount, totalSteps, currentTime);
                
                // Stream real-time results after each step
                streamCurrentResults(session);
            }
            
            sendStepMessage(session, "step_forward_completed");
            
        } catch (Exception e) {
            throw new ModelException("Step forward execution failed: " + e.getMessage());
        } finally {
            stepLock.unlock();
        }
    }
    
    /**
     * Execute step backward command with precise control
     */
    public void executeStepBackward(Session session, int steps, Double targetTime) throws ModelException {
        stepLock.lock();
        try {
            this.currentSession = session;
            
            if (targetTime != null) {
                // Jump to specific time
                sendStepMessage(session, "jumping_to_time");
                jumpToTime(targetTime);
                sendStepMessage(session, "time_jump_completed");
            } else {
                // Step backward incrementally
                sendStepMessage(session, "step_backward_started");
                
                for (int i = 0; i < steps; i++) {
                    if (currentTime <= 0) {
                        break; // Can't go before time 0
                    }
                    
                    currentTime = Math.max(0, currentTime - stepSize);
                    currentStepCount++;
                    
                    // Restore state at new time (simplified - in real implementation, 
                    // you'd restore from checkpoint or recalculate)
                    restoreStateAtTime(currentTime);
                    
                    // Send progress and stream results
                    sendStepProgressMessage(session, i + 1, steps, currentTime);
                    streamCurrentResults(session);
                }
                
                sendStepMessage(session, "step_backward_completed");
            }
            
        } catch (Exception e) {
            throw new ModelException("Step backward execution failed: " + e.getMessage());
        } finally {
            stepLock.unlock();
        }
    }
    
    /**
     * Execute one simulation step with precise control
     */
    private void executeOneStep() throws ModelException {
        try {
            // Create differential equation system for one step
            FirstOrderDifferentialEquations singleStepODE = new FirstOrderDifferentialEquations() {
                @Override
                public int getDimension() {
                    return currentStates.length;
                }
                
                @Override
                public void computeDerivatives(double t, double[] x, double[] xDot) {
                    calculateDerivatives(t, x, xDot);
                }
            };
            
            // Create integrator for single step - use protected method
            FirstOrderIntegrator integrator = createStepIntegrator(
                getConfig().getSolver(), 
                stepSize, 
                getConfig().getMinStep(), 
                getConfig().getMaxStep(), 
                getConfig().getAbsTol(), 
                getConfig().getRelTol()
            );
            
            // Execute one step
            double nextTime = currentTime + stepSize;
            integrator.integrate(singleStepODE, currentTime, currentStates, nextTime, currentStates);
            
            currentTime = nextTime;
            
            // Update block states and outputs
            updateBlockStates(currentTime, currentStates);
            
        } catch (Exception e) {
            throw new ModelException("Single step execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Jump to specific time (for goto_time command)
     */
    private void jumpToTime(double targetTime) throws ModelException {
        if (targetTime < 0) {
            targetTime = 0;
        }
        if (targetTime > getConfig().getStopTime()) {
            targetTime = getConfig().getStopTime();
        }
        
        // For backward time jump, we'd normally restore from checkpoint
        // For forward time jump, we simulate to target time
        if (targetTime > currentTime) {
            // Simulate forward to target time
            double stepsNeeded = Math.ceil((targetTime - currentTime) / stepSize);
            for (int i = 0; i < stepsNeeded && currentTime < targetTime; i++) {
                executeOneStep();
            }
        } else {
            // Jump backward (simplified - restore state)
            currentTime = targetTime;
            restoreStateAtTime(currentTime);
        }
    }
    
    /**
     * Restore simulation state at specific time (simplified implementation)
     */
    private void restoreStateAtTime(double time) {
        // In a complete implementation, this would restore from saved checkpoints
        // For now, we'll reset to initial conditions and re-simulate if needed
        if (time <= 0) {
            currentTime = 0;
            initializeStates();
        }
        // TODO: Implement proper state restoration from checkpoints
    }
    
    /**
     * Initialize simulation states
     */
    private void initializeStates() {
        // Initialize states to zero or initial conditions
        for (int i = 0; i < currentStates.length; i++) {
            currentStates[i] = 0.0;
        }
    }
    
    /**
     * Update block states with new values
     */
    private void updateBlockStates(double time, double[] states) {
        // Update all block states and compute outputs
        calculateOutputs(time);
        
        // Update terminal/scope data for visualization
        updateTerminalData(time);
    }
    
    /**
     * Send step-specific message to frontend
     */
    private void sendStepMessage(Session session, String message) {
        if (session != null) {
            try {
                WebSocketMessageDto msg = WebSocketMessageDto.createStatusMessage(message, null);
                session.getBasicRemote().sendText(JsonUtils.getObjectMapper().writeValueAsString(msg));
            } catch (IOException e) {
                System.err.println("Failed to send step message: " + e.getMessage());
            }
        }
    }
    
    /**
     * Send step progress message with detailed information
     */
    private void sendStepProgressMessage(Session session, int currentStep, int totalSteps, double time) {
        if (session != null) {
            try {
                WebSocketMessageDto progressMsg = WebSocketMessageDto.builder()
                    .msg("step_progress")
                    .status("in_progress")
                    .data(java.util.Map.of(
                        "current_step", currentStep,
                        "total_steps", totalSteps,
                        "current_time", time,
                        "step_size", stepSize,
                        "progress_percent", (double) currentStep / totalSteps * 100
                    ))
                    .timestamp(System.currentTimeMillis())
                    .build();
                    
                session.getBasicRemote().sendText(JsonUtils.getObjectMapper().writeValueAsString(progressMsg));
            } catch (IOException e) {
                System.err.println("Failed to send step progress: " + e.getMessage());
            }
        }
    }
    
    /**
     * Stream current simulation results to frontend
     */
    private void streamCurrentResults(Session session) {
        // Always stream results regardless of session (for testing with null sessions)
        try {
                // Debug: Log terminal information
                System.out.printf("Step Simulation Debug: Total terminals = %d%n", 
                    getTerminalList() != null ? getTerminalList().size() : 0);
                
                if (getTerminalList() != null) {
                    for (int i = 0; i < getTerminalList().size(); i++) {
                        var terminal = getTerminalList().get(i);
                        System.out.printf("  Terminal %d: %s (type: %s)%n", 
                            i, terminal.getName(), terminal.getClass().getSimpleName());
                    }
                }
                
                // Create comprehensive real-time scope data using DTO
                RealTimeScopeUpdateDto scopeUpdate = RealTimeScopeUpdateDto.fromTerminalList(
                    getTerminalList(), currentTime, currentStepCount, stepSize, isPaused);
                
                System.out.printf("Step Simulation Debug: Created scope update with %d scopes%n", 
                    scopeUpdate.getScopeCount());
                
                // Send as scope_update message using DTO structure
                WebSocketMessageDto resultMsg = WebSocketMessageDto.builder()
                    .msg("scope_update")
                    .status("streaming")
                    .data(java.util.Map.of(
                        "scopeData", scopeUpdate.toMap(),
                        "time", currentTime,
                        "step", currentStepCount,
                        "step_size", stepSize
                    ))
                    .timestamp(System.currentTimeMillis())
                    .build();
                    
                if (session != null) {
                    session.getBasicRemote().sendText(JsonUtils.getObjectMapper().writeValueAsString(resultMsg));
                } else {
                    // Redirect to console when session is null (for testing)
                    System.out.println("Step Simulation scope_update (null session): " + JsonUtils.getObjectMapper().writeValueAsString(resultMsg));
                }
                
                System.out.printf("Step Simulation: Streamed %d scopes at time %.6f, step %d%n", 
                    scopeUpdate.getScopeCount(), currentTime, currentStepCount);
                
        } catch (Exception e) {
            System.err.println("Failed to stream results: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    // Pause/Resume control
    public void pauseSimulation(Session session) {
        stepLock.lock();
        try {
            isPaused = true;
            sendStepMessage(session, "simulation_paused");
            System.out.println("Step simulation paused at time: " + currentTime);
        } finally {
            stepLock.unlock();
        }
    }
    
    public void resumeSimulation(Session session) {
        stepLock.lock();
        try {
            isPaused = false;
            sendStepMessage(session, "simulation_resumed");
            System.out.println("Step simulation resumed at time: " + currentTime);
        } finally {
            stepLock.unlock();
        }
    }
    
    // Getters for current state
    public double getCurrentTime() {
        return currentTime;
    }
    
    public int getCurrentStepCount() {
        return currentStepCount;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public double getStepSize() {
        return stepSize;
    }
    
    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
    }
    
    /**
     * Override simulate method to provide step-controlled simulation
     */
    @Override
    public void simulate(Session session) throws ModelException {
        System.out.println("StepSimulationModel: Executing step-controlled simulation...");
        
        // For step simulation, we don't run continuous simulation
        // Instead, we wait for step commands from frontend
        sendStepMessage(session, "step_simulation_ready");
        
        // Initialize simulation state
        initializeStates();
        currentTime = 0.0;
        currentStepCount = 0;
        
        System.out.println("Step simulation initialized and ready for commands");
    }
    
    /**
     * Get total number of data points (for frontend display)
     */
    public int getTotalDataPoints() {
        return currentStepCount;
    }
    
    /**
     * Update terminal data (simplified implementation)
     */
    private void updateTerminalData(double time) {
        // Update scope/terminal data for real-time visualization
        if (getTerminalList() != null) {
            for (var terminal : getTerminalList()) {
                if (terminal instanceof com.ncslab.block.io.terminal.ScopeStruct) {
                    com.ncslab.block.io.terminal.ScopeStruct scopeStruct = 
                        (com.ncslab.block.io.terminal.ScopeStruct) terminal;
                    
                    // Call calculateDiscreteUpdate on the scope block to collect data
                    if (scopeStruct.getBlock() instanceof com.ncslab.block.sink.Scope) {
                        com.ncslab.block.sink.Scope scopeBlock = 
                            (com.ncslab.block.sink.Scope) scopeStruct.getBlock();
                        scopeBlock.calculateDiscreteUpdate(time);
                        
                        // Debug logging for scope data collection
                        if (scopeStruct.getTimeList().size() <= 5) {
                            System.out.printf("Step Simulation: Scope %s collected data at t=%.6f (total points: %d)%n",
                                scopeBlock.getBlockName(), time, scopeStruct.getTimeList().size());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Create integrator for step simulation (protected method to access parent's logic)
     */
    protected FirstOrderIntegrator createStepIntegrator(String solverName, double step, double minStep, 
                                                      double maxStep, double absTol, double relTol) {
        // Use common integrator creation logic
        switch (solverName.toLowerCase()) {
            case "ode1":
                return new org.apache.commons.math3.ode.nonstiff.EulerIntegrator(step);
            case "ode2":
                return new org.apache.commons.math3.ode.nonstiff.MidpointIntegrator(step);
            case "ode3":
                return new org.apache.commons.math3.ode.nonstiff.ThreeEighthesIntegrator(step);
            case "ode4":
            default:
                return new org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator(step);
            case "ode45":
            case "auto":
                return new org.apache.commons.math3.ode.nonstiff.DormandPrince54Integrator(minStep, maxStep, absTol, relTol);
        }
    }
}