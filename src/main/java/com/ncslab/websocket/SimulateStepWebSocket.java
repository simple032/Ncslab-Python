package com.ncslab.websocket;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import org.json.JSONArray;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import com.ncslab.ncslablink.*;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.communication.WebSocketMessageDto;
import com.ncslab.dto.model.MdlDataDto;
import com.ncslab.util.JsonUtils;
import com.ncslab.util.UserContext;
import com.utils.Property;
import org.json.JSONObject;

@ServerEndpoint("/websocketsimulatestep")
public class SimulateStepWebSocket {

	// Instance variable to track step simulation model  
	private StepSimulationModel stepSimulationModel;

	@OnOpen
	public void onOpen(Session session) {
		session.setMaxTextMessageBufferSize(WebSocketSecurity.MAX_MESSAGE_SIZE);
		session.setMaxBinaryMessageBufferSize(WebSocketSecurity.MAX_MESSAGE_SIZE);
		System.out.println("[SimulateStepWebSocket] Opened session " + session.getId()
				+ " with max message size " + WebSocketSecurity.MAX_MESSAGE_SIZE + " bytes");
		
		// Send initial connection status for RT mode
		try {
			WebSocketMessageDto welcome = WebSocketMessageDto.builder()
				.msg("rt_connection_established")
				.status("connected")
				.data(java.util.Map.of(
					"endpoint", "/websocketsimulatesteprt",
					"realtime_features", java.util.List.of(
						"step_forward_streaming",
						"step_backward_streaming", 
						"live_progress_updates",
						"immediate_result_streaming"
					)
				))
				.timestamp(System.currentTimeMillis())
				.build();
				
			if (session != null) {
				session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(welcome));
			}
		} catch (IOException e) {
			System.err.println("Failed to send RT welcome message: " + e.getMessage());
		}
	}

	private void sendMessage(Session session, String msgString) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createStatusMessage(msgString, null);
        if(session!=null) {
		    session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(message));
        } else {
            // Redirect to console when session is null (for testing)
            System.out.println("WebSocket Message (null session): " + JsonUtils.serializeWebSocketMessage(message));
        }
	}

	/**
	 * Send real-time status message with enhanced data
	 */
	private void sendRealTimeStatus(Session session, String status, String message, java.util.Map<String, Object> data) throws IOException {
		WebSocketMessageDto rtMessage = WebSocketMessageDto.builder()
			.msg(status)
			.status("rt_update")
			.data(data != null ? data : java.util.Map.of("message", message))
			.timestamp(System.currentTimeMillis())
			.build();
			
		if (session != null) {
			session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(rtMessage));
		} else {
			// Redirect to console when session is null (for testing)
			System.out.println("WebSocket RT Status (null session): " + JsonUtils.serializeWebSocketMessage(rtMessage));
		}
	}



	private void sendErrorMessage(Session session, String msgString) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createErrorMessage(msgString);
        if(session!=null) {
            session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(message));
        } else {
            // Redirect to console when session is null (for testing)
            System.out.println("WebSocket Error (null session): " + JsonUtils.serializeWebSocketMessage(message));
        }
	}

	private void sendSimulatingMessage(Session session, double endTime) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createSimulationProgress(0.0, endTime);
        if(session!=null) {
		    session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(message));
        } else {
            // Redirect to console when session is null (for testing)
            System.out.println("WebSocket Simulation Progress (null session): " + JsonUtils.serializeWebSocketMessage(message));
        }
	}

	@OnMessage
	public void onMessage(Session session,String msgString){
		System.out.println("[SimulateStepWebSocket] Received WebSocket message length="
				+ (msgString != null ? msgString.length() : 0));

        // Try to parse as DTO first, fall back to legacy JSONObject (RT pattern)
        WebSocketMessageDto wsMessage = null;
        JSONObject msg = null;
        String com = null;        
        
		// Use Jackson ObjectMapper for direct deserialization
		try {
			wsMessage = JsonUtils.getObjectMapper().readValue(msgString, WebSocketMessageDto.class);
			com = wsMessage.getCom();
			System.out.println("[SimulateStepWebSocket] Parsed command=" + com);
		} catch (Exception e) {
			System.err.println("Failed to parse WebSocket message with Jackson: " + e.getMessage());
			try {
				sendMessage(session, "error");
			} catch (IOException ioEx) {
				System.err.println("Failed to send error message: " + ioEx.getMessage());
			}
			return;
		}

        // Route to appropriate handler for commands
        switch (com) {
            case "ping":
                handlePingCommand(session, wsMessage, msg);
                break;
            case "start":
                handleStartCommand(session, wsMessage, msg);
                break;
            case "step_forward":
                handleStepForwardCommand(session, wsMessage, msg);
                break;
            case "step_backward":
                handleStepBackwardCommand(session, wsMessage, msg);
                break;
            case "pause":
                handlePauseCommand(session, wsMessage, msg);
                break;
            case "resume":
                handleResumeCommand(session, wsMessage, msg);
                break;
            case "goto_time":
                handleGotoTimeCommand(session, wsMessage, msg);
                break;
            case "set_mode":
                handleSetModeCommand(session, wsMessage, msg);
                break;
            case "get_results":
                handleGetResultsCommand(session, wsMessage, msg);
                break;
            case "get_checkpoint_stats":
                handleGetCheckpointStatsCommand(session, wsMessage, msg);
                break;
            case "stream_current_state":
                handleStreamCurrentStateCommand(session, wsMessage, msg);
                break;
            case "enable_realtime_streaming":
                handleEnableRealtimeStreamingCommand(session, wsMessage, msg);
                break;
            case "run_to_end":
                handleRunToEndCommand(session, wsMessage, msg);
                break;
            default:
                handleUnknownCommand(session, com);
                break;        
        }
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		String sessionId = session != null ? session.getId() : "null";
		String message = throwable != null ? throwable.getMessage() : "unknown";
		System.err.println("[SimulateStepWebSocket] WebSocket error on session " + sessionId + ": " + message);
		if (throwable != null) {
			throwable.printStackTrace();
		}
	}

	private void handleStartCommand(Session session, WebSocketMessageDto wsMessage, JSONObject msg) {
		try 
		{
			sendMessage(session,"start");
			
			// Extract mdlData using DTO approach
			String jsonDataString;
			MdlDataDto mdlData = wsMessage.getMdlData();
			if (mdlData != null) {
				jsonDataString = mdlData.getJsonDataString();

				// ==================== SET USER CONTEXT FOR MULTI-USER SUPPORT ====================
				// Extract user ID and set in ThreadLocal context for expression parsing
				Integer userId = mdlData.getUserId();
				if (userId != null) {
					UserContext.setUserId(userId);
					System.out.println("SimulateStepWebSocket: Set user context to user ID: " + userId);
				} else {
					System.out.println("Warning: No user ID in mdlData, expression parsing will use default user ID");
				}
			} else {
				jsonDataString = null;
			}

			String errorMsgs="";

			sendMessage(session,"generating");

			// Enhanced approach: Try DTO parsing first, fall back to legacy (RT pattern)
			
			// Validate JSON structure first
			String validationError = JsonUtils.validateJsonStructure(jsonDataString);
			if (validationError != null) {
				throw new ModelException("Invalid JSON: " + validationError);
			}
			
			// Try DTO parsing first (most efficient path)
			ModelDto modelDto = JsonUtils.parseModelDto(jsonDataString);
			
			System.out.println("Using DTO-based RT Step Control WebSocket model creation for: " + modelDto.getModelName());
			stepSimulationModel = StepSimulationModel.createFromDto(modelDto, ModelMode.Simulation);                	
				
			
			if (stepSimulationModel == null) {
				throw new ModelException("Failed to create RT Step Control WebSocket simulation model from any parsing method");
			}
			
			System.out.println("RT Step Control WebSocket model created successfully: " + stepSimulationModel.getModelName() + 
						" with " + stepSimulationModel.getBlockList().size() + " blocks");

			sendMessage(session,"generated");

			if(!stepSimulationModel.getErrorList().isEmpty()) {
				for(ErrorMessage em: stepSimulationModel.getErrorList()) {
					errorMsgs += em.getMessage();
				}
				throw new ModelException(errorMsgs);
			}

			// RT simulation: Direct simulate call (no compilation step)
			stepSimulationModel.simulate(session);

			sendMessage(session,"simulated");
		}
		catch(IOException e) {
			System.err.println(e.getMessage());
			System.err.println("RT Step Control simulation terminated unsuccessfully");
		}
		catch(ModelException e) {
			try {
				sendErrorMessage(session,e.getMessage());
			}
			catch(IOException ee) {
			}
			System.err.println(e.getMessage());
			System.err.println("RT Step Control simulation terminated unsuccessfully");
		}
		catch(Exception e) {
			e.printStackTrace();
			try {
				sendErrorMessage(session,e.getMessage());
			}
			catch(IOException ee) {
				ee.printStackTrace();
			}
			catch (Exception ee) {
				ee.printStackTrace();
			}
		}
		finally {
			// ==================== CLEAR USER CONTEXT ====================
			// Critical: Clear ThreadLocal to prevent memory leaks and context bleeding
			UserContext.clear();
		}
	}

	/**
	 * Handle the 'ping' command for WebSocket connectivity check
	 */
	private void handlePingCommand(Session session, WebSocketMessageDto wsMessage, JSONObject msg) {
		try {
			// Extract timestamp from ping request
			long requestTimestamp = 0;
			if (wsMessage != null && wsMessage.getTimestamp() != null) {
				requestTimestamp = wsMessage.getTimestamp();
			} else if (msg != null && msg.has("timestamp")) {
				requestTimestamp = msg.getLong("timestamp");
			}
			
			// Create pong response with original and current timestamps
			WebSocketMessageDto pongResponse = WebSocketMessageDto.builder()
				.msg("pong")
				.status("success")
				.data(java.util.Map.of(
					"request_timestamp", requestTimestamp,
					"response_timestamp", System.currentTimeMillis(),
					"server_time", System.currentTimeMillis(),
					"connection_status", "active",
					"endpoint", "/websocketsimulatestep",
					"simulation_status", stepSimulationModel != null ? "ready" : "not_initialized"
				))
				.timestamp(System.currentTimeMillis())
				.build();
			
			if (session != null) {
				session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(pongResponse));
			} else {
				// Redirect to console when session is null (for testing)
				System.out.println("WebSocket Pong (null session): " + JsonUtils.serializeWebSocketMessage(pongResponse));
			}
			
			System.out.printf("RT Step Control: Ping received (req_ts=%d), pong sent (resp_ts=%d)%n", 
				requestTimestamp, System.currentTimeMillis());
			
		} catch (Exception e) {
			try {
				sendErrorMessage(session, "Ping handler error: " + e.getMessage());
			} catch (IOException ee) {
				System.err.println("Failed to send ping error response: " + ee.getMessage());
			}
			System.err.println("Ping command failed: " + e.getMessage());
		}
	}

	/**
	 * Handle the 'step_forward' command with precise StepSimulationModel control
	 */
	private void handleStepForwardCommand(Session session, WebSocketMessageDto wsMessage, JSONObject msg) {
		try {
			if (stepSimulationModel == null) {
				sendErrorMessage(session, "No active step simulation model. Please start a simulation first.");
				return;
			}
			
			// Extract step parameters using DTO or legacy approach
			int steps = 1;
			Double customStep = null;
			
			if (wsMessage != null && wsMessage.getSteps() != null) {
				steps = wsMessage.getSteps();
			} else if (msg != null && msg.has("steps")) {
				steps = msg.getInt("steps");
			}
			
			if (wsMessage != null && wsMessage.getCustomStepSize() != null) {
				customStep = wsMessage.getCustomStepSize();
			} else if (msg != null && msg.has("customStepSize")) {
				customStep = msg.getDouble("customStepSize");
			}
			
			// Use StepSimulationModel's precise step forward execution
			stepSimulationModel.executeStepForward(session, steps, customStep);
			
			System.out.println("Step forward simulation completed: " + steps + " steps at time " + stepSimulationModel.getCurrentTime());
			
		} catch (ModelException e) {
			try {
				sendErrorMessage(session, "Step forward failed: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		} catch (Exception e) {
			try {
				sendErrorMessage(session, "Step forward error: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}
	}

	/**
	 * Handle the 'step_backward' command with precise StepSimulationModel control  
	 */
	private void handleStepBackwardCommand(Session session, WebSocketMessageDto wsMessage, JSONObject msg) {
		try {
			if (stepSimulationModel == null) {
				sendErrorMessage(session, "No active step simulation model. Please start a simulation first.");
				return;
			}
			
			// Extract step parameters
			int steps = 1;
			Double targetTime = null;
			
			if (wsMessage != null && wsMessage.getSteps() != null) {
				steps = wsMessage.getSteps();
			} else if (msg != null && msg.has("steps")) {
				steps = msg.getInt("steps");
			}
			
			if (wsMessage != null && wsMessage.getTargetTime() != null) {
				targetTime = wsMessage.getTargetTime();
			} else if (msg != null && msg.has("targetTime")) {
				targetTime = msg.getDouble("targetTime");
			}
			
			sendMessage(session, "stepping_backward_rt");
			
			// Use StepSimulationModel's precise step backward execution
			stepSimulationModel.executeStepBackward(session, steps, targetTime);
			
			sendMessage(session, "step_backward_complete");
			System.out.println("Step backward simulation completed: " + steps + " steps at time " + stepSimulationModel.getCurrentTime());
			
		} catch (ModelException e) {
			try {
				sendErrorMessage(session, "Step backward failed: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		} catch (Exception e) {
			try {
				sendErrorMessage(session, "Step backward error: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}
	}

	/**
	 * Handle the 'pause' command with StepSimulationModel control
	 */
	private void handlePauseCommand(Session session, WebSocketMessageDto wsMessage, JSONObject msg) {
		try {
			if (stepSimulationModel != null) {
				stepSimulationModel.pauseSimulation(session);
			} else {
				sendMessage(session, "pause_acknowledged");
			}
			System.out.println("Step simulation pause requested");
		} catch (Exception e) {
			try {
				sendErrorMessage(session, "Pause error: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}
	}

	/**
	 * Handle the 'resume' command with StepSimulationModel control
	 */
	private void handleResumeCommand(Session session, WebSocketMessageDto wsMessage, JSONObject msg) {
		try {
			if (stepSimulationModel != null) {
				stepSimulationModel.resumeSimulation(session);
			} else {
				sendMessage(session, "resume_acknowledged");
			}
			System.out.println("Step simulation resume requested");
		} catch (Exception e) {
			try {
				sendErrorMessage(session, "Resume error: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}
	}

	/**
	 * Handle the 'goto_time' command with precise time control
	 */
	private void handleGotoTimeCommand(Session session, WebSocketMessageDto wsMessage, JSONObject msg) {
		try {
			if (stepSimulationModel == null) {
				sendErrorMessage(session, "No active step simulation model. Please start a simulation first.");
				return;
			}
			
			// Extract target time
			Double targetTime = null;
			if (wsMessage != null && wsMessage.getTargetTime() != null) {
				targetTime = wsMessage.getTargetTime();
			} else if (msg != null && msg.has("targetTime")) {
				targetTime = msg.getDouble("targetTime");
			}
			
			if (targetTime == null) {
				sendErrorMessage(session, "Target time not specified for goto_time command");
				return;
			}
			
			sendMessage(session, "goto_time_acknowledged");
			
			// Use step backward with target time (will handle forward/backward jumps)
			stepSimulationModel.executeStepBackward(session, 0, targetTime);
			
			System.out.println("Step simulation goto time completed: jumped to " + targetTime);
			
		} catch (ModelException e) {
			try {
				sendErrorMessage(session, "Goto time failed: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		} catch (Exception e) {
			try {
				sendErrorMessage(session, "Goto time error: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}
	}

	/**
	 * Handle the 'set_mode' command
	 */
	private void handleSetModeCommand(Session session, WebSocketMessageDto wsMessage, JSONObject msg) {
		try {
			sendMessage(session, "set_mode_acknowledged");
			System.out.println("RT simulation set mode requested");
		} catch (Exception e) {
			try {
				sendErrorMessage(session, "RT set mode error: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}
	}

	/**
	 * Handle the 'get_results' command
	 */
	private void handleGetResultsCommand(Session session, WebSocketMessageDto wsMessage, JSONObject msg) {
		try {
			if (stepSimulationModel == null) {
				sendErrorMessage(session, "No step simulation model available for streaming results.");
				return;
			}
			
			// Send current simulation state and results
			WebSocketMessageDto resultMsg = WebSocketMessageDto.builder()
				.msg("simulation_results")
				.status("success")
				.data(java.util.Map.of(
					"current_time", stepSimulationModel.getCurrentTime(),
					"current_step", stepSimulationModel.getCurrentStepCount(),
					"step_size", stepSimulationModel.getStepSize(),
					"is_paused", stepSimulationModel.isPaused(),
					"total_data_points", stepSimulationModel.getTotalDataPoints()
				))
				.timestamp(System.currentTimeMillis())
				.build();
			
			if (session != null) {
				session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(resultMsg));
			} else {
				// Redirect to console when session is null (for testing)
				System.out.println("WebSocket Results (null session): " + JsonUtils.serializeWebSocketMessage(resultMsg));
			}
			
		} catch (IOException e) {
			try {
				sendErrorMessage(session, "Failed to send results: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}
	}

	/**
	 * Handle the 'get_checkpoint_stats' command
	 */
	private void handleGetCheckpointStatsCommand(Session session, WebSocketMessageDto wsMessage, JSONObject msg) {
		try {
			if (stepSimulationModel == null) {
				sendErrorMessage(session, "No active step simulation model.");
				return;
			}
			
			// Create checkpoint stats from StepSimulationModel
			java.util.Map<String, Object> stats = java.util.Map.of(
				"current_time", stepSimulationModel.getCurrentTime(),
				"current_step", stepSimulationModel.getCurrentStepCount(),
				"step_size", stepSimulationModel.getStepSize(),
				"is_paused", stepSimulationModel.isPaused(),
				"checkpoint_time", System.currentTimeMillis()
			);
			
			WebSocketMessageDto message = WebSocketMessageDto.builder()
					.msg("checkpoint_stats")
					.status("success")
					.data(stats)
					.timestamp(System.currentTimeMillis())
					.build();
			
			if (session != null) {
				session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(message));
			} else {
				// Redirect to console when session is null (for testing)
				System.out.println("WebSocket Checkpoint Stats (null session): " + JsonUtils.serializeWebSocketMessage(message));
			}
			
		} catch (IOException e) {
			try {
				sendErrorMessage(session, "Failed to get checkpoint stats: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}
	}

	/**
	 * Handle the 'stream_current_state' command
	 */
	private void handleStreamCurrentStateCommand(Session session, WebSocketMessageDto wsMessage, JSONObject msg) {
		try {
			if (stepSimulationModel == null) {
				sendErrorMessage(session, "No active step simulation model.");
				return;
			}
			
			// Stream current simulation state
			WebSocketMessageDto stateMsg = WebSocketMessageDto.builder()
				.msg("current_state")
				.status("streaming")
				.data(java.util.Map.of(
					"current_time", stepSimulationModel.getCurrentTime(),
					"current_step", stepSimulationModel.getCurrentStepCount(),
					"step_size", stepSimulationModel.getStepSize(),
					"is_paused", stepSimulationModel.isPaused(),
					"stream_time", System.currentTimeMillis()
				))
				.timestamp(System.currentTimeMillis())
				.build();
			
			if (session != null) {
				session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(stateMsg));
			} else {
				// Redirect to console when session is null (for testing)
				System.out.println("WebSocket Current State (null session): " + JsonUtils.serializeWebSocketMessage(stateMsg));
			}
			
			sendMessage(session, "current_state_streamed");
			
		} catch (IOException e) {
			try {
				sendErrorMessage(session, "Failed to stream current state: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}
	}

	/**
	 * Handle the 'enable_realtime_streaming' command
	 */
	private void handleEnableRealtimeStreamingCommand(Session session, WebSocketMessageDto wsMessage, JSONObject msg) {
		try {
			sendMessage(session, "realtime_streaming_enabled");
			System.out.println("Real-time streaming enabled for step control simulation");
			
		} catch (IOException e) {
			try {
				sendErrorMessage(session, "Failed to enable real-time streaming: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}
	}

	/**
	 * Handle the 'run_to_end' command to run simulation from current time to configured end time
	 */
	private void handleRunToEndCommand(Session session, WebSocketMessageDto wsMessage, JSONObject msg) {
		try {
			if (stepSimulationModel == null) {
				sendErrorMessage(session, "No active step simulation model. Please start a simulation first.");
				return;
			}
			
			// Extract optional parameters using same pattern as other commands
			Double endTime = null;
			Double stepSize = null;
			boolean enableStreaming = true; // Default to true for real-time updates
			
			// Extract parameters using DTO or legacy approach (same pattern as other commands)
			if (wsMessage != null && wsMessage.getTargetTime() != null) {
				endTime = wsMessage.getTargetTime(); // Reuse targetTime field for endTime
			} else if (msg != null && msg.has("endTime")) {
				endTime = msg.getDouble("endTime");
			}
			
			if (wsMessage != null && wsMessage.getCustomStepSize() != null) {
				stepSize = wsMessage.getCustomStepSize();
			} else if (msg != null && msg.has("stepSize")) {
				stepSize = msg.getDouble("stepSize");
			}
			
			if (msg != null && msg.has("enableStreaming")) {
				enableStreaming = msg.getBoolean("enableStreaming");
			}
			
			// Get current simulation parameters
			double currentTime = stepSimulationModel.getCurrentTime();
			double targetEndTime = endTime != null ? endTime : stepSimulationModel.getConfig().getStopTime();
			double currentStepSize = stepSize != null ? stepSize : stepSimulationModel.getStepSize();
			
			// Validate parameters
			if (targetEndTime <= currentTime) {
				sendErrorMessage(session, "End time (" + targetEndTime + ") must be greater than current time (" + currentTime + ")");
				return;
			}
			
			sendMessage(session, "run_to_end_started");
			
			// Calculate total steps needed
			double timeRemaining = targetEndTime - currentTime;
			int totalSteps = (int) Math.ceil(timeRemaining / currentStepSize);
			
			System.out.printf("Run to end: Running from %.6f to %.6f (%.6f remaining, %d steps, stepSize=%.6f)%n", 
				currentTime, targetEndTime, timeRemaining, totalSteps, currentStepSize);
			
			// Send initial progress
			sendRealTimeStatus(session, "run_to_end_progress", "Starting continuous run", 
				java.util.Map.of(
					"start_time", currentTime,
					"target_end_time", targetEndTime,
					"total_steps", totalSteps,
					"progress_percent", 0.0,
					"enable_streaming", enableStreaming
				));
			
			// Execute simulation in chunks with progress updates
			int progressUpdateInterval = Math.max(1, totalSteps / 20); // Update every 5% progress
			int stepsCompleted = 0;
			
			while (stepSimulationModel.getCurrentTime() < targetEndTime && stepsCompleted < totalSteps) {
				// Calculate steps for this chunk (avoid overshooting)
				double remainingTime = targetEndTime - stepSimulationModel.getCurrentTime();
				int stepsThisChunk = Math.min(progressUpdateInterval, 
					(int) Math.ceil(remainingTime / currentStepSize));
				
				if (stepsThisChunk <= 0) break;
				
				// Execute step forward
				stepSimulationModel.executeStepForward(session, stepsThisChunk, currentStepSize);
				stepsCompleted += stepsThisChunk;
				
				// Send progress update
				double progressPercent = Math.min(100.0, (stepsCompleted * 100.0) / totalSteps);
				
				if (enableStreaming) {
					sendRealTimeStatus(session, "run_to_end_progress", "Running to end", 
						java.util.Map.of(
							"current_time", stepSimulationModel.getCurrentTime(),
							"target_end_time", targetEndTime,
							"steps_completed", stepsCompleted,
							"total_steps", totalSteps,
							"progress_percent", progressPercent,
							"enable_streaming", enableStreaming
						));
				}
				
				// Small delay to prevent overwhelming the client
				Thread.sleep(10);
			}
			
			// Send completion message
			sendRealTimeStatus(session, "run_to_end_complete", "Simulation completed", 
				java.util.Map.of(
					"final_time", stepSimulationModel.getCurrentTime(),
					"target_end_time", targetEndTime,
					"total_steps_completed", stepsCompleted,
					"progress_percent", 100.0
				));
			
			sendMessage(session, "run_to_end_completed");
			
			System.out.printf("Run to end completed: Final time %.6f, completed %d steps%n", 
				stepSimulationModel.getCurrentTime(), stepsCompleted);
			
		} catch (ModelException e) {
			try {
				sendErrorMessage(session, "Run to end failed: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		} catch (Exception e) {
			try {
				sendErrorMessage(session, "Run to end error: " + e.getMessage());
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}
	}

	/**
	 * Handle unknown commands
	 */
	private void handleUnknownCommand(Session session, String command) {
		try {
			sendErrorMessage(session, "Unknown step control command: " + command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
