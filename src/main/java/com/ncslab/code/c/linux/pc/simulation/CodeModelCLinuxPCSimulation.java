package com.ncslab.code.c.linux.pc.simulation;

import javax.websocket.Session;

import java.io.File;
import java.io.IOException;
// import org.apache.parquet.bytes.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataInputStream;
import org.json.JSONObject;

import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;

public class CodeModelCLinuxPCSimulation extends CodeModelC{

	private CodeStructCLinuxPCSimulation codeRaspberry = new CodeStructCLinuxPCSimulation(this);

	CodeModelCLinuxPCSimulation(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}

	@Override
	protected CodeStructC getCodeStructC() {
		return codeRaspberry;
	}

	public static CodeModelCLinuxPCSimulation createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCLinuxPCSimulation model = new CodeModelCLinuxPCSimulation(jsonIn,mode);
		return model;
	}

	private void sendSimulatingMessage(Session session, double time) throws IOException{
		JSONObject jb = new JSONObject();
		jb.put("msg", "simulating");
		jb.put("time", time);
		jb.put("timeLength", this.getConfig().getStopTime());
		session.getBasicRemote().sendText(jb.toString());
	}

	public void simulate(Session session) throws ModelException {
		Process process = null;
		System.out.println("Executing simulation codes...");
		try {
			// run the executable ncslab file
			process = Runtime.getRuntime().exec(
				"./ncslab " + this.getConfig().getStopTime(),
				null,
				new File(codeRaspberry.getCodePath()));

			// read primitive Java data types from an underlying InputStream in a little-endian format
			// This input stream is the stdout of the process
			LittleEndianDataInputStream out = new LittleEndianDataInputStream(process.getInputStream());

			long currentTime = new java.util.Date().getTime();

			while(true) {
				double time = out.readDouble();

				if (time < 0) {
					break;
				}

				// send the simulation data to the client
				sendSimulatingMessage(session, time);

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

			process = Runtime.getRuntime().exec(
				"sudo chown -R " + user + ":" + group + " " + codeRaspberry.getCodePath(),
				null,
				new File(codeRaspberry.getCodePath()));

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
