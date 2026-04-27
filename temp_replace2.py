with open(r'D:\NewLab\ncslab_link\src\main\java\com\ncslab\code\c\windows\simulation\CodeModelCWindowsSimulation.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Find case 5 block
marker = 'case 5: // RealtimeDataUpdate'
idx = content.find(marker)
if idx < 0:
    print('case 5 not found')
    exit(1)

# Find the end of case 5 - look for the catch block then break
start = idx
end_marker = '\t\t\t\t\t}\n\t\t\t\t\tbreak;'
end_idx = content.find(end_marker, idx)
if end_idx < 0:
    print('end of case 5 not found with first marker')
    # Try alternate indentation
    end_marker2 = '\t\t\t\t}\n\t\t\t\tbreak;'
    end_idx = content.find(end_marker2, idx)
    if end_idx < 0:
        print('end of case 5 not found with second marker')
        exit(1)
    end_marker = end_marker2

end_idx += len(end_marker)
old_block = content[start:end_idx]

new_block = '''case 5: // RealtimeDataUpdate
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
							// === 后端降采样：减少前后端 WebSocket 数据传输量 ===
							int maxPoints = calculateMaxPointsPerUpload();
							rtScopeDataList = downsampleScopes(rtScopeDataList, maxPoints);
							sendRealtimeDataUpdateMessage(session, rtDisplayData, rtScopeDataList, currentSimTime);
						} catch (IOException e) {
							System.err.println("[CodeModelCWindowsSimulation] Error parsing RealtimeDataUpdate: " + e.getMessage());
						}
						break;'''

if old_block in content:
    content = content.replace(old_block, new_block)
    with open(r'D:\NewLab\ncslab_link\src\main\java\com\ncslab\code\c\windows\simulation\CodeModelCWindowsSimulation.java', 'w', encoding='utf-8') as f:
        f.write(content)
    print('Replaced case 5 successfully')
else:
    print('old_block not found')
    print('Marker found at:', idx)
    print('End found at:', end_idx)
    print('Old block length:', len(old_block))
    print(repr(old_block[:200]))
