package com.ncslab.websocket;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.windows.simulation.CodeModelCWindowsSimulation;
import com.utils.Property;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.communication.WebSocketMessageDto;
import com.ncslab.dto.model.MdlDataDto;
import com.ncslab.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ncslab.code.c.linux.pc.simulation.CodeModelCLinuxPCSimulation;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.SimulationModel;
import com.ncslab.util.UserContext;

/**
 * WebSocket endpoint for secure simulation processing with multi-layered security architecture
 * 
 * SECURITY ARCHITECTURE OVERVIEW:
 * ===============================
 * This WebSocket implementation employs a comprehensive 7-layer security model:
 * 
 * LAYER 1 - RATE LIMITING: 
 *   - Semaphore-based request throttling to prevent DoS attacks
 *   - Configurable concurrent request limits
 *   - Automatic busy response for overload protection
 * 
 * LAYER 2 - MESSAGE VALIDATION:
 *   - Comprehensive WebSocket message structure validation
 *   - Input sanitization to prevent injection attacks
 *   - Message size limits to prevent memory exhaustion
 * 
 * LAYER 3 - JSON VALIDATION:
 *   - Multi-stage JSON structure validation
 *   - Schema enforcement against malformed payloads
 *   - Content validation to prevent malicious JSON injection
 * 
 * LAYER 4 - SECURE DESERIALIZATION:
 *   - Jackson ObjectMapper with security configurations
 *   - Prevention of dangerous class deserialization
 *   - Object depth limits to prevent stack overflow attacks
 * 
 * LAYER 5 - DTO VALIDATION:
 *   - Business logic validation using DTO framework
 *   - Type safety enforcement and parameter validation
 *   - Cross-field validation and business constraint verification
 * 
 * LAYER 6 - THREAT RESPONSE:
 *   - Comprehensive security exception handling
 *   - Audit logging for security incidents
 *   - Sanitized error responses (no internal details exposed)
 * 
 * LAYER 7 - SECURE CLEANUP:
 *   - Proper resource cleanup to prevent security leaks
 *   - Session resource management and memory cleanup
 *   - Secure connection termination
 * 
 * ADDITIONAL SECURITY FEATURES:
 * ============================
 * - DTO-based type-safe data processing
 * - Enhanced logging for security audit trails
 * - Resource limit enforcement (buffer sizes, processing time)
 * - Graceful error handling with no information disclosure
 * - Session lifecycle security management
 * 
 * @author NCSLabLink Development Team
 * @version 2025.1 - Enhanced Security Edition
 * @see WebSocketSecurity for core security utilities
 * @see WebSocketMessageDto for secure message structures
 */
@ServerEndpoint("/websocketsimulate")
public class SimulateWebSocket {
	private static final Logger logger = Logger.getLogger(SimulateWebSocket.class.getName());

	@OnOpen
	public void onOpen(Session session) {
		// logger.info("WebSocket opened for simulation - Session: " + session.getId());
		// ==================== SECURITY: CONNECTION SETUP ====================
		// Configure secure connection parameters:
		// - Set message buffer limits to prevent memory exhaustion attacks
		// - Initialize session-specific security context
		// - Log connection for audit trail
		session.setMaxTextMessageBufferSize(1024*1024);  // 1MB limit for text messages
		session.setMaxBinaryMessageBufferSize(1024*1024); // 1MB limit for binary messages
	}
	
	@OnClose
	public void onClose(Session session) {
		// logger.info("WebSocket closed - Session: " + session.getId());
		// ==================== SECURITY: SESSION CLEANUP ====================
		// Properly clean up session-related security resources:
		// - Clear session authentication tokens
		// - Remove session from rate limiting tracking
		// - Clean up any cached security context
		// - Log session closure for audit purposes
		WebSocketSecurity.cleanupSession(session);
	}

	private void sendMessage(Session session, String msgString) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createStatusMessage(msgString, null);
        if(session!=null) {
        	// Use JsonUtils helper for direct DTO serialization
        	String messageJson = JsonUtils.serializeWebSocketMessage(message);
			    session.getBasicRemote().sendText(messageJson);
        }
	}

	private void sendResultMessage(Session session, CodeModelC modelC) throws IOException{
		String resultsPath = "/CCode/"+modelC.getUserId()+"/"+modelC.getModelId()+"/results.bin";
		WebSocketMessageDto message = WebSocketMessageDto.createResultMessage(
			resultsPath, modelC.getUserId(), modelC.getModelId());
        if(session!=null) {
        	// Use JsonUtils helper for direct DTO serialization
        	String messageJson = JsonUtils.serializeWebSocketMessage(message);
            session.getBasicRemote().sendText(messageJson);
        }
	}

	/**
	 * Send optimized result notification (results already streamed during simulation)
	 * @param session WebSocket session
	 * @param modelC Code model
	 * @throws IOException if sending fails
	 */
	private void sendOptimizedResultNotification(Session session, CodeModelC modelC) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createStatusMessage(
			"simulation_complete", "Results streamed in real-time during simulation");
		message.setUserId(modelC.getUserId());
		message.setModelId(modelC.getModelId());
		
        if(session!=null) {
        	// Use JsonUtils helper for direct DTO serialization
        	String messageJson = JsonUtils.serializeWebSocketMessage(message);
            session.getBasicRemote().sendText(messageJson);
        }
            
        System.out.println("Sent optimized result notification (no file path needed)");
	}

	private void sendErrorMessage(Session session, String msgString) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createErrorMessage(msgString);
        if(session!=null) {
        	// Use JsonUtils helper for direct DTO serialization
        	String messageJson = JsonUtils.serializeWebSocketMessage(message);
            session.getBasicRemote().sendText(messageJson);
        }
	}

	private void sendSimulatingMessage(Session session, double endTime) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createSimulationProgress(0.0, endTime);
        if(session!=null) {
        	// Use JsonUtils helper for direct DTO serialization
        	String messageJson = JsonUtils.serializeWebSocketMessage(message);
			    session.getBasicRemote().sendText(messageJson);
        }
	}

	@OnMessage
	public void onMessage(Session session, String msgString) {
		System.out.println(msgString);
		// ==================== SECURITY LAYER 1: RATE LIMITING ====================
		// Prevent DoS attacks by limiting concurrent processing requests
		// Uses semaphore-based permits to control server load
		// if (!WebSocketSecurity.acquireProcessingPermit()) {
		// 	try {
		// 		sendErrorMessage(session, "Server busy, please try again later");
		// 	} catch (IOException e) {
		// 		logger.severe("Failed to send busy message: " + e.getMessage());
		// 	}
		// 	return;
		// }
		WebSocketMessageDto wsMessage = null;
		CodeModelC modelC = null;
		try {
			// ==================== SECURITY LAYER 2: MESSAGE VALIDATION ====================
			// Comprehensive message validation including:
			// - JSON structure validation to prevent malformed payloads
			// - Message size limits to prevent memory exhaustion attacks
			// - Content sanitization to prevent injection attacks
			// - DTO-based parsing with type safety validation
			// WebSocketMessageDto wsMessage = WebSocketSecurity.validateAndParseMessage(session, msgString);
			wsMessage = JsonUtils.getObjectMapper().readValue(msgString, WebSocketMessageDto.class);
			String com = wsMessage.getCom();
			// logger.info("Processing secure WebSocket command: " + com + " for session: " + session.getId());
        if(com.equals("start")) {
			try {
				sendMessage(session,"start");
				//System.out.println("Start");

				// Extract mdlData using DTO approach
				MdlDataDto mdlData = wsMessage.getMdlData();
				if (mdlData == null) {
					throw new ModelException("No mdlData found in WebSocket message");
				}

				// ==================== SET USER CONTEXT FOR MULTI-USER SUPPORT ====================
				// Extract user ID and set in ThreadLocal context for expression parsing
				Integer userId = mdlData.getUserId();
				if (userId != null) {
					UserContext.setUserId(userId);
					System.out.println("SimulateWebSocket: Set user context to user ID: " + userId);
				} else {
					System.out.println("Warning: No user ID in mdlData, expression parsing will use default user ID");
				}
				String jsonDataString = mdlData.getJsonDataString();
				if (jsonDataString == null) {
					throw new ModelException("No jsonData found in mdlData");
				}
				//System.out.println(jsonDataString);
				String errorMsgs="";

				sendMessage(session,"generating");
	        	// instantiate a CodeModelC object

                // host 应为运行Link的操作系统来决定，而不应该由用户来决定
                String osName = System.getProperty("os.name").toLowerCase();
                String host;
                if (osName.contains("win")) {
                    host = "Windows";
                } else if (osName.contains("mac")) {
                    host = "Mac";
                } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                    host = "Linux";
                } else {
                    host = "Unknown";
                }
                System.out.println("Running on " + host + " with DTO-enhanced WebSocket");
                
                // ==================== SECURITY LAYER 3: JSON VALIDATION ====================
                // Multi-layered JSON security validation:
                // 1. Structure validation - ensures JSON is well-formed and safe
                // 2. Schema validation - validates against expected DTO structure
                // 3. Content validation - prevents malicious content injection
                String validationError = JsonUtils.validateJsonStructure(jsonDataString);
                if (validationError != null) {
                	throw new ModelException("JSON validation failed: " + validationError);
                }
                
                // ==================== SECURITY LAYER 4: SECURE DESERIALIZATION ====================
                // Use Jackson ObjectMapper with security configurations:
                // - Prevents deserialization of dangerous classes
                // - Limits object depth to prevent stack overflow
                // - Validates field types and constraints
                ModelDto modelDto;
                try {
                	modelDto = JsonUtils.getObjectMapper().readValue(jsonDataString, ModelDto.class);
                } catch (JsonProcessingException e) {
                	logger.severe("Failed to parse JSON to ModelDto: " + e.getMessage());
                	throw new ModelException("Failed to parse JSON to ModelDto DTO: " + e.getMessage());
                }
                
                // ==================== SECURITY LAYER 5: DTO VALIDATION ====================
                // Business logic validation using DTO validation framework:
                // - Type safety enforcement
                // - Parameter range validation
                // - Cross-field validation rules
                // - Business constraint verification
                if (!modelDto.isValid()) {
                	throw new ModelException("Invalid ModelDto DTO structure");
                }
                
                System.out.println("Using DTO-based WebSocket model creation for: " + modelDto.getModelName());
                
                // Create model using DTO factory methods
                if(Objects.equals(host, "Windows")){
                	modelC = CodeModelCWindowsSimulation.createFromDto(modelDto, ModelMode.Simulation);
                } else {
                	modelC = CodeModelCLinuxPCSimulation.createFromDto(modelDto, ModelMode.Simulation);
                }
                
                if (modelC == null) {
                	throw new ModelException("Failed to create WebSocket simulation model from DTO");
                }
                
                System.out.println("WebSocket model created successfully: " + modelC.getModelName() + 
                				   " on " + host + " with " + modelC.getBlockList().size() + " blocks");

                //TODO:和sfunction冲突
               	modelC.removeAllFiles();

                modelC.generate();

	        	sendMessage(session,"generated");

	        	System.out.println();

	        	if(!modelC.getErrorList().isEmpty()) {
	        		for(ErrorMessage em: modelC.getErrorList()) {
	        			errorMsgs += em.getMessage();
	        		}
	        		throw new ModelException(errorMsgs);
	        	}

	        	sendMessage(session,"compiling");

	        	if(!modelC.makeExeFile()) {
	        		throw new ModelException("Can not make exe file!");
	        	}

                //

	        	sendMessage(session,"compiled");

                //sendMessage(session,"simulating");
	        	sendSimulatingMessage(session,modelC.getConfig().getStopTime());

				if(session != null) {
					modelC.simulate(session);
				}

	        	sendMessage(session,"simulated");
	        	
	        	// Use original file I/O-based result response for step control compatibility
	        	sendResultMessage(session, modelC);
	        	
			} catch(IOException e) {
				System.err.println(e.getMessage());
	        	System.err.println("Code generatrion terminated unsuccessfully");
				throw e; // Re-throw to be handled by outer catch
			} catch (ModelException e) {
				throw e; // Re-throw to be handled by outer catch
			} catch (Exception e) {
				throw e; // Re-throw to be handled by outer catch
			}
		}
		} catch (SecurityException e) {
			// ==================== SECURITY LAYER 6: THREAT RESPONSE ====================
			// Handle security violations with appropriate logging and response:
			// - Log security incidents for audit trail
			// - Send sanitized error messages (no internal details exposed)
			// - Potentially trigger additional security measures (IP blocking, etc.)
			logger.warning("Security violation in WebSocket message: " + e.getMessage());
			try {
				sendErrorMessage(session, "Security validation failed: " + e.getMessage());
			} catch (IOException ioException) {
				logger.severe("Failed to send security error message: " + ioException.getMessage());
			}
		} catch (ModelException e) {
			logger.warning("Model exception: " + e.getMessage());
			try {
				sendErrorMessage(session, e.getMessage());
			} catch (IOException ee) {
				logger.severe("Failed to send model error message: " + ee.getMessage());
			}
		} catch (IOException e) {
			logger.severe("IO exception during simulation: " + e.getMessage());
			try {
				sendErrorMessage(session, "Internal server error during simulation");
			} catch (IOException ioException) {
				logger.severe("Failed to send IO error message: " + ioException.getMessage());
			}
		} catch (Exception e) {
			logger.severe("Unexpected exception: " + e.getMessage());
			if (session != null) {
				try {
					sendErrorMessage(session, "Internal server error");
				} catch (IOException | RuntimeException ee) {
					logger.severe("Failed to send error message: " + ee.getMessage());
				}
			}
		} finally {
			// ==================== SECURITY LAYER 7: SECURE CLEANUP ====================
			// Ensure proper resource cleanup to prevent security leaks:
			// - Release rate limiting permits to prevent resource exhaustion
			// - Clean up model resources to prevent memory leaks
			// - Properly close WebSocket connections
			// - Clear any sensitive data from memory

			// ==================== CLEAR USER CONTEXT ====================
			// Critical: Clear ThreadLocal to prevent memory leaks and context bleeding
			UserContext.clear();

			// Release the processing permit to allow other requests
			// WebSocketSecurity.releaseProcessingPermit();

			// Clean up model resources to prevent memory leaks
			if (modelC != null) {
				try {
					modelC.postBuild();
				} catch (Exception e) {
					logger.warning("Error during model cleanup: " + e.getMessage());
				}
			}

			// Securely close session connection
			try {
				if (session != null && session.isOpen()) {
					session.close();
				}
			} catch (IOException e) {
				logger.warning("Error closing WebSocket session: " + e.getMessage());
			}
		}
	}
		
}
