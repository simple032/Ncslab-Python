package com.ncslab.websocket;

import java.io.IOException;
import java.util.Optional;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.ncslab.code.CodeModelFactory;
import com.ncslab.code.c.CodeModelC;

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
		JSONObject jb = new JSONObject();
		jb.put("msg", msgString);
		session.getBasicRemote().sendText(jb.toString());
	}

	private void sendErrorMessage(Session session, String msgString) throws IOException {
		JSONObject jb = new JSONObject();
		jb.put("msg", "error");
		jb.put("error", msgString);
		session.getBasicRemote().sendText(jb.toString());
	}

	@OnMessage
	public void onMessage(Session session, String msgString) {
		 System.out.println(msgString);
		JSONObject msg = new JSONObject(msgString);
		String com = msg.getString("com");
        CodeModelC modelC = null;
		if (com.equals("start")) {
			try {
				sendMessage(session, "start");
				// System.out.println("Start");
				JSONObject mdlData = msg.getJSONObject("mdlData");
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
					session.close();
				} catch (IOException e) {
                    log.error("e: ", e);
				}
			}

		}
	}
}
