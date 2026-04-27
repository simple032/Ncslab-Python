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
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class CodeModelCWindowsSimulation extends CodeModelC{

	private CodeStructCWindowsSimulation codeStructC = new CodeStructCWindowsSimulation(this);
	private volatile Process currentProcess;
	private volatile boolean stoppedByUser = false;
	private volatile Socket simSocket;
	private volatile ServerSocket serverSocket;

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

	public void setCodeStructC(CodeStructCWindowsSimulation codeStructC) {
		this.codeStructC = codeStructC;
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

	/**
	 * Send real-time Display + Scope data update via WebSocket
	 * @param session WebSocket session
	 * @param displayData Map of display UUID -> current value
	 * @param scopeDataList List of scope data maps
	 * @param currentTime Current simulation time
	 * @throws IOException if sending fails
	 */
	private void sendRealtimeDataUpdateMessage(Session session, Map<String, Double> displayData,
													 List<Map<String, Object>> scopeDataList,
													 double currentTime) throws IOException {
		if (session == null || !session.isOpen()) return;
		JSONObject jb = new JSONObject();
		jb.put("msg", "realtime_data_update");
		jb.put("timestamp", System.currentTimeMillis());
		jb.put("currentTime", currentTime);
		JSONObject dataObj = new JSONObject();
		for (Map.Entry<String, Double> entry : displayData.entrySet()) {
			dataObj.put(entry.getKey(), entry.getValue());
		}
		jb.put("displayData", dataObj);
		org.json.JSONArray scopeArray = new org.json.JSONArray();
		for (Map<String, Object> scope : scopeDataList) {
			JSONObject scopeObj = new JSONObject();
			scopeObj.put("uuid", scope.get("uuid"));
			scopeObj.put("width", scope.get("width"));
			scopeObj.put("height", scope.get("height"));
			org.json.JSONArray timeArr = new org.json.JSONArray();
			org.json.JSONArray dataArr = new org.json.JSONArray();
			for (Double t : (List<Double>) scope.get("time")) {
				timeArr.put(t);
			}
			for (Double d : (List<Double>) scope.get("data")) {
				dataArr.put(d);
			}
			scopeObj.put("time", timeArr);
			scopeObj.put("data", dataArr);
			scopeArray.put(scopeObj);
		}
		jb.put("scopeData", scopeArray);
		session.getBasicRemote().sendText(jb.toString());
	}

	/**
	 * Calculate the maximum number of scope data points to upload per batch.
	 * Based on: maxScopePoints * uploadInterval / stopTime
	 * @return max points per upload (at least 1)
	 */
	private int calculateMaxPointsPerUpload() {
		int maxScopePoints = 1000;      // 趋势图最大点数，可改为从配置读取
		double uploadInterval = 1.0;    // 上传更新间距(秒)，根据 C++ 调用频率调整
		double stopTime = this.getConfig().getStopTime();
		if (stopTime > 0) {
			return Math.max(1, (int) (maxScopePoints * uploadInterval / stopTime));
		} else {
			// 无限实时运行的默认值
			return 100;
		}
	}

	/**
	 * Downsample scope data using min-max bucketing to preserve peaks and step edges.
	 * Each bucket retains: first point, min point, max point, last point (de-duplicated).
	 * This prevents step signals from appearing as gradual slopes after downsampling.
	 *
	 * @param scopeDataList list of scope data maps
	 * @param maxPoints maximum points to keep per scope
	 * @return downsampled list
	 */
	private List<Map<String, Object>> downsampleScopes(List<Map<String, Object>> scopeDataList, int maxPoints) {
		if (scopeDataList == null || scopeDataList.isEmpty() || maxPoints <= 0) {
			return scopeDataList;
		}
		for (Map<String, Object> scope : scopeDataList) {
			List<Double> timeList = (List<Double>) scope.get("time");
			List<Double> dataList = (List<Double>) scope.get("data");
			if (timeList == null || dataList == null || timeList.size() <= maxPoints) {
				continue;
			}

			int width = (Integer) scope.get("width");
			int height = (Integer) scope.get("height");
			int valuesPerPoint = width * height;
			int originalSize = timeList.size();

			// Bucket size: aim for ~maxPoints total after min-max expansion
			int targetBuckets = Math.max(1, maxPoints / 4);
			int bucketSize = Math.max(2, originalSize / targetBuckets);

			List<Double> dsTime = new ArrayList<>();
			List<Double> dsData = new ArrayList<>();

			for (int i = 0; i < originalSize; ) {
				int end = Math.min(i + bucketSize, originalSize);

				// Always keep first point of bucket
				addDownsamplePoint(dsTime, dsData, timeList, dataList, i, valuesPerPoint);

				// For scalar data, also keep min and max inside the bucket
				if (end - i > 2 && valuesPerPoint == 1) {
					int minIdx = i;
					int maxIdx = i;
					double minVal = dataList.get(i);
					double maxVal = dataList.get(i);
					for (int j = i + 1; j < end; j++) {
						double val = dataList.get(j);
						if (val < minVal) { minVal = val; minIdx = j; }
						if (val > maxVal) { maxVal = val; maxIdx = j; }
					}
					if (minIdx != i) {
						addDownsamplePoint(dsTime, dsData, timeList, dataList, minIdx, valuesPerPoint);
					}
					if (maxIdx != i && maxIdx != minIdx) {
						addDownsamplePoint(dsTime, dsData, timeList, dataList, maxIdx, valuesPerPoint);
					}
				}

				// Always keep last point of bucket
				int lastIdx = end - 1;
				if (lastIdx != i) {
					addDownsamplePoint(dsTime, dsData, timeList, dataList, lastIdx, valuesPerPoint);
				}

				i = end;
			}

			// Fallback: if still over limit, uniform decimation on the pre-selected key points
			if (dsTime.size() > maxPoints) {
				int step = Math.max(1, dsTime.size() / maxPoints);
				List<Double> finalTime = new ArrayList<>(maxPoints);
				List<Double> finalData = new ArrayList<>(maxPoints * valuesPerPoint);
				for (int i = 0; i < dsTime.size() && finalTime.size() < maxPoints; i += step) {
					addDownsamplePoint(finalTime, finalData, dsTime, dsData, i, valuesPerPoint);
				}
				// Ensure last (newest) point is preserved
				double lastTime = dsTime.get(dsTime.size() - 1);
				if (finalTime.isEmpty() || !finalTime.get(finalTime.size() - 1).equals(lastTime)) {
					if (finalTime.size() >= maxPoints) {
						finalTime.set(finalTime.size() - 1, lastTime);
						int lastDsIdx = finalTime.size() - 1;
						int lastSrcOffset = (dsTime.size() - 1) * valuesPerPoint;
						for (int v = 0; v < valuesPerPoint; v++) {
							finalData.set(lastDsIdx * valuesPerPoint + v, dsData.get(lastSrcOffset + v));
						}
					} else {
						addDownsamplePoint(finalTime, finalData, dsTime, dsData, dsTime.size() - 1, valuesPerPoint);
					}
				}
				dsTime = finalTime;
				dsData = finalData;
			}

			scope.put("time", dsTime);
			scope.put("data", dsData);
		}
		return scopeDataList;
	}

	private void addDownsamplePoint(List<Double> dsTime, List<Double> dsData,
									List<Double> timeList, List<Double> dataList,
									int idx, int valuesPerPoint) {
		dsTime.add(timeList.get(idx));
		int offset = idx * valuesPerPoint;
		for (int v = 0; v < valuesPerPoint; v++) {
			dsData.add(dataList.get(offset + v));
		}
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

	/**
	 * Validate that a length field read from the stream is within reasonable bounds.
	 * Prevents NegativeArraySizeException and OOM from corrupted/misaligned data.
	 */
	private boolean isValidLength(int len, int maxAllowed) {
		return len >= 0 && len <= maxAllowed;
	}

    
    
	/**
	 * Send parameter updates to the C++ simulation process.
	 * TCP mode: sends length-prefixed JSON via the active TCP socket.
	 * Fallback mode: writes param_updates.json file.
	 * Called when the frontend sends parameter changes during real-time simulation.
	 */
	public void updateParameters(java.util.Map<String, Object> paramUpdates) throws IOException {
		if (paramUpdates == null || paramUpdates.isEmpty()) return;

		JSONObject json = new JSONObject();
		org.json.JSONArray updatesArray = new org.json.JSONArray();
		for (java.util.Map.Entry<String, Object> entry : paramUpdates.entrySet()) {
			@SuppressWarnings("unchecked")
			java.util.Map<String, Object> update = (java.util.Map<String, Object>) entry.getValue();
			JSONObject updateObj = new JSONObject();
			updateObj.put("blockPath", update.get("blockPath"));
			updateObj.put("blockName", update.get("blockName"));
			updateObj.put("paramName", update.get("paramName"));
			updateObj.put("value", update.get("value"));
			updatesArray.put(updateObj);
		}
		json.put("updates", updatesArray);

		// TCP mode: send via socket
		Socket socket = this.simSocket;
		if (socket != null && socket.isConnected() && !socket.isClosed()) {
			byte[] jsonBytes = json.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
			java.io.OutputStream os = socket.getOutputStream();
			// Send 4-byte little-endian length prefix
			os.write(jsonBytes.length & 0xFF);
			os.write((jsonBytes.length >> 8) & 0xFF);
			os.write((jsonBytes.length >> 16) & 0xFF);
			os.write((jsonBytes.length >> 24) & 0xFF);
			os.write(jsonBytes);
			os.flush();
			return;
		}

		// Fallback: write to file for non-TCP mode
		File dir = new File(codeStructC.getCodePath());
		File updateFile = new File(dir, "param_updates.json");
		try (java.io.FileWriter writer = new java.io.FileWriter(updateFile)) {
			writer.write(json.toString());
		}
	}

	public void simulate(Session session) throws ModelException {
		Process process = null;
		System.out.println("Executing simulation codes...");
		ServerSocket srvSocket = null;
		Socket socket = null;
		LittleEndianDataInputStream out = null;
		try {
			// run the executable ncslab file

            File dir = new File(codeStructC.getCodePath());
            File exeFile = new File(dir, "ncslab.exe");
            System.out.println("The executing file is on " + exeFile);

            String exeFilePath = exeFile.getAbsolutePath();

            // Create a ServerSocket to accept TCP connection from the C++ process
            srvSocket = new ServerSocket(0);
            srvSocket.setSoTimeout(30000);
            int port = srvSocket.getLocalPort();
            System.out.println("[CodeModelCWindowsSimulation] Waiting for C++ simulation to connect on TCP port " + port);

            // Use ProcessBuilder instead of deprecated Runtime.exec()
            ProcessBuilder processBuilder = new ProcessBuilder(
                exeFilePath,
                String.valueOf(this.getConfig().getStopTime()),
                String.valueOf(port)
            );
            processBuilder.directory(dir);
            process = processBuilder.start();
            this.currentProcess = process;

            // Accept TCP connection from the C++ process
            socket = srvSocket.accept();
            this.simSocket = socket;
            this.serverSocket = srvSocket;
            System.out.println("[CodeModelCWindowsSimulation] C++ simulation connected via TCP");

			// read primitive Java data types from an underlying InputStream in a little-endian format
			// This input stream is the TCP socket from the process
			out = new LittleEndianDataInputStream(socket.getInputStream());

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
					// 4-byte preamble sync: 0x55 0xAA 0x55 0xAA
					int b1 = 0, b2 = 0, b3 = 0, b4 = 0;
					do {
						b1 = b2; b2 = b3; b3 = b4;
						b4 = out.readByte() & 0xFF;
						if (b1 == 0x55 && b2 == 0xAA && b3 == 0x55 && b4 == 0xAA) {
							break;
						}
					} while (true);
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
				case 5: // RealtimeDataUpdate
						try {
							int rtDisplayCount = out.readInt();
							if (!isValidLength(rtDisplayCount, 10000)) {
								System.err.println("[CodeModelCWindowsSimulation] Invalid rtDisplayCount: " + rtDisplayCount + ", skipping RealtimeDataUpdate");
								break;
							}
							Map<String, Double> rtDisplayData = new HashMap<>();
							for (int i = 0; i < rtDisplayCount; i++) {
								int uuidLen = out.readInt();
								if (!isValidLength(uuidLen, 1024)) {
									System.err.println("[CodeModelCWindowsSimulation] Invalid display uuidLen: " + uuidLen + ", skipping RealtimeDataUpdate");
									break;
								}
								byte[] uuidBytes = new byte[uuidLen];
								out.readFully(uuidBytes);
								String uuid = new String(uuidBytes, StandardCharsets.UTF_8);
								double displayValue = out.readDouble();
								rtDisplayData.put(uuid, displayValue);
							}
							int rtScopeCount = out.readInt();
							if (!isValidLength(rtScopeCount, 10000)) {
								System.err.println("[CodeModelCWindowsSimulation] Invalid rtScopeCount: " + rtScopeCount + ", skipping RealtimeDataUpdate");
								break;
							}
							List<Map<String, Object>> rtScopeDataList = new ArrayList<>();
							for (int i = 0; i < rtScopeCount; i++) {
								int uuidLen = out.readInt();
								if (!isValidLength(uuidLen, 1024)) {
									System.err.println("[CodeModelCWindowsSimulation] Invalid scope uuidLen: " + uuidLen + ", skipping RealtimeDataUpdate");
									break;
								}
								byte[] uuidBytes = new byte[uuidLen];
								out.readFully(uuidBytes);
								String uuid = new String(uuidBytes, StandardCharsets.UTF_8);
								int width = out.readInt();
								int height = out.readInt();
								int dataPointCount = out.readInt();
								if (!isValidLength(dataPointCount, 10000)) {
									System.err.println("[CodeModelCWindowsSimulation] Invalid dataPointCount: " + dataPointCount + ", skipping RealtimeDataUpdate");
									break;
								}
								List<Double> timeList = new ArrayList<>();
								List<Double> dataList = new ArrayList<>();
								for (int p = 0; p < dataPointCount; p++) {
									double t = out.readDouble();
									timeList.add(t);
									for (int h = 0; h < height; h++) {
										for (int w = 0; w < width; w++) {
											double val = out.readDouble();
											dataList.add(val);
										}
									}
								}
								Map<String, Object> scopeData = new HashMap<>();
								scopeData.put("uuid", uuid);
								scopeData.put("width", width);
								scopeData.put("height", height);
								scopeData.put("time", timeList);
								scopeData.put("data", dataList);
								rtScopeDataList.add(scopeData);
							}
							double currentSimTime = out.readDouble();

							sendRealtimeDataUpdateMessage(session, rtDisplayData, rtScopeDataList, currentSimTime);
						} catch (IOException e) {
							System.err.println("[CodeModelCWindowsSimulation] Error parsing RealtimeDataUpdate: " + e.getMessage());
						}
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
			this.simSocket = null;
			this.serverSocket = null;
		}

		System.out.println("Simulation codes executed successfully!");

	}
}
