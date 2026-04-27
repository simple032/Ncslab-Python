package com.ncslab.code.c.linux.pc.simulation;

import jakarta.websocket.Session;

import java.io.File;
import java.io.IOException;
// import org.apache.parquet.bytes.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataInputStream;
import org.json.JSONObject;
import com.ncslab.dto.core.ModelDto;

import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;

import java.net.ServerSocket;
import java.net.Socket;

public class CodeModelCLinuxPCSimulation extends CodeModelC{

	private CodeStructCLinuxPCSimulation codeRaspberry = new CodeStructCLinuxPCSimulation(this);

	// 原有JSONObject构造函数
	CodeModelCLinuxPCSimulation(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	// 新增ModelDto DTO构造函数
	CodeModelCLinuxPCSimulation(ModelDto modelDto, ModelMode mode) throws ModelException{
		super(modelDto, mode);
	}

	@Override
	protected CodeStructC getCodeStructC() {
		return codeRaspberry;
	}

	// 原有JSONObject工厂方法  
	public static CodeModelCLinuxPCSimulation createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCLinuxPCSimulation model = new CodeModelCLinuxPCSimulation(jsonIn,mode);
		return model;
	}

	
	// 新增ModelDto DTO工厂方法
	public static CodeModelCLinuxPCSimulation createFromDto(ModelDto modelDto, ModelMode mode) throws ModelException {
		CodeModelCLinuxPCSimulation model = new CodeModelCLinuxPCSimulation(modelDto, mode);
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
		ServerSocket srvSocket = null;
		Socket socket = null;
		LittleEndianDataInputStream out = null;
		try {
			// run the executable ncslab file
			// Create a ServerSocket to accept TCP connection from the C++ process
			srvSocket = new ServerSocket(0);
			srvSocket.setSoTimeout(30000);
			int port = srvSocket.getLocalPort();
			System.out.println("[CodeModelCLinuxPCSimulation] Waiting for C++ simulation to connect on TCP port " + port);

			// Use ProcessBuilder instead of deprecated Runtime.exec()
			ProcessBuilder processBuilder = new ProcessBuilder(
				"./ncslab",
				String.valueOf(this.getConfig().getStopTime()),
				String.valueOf(port)
			);
			processBuilder.directory(new File(codeRaspberry.getCodePath()));
			process = processBuilder.start();

			// Accept TCP connection from the C++ process
			socket = srvSocket.accept();
			System.out.println("[CodeModelCLinuxPCSimulation] C++ simulation connected via TCP");

			// read primitive Java data types from an underlying InputStream in a little-endian format
			// This input stream is the TCP socket from the process
			out = new LittleEndianDataInputStream(socket.getInputStream());

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

			// Use ProcessBuilder instead of deprecated Runtime.exec()
			ProcessBuilder chownBuilder = new ProcessBuilder(
				"sudo", "chown", "-R",
				user + ":" + group,
				codeRaspberry.getCodePath()
			);
			chownBuilder.directory(new File(codeRaspberry.getCodePath()));
			process = chownBuilder.start();

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
			if (out != null) {
				try { out.close(); } catch (IOException ignored) {}
			}
			if (socket != null) {
				try { socket.close(); } catch (IOException ignored) {}
			}
			if (srvSocket != null) {
				try { srvSocket.close(); } catch (IOException ignored) {}
			}
			if(process!=null) {
				process.destroy();
			}
		}

		System.out.println("Simulation codes executed successfully!");

	}
}
