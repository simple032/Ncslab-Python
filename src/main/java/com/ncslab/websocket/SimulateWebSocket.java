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

@ServerEndpoint("/websocketsimulate")
public class SimulateWebSocket {
	private static final Logger logger = Logger.getLogger(SimulateWebSocket.class.getName());

	@OnOpen
	public void onOpen(Session session) {
		logger.info("WebSocket opened for simulation - Session: " + session.getId());
		session.setMaxTextMessageBufferSize(1024*1024);
		session.setMaxBinaryMessageBufferSize(1024*1024);
	}
	
	@OnClose
	public void onClose(Session session) {
		logger.info("WebSocket closed - Session: " + session.getId());
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
		String resultsPath = "/CCode/"+modelC.getUserId()+"/"+modelC.getModelId()+"/results.json";
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
		// Validate input and check rate limiting
		if (!WebSocketSecurity.acquireProcessingPermit()) {
			try {
				sendErrorMessage(session, "Server busy, please try again later");
			} catch (IOException e) {
				logger.severe("Failed to send busy message: " + e.getMessage());
			}
			return;
		}
		
		CodeModelC modelC = null;
		try {
			// Secure validation and parsing of the message
			WebSocketMessageDto wsMessage = WebSocketSecurity.validateAndParseMessage(session, msgString);
			
			String com = wsMessage.getCom();
			logger.info("Processing secure WebSocket command: " + com + " for session: " + session.getId());
        if(com.equals("start")) {
			try {
				sendMessage(session,"start");
				//System.out.println("Start");
				
				// Extract mdlData using DTO approach
				MdlDataDto mdlData = wsMessage.getMdlData();
				if (mdlData == null) {
					throw new ModelException("No mdlData found in WebSocket message");
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
                    throw new UnsupportedOperationException("Unsupported OS: " + osName);
                }
                System.out.println("Running on " + host + " with DTO-enhanced WebSocket");
                
                // Direct ObjectMapper usage - no intermediate JSONObject
                // Validate JSON structure first
                String validationError = JsonUtils.validateJsonStructure(jsonDataString);
                if (validationError != null) {
                	throw new ModelException("JSON validation failed: " + validationError);
                }
                
                // Parse JSON string directly to DTO using ObjectMapper
                ModelDto modelDto;
                try {
                	modelDto = JsonUtils.getObjectMapper().readValue(jsonDataString, ModelDto.class);
                } catch (JsonProcessingException e) {
                	logger.severe("Failed to parse JSON to ModelDto: " + e.getMessage());
                	throw new ModelException("Failed to parse JSON to ModelDto DTO: " + e.getMessage());
                }
                
                // Validate DTO structure
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

                //和sfunction冲突
//                modelC.removeAllFiles();

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

				if(session != null)
	        		modelC.simulate(session);

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
			// Release the processing permit
			WebSocketSecurity.releaseProcessingPermit();
			
			// Clean up model resources
			if (modelC != null) {
				try {
					modelC.postBuild();
				} catch (Exception e) {
					logger.warning("Error during model cleanup: " + e.getMessage());
				}
			}
			
			// Close session if still open
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
