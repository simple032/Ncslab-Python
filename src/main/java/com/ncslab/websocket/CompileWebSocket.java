package com.ncslab.websocket;

import java.io.IOException;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;

import com.ncslab.code.Solver;
import com.ncslab.code.c.linux.pc.simulation.CodeModelCLinuxPCSimulation;
import com.ncslab.code.c.linux.raspberry.CodeModelCLinuxRaspberry;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;

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
				JSONObject mdlData = msg.getJSONObject("mdlData");
				String jsonDataString = mdlData.getString("jsonData");
				JSONObject jsonData = new JSONObject(jsonDataString);
				// System.out.println(jsonDataString);
				String errorMsgs = "";

				sendMessage(session, "generating");

				CodeModelCLinuxRaspberry modelC = CodeModelCLinuxRaspberry.createFromJSON(jsonData,
						ModelMode.Compilation);
				// CodeModelCLinuxPC
				// modelC=CodeModelCLinuxPC.createFromJSON(jsonIn,ModelMode.Compilation);

				modelC.setSolver(Solver.ode4);

				modelC.generate();

				sendMessage(session, "generated");

				if (modelC.getErrorList().size() > 0) {
					for (ErrorMessage em : modelC.getErrorList()) {
						errorMsgs += em.getMessage();
					}
					throw new ModelException(errorMsgs);
				}

				sendMessage(session, "compiling");

				if (modelC.makeExeFile() == false) {
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

			} catch (IOException e) {
				System.err.println(e.getMessage());
				System.err.println("Code generatrion terminated unsuccessfully");
			}

			catch (ModelException e) {
				try {
					sendErrorMessage(session, e.getMessage());
				} catch (IOException ee) {
				}
				System.err.println(e.getMessage());
				System.err.println("Code generatrion terminated unsuccessfully");
			} catch (Exception e) {
				try {
					sendErrorMessage(session, e.getMessage());
				} catch (IOException ee) {
				}
			} finally {
				try {
					session.close();
				} catch (IOException e) {

				}
			}

		}
	}
}
