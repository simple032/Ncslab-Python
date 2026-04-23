package com.ncslab.code.c.windows.simulation;

import com.google.common.io.LittleEndianDataInputStream;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.utils.Property;
import org.json.JSONObject;

import jakarta.websocket.Session;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class CodeModelCWindowsSimulation extends CodeModelC{

	private CodeStructCWindowsSimulation codeStructC = new CodeStructCWindowsSimulation(this);
	private volatile Process currentProcess;
	private volatile boolean stoppedByUser = false;

	// 原有JSONObject构造函数
	CodeModelCWindowsSimulation(JSONObject jsonIn, ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	// DTO构造函数 - 现代化直接DTO支持
	CodeModelCWindowsSimulation(ModelDto modelDto, ModelMode mode) throws ModelException{
		super(modelDto, mode);
	}

	@Override
	protected CodeStructC getCodeStructC() {
		return codeStructC;
	}

	@Override
	public void stop() {
		this.stoppedByUser = true;
		if (currentProcess != null) {
			currentProcess.destroyForcibly();
			currentProcess = null;
		}
	}

	// 原有JSONObject工厂方法
	public static CodeModelCWindowsSimulation createFromJSON(JSONObject jsonIn, ModelMode mode) throws ModelException {
        return new CodeModelCWindowsSimulation(jsonIn,mode);
	}
	
	// DTO工厂方法 - 现代化直接DTO支持
	public static CodeModelCWindowsSimulation createFromDto(ModelDto modelDto, ModelMode mode) throws ModelException {
        return new CodeModelCWindowsSimulation(modelDto, mode);
	}

	private void sendSimulatingMessage(Session session, double time) throws IOException{
		if (session == null || !session.isOpen()) {
			return;
		}
		JSONObject jb = new JSONObject();
		jb.put("msg", "simulating");
		jb.put("time", time);
		jb.put("timeLength", this.getConfig().getStopTime());
		session.getBasicRemote().sendText(jb.toString());
	}

    public void copyLibraryFiles(){

    }
    
    private void sendSavingMessage(Session session,int currentTerminal,int terminalNum) throws IOException{
		JSONObject jb=new JSONObject();
		jb.put("msg", "saving");
		jb.put("currentTerminal", currentTerminal);
		jb.put("terminalNum",terminalNum);
		session.getBasicRemote().sendText(jb.toString());
	}

	/**
	 * Send real-time Display block data update via WebSocket
	 * @param session WebSocket session
	 * @param displayData Map of display UUID → current value
	 * @throws IOException if sending fails
	 */
	private void sendDisplayUpdateMessage(Session session, Map<String, Double> displayData) throws IOException {
		if (session == null || displayData == null || displayData.isEmpty()) return;
		JSONObject jb = new JSONObject();
		jb.put("msg", "display_update");
		jb.put("timestamp", System.currentTimeMillis());
		JSONObject dataObj = new JSONObject();
		for (Map.Entry<String, Double> entry : displayData.entrySet()) {
			dataObj.put(entry.getKey(), entry.getValue());
		}
		jb.put("displayData", dataObj);
		session.getBasicRemote().sendText(jb.toString());
	}

	/**
	 * Send Stateflow state change update via WebSocket
	 * @param session WebSocket session
	 * @param chartUUID Chart block UUID (frontend cell id)
	 * @param stateId Target state ID (frontend state node id)
	 * @param stateName Target state name for display
	 * @throws IOException if sending fails
	 */
	private void sendStateflowStateUpdateMessage(Session session, String chartUUID, String stateId, String stateName) throws IOException {
		if (session == null || chartUUID == null || stateId == null) return;
		JSONObject jb = new JSONObject();
		jb.put("msg", "stateflow_state_update");
		jb.put("timestamp", System.currentTimeMillis());
		jb.put("chartUUID", chartUUID);
		jb.put("stateId", stateId);
		jb.put("stateName", stateName != null ? stateName : "");
		session.getBasicRemote().sendText(jb.toString());
	}
    
    private double readDouble(LittleEndianDataInputStream out)  throws IOException{
		byte c;
		String valueString="";
		while((c=out.readByte())!='\n') {
			valueString+=(char)c;
		}
		//System.out.println(valueString);
		return Double.parseDouble(valueString);
	}

    
    
	public void simulate(Session session) throws ModelException {
		Process process = null;
		System.out.println("Executing simulation codes...");
		try {
			// run the executable ncslab file

            File dir = new File(codeStructC.getCodePath());
            File exeFile = new File(dir, "ncslab.exe");
            System.out.println("The executing file is on " + exeFile);

            String exeFilePath = exeFile.getAbsolutePath();

            // 获取当前环境变量
//            Map<String, String> currentEnv = System.getenv();
//
//            // 创建一个新的环境变量映射
//            Map<String, String> env = new HashMap<>(currentEnv);
//
//            // 添加或修改环境变量
//            env.put("Path", "%M2PLAB_ROOT%/server/cruntime/bin;" + env.get("Path")); // 例如，添加新的路径
//
//            // 将环境变量映射转换为字符串数组
//            String[] envArray = new String[env.size()];
//            int i = 0;
//            for (Map.Entry<String, String> entry : env.entrySet()) {
//                envArray[i++] = entry.getKey() + "=" + entry.getValue();
//            }
//            String dllFolderName = Optional.ofNullable(Property.instance.getProperty("DllFolder"))
//                .orElse(codeStructC.getM2plabRoot()+"/server/cruntime/bin");
//
//            File dllFolder = new File(dllFolderName);
//            if(!dllFolder.exists()){
//                dllFolder = new File(codeStructC.getM2plabRoot()+"/server/cruntime/bin");
//            }
//            File[] files = dllFolder.listFiles();
//            if(files != null){
//                for(File file : files){
//                    if(file.isFile() && file.getName().endsWith(".dll")){
//                        try{
//                            Path sourcePath = Paths.get(file.getAbsolutePath());
//                            Path targetPath = Paths.get(dir.getAbsolutePath(), file.getName());
//                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
//                        }catch(IOException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }

            // Use ProcessBuilder instead of deprecated Runtime.exec()
            ProcessBuilder processBuilder = new ProcessBuilder(
                exeFilePath,
                String.valueOf(this.getConfig().getStopTime())
            );
            processBuilder.directory(dir);
            process = processBuilder.start();
            this.currentProcess = process;

			// read primitive Java data types from an underlying InputStream in a little-endian format
			// This input stream is the stdout of the process
			LittleEndianDataInputStream out = new LittleEndianDataInputStream(process.getInputStream());

			long currentTime = new java.util.Date().getTime();

			while(true) {
				// Check if simulation should be stopped (session closed or thread interrupted)
				if (session != null && !session.isOpen()) {
					System.out.println("[CodeModelCWindowsSimulation] Session closed, stopping simulation");
					return;
				}
				if (Thread.interrupted()) {
					System.out.println("[CodeModelCWindowsSimulation] Thread interrupted, stopping simulation");
					return;
				}
				int pre1=0,pre2=0;
				do {
					pre1=pre2;
					pre2=out.readByte();
					if(pre1==0x55&&pre2==0x55) {
						break;
					}
				}
				while(true);
				int cmd=out.readInt();
				//System.out.println(cmd);
				if(cmd==-1) {
					break;
				}
				switch(cmd) {
				case 1:
					double time=readDouble(out);//out.readDouble();
					sendSimulatingMessage(session,time);
					break;
				case 2:
					int currentTerminal=out.readInt();
				
					int terminalNum=out.readInt();
					sendSavingMessage(session,currentTerminal,terminalNum);
					break;
				case 3: // DisplayUpdate - 实时 Display 模块数据
					int displayCount = out.readInt();
					Map<String, Double> displayData = new HashMap<>();
					for (int i = 0; i < displayCount; i++) {
						int uuidLen = out.readInt();
						byte[] uuidBytes = new byte[uuidLen];
						out.readFully(uuidBytes);
						String uuid = new String(uuidBytes, StandardCharsets.UTF_8);
						double displayValue = out.readDouble();
						displayData.put(uuid, displayValue);
					}
					if (!displayData.isEmpty()) {
						sendDisplayUpdateMessage(session, displayData);
					}
					break;
				case 4: // StateflowStateUpdate - 状态机状态变化
					int chartUuidLen = out.readInt();
					byte[] chartUuidBytes = new byte[chartUuidLen];
					out.readFully(chartUuidBytes);
					String chartUUID = new String(chartUuidBytes, StandardCharsets.UTF_8);
					
					int stateIdLen = out.readInt();
					byte[] stateIdBytes = new byte[stateIdLen];
					out.readFully(stateIdBytes);
					String stateId = new String(stateIdBytes, StandardCharsets.UTF_8);
					
					int stateNameLen = out.readInt();
					byte[] stateNameBytes = new byte[stateNameLen];
					out.readFully(stateNameBytes);
					String stateName = new String(stateNameBytes, StandardCharsets.UTF_8);
					
					sendStateflowStateUpdateMessage(session, chartUUID, stateId, stateName);
					break;
				}
				
				//if((new java.util.Date().getTime())-currentTime>1000) {
				//	currentTime=new java.util.Date().getTime();
					//sendSimulatingMessage(session,time);
				//}
				
				//System.out.println(time);
			}
			
			out.close();
			process.waitFor();
			
		}
		catch(InterruptedException|IOException e) {
			if (stoppedByUser) {
				System.out.println("[CodeModelCWindowsSimulation] Simulation stopped by user, exiting cleanly");
				return;
			}
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
