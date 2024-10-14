package com.ncslab.code.c.windows.simulation;

import com.google.common.io.LittleEndianDataInputStream;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;

import javax.websocket.Session;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class CodeModelCWindowsSimulation extends CodeModelC{

	private CodeStructCWindowsSimulation codeStructC = new CodeStructCWindowsSimulation(this);

	CodeModelCWindowsSimulation(JSONObject jsonIn, ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}

	@Override
	protected CodeStructC getCodeStructC() {
		return codeStructC;
	}

	public static CodeModelCWindowsSimulation createFromJSON(JSONObject jsonIn, ModelMode mode) throws ModelException {
        return new CodeModelCWindowsSimulation(jsonIn,mode);
	}

	private void sendSimulatingMessage(Session session, double time) throws IOException{
		JSONObject jb = new JSONObject();
		jb.put("msg", "simulating");
		jb.put("time", time);
		jb.put("timeLength", this.getConfig().getStopTime());
		session.getBasicRemote().sendText(jb.toString());
	}

	public void simulate(Session session) throws ModelException{
		Process process = null;
		System.out.println("Executing simulation codes...");
		try {
			// run the executable ncslab file

            File dir = new File(codeStructC.getCodePath());
            File exeFile = new File(dir, "ncslab.exe");
            System.out.println("The executing file is on " + exeFile);

			process = Runtime.getRuntime().exec(
				exeFile.getAbsolutePath() + " " + this.getConfig().getStopTime(),
				null,dir
				);

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

			process.waitFor();
		}
		catch(InterruptedException|IOException e) {
            e.printStackTrace();
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
