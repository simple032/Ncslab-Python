package com.ncslab.code.m;

import org.json.JSONArray;
import org.json.JSONObject;

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
                System.err.println("Failed to create MfcalcClient for user: " + userId);
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
        return client.runScript(script);
    }

    // 调试用户脚本
    public JSONObject debugScriptForUser(String userId, String script, int[] breakpoints) {
        MfcalcClient client = getClientForUser(userId);
        return client.debugScript(script, breakpoints);
    }

    // 执行用户命令
    public JSONObject runCommandForUser(String userId, String command) {
        MfcalcClient client = getClientForUser(userId);
        return client.runCommand(command);
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
        return client.getVariable(variableName);
    }

    // 设置变量
    public JSONObject setVariableForUser(String userId, String variableName, JSONObject value) {
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
}
