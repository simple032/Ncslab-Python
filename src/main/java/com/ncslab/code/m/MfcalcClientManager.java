package com.ncslab.code.m;

import org.json.JSONArray;
import org.json.JSONObject;
import com.ncslab.dto.ServerRequestJson;
import com.ncslab.dto.ServerResponseJson;
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
    public JSONObject runScriptForUser(String userId, String script) {
        MfcalcClient client = getClientForUser(userId);
        JSONObject result = client.runScript(script);
        // Track request
        String status = result != null && result.has("status") && "success".equals(result.getString("status")) ? "SUCCESS" : "FAILED";
        return result;
    }

    // 调试用户脚本
    public JSONObject debugScriptForUser(String userId, String script, int[] breakpoints) {
        MfcalcClient client = getClientForUser(userId);
        JSONObject result = client.debugScript(script, breakpoints);
        // Track request
        String status = result != null && result.has("status") && "success".equals(result.getString("status")) ? "SUCCESS" : "FAILED";
        return result;
    }

    // 执行用户命令
    public JSONObject runCommandForUser(String userId, String command) {
        MfcalcClient client = getClientForUser(userId);
        JSONObject result = client.runCommand(command);
        // Track request
        String status = result != null && result.has("status") && "success".equals(result.getString("status")) ? "SUCCESS" : "FAILED";
        return result;
    }

    // 获取所有变量
    public JSONArray getVariablesForUser(String userId) {
        MfcalcClient client = getClientForUser(userId);
        JSONObject jo = client.getVariables();
        return jo.getJSONArray("data");
    }

    // 获取指定变量
    public JSONObject getVariableForUser(String userId, String variableName) {
        MfcalcClient client = getClientForUser(userId);
        JSONObject result = client.getVariable(variableName);
        // Track request
        String status = result != null && result.has("status") && "success".equals(result.getString("status")) ? "SUCCESS" : "FAILED";
        return result;
    }

    // 设置变量
    public JSONObject setVariableForUser(String userId, String variableName, JSONObject value) {
        MfcalcClient client = getClientForUser(userId);
        JSONObject result = client.setVariable(variableName, value);
        // Track request
        String status = result != null && result.has("status") && "success".equals(result.getString("status")) ? "SUCCESS" : "FAILED";
        return result;
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
    public ServerResponseJson executeScript(ServerRequestJson request) {
        if (!request.isValid()) {
            return ServerResponseJson.createError("Invalid request: " + request.getValidationError(), "mfcalc");
        }
        
        if (!"mfcalc".equals(request.getServerType())) {
            return ServerResponseJson.createError("Invalid server type for MFCalc: " + request.getServerType(), "mfcalc");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            MfcalcClient client = getClientForUser(request.getUserId());
            if (client == null) {
                return ServerResponseJson.createError("Failed to create MFCalc client for user: " + request.getUserId(), "mfcalc");
            }
            
            JSONObject legacyResult = null;
            
            if ("execute".equals(request.getCommand())) {
                legacyResult = client.runScript(request.getScript());
            } else if ("debug".equals(request.getCommand())) {
                // Handle debug command (would need breakpoints in parameters)
                Object breakpointsObj = request.getParameters() != null ? request.getParameters().get("breakpoints") : null;
                int[] breakpoints = new int[0]; // Default empty breakpoints
                if (breakpointsObj instanceof int[]) {
                    breakpoints = (int[]) breakpointsObj;
                }
                legacyResult = client.debugScript(request.getScript(), breakpoints);
            } else if ("command".equals(request.getCommand())) {
                legacyResult = client.runCommand(request.getScript()); // Use script field for command
            } else {
                return ServerResponseJson.createError("Unsupported command: " + request.getCommand(), "mfcalc");
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Convert legacy JSONObject response to DTO
            ServerResponseJson response = ServerResponseJson.fromLegacyJson(legacyResult);
            if (response != null) {
                response.setServerType("mfcalc");
                response.setExecutionTime(executionTime);
                response.setSessionId(request.getSessionId());
                return response;
            } else {
                return ServerResponseJson.createError("Failed to process MFCalc response", "mfcalc");
            }
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            ServerResponseJson errorResponse = ServerResponseJson.createError("MFCalc execution error: " + e.getMessage(), "mfcalc");
            errorResponse.setExecutionTime(executionTime);
            return errorResponse;
        }
    }
    
    /**
     * Enhanced run script method with DTO support and fallback
     * @param userId User ID
     * @param script Script to execute
     * @return ServerResponseJson (DTO) or null if DTO creation fails
     */
    public ServerResponseJson runScriptEnhanced(String userId, String script) {
        ServerRequestJson request = ServerRequestJson.createMfcalcRequest(userId, script);
        return executeScript(request);
    }
    
    /**
     * Legacy compatibility method that returns JSONObject
     * @param userId User ID
     * @param script Script to execute
     * @return JSONObject (legacy format)
     * @deprecated Use executeScript(ServerRequestJson) or runScriptEnhanced() instead
     */
    @Deprecated
    public JSONObject runScriptLegacyCompat(String userId, String script) {
        ServerResponseJson response = runScriptEnhanced(userId, script);
        return response != null ? response.toLegacyJson() : null;
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
