package com.ncslab.websocket;

import java.io.IOException;
import java.util.Objects;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.windows.simulation.CodeModelCWindowsSimulation;
import org.json.JSONObject;

import com.ncslab.code.c.linux.pc.simulation.CodeModelCLinuxPCSimulation;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;

@ServerEndpoint("/websocketsimulate")

public class SimulateWebSocket {

	@OnOpen
	public void onOpen(Session session) {
		System.out.println("WEBopen Experiment for Simulation");
		session.setMaxTextMessageBufferSize(1024*1024);
		session.setMaxBinaryMessageBufferSize(1024*1024);
	}

	private void sendMessage(Session session,String msgString) throws IOException{
		JSONObject jb=new JSONObject();
		jb.put("msg", msgString);
		session.getBasicRemote().sendText(jb.toString());
	}

	private void sendResultMessage(Session session, CodeModelC modelC) throws IOException{
		JSONObject jb=new JSONObject();
		jb.put("msg", "result");
		jb.put("resultsFile", "/CCode/"+modelC.getUserId()+"/"+modelC.getModelId()+"/results.json");
		session.getBasicRemote().sendText(jb.toString());
	}

	private void sendErrorMessage(Session session,String msgString) throws IOException{
		JSONObject jb=new JSONObject();
		jb.put("msg", "error");
		jb.put("error", msgString);
		session.getBasicRemote().sendText(jb.toString());
	}

	private void sendSimulatingMessage(Session session,double endTime) throws IOException{
		JSONObject jb=new JSONObject();
		jb.put("msg", "simulating");
		jb.put("time", 0);
		jb.put("timeLength",endTime);
		session.getBasicRemote().sendText(jb.toString());
	}

	@OnMessage
	public void onMessage(Session session,String msgString){
		System.out.println(msgString);



		JSONObject  msg= new JSONObject(msgString);
		String com=msg.getString("com");
		if(com.equals("start")) {
			try {
				sendMessage(session,"start");
				//System.out.println("Start");
				JSONObject  mdlData=msg.getJSONObject("mdlData");
                String target = mdlData.optString("target");
                String jsonDataString=mdlData.getString("jsonData");
				JSONObject jsonData=new JSONObject(jsonDataString);
				//System.out.println(jsonDataString);
				String errorMsgs="";

				sendMessage(session,"generating");


	        	// instantiate a CodeModelC object
	        	//CodeModelCLinuxRaspberry modelC=CodeModelCLinuxRaspberry.createFromJSON(jsonIn,ModelMode.Compilation);
                CodeModelC modelC = null;
                if(Objects.equals(target, "linux")){
                    modelC = CodeModelCLinuxPCSimulation.createFromJSON(jsonData, ModelMode.Simulation);
                }else if(Objects.equals(target, "linux-rpi")){
                    modelC= CodeModelCLinuxPCSimulation.createFromJSON(jsonData,ModelMode.Simulation);
                }else{
					modelC= CodeModelCWindowsSimulation.createFromJSON(jsonData,ModelMode.Simulation);
				}
				modelC.removeAllFiles();

	        	//modelC.setSolver(Solver.ode4);
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

                //


	        	sendMessage(session,"compiled");

				modelC.removeAllFiles();

	        	//sendMessage(session,"simulating");
	        	sendSimulatingMessage(session,modelC.getConfig().getStopTime());

	        	modelC.simulate(session);

	        	sendMessage(session,"simulated");
	        	sendResultMessage(session, modelC);
	        }
			catch(IOException e) {
				System.err.println(e.getMessage());
	        	System.err.println("Code generatrion terminated unsuccessfully");
			}
	        catch(ModelException e) {
	        	try {
	        		sendErrorMessage(session,e.getMessage());
	        	}
	        	catch(IOException ee) {
	        	}
	        	System.err.println(e.getMessage());
	        	System.err.println("Code generatrion terminated unsuccessfully");
	        }
			catch(Exception e) {
				e.printStackTrace();
				try {
	        		sendErrorMessage(session,e.getMessage());
	        	}
	        	catch(IOException ee) {
	        	}
			}
			finally {
				try {
					session.close();
				}
				catch(IOException e) {

				}
			}
		}
	}
}
