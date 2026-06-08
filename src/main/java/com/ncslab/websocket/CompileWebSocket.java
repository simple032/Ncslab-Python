package com.ncslab.websocket;

import java.io.IOException;
import java.util.Optional;

import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ncslab.code.CodeModelFactory;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.model.MdlDataDto;
import com.ncslab.dto.model.PlantInfoDto;
import com.ncslab.dto.communication.WebSocketMessageDto;
import com.ncslab.util.JsonUtils;
import com.utils.Property;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import com.ncslab.code.Solver;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.util.UserContext;

@Slf4j
@ServerEndpoint("/websocketcompile")

public class CompileWebSocket {
	@OnOpen
	public void onOpen(Session session) {
		System.out.println("CompileWebSocket opened - Session: " + session.getId());
		session.setMaxTextMessageBufferSize(WebSocketSecurity.MAX_MESSAGE_SIZE);
		session.setMaxBinaryMessageBufferSize(WebSocketSecurity.MAX_MESSAGE_SIZE);
		System.out.println("CompileWebSocket max message size: " + WebSocketSecurity.MAX_MESSAGE_SIZE + " bytes");
		
		// Set session timeout to prevent premature closure during compilation
		session.setMaxIdleTimeout(300000); // 5 minutes
		
		// Log connection details
		System.out.println("CompileWebSocket connection established from: " + 
			session.getRequestURI() + " - ID: " + session.getId());
	}

	private void sendMessage(Session session, String msgString) throws IOException {
		WebSocketMessageDto message = WebSocketMessageDto.createStatusMessage(msgString, null);
		if(session != null && session.isOpen()) {
			session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(message));
		} else {
			System.out.println("Send message (session closed/null): " + msgString);
		}
	}

	private void sendErrorMessage(Session session, String msgString) throws IOException {
		WebSocketMessageDto message = WebSocketMessageDto.createErrorMessage(msgString);
		if(session != null && session.isOpen()) {
			session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(message));
		} else {
			System.out.println("Send error message (session closed/null): " + msgString);
		}
	}

	@OnMessage
	public void onMessage(Session session, String msgString) {
		 System.out.println(msgString);
		
        // Try to parse as DTO first, fall back to legacy JSONObject
        WebSocketMessageDto wsMessage = null;
        JSONObject msg = null;
        String com = null;
        
        try {
        	// Use Jackson ObjectMapper for direct deserialization
        	wsMessage = JsonUtils.getObjectMapper().readValue(msgString, WebSocketMessageDto.class);
        	if (wsMessage != null && wsMessage.getCom() != null) {
        		com = wsMessage.getCom();
        		System.out.println("Using Jackson DTO-based Compile WebSocket message parsing for command: " + com);
        	} else {
        		throw new Exception("DTO parsing failed or no command");
        	}
        } catch (Exception e) {
        	// Fall back to legacy parsing
        	System.out.println("DTO Compile WebSocket message parsing failed, using legacy JSONObject: " + e.getMessage());
        	msg = new JSONObject(msgString);
        	if(!msg.has("com")) {
        		try {
        			sendErrorMessage(session, "Invalid message format: 'com' key is missing.");
        		} catch (IOException ioException) {
        			log.error("Error sending error message: ", ioException);
        		}
        		return;
        	}
        	com = msg.getString("com");
        }
        CodeModelC modelC = null;
		if (com.equals("start")) {
			try {
				sendMessage(session, "start");
				// System.out.println("Start");
				
				// Extract mdlData using DTO or legacy approach
				MdlDataDto mdlData = wsMessage.getMdlData();
				if (mdlData == null) {
					throw new ModelException("No mdlData found in WebSocket message");
				}

				// ==================== SET USER CONTEXT FOR MULTI-USER SUPPORT ====================
				// Extract user ID and set in ThreadLocal context for expression parsing
				Integer userId = mdlData.getUserId();
				if (userId != null) {
					UserContext.setUserId(userId);
					System.out.println("CompileWebSocket: Set user context to user ID: " + userId);
				} else {
					System.out.println("Warning: No user ID in mdlData, expression parsing will use default user ID");
				}

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

                String target = mdlData.getTarget();

                PlantInfoDto plantInfo = mdlData.getPlantInfo();
                String matlabExt = plantInfo.getMatlabExt();
                if(!"".equals(matlabExt)){
                    if(matlabExt.toLowerCase().contains("pi")){
                        target = "Raspberry";
                    }
                }

                System.out.println("The target is:" + target);

				String jsonDataString = mdlData.getJsonDataString();
				
				// Parse JSON string directly to DTO using ObjectMapper
                ModelDto modelDto;
                try {
                	modelDto = JsonUtils.getObjectMapper().readValue(jsonDataString, ModelDto.class);
                } catch (JsonProcessingException e) {
                	System.err.println("Failed to parse JSON to ModelDto: " + e.getMessage());
                	throw new ModelException("Failed to parse JSON to ModelDto DTO: " + e.getMessage());
                }

				// System.out.println(jsonDataString);
				String errorMsgs = "";

				sendMessage(session, "generating");

                // 使用反射构建
                modelC = CodeModelFactory.createFromDto(host, target, modelDto, ModelMode.Compilation);

                if(modelC == null){
                    throw new ModelException("Can not find the host/target:" + host + target);
                }
				// CodeModelCLinuxPC
				// modelC=CodeModelCLinuxPC.createFromJSON(jsonIn,ModelMode.Compilation);

				modelC.setSolver(Solver.ode4);

                modelC.removeAllFiles();

                modelC.generate();

				sendMessage(session, "generated");

				if (!modelC.getErrorList().isEmpty()) {
					for (ErrorMessage em : modelC.getErrorList()) {
						errorMsgs += em.getMessage();
					}
					throw new ModelException(errorMsgs);
				}

				sendMessage(session, "compiling");

				if (!modelC.makeExeFile()) {
					throw new ModelException("Can not make exe file!");
				}

				sendMessage(session, "compiled");

                sendMessage(session, "database inserting");

				if(session != null)
					modelC.saveToDatabase();

				sendMessage(session, "database inserted");


				sendMessage(session, "finished");

				/*
				 * if(modelC.getErrorList().size()==0) {
				 * if(modelC.makeExeFile()) {
				 * modelC.saveToDatabase();
				 * errorMsgs += "make exe success.";
				 * }
				 * else {
				 * throw new ModelException("Can not make exe file!");
				 * }
				 * }else {
				 * for(ErrorMessage em: modelC.getErrorList()) {
				 * errorMsgs += em.getMessage();
				 * }
				 * throw new ModelException(errorMsgs);
				 * }
				 */

			} catch (IOException|ModelException|NullPointerException e) {
				try {
					sendErrorMessage(session, e.getMessage());

				} catch (IOException ee) {
                    log.error("e: ", ee);
				}
                log.error("e: ", e);
				System.err.println("Code generatrion terminated unsuccessfully");
			} finally {
				// ==================== CLEAR USER CONTEXT ====================
				// Critical: Clear ThreadLocal to prevent memory leaks and context bleeding
				UserContext.clear();

                if(modelC != null)
                    modelC.postBuild();
				try {
					if(session != null)
						session.close();
				} catch (IOException e) {
                    log.error("e: ", e);
				}
			}

		} else if (com.equals("ping")) {
			// Handle ping command for heartbeat
			handlePingCommand(session, wsMessage, msg);
		} else {
			try {
				sendErrorMessage(session, "Unknown command: " + com);
			} catch (IOException e) {
				log.error("Error sending unknown command error: ", e);
			}
		}
	}
	
	/**
	 * Handle ping command for WebSocket heartbeat
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
					"endpoint", "/websocketcompile"
				))
				.timestamp(System.currentTimeMillis())
				.build();
			
			// Send pong response
			if (session != null && session.isOpen()) {
				session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(pongResponse));
			} else {
				// Redirect to console when session is null (for testing)
				System.out.println("WebSocket Pong (null session): " + JsonUtils.serializeWebSocketMessage(pongResponse));
			}
			
			System.out.printf("Compile: Ping received (req_ts=%d), pong sent (resp_ts=%d)%n", 
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
	
	@OnClose
	public void onClose(Session session) {
		System.out.println("CompileWebSocket connection closed: " + session.getId());
		// Clean up any resources if needed
		com.ncslab.websocket.WebSocketSecurity.cleanupSession(session);
	}
	
	@OnError
	public void onError(Session session, Throwable throwable) {
		log.error("CompileWebSocket error for session " + session.getId() + ": " + throwable.getMessage(), throwable);
		
		// Try to send error message to client if session is still open
		try {
			if (session != null && session.isOpen()) {
				sendErrorMessage(session, "WebSocket error: " + throwable.getMessage());
			}
		} catch (IOException e) {
			log.error("Failed to send error message to client: " + e.getMessage(), e);
		}
		
		// Close the session if it's still open
		try {
			if (session != null && session.isOpen()) {
				session.close();
			}
		} catch (IOException e) {
			log.error("Error closing session after error: " + e.getMessage(), e);
		}
	}
}
