package com.ncslab.websocket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import org.json.JSONObject;

import com.ncslab.code.c.SFcnCompileModelC;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.communication.WebSocketMessageDto;
import com.ncslab.util.JsonUtils;
import com.ncslab.code.c.linux.pc.simulation.CodeModelCLinuxPCSimulation;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.SFcnException;
import com.ncslab.ncslablink.SFcnModel;

@ServerEndpoint("/websocketsfcncompile")

public class SFcnCompileWebSocket {

	@OnOpen
	public void onOpen(Session session){
		System.out.println("WEBopen Experiment for Sfunction Compile");
		session.setMaxTextMessageBufferSize(WebSocketSecurity.MAX_MESSAGE_SIZE);
		session.setMaxBinaryMessageBufferSize(WebSocketSecurity.MAX_MESSAGE_SIZE);
	}
	
	private void sendMessage(Session session, String msgString) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createStatusMessage(msgString, null);
		session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(message));
	}
	
	private void sendErrorMessage(Session session, String msgString) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.createErrorMessage(msgString);
		session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(message));
	}
	
	private void sendResultMessage(Session session, String msgString) throws IOException{
		WebSocketMessageDto message = WebSocketMessageDto.builder()
			.msg("result")
			.status("success")
			.data(msgString)
			.timestamp(System.currentTimeMillis())
			.build();
		session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(message));
	}
	
	@OnMessage
	public void onMessage(Session session,String msgString) {

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
        		System.out.println("Using Jackson DTO-based SFcn Compile WebSocket message parsing for command: " + com);
        	} else {
        		throw new Exception("DTO parsing failed or no command");
        	}
        } catch (Exception e) {
        	// Fall back to legacy parsing
        	System.out.println("DTO SFcn Compile WebSocket message parsing failed, using legacy JSONObject: " + e.getMessage());
        	msg = new JSONObject(msgString);
        	com = msg.getString("com");
        }
		if(com.equals("sfcncompile")) {
			try {
				sendMessage(session, "start");
				
				// Extract jsonData using DTO or legacy approach
				JSONObject jsonData;
				if (wsMessage != null && wsMessage.getData() != null) {
					// Use DTO approach - convert data back to JSONObject for compatibility
					if (wsMessage.getData() instanceof JSONObject) {
						jsonData = (JSONObject) wsMessage.getData();
					} else {
						jsonData = new JSONObject(wsMessage.getData().toString());
					}
				} else {
					// Use legacy approach
					jsonData = msg.getJSONObject("jsonData");
				}
				
				SFcnCompileModelC modelSFcn=new SFcnCompileModelC(jsonData);
				modelSFcn.writeSFcnFile();
				if(modelSFcn.makeExeFile()==false) {
					throw new SFcnException("Can not make exe file!");
				};
				modelSFcn.compile();
				String result = modelSFcn.checkDimentions();
				if(result.startsWith("Error")) {
					sendErrorMessage(session, result);
				}
				if(result.startsWith("result")) {
					sendResultMessage(session, result);
				}
				
			}
			catch (IOException e) {
				System.err.println(e.getMessage());
	        	System.err.println("Code generatrion terminated unsuccessfully������");
			}
			catch (SFcnException e) {
				try {
					sendErrorMessage(session,e.getMessage());
				}catch (IOException ee) {
				}
				System.err.println(e.getMessage());
				System.err.println("Code generatrion terminated unsuccessfully������");
			}
			catch (Exception e) {
				e.printStackTrace();
				try {
					sendErrorMessage(session, e.getMessage());
				} catch (Exception ee) {
				}
			}
			finally {
				try {
					session.close();
				}
				catch (IOException e) {
				}
			}
		}
	}
}
