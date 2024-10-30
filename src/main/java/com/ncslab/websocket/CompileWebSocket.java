package com.ncslab.websocket;

import java.io.IOException;
import java.util.Objects;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.linux.pc.CodeModelCLinuxPC;
import com.ncslab.code.c.windows.CodeModelCWindows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import com.ncslab.code.Solver;
import com.ncslab.code.c.linux.pc.simulation.CodeModelCLinuxPCSimulation;
import com.ncslab.code.c.linux.raspberry.CodeModelCLinuxRaspberry;
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
		// System.out.println(msgString);
		JSONObject msg = new JSONObject(msgString);
		String com = msg.getString("com");
		if (com.equals("start")) {
			try {
				sendMessage(session, "start");
				// System.out.println("Start");
                String target = msg.getString("target");
				JSONObject mdlData = msg.getJSONObject("mdlData");
				String jsonDataString = mdlData.getString("jsonData");
				JSONObject jsonData = new JSONObject(jsonDataString);
				// System.out.println(jsonDataString);
				String errorMsgs = "";

				sendMessage(session, "generating");

                CodeModelC modelC = null;
//				CodeModelCLinuxRaspberry modelC = CodeModelCLinuxRaspberry.createFromJSON(jsonData,
//						ModelMode.Compilation);
                if(Objects.equals(target, "linux")){
                    modelC = CodeModelCLinuxPC.createFromJSON(jsonData, ModelMode.Compilation);
                }else{
                    modelC = CodeModelCLinuxRaspberry.createFromJSON(jsonData, ModelMode.Compilation);
                }


				// CodeModelCLinuxPC
				// modelC=CodeModelCLinuxPC.createFromJSON(jsonIn,ModelMode.Compilation);

				modelC.setSolver(Solver.ode4);

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

                modelC.removeAllFiles();
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

			} catch (IOException|ModelException e) {
				try {
					sendErrorMessage(session, e.getMessage());
				} catch (IOException ee) {
                    log.error("e: ", ee);
				}
				System.err.println(e.getMessage());
				System.err.println("Code generatrion terminated unsuccessfully");
			} finally {
				try {
					session.close();
				} catch (IOException e) {
                    log.error("e: ", e);
				}
			}

		}
	}
}
