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
import com.ncslab.code.c.windows.simulation.CodeStructCWindowsSimulationRT;
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
import com.ncslab.util.UserContext;

@ServerEndpoint("/websocketsimulaterealtime")
public class SimulateRealtimeWebSocket {
	private static final Logger logger = Logger.getLogger(SimulateRealtimeWebSocket.class.getName());

	@OnOpen
	public void onOpen(Session session) {
		session.setMaxTextMessageBufferSize(1024*1024);
		session.setMaxBinaryMessageBufferSize(1024*1024);
	}
	
	@OnClose
	public void onClose(Session session) {
		Thread simThread = (Thread) session.getUserProperties().get("simulationThread");
		if (simThread != null && simThread.isAlive()) {
			System.out.println("[SimulateRealtimeWebSocket] Session closed, interrupting simulation thread...");
			simThread.interrupt();
		}
		Object modelObj = session.getUserProperties().get("modelC");
		if (modelObj instanceof CodeModelC) {
			System.out.println("[SimulateRealtimeWebSocket] Session closed, stopping simulation...");
			((CodeModelC) modelObj).stop();
		}
		WebSocketSecurity.cleanupSession(session);
	}

	private void sendMessage(Session session, String msgString) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createStatusMessage(msgString, null);
        if(session!=null && session.isOpen()) {
        	String messageJson = JsonUtils.serializeWebSocketMessage(message);
			    session.getBasicRemote().sendText(messageJson);
        }
	}

	private void sendResultMessage(Session session, CodeModelC modelC) throws IOException{
		String resultsPath = "/CCode/"+modelC.getUserId()+"/"+modelC.getModelId()+"/results.json";
		WebSocketMessageDto message = WebSocketMessageDto.createResultMessage(
			resultsPath, modelC.getUserId(), modelC.getModelId());
        if(session!=null && session.isOpen()) {
        	String messageJson = JsonUtils.serializeWebSocketMessage(message);
            session.getBasicRemote().sendText(messageJson);
        }
	}

	private void sendErrorMessage(Session session, String msgString) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createErrorMessage(msgString);
        if(session!=null) {
        	String messageJson = JsonUtils.serializeWebSocketMessage(message);
            session.getBasicRemote().sendText(messageJson);
        }
	}

	private void sendSimulatingMessage(Session session, double endTime) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createSimulationProgress(0.0, endTime);
        if(session!=null) {
        	String messageJson = JsonUtils.serializeWebSocketMessage(message);
			    session.getBasicRemote().sendText(messageJson);
        }
	}

	@OnMessage
	public void onMessage(Session session, String msgString) {
		System.out.println(msgString);
		WebSocketMessageDto wsMessage = null;
		CodeModelC modelC = null;
		try {
			wsMessage = JsonUtils.getObjectMapper().readValue(msgString, WebSocketMessageDto.class);
			String com = wsMessage.getCom();

			// Handle real-time parameter updates from frontend
			if (com.equals("update_params")) {
				Object modelObj = session.getUserProperties().get("modelC");
				if (modelObj instanceof CodeModelCWindowsSimulation) {
					CodeModelCWindowsSimulation model = (CodeModelCWindowsSimulation) modelObj;
					Object data = wsMessage.getData();
					if (data instanceof java.util.Map) {
						@SuppressWarnings("unchecked")
						java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) data;
						Object updates = dataMap.get("updates");
						if (updates instanceof java.util.List) {
								@SuppressWarnings("unchecked")
								java.util.List<Object> updatesList = (java.util.List<Object>) updates;
								// Convert list to indexed map for updateParameters
								java.util.Map<String, Object> paramUpdates = new java.util.HashMap<>();
								for (int i = 0; i < updatesList.size(); i++) {
									paramUpdates.put(String.valueOf(i), updatesList.get(i));
								}
								model.updateParameters(paramUpdates);
							}
					}
				}
				return;
			}

        if(com.equals("start")) {
			try {
				sendMessage(session,"start");

				MdlDataDto mdlData = wsMessage.getMdlData();
				if (mdlData == null) {
					throw new ModelException("No mdlData found in WebSocket message");
				}

				Integer userId = mdlData.getUserId();
				if (userId != null) {
					UserContext.setUserId(userId);
					System.out.println("SimulateRealtimeWebSocket: Set user context to user ID: " + userId);
				}
				String jsonDataString = mdlData.getJsonDataString();
				if (jsonDataString == null) {
					throw new ModelException("No jsonData found in mdlData");
				}
				String errorMsgs="";

				sendMessage(session,"generating");

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
                System.out.println("Running REALTIME simulation on " + host);
                
                String validationError = JsonUtils.validateJsonStructure(jsonDataString);
                if (validationError != null) {
                	throw new ModelException("JSON validation failed: " + validationError);
                }
                
                ModelDto modelDto;
                try {
                	modelDto = JsonUtils.getObjectMapper().readValue(jsonDataString, ModelDto.class);
                } catch (JsonProcessingException e) {
                	logger.severe("Failed to parse JSON to ModelDto: " + e.getMessage());
                	throw new ModelException("Failed to parse JSON to ModelDto DTO: " + e.getMessage());
                }
                
                if (!modelDto.isValid()) {
                	throw new ModelException("Invalid ModelDto DTO structure");
                }
                
                System.out.println("Using DTO-based REALTIME WebSocket model creation for: " + modelDto.getModelName());
                
                if(Objects.equals(host, "Windows")){
                	modelC = CodeModelCWindowsSimulation.createFromDto(modelDto, ModelMode.Simulation);
                } else {
                	modelC = CodeModelCLinuxPCSimulation.createFromDto(modelDto, ModelMode.Simulation);
                }
                
                if (modelC == null) {
                	throw new ModelException("Failed to create REALTIME WebSocket simulation model from DTO");
                }
                
                // Enable real-time mode
                modelC.setRealtime(true);
                // Use real-time code struct for writing ncslabmainsimurt.cpp
                if (modelC instanceof CodeModelCWindowsSimulation) {
                	((CodeModelCWindowsSimulation) modelC).setCodeStructC(new CodeStructCWindowsSimulationRT(modelC));
                }
                
                System.out.println("REALTIME WebSocket model created successfully: " + modelC.getModelName() + 
                				   " on " + host + " with " + modelC.getBlockList().size() + " blocks");

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

	        	sendMessage(session,"compiled");

	        	sendSimulatingMessage(session,modelC.getConfig().getStopTime());

				if(session != null) {
					session.getUserProperties().put("modelC", modelC);
					final CodeModelC finalModelC = modelC;
					Thread simThread = new Thread(() -> {
						try {
							finalModelC.simulate(session);
							if (session.isOpen()) {
								sendMessage(session, "simulated");
								sendResultMessage(session, finalModelC);
							}
						} catch (Exception e) {
							System.err.println("[SimulateRealtimeWebSocket] Simulation thread error: " + e.getMessage());
							e.printStackTrace();
						} finally {
							try {
								if (session != null && session.isOpen()) {
									session.close();
								}
							} catch (IOException e) {
							}
							try {
								session.getUserProperties().remove("simulationThread");
							} catch (IllegalStateException e) {
							}
						}
					}, "Realtime-Simulation-" + session.getId());
					simThread.setDaemon(true);
					session.getUserProperties().put("simulationThread", simThread);
					simThread.start();
				}
	       		
			} catch(IOException e) {
				System.err.println(e.getMessage());
        		System.err.println("Code generation terminated unsuccessfully");
				throw e;
			} catch (ModelException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
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
			UserContext.clear();
			if (modelC != null) {
				try {
					modelC.postBuild();
				} catch (Exception e) {
					logger.warning("Error during model cleanup: " + e.getMessage());
				}
			}
		}
	}
		
}
