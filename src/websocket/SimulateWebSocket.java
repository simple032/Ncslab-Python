package websocket;

import java.io.IOException;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;

import code.c.linux.pc.simulation.CodeModelCLinuxPCSimulation;
import ncslablink.ErrorMessage;
import ncslablink.ModelException;
import ncslablink.ModelMode;

@ServerEndpoint("/websocketsimulate") 

public class SimulateWebSocket {
	
	@OnOpen
	public void onOpen(Session session) {
		//System.out.println("WEBopen Experiment");
	}
	
	private void sendMessage(Session session,String msgString) throws IOException{
		JSONObject jb=new JSONObject();
		jb.put("msg", msgString);
		session.getBasicRemote().sendText(jb.toString());
	}
	
	private void sendResultMessage(Session session,CodeModelCLinuxPCSimulation modelC) throws IOException{
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
				String jsonDataString=mdlData.getString("jsonData");
				JSONObject jsonData=new JSONObject(jsonDataString);
				//System.out.println(jsonDataString);
				String errorMsgs="";
				
				sendMessage(session,"generating");
	        

	        	//����C���Ե�������CodeModelC
	        	//CodeModelCLinuxRaspberry modelC=CodeModelCLinuxRaspberry.createFromJSON(jsonIn,ModelMode.Compilation);
	        	CodeModelCLinuxPCSimulation modelC=CodeModelCLinuxPCSimulation.createFromJSON(jsonData,ModelMode.Simulation);
	        	//modelC.setSolver(Solver.ode4);

	        	modelC.generate();
	        	
	        	sendMessage(session,"generated");
	        	
	        	System.out.println();
	        	
	        	if(modelC.getErrorList().size()>0) {
	        		for(ErrorMessage em: modelC.getErrorList()) {
	        			errorMsgs += em.getMessage();
	        		}
	        		throw new ModelException(errorMsgs);
	        	}
	        	
	        	sendMessage(session,"compiling");
	        	
	        	if(modelC.makeExeFile()==false) {
	        		throw new ModelException("Can not make exe file!");
	        	}
	        	
	        	sendMessage(session,"compiled");
	        	
	        	//sendMessage(session,"simulating");
	        	sendSimulatingMessage(session,modelC.getConfig().getStopTime());
	        	
	        	modelC.simulate(session);
	        	
	        	sendMessage(session,"simulated");
	        	sendResultMessage(session,modelC);
	        }
			catch(IOException e) {
				System.err.println(e.getMessage());
	        	System.err.println("Code generatrion terminated unsuccessfully������");
			}
	        catch(ModelException e) {
	        	try {
	        		sendErrorMessage(session,e.getMessage());
	        	}
	        	catch(IOException ee) {
	        	}
	        	System.err.println(e.getMessage());
	        	System.err.println("Code generatrion terminated unsuccessfully������");
	        }
			catch(Exception e) {
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
