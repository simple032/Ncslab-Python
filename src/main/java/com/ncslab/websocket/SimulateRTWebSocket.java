package com.ncslab.websocket;

import java.io.IOException;

import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.ncslablink.*;
import com.ncslab.dto.ModelJson;
import com.ncslab.dto.WebSocketMessageJson;
import com.ncslab.util.JsonUtils;
import com.utils.Property;
import org.json.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;

@ServerEndpoint("/websocketsimulatert")

public class SimulateRTWebSocket {

	@OnOpen
	public void onOpen(Session session) {
		System.out.println("WEBopen Experiment for RT Simulation");
		session.setMaxTextMessageBufferSize(1024*1024);
		session.setMaxBinaryMessageBufferSize(1024*1024);
	}

	private void sendMessage(Session session, String msgString) throws IOException{
		WebSocketMessageJson message = WebSocketMessageJson.createStatusMessage(msgString, null);
        if(session!=null) {
        	// Use JsonUtils helper for direct DTO serialization
        	String messageJson = JsonUtils.serializeWebSocketMessage(message);
			    session.getBasicRemote().sendText(messageJson);
        }
	}


	private void sendErrorMessage(Session session, String msgString) throws IOException{
		WebSocketMessageJson message = WebSocketMessageJson.createErrorMessage(msgString);
        if(session!=null) {
        	// Use JsonUtils helper for direct DTO serialization
        	String messageJson = JsonUtils.serializeWebSocketMessage(message);
            session.getBasicRemote().sendText(messageJson);
        }
	}

	@OnMessage
	public void onMessage(Session session,String msgString){
		System.out.println(msgString);

        // Try to parse as DTO first, fall back to legacy JSONObject
        WebSocketMessageJson wsMessage = null;
        JSONObject msg = null;
        String com = null;
        
        try {
        	// Direct ObjectMapper parsing
        	try {
        		wsMessage = JsonUtils.getObjectMapper().readValue(msgString, WebSocketMessageJson.class);
        		if (wsMessage != null && wsMessage.getCom() != null) {
        			com = wsMessage.getCom();
        			System.out.println("Using ObjectMapper-based RT WebSocket message parsing for command: " + com);
        		} else {
        			throw new Exception("Parsed message or command is null");
        		}
        	} catch (JsonProcessingException e) {
        		// Fall back to legacy JSONObject if ObjectMapper fails
        		System.out.println("ObjectMapper parsing failed, using legacy JSONObject: " + e.getMessage());
        		JSONObject tempJson = new JSONObject(msgString);
        		wsMessage = WebSocketMessageJson.fromLegacyJson(tempJson);
        		com = wsMessage != null ? wsMessage.getCom() : null;
        	}
        } catch (Exception e) {
        	// Final fall back to direct JSONObject parsing
        	System.out.println("All DTO parsing failed, using direct JSONObject: " + e.getMessage());
        	msg = new JSONObject(msgString);
        	com = msg.getString("com");
        }
        SimulationModel model = null;
        if(com.equals("start")) {
			try {
				sendMessage(session,"start");
				
				// Extract mdlData using DTO or legacy approach
				String jsonDataString;
				if (wsMessage != null && wsMessage.getMdlData() != null) {
					// Use DTO approach
					jsonDataString = (String) wsMessage.getMdlData().get("jsonData");
				} else {
					// Use legacy approach
					JSONObject mdlData = msg.getJSONObject("mdlData");
					jsonDataString = mdlData.getString("jsonData");
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
                ModelJson modelDto;
                try {
                	modelDto = JsonUtils.getObjectMapper().readValue(jsonDataString, ModelJson.class);
                } catch (JsonProcessingException e) {
                	System.err.println("Failed to parse JSON to ModelJson: " + e.getMessage());
                	throw new ModelException("Failed to parse JSON to ModelJson DTO: " + e.getMessage());
                }
                
                // Validate DTO structure
                if (!modelDto.isValid()) {
                	throw new ModelException("Invalid ModelJson DTO structure");
                }
                
                System.out.println("Using DTO-based RT WebSocket model creation for: " + modelDto.getModelName());
                
                // Create model using DTO factory method
                model = SimulationModel.createFromDto(modelDto, ModelMode.Simulation);
                
                if (model == null) {
                	throw new ModelException("Failed to create RT WebSocket simulation model from DTO");
                }
                
                System.out.println("RT WebSocket model created successfully: " + model.getModelName() + 
                		   " with " + model.getBlockList().size() + " blocks");

	        	sendMessage(session,"generated");

	        	if(!model.getErrorList().isEmpty()) {
	        		for(ErrorMessage em: model.getErrorList()) {
	        			errorMsgs += em.getMessage();
	        		}
	        		throw new ModelException(errorMsgs);
	        	}

	        	model.simulate(session);

	        	sendMessage(session,"simulated");

                // sendResultMessage(session, model);
	        }
			catch(IOException e) {
				System.err.println(e.getMessage());
	        	System.err.println("Code generation terminated unsuccessfully");
			}
	        catch(ModelException e) {
	        	try {
	        		sendErrorMessage(session,e.getMessage());
	        	}
	        	catch(IOException ee) {
	        	}
	        	System.err.println(e.getMessage());
	        	System.err.println("Code generation terminated unsuccessfully");
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
                try {
                    if(session != null)
					    session.close();
				}
				catch(IOException e) {
                    e.printStackTrace();
				}
                catch (Exception ee) {
                    ee.printStackTrace();
                }
			}
		}
	}
}