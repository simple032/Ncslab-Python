package server;

import java.net.*;
import org.json.JSONObject;
import org.json.JSONArray;

import code.m.CodeModelM;

import java.io.*;

public class SimulationThread extends Thread {
	
	private Socket socket;
	private SimulationServer server;
	
	private DataInputStream in;
	private DataOutputStream out;
	
	private Object waitObject=new Object();
	private Object finishObject=new Object();
	
	private boolean isBusy=true;
	
	private CodeModelM model=null;
	
	public SimulationThread(Socket socket,SimulationServer server){
		this.socket=socket;
		this.server=server;
	}
	
	public boolean getIsBusy() {
		return isBusy;
	}
	
	public void setIsBusy(boolean isBusy) {
		this.isBusy=isBusy;
	}
	
	public void startSimulation(CodeModelM model) {
		this.model=model;
		synchronized(waitObject) {
			waitObject.notify();
		}
		
		try {
			synchronized(finishObject) {
				finishObject.wait();
			}
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public String getResult() {
		JSONObject jb=new JSONObject();
		jb.put("code", 2000);
		jb.put("message", "SUCCESS");
		JSONObject data=new JSONObject();
		data.put("figureFileUrl", "scope");
		jb.put("data", data);
		return jb.toString();
	}
	
	public void run() {
		try {
			//socket.setKeepAlive(true);
			in=new DataInputStream(socket.getInputStream());
			out=new DataOutputStream(socket.getOutputStream());
			
			while(true) {
				isBusy=false;
				synchronized(waitObject) {
					waitObject.wait();
				}
				
				isBusy=true;
				
				String codePath=model.getCodePath();
				byte data[]=codePath.getBytes();
				out.writeInt(data.length);
				out.write(data);
				
				String modelSeq=""+model.getModelSeq();
				data=modelSeq.getBytes();
				out.writeInt(data.length);
				out.write(data);
				
				System.out.println("Executing...");
				
				in.readByte();
				System.out.println("Done...");
				
				synchronized(finishObject) {
					finishObject.notify();
				}
			}		
			
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			server.removeSimulationThread(this);
		}
	}
}
