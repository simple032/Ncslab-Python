package com.ncslab.ncslablink;

import Jama.Matrix;
import com.google.common.io.LittleEndianDataInputStream;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.block.io.terminal.ScopeStruct;
import com.ncslab.block.io.terminal.Terminal;
import com.ncslab.block.sink.Display;
import com.ncslab.block.sink.Scope;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.util.JsonUtils;
import com.utils.Property;

import lombok.Data;
import lombok.Getter;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.*;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.communication.WebSocketMessageDto;

import jakarta.websocket.Session;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.sampling.FixedStepHandler;
import org.apache.commons.math3.ode.sampling.StepNormalizer;



public class SimulationModel extends NCSLabModel{

    // 系统状态和参数
    @Getter
    private double[] states;
    private double[] parameters;
    private double[] inputs;
    private double[] outputs;
    private JSONObject result = new JSONObject();

    private long lastSessionTime;
    private long lastDisplayUpdateTime;

    @Getter
    private boolean isMajorStep=false;

    // Flag to request simulation stop from WebSocket disconnect
    private volatile boolean stopRequested = false;
    
	// 原有JSONObject构造函数
	SimulationModel(JSONObject jsonIn, ModelMode mode) throws ModelException{
		super(jsonIn,mode);
		initializeStatesArray();
	}
	
	// 新增ModelDto DTO构造函数
	SimulationModel(ModelDto modelDto, ModelMode mode) throws ModelException{
		super(modelDto, mode);
		initializeStatesArray();
	}
	
	// 初始化状态数组
	private void initializeStatesArray() {
        int statesSize = 0;
        for(Block block: getBlockList()) {
            for (State state : block.getStateList()) {
                if (state.getDataType() == DataType.REAL) {
                    statesSize++;
                } else {
                    Matrix matrix = state.getData().getMatrix();
                    statesSize += matrix.getRowDimension() * matrix.getColumnDimension();
                }
            }
        }
        states = new double[statesSize];
    }

	/**
	 * Sanitize numeric value for JSON encoding
	 * Converts non-finite numbers (NaN, Infinity, -Infinity) to string representations
	 * to avoid JSONException while preserving information about the condition
	 *
	 * @param value The numeric value to sanitize
	 * @param blockType Optional block type for diagnostic logging
	 * @param signalName Optional signal name for diagnostic logging
	 * @param time Optional simulation time for diagnostic logging
	 * @return The value if finite, otherwise a string representation
	 */
	private Object sanitizeNumericValue(double value, String blockType, String signalName, double time) {
		if (Double.isNaN(value)) {
			System.err.printf("WARNING: NaN detected at t=%.6f in %s signal '%s'%n", time, blockType, signalName);
			return "NaN";
		} else if (Double.isInfinite(value)) {
			String infinityType = value > 0 ? "Infinity" : "-Infinity";
			System.err.printf("WARNING: %s detected at t=%.6f in %s signal '%s'%n", infinityType, time, blockType, signalName);
			return infinityType;
		} else {
			return value;
		}
	}

	/**
	 * Sanitize numeric value for JSON encoding (simple version)
	 * @param value The numeric value to sanitize
	 * @return The value if finite, otherwise a string representation
	 */
    // TODO:前端如何处理
	private Object sanitizeNumericValue(double value) {
		if (Double.isNaN(value)) {
			return "NaN";
		} else if (Double.isInfinite(value)) {
			return value > 0 ? "Infinity" : "-Infinity";
		} else {
			return value;
		}
	}

	public static SimulationModel createFromJSON(JSONObject jsonIn, ModelMode mode) throws ModelException {
        return new SimulationModel(jsonIn,mode);
	}
	
	// 新增ModelDto DTO工厂方法
	public static SimulationModel createFromDto(ModelDto modelDto, ModelMode mode) throws ModelException {
        return new SimulationModel(modelDto, mode);
	}

	public void sendSimulatingMessage(Session session, double time) throws IOException{
		if (session == null || !session.isOpen()) {
			throw new IOException("WebSocket session closed, stopping simulation");
		}
		JSONObject jb = new JSONObject();
		jb.put("msg", "simulating");
		jb.put("time", time);
		jb.put("timeLength", this.getConfig().getStopTime());
		session.getBasicRemote().sendText(jb.toString());
        sendDisplayUpdateMessage(session);
	}

    private void sendDisplayUpdateMessage(Session session) throws IOException {
        if (session == null || !session.isOpen()) return;
        long currentTime = new java.util.Date().getTime();
        if (currentTime <= lastDisplayUpdateTime + 500) {
            return; // throttle to every 500ms
        }
        lastDisplayUpdateTime = currentTime;
        sendDisplayUpdateMessageForce(session);
    }

    /**
     * Force send display update message without throttling.
     * Used for sending initial and final display values at simulation start/end.
     */
    private void sendDisplayUpdateMessageForce(Session session) throws IOException {
        if (session == null || !session.isOpen()) return;

        Map<String, Double> displayData = new HashMap<>();
        for (Block block : getBlockList()) {
            if ("Display".equals(block.getBlockType()) && block instanceof Display) {
                Display displayBlock = (Display) block;
                double latestValue = displayBlock.getLatestDisplayValue();
                String uuid = block.getBlockUUID();
                if (uuid != null && !"null".equals(uuid) && !uuid.isEmpty()) {
                    displayData.put(uuid, latestValue);
                }
            }
        }
        if (!displayData.isEmpty()) {
            JSONObject jb = new JSONObject();
            jb.put("msg", "display_update");
            jb.put("timestamp", System.currentTimeMillis());
            JSONObject dataObj = new JSONObject();
            for (Map.Entry<String, Double> entry : displayData.entrySet()) {
                dataObj.put(entry.getKey(), entry.getValue());
            }
            jb.put("displayData", dataObj);
            session.getBasicRemote().sendText(jb.toString());
        }
    }

    private void sendTimeSeriesMessage(Session session, JSONObject timeseries) throws IOException{
		JSONObject jb=new JSONObject();
		jb.put("msg", "timeseries");
        jb.put("version", 1.0);
        jb.put("timeseries", timeseries);
        if(session!=null)
            session.getBasicRemote().sendText(jb.toString());
        else
            System.out.println(jb);
	}

    /**
     * Send optimized real-time scope data directly via WebSocket
     * @param session WebSocket session
     * @param scopeData Scope data to send
     * @param currentTime Current simulation time
     * @throws IOException if sending fails
     */
    private void sendOptimizedScopeData(Session session, JSONObject scopeData, double currentTime) throws IOException {
        if (session != null) {
            WebSocketMessageDto message = WebSocketMessageDto.createRealTimeScopeUpdate(
                null, scopeData.toMap(), currentTime);
            session.getBasicRemote().sendText(JsonUtils.getObjectMapper().writeValueAsString(message));
        }
    }

    /**
     * Send final optimized results directly via WebSocket (no file I/O)
     * @param session WebSocket session
     * @throws IOException if sending fails
     */
    private void sendOptimizedFinalResults(Session session) throws IOException {
        if (session == null) return;
        
        JSONObject allResults = new JSONObject();
        JSONArray jsonScopes = new JSONArray();
        int scopeCursor = 0;

        // Debug: Log terminal list and scope status 
        System.out.printf("RT Simulation: Found %d terminals in model%n", getTerminalList().size());
        int scopeCount = 0;
        for (Terminal terminal : getTerminalList()) {
            if (terminal instanceof ScopeStruct) {
                scopeCount++;
                ScopeStruct scope = (ScopeStruct) terminal;
                System.out.printf("RT Simulation: Scope %s - timeList=%d, dataList=%d%n", 
                    scope.getName(), scope.getTimeList().size(), scope.getDataList().size());
            }
        }
        System.out.printf("RT Simulation: Total scopes found: %d%n", scopeCount);

        // Build final results data
        for (Terminal terminal : getTerminalList()) {
            if (!(terminal instanceof ScopeStruct)) continue;
            
            JSONObject scopeJson = new JSONObject();
            buildScopeJson(scopeCursor, (ScopeStruct) terminal, scopeJson);
            jsonScopes.put(scopeJson);
            scopeCursor++;
        }

        allResults.put("version", "0.2");
        allResults.put("optimized", true);
        allResults.put("scopes", jsonScopes);

        // Send via WebSocket directly
        if (!session.isOpen()) {
            System.out.println("RT Simulation: Session closed before sending final results");
            return;
        }
        WebSocketMessageDto message = WebSocketMessageDto.createFinalResultsMessage(
            allResults.toMap(), getUserId(), getModelId());
        session.getBasicRemote().sendText(JsonUtils.getObjectMapper().writeValueAsString(message));
        
        System.out.printf("RT Simulation: Final results sent - %d scopes processed%n", scopeCursor);
    }
    
    private double lastOutputTime=0;
    
    private double step;

    private static final int CHUNK_SIZE = 100000;

    /**
     * Get the output directory for simulation result files.
     * Format: {CCodePathWin}/{userId}/{modelId}/
     */
    private String getOutputDir() {
        String basePath = Property.instance.getProperty("CCodePathWin", "D:/NewLab/Code/CCode/");
        if (!basePath.endsWith("/") && !basePath.endsWith("\\")) {
            basePath += "/";
        }
        return basePath + getUserId() + "/" + getModelId() + "/";
    }

    /**
     * Check all Scope terminals and flush chunks to disk if their in-memory data exceeds CHUNK_SIZE.
     */
    private void flushScopeChunksIfNeeded() {
        String outputDir = getOutputDir();
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        for (Terminal terminal : getTerminalList()) {
            if (terminal instanceof ScopeStruct) {
                ScopeStruct scope = (ScopeStruct) terminal;
                if (scope.getTimeList().size() > CHUNK_SIZE) {
                    try {
                        scope.flushChunk(outputDir);
                        System.out.printf("RT Simulation: Flushed chunk %d for scope %s%n",
                            scope.getChunkCount() - 1, scope.getName());
                    } catch (IOException e) {
                        System.err.println("Failed to flush scope chunk for " + scope.getName() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Save tail scope data and results.json metadata to disk at the end of simulation.
     * This allows the frontend to load results via HTTP just like standard simulation.
     */
    private void saveFinalResultsToFile() {
        String outputDir = getOutputDir();
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        JSONArray scopesMeta = new JSONArray();
        for (Terminal terminal : getTerminalList()) {
            if (!(terminal instanceof ScopeStruct)) continue;
            ScopeStruct scope = (ScopeStruct) terminal;

            // Write tail file for any remaining in-memory data (or empty tail if chunks exist)
            if (!scope.getTimeList().isEmpty() || scope.getChunkCount() > 0) {
                JSONObject tailJson = new JSONObject();
                tailJson.put("uuid", scope.getBlock().getBlockUUID());
                tailJson.put("name", scope.getBlock().getBlockName());
                tailJson.put("path", scope.getBlock().getBlockPath());
                tailJson.put("width", scope.getWidth());
                tailJson.put("height", scope.getHeight());
                tailJson.put("chunkIndex", scope.getChunkCount());

                JSONArray timeArray = new JSONArray();
                JSONArray dataArray = new JSONArray();
                for (Double t : scope.getTimeList()) {
                    timeArray.put(t);
                }
                for (Double d : scope.getDataList()) {
                    dataArray.put(d);
                }
                tailJson.put("time", timeArray);
                tailJson.put("data", dataArray);

                String tailFilename = "scope_" + scope.getBlock().getBlockUUID() + ".json";
                try (FileWriter writer = new FileWriter(new File(outputDir, tailFilename))) {
                    writer.write(tailJson.toString());
                } catch (IOException e) {
                    System.err.println("Failed to write tail file for " + scope.getName() + ": " + e.getMessage());
                }
            }

            // Build metadata entry for results.json
            JSONObject meta = new JSONObject();
            meta.put("uuid", scope.getBlock().getBlockUUID());
            meta.put("name", scope.getBlock().getBlockName());
            meta.put("path", scope.getBlock().getBlockPath());
            meta.put("width", scope.getWidth());
            meta.put("height", scope.getHeight());

            JSONArray chunks = new JSONArray();
            for (int c = 0; c < scope.getChunkCount(); c++) {
                chunks.put("scope_" + scope.getBlock().getBlockUUID() + "_chunk" + c + ".json");
            }
            if (!scope.getTimeList().isEmpty() || scope.getChunkCount() > 0) {
                chunks.put("scope_" + scope.getBlock().getBlockUUID() + ".json");
            }
            meta.put("chunks", chunks);
            scopesMeta.put(meta);
        }

        JSONObject result = new JSONObject();
        result.put("version", "0.2");
        result.put("scopes", scopesMeta);

        try (FileWriter writer = new FileWriter(new File(outputDir, "results.json"))) {
            writer.write(result.toString());
            System.out.printf("RT Simulation: Saved results.json to %s%n", outputDir);
        } catch (IOException e) {
            System.err.println("Failed to write results.json: " + e.getMessage());
        }
    }

	public void stop() {
		this.stopRequested = true;
		System.out.println("[SimulationModel] Stop flag set");
	}

	public void simulate(Session session) throws ModelException {
		System.out.println("Executing simulation codes...");
		System.out.printf("RT Debug: simulate() called with session=%s, terminals=%d%n", 
			(session != null ? "present" : "null"), getTerminalList().size());
        // 初始化解算器
        double absTol = getConfig().getAbsTol();
        double relTol = getConfig().getRelTol();
        double minStep = getConfig().getMinStep();
        double maxStep = getConfig().getFixedStep();
        double step = getConfig().getFixedStep();
        
        this.step=step;

        // Flag to track if calculateTerminates has been called to prevent double execution
        final boolean[] terminateCalled = new boolean[]{false};

        try {

            // 定义控制系统的微分方程
            FirstOrderDifferentialEquations systemODE = new FirstOrderDifferentialEquations() {
                @Override
                public int getDimension() {
                    return states.length; // 状态变量数量
                }

                @Override
                public void computeDerivatives(double t, double[] x, double[] xDot) {

                    // 1. 计算误差（设定值与实际值的差）

                    // 2. 计算积分项导数（对应mdlDerivatives）
                    calculateDerivatives(t,x,xDot);
                    // 检查是否到达采样时间点
//                    if (Math.abs(t % dt) < 1e-10 || t == 0) {
//                        updateDiscreteStates(t, y);
//                    }

                    // 打印当前状态（实际应用中可通过WebSocket发送）
//                     System.out.printf("t=%.3f, y=%.6f, error=%.6f, output=%.6f%n",
//                                     t, y[0], error, output);

                }

            };

            String solverName = getConfig().getSolver();
            FirstOrderIntegrator integrator = createIntegrator(solverName, step, minStep, maxStep, absTol, relTol);

            // 添加固定步长处理器
            FixedStepHandler fixedStepHandler = new FixedStepHandler() {
                @Override
                public void init(double t0, double[] y0, double t) {
//                    calculateInits(t0, states);
                }

                @Override
                public void handleStep(double t, double[] y, double[] yDot, boolean isLast) {
                    // 复制当前状态
                    double[] state = Arrays.copyOf(y, y.length);
                    // 计算代数输出（可以直接调用系统ODE的计算部分）
//                    systemODE.computeDerivatives(t, y, yDot);
                    // 3. 处理离散状态更新
                    calculateUpdates(t);  // NEW: Major time step update
                    calculateDiscreteUpdates(t);
                    flushScopeChunksIfNeeded();

                    double iteration = Math.floor(t/step)/1000;
                    // 4. 发送时间序列消息（无论是否有状态）
                    if (iteration - Math.floor((iteration))  < minStep) {
                        try {
                            sendSimulatingMessage(session, t);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(isLast){
                        calculateStops(t);  // NEW: Graceful shutdown
                        calculateTerminates(t);
                        terminateCalled[0] = true;
                    }
                }
            };
//

            StepHandler stepHandler = new StepHandler() {
                @Override
                public void init(double t0, double[] y0, double t) {
//                    calculateInits(t0, states);
                }

                @Override
                public void handleStep(StepInterpolator interpolator, boolean isLast) {
                    double currentTime = interpolator.getCurrentTime();
                    double[] state = interpolator.getInterpolatedState();

                    // 处理离散状态更新
                    calculateUpdates(currentTime);  // NEW: Major time step update
                    calculateDiscreteUpdates(currentTime);
                    flushScopeChunksIfNeeded();

                    // 处理主步长的输出

                    // 发送时间序列消息
                    if(currentTime - Math.floor(currentTime) < minStep) {
                        try {
                            sendSimulatingMessage(session, currentTime);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(isLast){
                        calculateTerminates(currentTime);
                        terminateCalled[0] = true;
                    }
                }
            };


            // send the simulation data to the client
            double tStart = getConfig().getStartTime();
            double tEnd = getConfig().getStopTime();

            // === Pre-Simulation Phase ===
            calculateCheckParameters();  // NEW: Validate parameters
            calculateInits(tStart, states);  // Initialize states and outputs
            calculateStarts();  // NEW: One-time startup actions
            
            // Send initial display values after startup
            if (session != null) {
                sendDisplayUpdateMessageForce(session);
            }
            
            if(circuitModel!=null) {
            	circuitModel.calculateInits(tStart);
            }
            

            boolean hasState = systemODE.getDimension() > 0;
            
            System.out.printf("Simulation setup: %s, hasState=%s, tStart=%.3f, tEnd=%.3f%n", 
                getSolverDisplayName(solverName), hasState, tStart, tEnd);
            
            lastSessionTime=new java.util.Date().getTime();
            lastDisplayUpdateTime=lastSessionTime;
            
            if(hasState) {
                if(isVariableStepSolver(solverName)) {
                    // Variable-step simulation
                	this.isMajorStep=true;
                    System.out.println("Using variable-step integration with adaptive step handler");
                    integrator.addStepHandler(stepHandler);
                    double tEndActual = integrator.integrate(systemODE, tStart, states, tEnd, states);
                    System.out.printf("Variable-step simulation completed: actual_end=%.6f (target=%.6f)%n", tEndActual, tEnd);
                }
                else {
                    // Fixed-step simulation
                    System.out.println("Using fixed-step integration with precise step control");
                    performFixedStepIntegration(session, integrator, systemODE, tStart, tEnd, step, minStep);
                }
            }else {
                double t = tStart;
                boolean finalTimeProcessed = false;

                this.isMajorStep=true;

                while(t < tEnd){
                    if (stopRequested || (session != null && !session.isOpen())) {
                        System.out.println("[SimulationModel] Stop requested or session closed, breaking loop at t=" + t);
                        break;
                    }
                    calculateOutputs(t);
                    if(circuitModel!=null) {
                    	circuitModel.calculateOutputs(t);
                    	circuitModel.calculateUpdate(t);
                    }
                    
                    calculateUpdates(t);  // NEW: Major time step update
                    calculateDiscreteUpdates(t);
                    flushScopeChunksIfNeeded();
                    // 发送时间序列消息
                    if(t - Math.floor(t) < minStep) {
                        try {
                            sendSimulatingMessage(session, t);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    //saveStepResult(t);
                    t += step;
                    
                    long sessionTime=new java.util.Date().getTime();
                    if(sessionTime>lastSessionTime+1000) {
                    	lastSessionTime=sessionTime;
                    	sendSimulatingMessage(session, t);
                    }
                    
                    // Check if next step would overshoot the end time
                    if(t >= tEnd) {
                        // Process final time point exactly
                        calculateOutputs(tEnd);
                        calculateUpdates(tEnd);  // NEW: Major time step update
                        calculateDiscreteUpdates(tEnd);
                        try {
                            sendSimulatingMessage(session, tEnd);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        finalTimeProcessed = true;
                        break; // Exit the loop after processing final time
                    }
                    
                }
                
                // Safety check: ensure final time is always processed (should not be needed with above logic)
                if(!stopRequested && !finalTimeProcessed) {
                    System.out.printf("Warning: Processing final time point as safety measure: t=%.6f, tEnd=%.6f%n", t, tEnd);
                    calculateOutputs(tEnd);
                    calculateUpdates(tEnd);  // NEW: Major time step update
                    calculateDiscreteUpdates(tEnd);
                    try {
                        sendSimulatingMessage(session, tEnd);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

            // === Post-Simulation Phase ===
            // Only call calculateStops and calculateTerminates if not already called by a step handler
            if (!terminateCalled[0]) {
                calculateStops(tEnd);  // NEW: Graceful shutdown
                calculateTerminates(tEnd);  // Cleanup resources
            }

            // Save scope chunks and results.json to disk (same format as standard simulation)
            saveFinalResultsToFile();

            // Send final display values before sending results
            if (session != null) {
                sendDisplayUpdateMessageForce(session);
            }

            // Use optimized WebSocket streaming instead of file I/O
            System.out.printf("RT Debug: About to send results - session=%s, terminals=%d%n", 
                (session != null ? "present" : "null"), getTerminalList().size());
            if (session != null && session.isOpen()) {
                sendOptimizedFinalResults(session);
            } else {
                System.out.println("RT Debug: Session closed or null, skipping result sending");
            }
		}
		catch(Exception e) {            
            e.printStackTrace();
			throw new ModelException("Can not execute the exe file!");
		}

		if (stopRequested) {
			System.out.println("Simulation stopped by user.");
		} else {
			System.out.println("Simulation executed successfully!");
		}

	}

    /**
     * Performs fixed-step integration with precise output control
     */
    private void performFixedStepIntegration(Session session, FirstOrderIntegrator integrator,
                                             FirstOrderDifferentialEquations systemODE,
                                             double tStart, double tEnd, double step, double minStep)
        throws IOException {

        double currentTime = tStart;
        double[] currentStates = Arrays.copyOf(states, states.length);

        // Calculate the exact number of steps needed
        int totalSteps = (int) Math.round((tEnd - tStart) / step);
        double actualStep = (tEnd - tStart) / totalSteps;

        System.out.printf("Fixed-step integration: start=%.6f, end=%.6f, steps=%d, actualStep=%.6f%n",
            tStart, tEnd, totalSteps, actualStep);

        // Initialize and send initial condition
        this.isMajorStep=true;
        calculateOutputs(currentTime);
        this.isMajorStep=false;
        calculateDiscreteUpdates(currentTime);
        sendSimulatingMessage(session, currentTime);
//        System.out.printf("Step 0: t=%.6f (initial)%n", currentTime);
         
        // Perform step-by-step integration
        for (int stepIndex = 1; stepIndex <= totalSteps; stepIndex++) {
            if (stopRequested || (session != null && !session.isOpen())) {
                System.out.println("[SimulationModel] Stop requested or session closed, breaking fixed-step integration at step " + stepIndex);
                break;
            }
            double targetTime = tStart + stepIndex * actualStep;

            // Clamp to exact end time to avoid floating-point overshoot
            if (stepIndex == totalSteps) {
                targetTime = tEnd;
            }

            try {
                // Integrate from current time to target time
                double actualEndTime = integrator.integrate(systemODE, currentTime, currentStates, targetTime, currentStates);

                // Update current time
                currentTime = actualEndTime;

                // Calculate outputs and discrete updates at this precise time point
                this.isMajorStep=true;
                calculateOutputs(currentTime);
                if(circuitModel!=null) {
                	circuitModel.calculateOutputs(currentTime);
                	circuitModel.calculateUpdate(currentTime);
                }
                this.isMajorStep=false;
                calculateDiscreteUpdates(currentTime);
                flushScopeChunksIfNeeded();

                // Send simulation message
                /*
                if(stepIndex % 1000 == 0) {
                    sendSimulatingMessage(session, currentTime);
                }*/
                
                long sessionTime=new java.util.Date().getTime();
                if(sessionTime>lastSessionTime+1000) {
                	lastSessionTime=sessionTime;
                	sendSimulatingMessage(session, currentTime);
                }
                
                //System.out.printf("Step %d: t=%.6f (target=%.6f)%n", stepIndex, currentTime, targetTime);

                // Verify we're making progress and haven't stalled
                if (Math.abs(currentTime - targetTime) > minStep) {
                    System.out.printf("Warning: Integration stopped at t=%.6f instead of target t=%.6f%n",
                        currentTime, targetTime);
                }
                
                //saveStepResult(currentTime);

            } catch (Exception e) {
                System.err.printf("Integration failed at step %d, time=%.6f, target=%.6f%n",
                    stepIndex, currentTime, targetTime);
                throw new RuntimeException("Integration step failed", e);
            }
        }

        // Final verification and cleanup
        if (!stopRequested && Math.abs(currentTime - tEnd) > minStep) {
            System.out.printf("Warning: Final time %.6f differs from target %.6f%n", currentTime, tEnd);

            // Force final step if needed
            try {
                currentTime = integrator.integrate(systemODE, currentTime, currentStates, tEnd, currentStates);
                calculateOutputs(currentTime);
                calculateDiscreteUpdates(currentTime);
                sendSimulatingMessage(session, currentTime);
                System.out.printf("Final correction: t=%.6f%n", currentTime);
            } catch (Exception e) {
                System.err.printf("Final correction step failed at t=%.6f%n", currentTime);
            }
        }

        System.out.printf("Fixed-step integration completed at t=%.6f%n", currentTime);
    }
    
    /**
     * Create appropriate integrator based on solver name and parameters
     * Supports both variable-step and fixed-step solvers as per expanded frontend options
     * 
     * @param solverName Name of the solver (e.g., "ode45", "ode1", "auto")
     * @param step Fixed step size for fixed-step solvers
     * @param minStep Minimum step size for variable-step solvers
     * @param maxStep Maximum step size for variable-step solvers
     * @param absTol Absolute tolerance for variable-step solvers
     * @param relTol Relative tolerance for variable-step solvers
     * @return Configured FirstOrderIntegrator
     */
    private FirstOrderIntegrator createIntegrator(String solverName, double step, double minStep, 
                                                  double maxStep, double absTol, double relTol) {
        System.out.printf("Creating integrator: %s (step=%.6f, minStep=%.6f, maxStep=%.6f)%n", 
                          solverName, step, minStep, maxStep);
        
        switch (solverName.toLowerCase()) {
            // Variable-step solvers
            case "auto":
            case "variablestepauto":
            case "variablestep":
                System.out.println("Using automatic variable-step solver selection");
                return new DormandPrince54Integrator(minStep, maxStep, absTol, relTol);
                
            case "ode45":
                System.out.println("Using ode45: Dormand-Prince 4th/5th order variable-step");
                return new DormandPrince54Integrator(minStep, maxStep, absTol, relTol);
                
            case "ode23":
                System.out.println("Using ode23: Bogacki-Shampine 2nd/3rd order variable-step");
                return new DormandPrince54Integrator(minStep, maxStep, absTol, relTol); // Fallback to DP54
                
            case "ode113":
                System.out.println("Using ode113: Adams-Bashforth-Moulton multistep variable-step (fallback to DP54)");
                return new DormandPrince54Integrator(minStep, maxStep, absTol, relTol); // Fallback since multistep not available
                
            case "ode15s":
                System.out.println("Using ode15s: Stiff/NDF variable-step (fallback to DP54)");
                return new DormandPrince54Integrator(minStep, maxStep, absTol, relTol); // Fallback for stiff
                
            case "ode23s":
                System.out.println("Using ode23s: Rosenbrock stiff variable-step (fallback to DP54)");
                return new DormandPrince54Integrator(minStep, maxStep, absTol, relTol); // Fallback for stiff
                
            case "ode23t":
                System.out.println("Using ode23t: Trapezoidal rule variable-step (fallback to DP54)");
                return new DormandPrince54Integrator(minStep, maxStep, absTol, relTol); // Fallback for stiff
                
            case "ode23tb":
                System.out.println("Using ode23tb: TR-BDF2 variable-step (fallback to DP54)");
                return new DormandPrince54Integrator(minStep, maxStep, absTol, relTol); // Fallback for stiff
                
            // Fixed-step solvers
            case "ode1":
                System.out.println("Using ode1: Euler 1st order fixed-step");
                return new EulerIntegrator(step);
                
            case "ode2":
                System.out.println("Using ode2: Heun 2nd order fixed-step");
                return new MidpointIntegrator(step);
                
            case "ode3":
                System.out.println("Using ode3: Bogacki-Shampine 3rd order fixed-step");
                return new ThreeEighthesIntegrator(step);
                
            case "ode4":
                System.out.println("Using ode4: Classical Runge-Kutta 4th order fixed-step");
                return new ClassicalRungeKuttaIntegrator(step);
                
            case "ode5":
                System.out.println("Using ode5: Dormand-Prince 5th order fixed-step");
                return new DormandPrince54Integrator(step, step, absTol, relTol); // Fixed step DP
                
            case "ode8":
                System.out.println("Using ode8: Dormand-Prince RK8(7) high-order fixed-step");
                return new DormandPrince853Integrator(step, step, absTol, relTol);
                
            case "ode14x":
                System.out.println("Using ode14x: Extrapolation variable-order fixed-step");
                return new GraggBulirschStoerIntegrator(step, step, absTol, relTol);
                
            // Legacy compatibility
            case "fixedstepauto":
            case "fixedstep":
                System.out.println("Using automatic fixed-step solver selection (ode4)");
                return new ClassicalRungeKuttaIntegrator(step);
                
            default:
                System.err.printf("Unknown solver: %s, falling back to ode45%n", solverName);
                return new DormandPrince54Integrator(minStep, maxStep, absTol, relTol);
        }
    }
    
    /**
     * Determine if the solver is a variable-step solver
     * @param solverName Name of the solver
     * @return true if variable-step, false if fixed-step
     */
    private boolean isVariableStepSolver(String solverName) {
        switch (solverName.toLowerCase()) {
            case "auto":
            case "variablestepauto":
            case "variablestep":
            case "ode45":
            case "ode23":
            case "ode113":
            case "ode15s":
            case "ode23s":
            case "ode23t":
            case "ode23tb":
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Get solver display name for logging and debugging
     * @param solverName Internal solver name
     * @return Human-readable solver description
     */
    private String getSolverDisplayName(String solverName) {
        switch (solverName.toLowerCase()) {
            case "auto":
            case "variablestepauto":
                return "Auto (Variable-step Dormand-Prince)";
            case "ode45": return "ODE45 (Dormand-Prince 4/5)";
            case "ode23": return "ODE23 (Bogacki-Shampine 2/3)";
            case "ode113": return "ODE113 (Adams-Bashforth-Moulton)";
            case "ode15s": return "ODE15s (Stiff/NDF)";
            case "ode23s": return "ODE23s (Rosenbrock)";
            case "ode23t": return "ODE23t (Trapezoidal)";
            case "ode23tb": return "ODE23tb (TR-BDF2)";
            case "ode1": return "ODE1 (Euler)";
            case "ode2": return "ODE2 (Heun)";
            case "ode3": return "ODE3 (Bogacki-Shampine)";
            case "ode4": return "ODE4 (Classical Runge-Kutta)";
            case "ode5": return "ODE5 (Dormand-Prince)";
            case "ode8": return "ODE8 (Dormand-Prince RK8(7))";
            case "ode14x": return "ODE14x (Extrapolation)";
            case "fixedstepauto": return "Auto (Fixed-step Runge-Kutta)";
            default: return solverName + " (Unknown)";
        }
    }
    
    private void saveStepResult(double t) {
    	JSONArray series = new JSONArray();
        for (Block block : getBlockList()) {
            JSONObject blockData = new JSONObject();
            JSONArray outputDataArray = new JSONArray();
            JSONArray inputDataArray = new JSONArray();
            blockData.put("blockUUID", block.getBlockUUID());
            try {
                for (OutputPort outputPort : block.getOutputPortList()) {
                    JSONObject outputData = new JSONObject();
                    outputData.put("name", outputPort.getOutputSignalC().getName());
                    outputData.put("type", outputPort.getOutputSignalC().getDataType());
                    if(outputPort.getOutputSignalC().getDataType()==DataType.REAL){
                        // Sanitize non-finite numbers before JSON encoding
                        double value = outputPort.getOutputSignalC().getData().getInitValue();
                        outputData.put("value", sanitizeNumericValue(value));
                    }else{
                        outputData.put("value", outputPort.getOutputSignalC().getData().getMatrix());
                    }
                    outputDataArray.put(outputData);
                }
                for (InputPort inputPort : block.getInputPortList()) {
                    if (inputPort.getLinkedLine() != null && inputPort.getLinkedLine().getLinkedOutputPort() != null) {
                        JSONObject inputData = new JSONObject();
                        OutputPort outputPort = inputPort.getLinkedLine().getLinkedOutputPort();
                        if (outputPort.getOutputSignalC() != null) {
                            inputData.put("name", outputPort.getOutputSignalC().getName());
                            inputData.put("type", outputPort.getOutputSignalC().getDataType());
                            if(outputPort.getOutputSignalC().getDataType()==DataType.REAL){
                                // Sanitize non-finite numbers before JSON encoding
                                double value = outputPort.getOutputSignalC().getData().getInitValue();
                                inputData.put("value", sanitizeNumericValue(value));
                            }else{
                                inputData.put("value", outputPort.getOutputSignalC().getData().getMatrix());
                            }
                            inputDataArray.put(inputData);
                        }
                    }
                }
            }
            catch (JSONException e) {
                System.err.println("JSON encode error " + block.getBlockType() + " at time " + t);
                throw e;
            }
            catch (NullPointerException e) {
                System.err.println("No output or input data for block " + block.getBlockType() + " at time " + t);
                throw e;
            }
            blockData.put("outputs", outputDataArray);
            blockData.put("inputs", inputDataArray);
            series.put(blockData);
        }
        
        //System.out.println(t);

        result.put("time", t);
        result.put("series", series);
    }

    protected void calculateOutputs(double t) {
        // 计算各个模块的输出
        // 类似Simulink的mdlOutputs
        for(Block block: getOutputChain()) {
            block.calculateOutput(t);
        }
        
        //System.out.println(t);
        
        
    }

    protected void calculateDerivatives(double t, double[] x, double[] xDot) {
        // 计算连续状态的导数
        // 类似Simulink的mdlDerivatives

        int index = 0;
        for(Block block: getBlockList()) {
            for (State state : block.getStateList()) {
                if (state.getDataType() == DataType.REAL) {
                    state.getData().setInitValue(x[index++]);
                } else {
                    Matrix matrix = state.getData().getMatrix();
                    for (int i = 0; i < matrix.getRowDimension(); i++) {
                        for (int j = 0; j < matrix.getColumnDimension(); j++) {
                            matrix.set(i, j, x[index++]);
                        }
                    }
                }
            }
        }

        calculateOutputs(t);

        for (Block block: getBlockList()) {
            block.calculateDerivative(t);
        }

        index = 0;
        for (Block block: getBlockList()) {
            for(State state: block.getStateList()){
                if(state.getDataType() == DataType.REAL){
                    xDot[index++] = state.getDerivateData().getInitValue();
                }else{
                    Matrix matrix = state.getDerivateData().getMatrix();
                    for (int i = 0; i < matrix.getRowDimension(); i++) {
                        for (int j = 0; j < matrix.getColumnDimension(); j++) {
                            xDot[index++] = matrix.get(i, j);
                        }
                    }
                }
            }
        }

    }

    protected void calculateDiscreteUpdates(double t) {
        // 更新离散状态
        // 类似Simulink的mdlUpdate
        for(Block block: getBlockList()){
            block.calculateDiscreteUpdate(t);
        }
    }

    private void calculateInits(double t, double[] x){
        // 计算各个模块的初始值
        // 类似Simulink的mdlInitialize
        for(Block block: getBlockList()){
            block.calculateInit();
        }

        int index = 0;
        for(Block block: getBlockList()) {
            for (State state : block.getStateList()) {
                if (state.getDataType() == DataType.REAL) {
                    x[index++] = state.getData().getInitValue();
                } else {
                    Matrix matrix = state.getData().getMatrix();
                    for (int i = 0; i < matrix.getRowDimension(); i++) {
                        for (int j = 0; j < matrix.getColumnDimension(); j++) {
                            x[index++] = matrix.get(i, j);
                        }
                    }
                }
            }
        }
    }

    /**
     * Validate all block parameters before simulation starts.
     * SIMULINK equivalent: mdlCheckParameters
     */
    private void calculateCheckParameters() {
        for(Block block: getBlockList()){
            block.calculateCheckParameters();
        }
    }

    /**
     * Perform one-time startup actions after initialization.
     * SIMULINK equivalent: mdlStart
     */
    private void calculateStarts() {
        for(Block block: getBlockList()){
            block.calculateStart();
        }
    }

    /**
     * Update blocks at major time step (for continuous blocks).
     * SIMULINK equivalent: mdlUpdate
     * @param t Current simulation time
     */
    private void calculateUpdates(double t) {
        for(Block block: getBlockList()){
            block.calculateUpdate(t);
        }
    }

    /**
     * Graceful shutdown before termination.
     * @param t Current simulation time
     */
    private void calculateStops(double t) {
        for(Block block: getBlockList()){
            block.calculateStop();
        }
    }

    public void calculateTerminates(double t){
        for(Block block: getBlockList()){
            block.calculateTerminate(t);
        }
    }

    /**
     * Build scope JSON data (optimized version)
     * @param cursor Scope cursor/index
     * @param scope ScopeStruct to process
     * @param jsonScope Target JSON object to populate
     */
    private void buildScopeJson(int cursor, ScopeStruct scope, JSONObject jsonScope) {
        // Get the block - can be Scope, ToWorkspace, ToFile, Display, XYGraph, Matplotlib, etc.
        Block block = scope.getBlock();

        jsonScope.put("width", scope.getWidth());
        jsonScope.put("height", scope.getHeight());
        jsonScope.put("name", block.getBlockName());
        jsonScope.put("path", block.getBlockPath());
        jsonScope.put("uuid", block.getBlockUUID());

        int size = scope.getTimeList().size();
        jsonScope.put("length", size);

        JSONArray time = new JSONArray();
        JSONArray data = new JSONArray();

        // Process time and data lists efficiently
        while (!scope.getTimeList().isEmpty() && !scope.getDataList().isEmpty()) {
            time.put(scope.getTimeList().remove(0));

            for (int h = 0; h < scope.getHeight(); h++) {
                for (int w = 0; w < scope.getWidth(); w++) {                    
                    data.put(sanitizeNumericValue(scope.getDataList().remove(0)));
                }
            }
        }

        jsonScope.put("time", time);
        jsonScope.put("data", data);
    }

    @Data
    @Getter
    public class SimulationContext {

        double tStart;
        double tEnd;
        double step;
        String solverName;
        double minStep;

        double states[];

        boolean isVariableStep(){
            // FIXME: Implement variable step logic
            return step > 0;
        }

        boolean hasStates(){
            // FIXME: Implement hasStates logic
            return states != null && states.length > 0;
        }
    }

}


