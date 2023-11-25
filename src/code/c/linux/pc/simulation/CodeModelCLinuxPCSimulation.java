package code.c.linux.pc.simulation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.websocket.Session;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.DataInputStream;

import org.apache.parquet.bytes.LittleEndianDataInputStream;
import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import code.CodeGenerationOption;
import code.CodeModel;
import line.Line;
import ncslablink.ErrorMessage;
import ncslablink.ModelException;
import ncslablink.ModelMode;
import main.database.Algorithms;
import code.c.CodeModelC;
import code.c.CodeStructC;

public class CodeModelCLinuxPCSimulation extends CodeModelC{
	
	private CodeStructCLinuxPCSimulation codeRaspberry = new CodeStructCLinuxPCSimulation(this);
	
	CodeModelCLinuxPCSimulation(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	protected CodeStructC getCodeStructC() {
		return codeRaspberry;
	}
	
	public static CodeModelCLinuxPCSimulation createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCLinuxPCSimulation model=new CodeModelCLinuxPCSimulation(jsonIn,mode);
		
		return model;
	}
	
	private void sendSimulatingMessage(Session session,double time) throws IOException{
		JSONObject jb=new JSONObject();
		jb.put("msg", "simulating");
		jb.put("time", time);
		jb.put("timeLength",this.getConfig().getStopTime());
		session.getBasicRemote().sendText(jb.toString());
	}
	
	public void simulate(Session session) throws ModelException{
		Process process=null;
		System.out.println("Executing simulation codes...");
		try {
			process=Runtime.getRuntime().exec("./ncslab "+this.getConfig().getStopTime(),null, new File(codeRaspberry.getCodePath()));
			LittleEndianDataInputStream out=new LittleEndianDataInputStream(process.getInputStream());
			//LittleEndianInputStream out=new LittleEndianInputStream(process.getInputStream());
			
			long currentTime=new java.util.Date().getTime();
			
			while(true) {
				double time=out.readDouble();
				if(time<0) {
					break;
				}
				
				sendSimulatingMessage(session,time);
				
				//if((new java.util.Date().getTime())-currentTime>1000) {
				//	currentTime=new java.util.Date().getTime();
				//	sendSimulatingMessage(session,time);
				//}
				
				//System.out.println(time);
			}
			
			out.close();
			
			try {
				process.waitFor();
			}
			catch(InterruptedException e) {
				throw new ModelException("Can not execute the exe file!");
			}
		}
		catch(IOException e) {
			throw new ModelException("Can not execute the exe file!");
		}
		finally {
			if(process!=null) {
				process.destroy();
			}
		}
		
		System.out.println("Simulation codes executed successfully!");
		
	}
}
