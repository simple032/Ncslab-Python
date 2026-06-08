package com.ncslab.websocket;

import java.io.IOException;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

import com.ncslab.ncslablink.*;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.communication.WebSocketMessageDto;
import com.ncslab.dto.model.MdlDataDto;
import com.ncslab.util.JsonUtils;
import com.ncslab.util.UserContext;
import com.fasterxml.jackson.core.JsonProcessingException;

@ServerEndpoint("/websocketsimulatert")
@Slf4j
public class SimulateRTWebSocket {

	@OnOpen
	public void onOpen(Session session) {
		session.setMaxTextMessageBufferSize(WebSocketSecurity.MAX_MESSAGE_SIZE);
		session.setMaxBinaryMessageBufferSize(WebSocketSecurity.MAX_MESSAGE_SIZE);
		System.out.println("[SimulateRTWebSocket] Opened session " + session.getId()
				+ " with max message size " + WebSocketSecurity.MAX_MESSAGE_SIZE + " bytes");
	}

	@OnClose
	public void onClose(Session session) {
		// Interrupt the simulation thread if still running
		Thread simThread = (Thread) session.getUserProperties().get("simulationThread");
		if (simThread != null && simThread.isAlive()) {
			System.out.println("[SimulateRTWebSocket] Session closed, interrupting simulation thread...");
			simThread.interrupt();
		}
		// Also set the stop flag on the model
		Object modelObj = session.getUserProperties().get("model");
		if (modelObj instanceof SimulationModel) {
			System.out.println("[SimulateRTWebSocket] Session closed, setting stop flag on model...");
			((SimulationModel) modelObj).stop();
		}
	}

	private void sendMessage(Session session, String msgString) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createStatusMessage(msgString, null);
        if(session!=null && session.isOpen()) {
        	// Use JsonUtils helper for direct DTO serialization
        	String messageJson = JsonUtils.serializeWebSocketMessage(message);
			    session.getBasicRemote().sendText(messageJson);
        }
	}


	private void sendErrorMessage(Session session, String msgString) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createErrorMessage(msgString);
        if(session!=null && session.isOpen()) {
        	// Use JsonUtils helper for direct DTO serialization
        	String messageJson = JsonUtils.serializeWebSocketMessage(message);
            session.getBasicRemote().sendText(messageJson);
        }
	}

	@OnMessage
	public void onMessage(Session session,String msgString){
		System.out.println("[SimulateRTWebSocket] Received WebSocket message length="
				+ (msgString != null ? msgString.length() : 0));

        // Try to parse as DTO first, fall back to legacy JSONObject
        WebSocketMessageDto wsMessage = null;
        String com = null;        
        
		// Direct ObjectMapper parsing
		try {
			wsMessage = JsonUtils.getObjectMapper().readValue(msgString, WebSocketMessageDto.class);
			com = wsMessage.getCom();
			System.out.println("[SimulateRTWebSocket] Parsed command=" + com);
		} catch (JsonProcessingException e) {
			// If Jackson parsing fails, log error and return
			log.error("Failed to parse WebSocket message with Jackson: " + e.getMessage());
			try {
				sendMessage(session, "error");
			} catch (IOException ioEx) {
				log.error("Failed to send error message: " + ioEx.getMessage());
			}
			return;
		}
        
        SimulationModel model = null;
        if(com.equals("start")) {
			// Prevent duplicate simulation for the same session
			if (session.getUserProperties().get("simulationThread") != null) {
				log.warn("Simulation already running for this session, ignoring start command");
				return;
			}

			try {
				sendMessage(session,"start");
				
				// Extract mdlData using DTO or legacy approach
				String jsonDataString;
				// Use DTO approach
				MdlDataDto mdlData = wsMessage.getMdlData();
				jsonDataString = mdlData.getJsonDataString();

				// ==================== SET USER CONTEXT FOR MULTI-USER SUPPORT ====================
				// Extract user ID and set in ThreadLocal context for expression parsing
				Integer userId = mdlData.getUserId();
				if (userId != null) {
					UserContext.setUserId(userId);
					log.info("SimulateRTWebSocket: Set user context to user ID: " + userId);
				} else {
					log.info("Warning: No user ID in mdlData, expression parsing will use default user ID");
				}

				String errorMsgs="";

				sendMessage(session,"generating");

                // Direct ObjectMapper usage - no intermediate steps
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
                	log.error("Failed to parse JSON to ModelDto: " + e.getMessage());
                	throw new ModelException("Failed to parse JSON to ModelDto DTO: " + e.getMessage());
                }

                // DEBUG: Check Delay4's IC after Jackson parsing
                if (modelDto.getBlocks() != null) {
                    for (BlockDto block : modelDto.getBlocks()) {
                        if ("Delay4".equals(block.getBlockName())) {
                            Object icValue = block.getParamValues() != null ? block.getParamValues().get("InitialCondition") : null;
                            System.out.println("SimulateRTWebSocket: After Jackson parse, Delay4 IC = '" + icValue + "'" +
                                             ", class = " + (icValue != null ? icValue.getClass().getSimpleName() : "null"));
                        }
                    }
                }

                // Validate DTO structure
                if (!modelDto.isValid()) {
                	throw new ModelException("Invalid ModelDto DTO structure");
                }
                
                log.info("Using DTO-based RT WebSocket model creation for: " + modelDto.getModelName());
                
                // Create model using DTO factory method
                model = SimulationModel.createFromDto(modelDto, ModelMode.Simulation);
                
                if (model == null) {
                	throw new ModelException("Failed to create RT WebSocket simulation model from DTO");
                }
                
                log.info("RT WebSocket model created successfully: " + model.getModelName() + 
                		   " with " + model.getBlockList().size() + " blocks");

		        	sendMessage(session,"generated");

		        	if(!model.getErrorList().isEmpty()) {
		        		for(ErrorMessage em: model.getErrorList()) {
		        			errorMsgs += em.getMessage();
		        		}
		        		throw new ModelException(errorMsgs);
		        	}

		        	// Run simulation in a separate thread so @OnClose can be called while simulating
		        	final SimulationModel finalModel = model;
		        	Thread simThread = new Thread(() -> {
		        		try {
		        			session.getUserProperties().put("model", finalModel);
		        			finalModel.simulate(session);
		        			sendMessage(session, "simulated");
		        		} catch (Exception e) {
		        			System.err.println("[SimulateRTWebSocket] Simulation thread error: " + e.getMessage());
		        			e.printStackTrace();
		        			try {
		        				sendErrorMessage(session, e.getMessage());
		        			} catch (IOException ee) {
		        				// Session may already be closed
		        			}
		        		} finally {
		        			UserContext.clear();
		        			try {
		        				if (session != null && session.isOpen()) {
		        					session.close();
		        				}
		        			} catch (IOException e) {
		        				// Ignore
		        			}
		        			// Remove thread reference so session can start a new simulation later
		        			try {
		        				session.getUserProperties().remove("simulationThread");
		        			} catch (IllegalStateException e) {
		        				// Session already closed, ignore
		        			}
		        		}
		        	}, "RT-Simulation-" + session.getId());
		        	simThread.setDaemon(true);
		        	session.getUserProperties().put("simulationThread", simThread);
		        	simThread.start();

                // sendResultMessage(session, model);
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
		        	log.error("Code generation terminated unsuccessfully");
				// Clean up thread reference on pre-simulation error
				session.getUserProperties().remove("simulationThread");
			}
			finally {
				// UserContext is cleared in the simulation thread, not here
			}
		}
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		String sessionId = session != null ? session.getId() : "null";
		String message = throwable != null ? throwable.getMessage() : "unknown";
		System.err.println("[SimulateRTWebSocket] WebSocket error on session " + sessionId + ": " + message);
		if (throwable != null) {
			throwable.printStackTrace();
		}
	}
}
