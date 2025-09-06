package com.ncslab.code.m;

import com.ncslab.dto.communication.ServerRequestDto;
import com.ncslab.dto.communication.ServerResponseDto;
import com.ncslab.dto.communication.MfcalcResponseDto;
import com.ncslab.util.JsonUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MfcalcClientManager {

    // 线程安全的用户客户端管理容器
    private static final Map<String, MfcalcClient> clientMap = new ConcurrentHashMap<>();

    private MfcalcClientManager() {
        // 私有构造函数，防止外部实例化
    }

    private static class SingletonHolder {
        private static final MfcalcClientManager INSTANCE = new MfcalcClientManager();
    }

    // 静态内部类实现单例模式
    public static MfcalcClientManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // 获取指定用户的 MfcalcClient 实例（如果不存在则创建）
    public static MfcalcClient getClientForUser(String userId) {
        return clientMap.computeIfAbsent(userId, k -> {
            try {
                return new MfcalcClient(null);
            } catch (IOException e) {
                // System.err.println("Failed to create MfcalcClient for user: " + userId);
                return null;
            }
        });
    }

    // 删除指定用户的 MfcalcClient 实例
    public void removeClientForUser(String userId) {
        MfcalcClient client = clientMap.remove(userId);
        if (client != null) {
            client.close();
        }
    }

    // 判断是否存在用户对应的 MfcalcClient
    public boolean hasClientForUser(String userId) {
        return clientMap.containsKey(userId);
    }

    // 运行用户脚本
    public MfcalcResponseDto runScriptForUser(String userId, String script) {
        MfcalcClient client = getClientForUser(userId);
        return client.runScript(script);
    }

    // 调试用户脚本
    public MfcalcResponseDto debugScriptForUser(String userId, String script, int[] breakpoints) {
        MfcalcClient client = getClientForUser(userId);
        return client.debugScript(script, breakpoints);
    }

    // 执行用户命令
    public MfcalcResponseDto runCommandForUser(String userId, String command) {
        MfcalcClient client = getClientForUser(userId);
        return client.runCommand(command);
    }

    // 获取所有变量
    public MfcalcResponseDto getVariablesForUser(String userId) {
        MfcalcClient client = getClientForUser(userId);
        return client.getVariables();
    }

    // 获取指定变量
    public MfcalcResponseDto getVariableForUser(String userId, String variableName) {
        MfcalcClient client = getClientForUser(userId);
        return client.getVariable(variableName);
    }

    // 设置变量
    public MfcalcResponseDto setVariableForUser(String userId, String variableName, Object value) {
        MfcalcClient client = getClientForUser(userId);
        return client.setVariable(variableName, value);
    }

    // 关闭所有客户端连接
    public void closeAllClients() {
        for (MfcalcClient client : clientMap.values()) {
            client.close();
        }
        clientMap.clear();
    }
    
    // ========================= DTO-BASED METHODS =========================
    
    /**
     * Execute script using DTO pattern (preferred method)
     * @param request Server request DTO
     * @return Server response DTO
     */
    public ServerResponseDto executeScript(ServerRequestDto request) {
        if (!request.isValid()) {
            return ServerResponseDto.createError("Invalid request: " + request.getValidationError(), "mfcalc");
        }
        
        if (!"mfcalc".equals(request.getServerType())) {
            return ServerResponseDto.createError("Invalid server type for MFCalc: " + request.getServerType(), "mfcalc");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            MfcalcClient client = getClientForUser(request.getUserId());
            if (client == null) {
                return ServerResponseDto.createError("Failed to create MFCalc client for user: " + request.getUserId(), "mfcalc");
            }
            
            MfcalcResponseDto mfcalcResult = null;
            
            if ("execute".equals(request.getCommand())) {
                mfcalcResult = client.runScript(request.getScript());
            } else if ("debug".equals(request.getCommand())) {
                // Handle debug command (would need breakpoints in parameters)
                Object breakpointsObj = request.getParameters() != null ? request.getParameters().get("breakpoints") : null;
                int[] breakpoints = new int[0]; // Default empty breakpoints
                if (breakpointsObj instanceof int[]) {
                    breakpoints = (int[]) breakpointsObj;
                }
                mfcalcResult = client.debugScript(request.getScript(), breakpoints);
            } else if ("command".equals(request.getCommand())) {
                mfcalcResult = client.runCommand(request.getScript()); // Use script field for command
            } else {
                return ServerResponseDto.createError("Unsupported command: " + request.getCommand(), "mfcalc");
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Convert MfcalcResponseDto to ServerResponseDto
            if (mfcalcResult != null) {
                ServerResponseDto.ServerResponseDtoBuilder responseBuilder = ServerResponseDto.builder()
                        .serverType("mfcalc")
                        .executionTime(executionTime)
                        .sessionId(request.getSessionId());
                
                // Map status from MfcalcResponseDto
                String status = mfcalcResult.getStatus();
                if (status == null) {
                    status = mfcalcResult.isError() ? "error" : "success";
                }
                responseBuilder.status(status);
                
                // Map other fields
                if (mfcalcResult.getData() != null) {
                    responseBuilder.result(mfcalcResult.getData());
                }
                if (mfcalcResult.getOutput() != null) {
                    responseBuilder.output(mfcalcResult.getOutput());
                }
                if (mfcalcResult.getError() != null) {
                    responseBuilder.error(mfcalcResult.getError());
                    responseBuilder.message(mfcalcResult.getError());
                }
                
                // Set HTTP-style response code
                responseBuilder.code(mfcalcResult.isError() ? 400 : 200);
                
                return responseBuilder.build();
            } else {
                return ServerResponseDto.createError("Failed to process MFCalc response", "mfcalc");
            }
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            ServerResponseDto errorResponse = ServerResponseDto.createError("MFCalc execution error: " + e.getMessage(), "mfcalc");
            errorResponse.setExecutionTime(executionTime);
            return errorResponse;
        }
    }
    
    
    /**
     * Get server statistics and metrics
     * @return Map containing server statistics
     */
    public Map<String, Object> getServerMetrics() {
        Map<String, Object> metrics = new java.util.HashMap<>();
        
        metrics.put("activeClients", clientMap.size());
        metrics.put("serverType", "mfcalc");
        metrics.put("supportsDTOs", true);
        
        // Add client status information
        int connectedClients = 0;
        for (MfcalcClient client : clientMap.values()) {
            if (client != null) {
                connectedClients++;
            }
        }
        metrics.put("connectedClients", connectedClients);
        
        return metrics;
    }
}
