package com.ncslab.websocket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;

import com.ncslab.code.c.SFcnCompileModelC;
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
		session.setMaxTextMessageBufferSize(1024*1024);
		session.setMaxBinaryMessageBufferSize(1024*1024);
	}
	
	private void sendMessage(Session session,String msgString) throws IOException{
		JSONObject jb=new JSONObject();
		jb.put("msg", msgString);
		session.getBasicRemote().sendText(jb.toString());
	}
	
	private void sendErrorMessage(Session session,String msgString) throws IOException{
		JSONObject jb=new JSONObject();
		jb.put("msg", "error");
		jb.put("error", msgString);
		session.getBasicRemote().sendText(jb.toString());
	}
	
	private void sendResultMessage(Session session,String msgString) throws IOException{
		JSONObject jb=new JSONObject();
		jb.put("msg", "result");
		jb.put("result", msgString);
		session.getBasicRemote().sendText(jb.toString());
	}
	
	@OnMessage
	public void onMessage(Session session,String msgString) {

		System.out.println(msgString);
		JSONObject msg = new JSONObject(msgString);
		String com = msg.getString("com");
		if(com.equals("sfcncompile")) {
			try {
				sendMessage(session, "start");
				JSONObject jsonData=msg.getJSONObject("jsonData");
				
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
