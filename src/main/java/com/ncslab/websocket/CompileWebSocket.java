package com.ncslab.websocket;

import java.io.IOException;
import java.util.Optional;

import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import com.ncslab.code.CodeModelFactory;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.communication.WebSocketMessageDto;
import com.ncslab.util.JsonUtils;
import com.utils.Property;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import com.ncslab.code.Solver;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;

@Slf4j
@ServerEndpoint("/websocketcompile")

public class CompileWebSocket {
	@OnOpen
	public void onOpen(Session session) {
		System.out.println("WEBopen Experiment");
		session.setMaxTextMessageBufferSize(1024 * 1024);
		session.setMaxBinaryMessageBufferSize(1024 * 1024);
	}

	private void sendMessage(Session session, String msgString) throws IOException {
		WebSocketMessageDto message = WebSocketMessageDto.createStatusMessage(msgString, null);
		if(session != null)
			session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(message));
		else
			System.out.println("Send message: " + msgString);
	}

	private void sendErrorMessage(Session session, String msgString) throws IOException {
		WebSocketMessageDto message = WebSocketMessageDto.createErrorMessage(msgString);
		if(session != null)
			session.getBasicRemote().sendText(JsonUtils.serializeWebSocketMessage(message));
		else
			System.out.println("Send error message: " + msgString);
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
				JSONObject mdlData;
				if (wsMessage != null && wsMessage.getMdlData() != null) {
					// Use DTO approach - convert Map back to JSONObject for compatibility
					mdlData = new JSONObject(wsMessage.getMdlData());
				} else {
					// Use legacy approach
					mdlData = msg.getJSONObject("mdlData");
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
                    throw new UnsupportedOperationException("Unsupported OS: " + osName);
                }

                String target = mdlData.optString("target", "PC");

                JSONObject plantInfo = mdlData.getJSONObject("plantInfo");
                String matlabExt = plantInfo.getString("matlabExt");
                if(!"".equals(matlabExt)){
                    if(matlabExt.toLowerCase().contains("pi")){
                        target = "Raspberry";
                    }
                }

                System.out.println("The target is:" + target);

				String jsonDataString = mdlData.getString("jsonData");
				JSONObject jsonData = new JSONObject(jsonDataString);
				// System.out.println(jsonDataString);
				String errorMsgs = "";

				sendMessage(session, "generating");

                // 使用反射构建
                modelC = CodeModelFactory.createModel(host, target, jsonData, ModelMode.Compilation);

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
                if(modelC != null)
                    modelC.postBuild();
				try {
					if(session != null)
						session.close();
				} catch (IOException e) {
                    log.error("e: ", e);
				}
			}

		}
	}
}
