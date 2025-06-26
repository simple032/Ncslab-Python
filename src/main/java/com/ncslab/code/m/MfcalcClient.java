package com.ncslab.code.m;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Optional;

import com.utils.Property;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

public class MfcalcClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9090;
    private Socket socket;
    private OutputStream outputStream;
    private BufferedReader reader;

    // 单例实例
    private static MfcalcClient instance;
    private static boolean initialized = false;
    @Getter
    private static JSONArray localVariables;

    public MfcalcClient(Socket socket) throws IOException {
        String server_host = Optional.ofNullable(System.getenv("MfcalcServerHost"))
            .orElse(SERVER_HOST);
        int server_port = SERVER_PORT;
        if (socket == null) {
            socket = new Socket(server_host, server_port);
        }
        outputStream = socket.getOutputStream();
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    }

    // 获取单例实例的静态方法
    private JSONObject sendRequest(JSONObject request) {
        try {
            outputStream.write((request.toString() + "\n").getBytes());
            outputStream.flush();

            // 读取缓冲区长度
            StringBuilder lengthBuilder = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1 && Character.isDigit(c)) {
                lengthBuilder.append((char) c);
            }
            int bufferLength = Integer.parseInt(lengthBuilder.toString());

            // 读取字符串
            StringBuilder response = new StringBuilder();
            int count = 0;
            while (count < bufferLength && (c = reader.read()) != -1) {
                response.append((char) c);
                count++;
            }
            return new JSONObject(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject runScript(String script) {
        JSONObject request = new JSONObject();
        request.put("message_type", "run_script");
        request.put("message_id", generateMessageId());
        request.put("data", new JSONObject().put("script", script));
        return sendRequest(request);
    }

    public JSONObject debugScript(String script, int[] breakpoints) {
        JSONObject request = new JSONObject();
        request.put("message_type", "debug_script");
        request.put("message_id", generateMessageId());
        request.put("data", new JSONObject().put("script", script).put("breakpoints", breakpoints));
        return sendRequest(request);
    }

    public JSONObject runCommand(String command) {
        JSONObject request = new JSONObject();
        request.put("message_type", "run_command");
        request.put("message_id", generateMessageId());
        request.put("data", new JSONObject().put("command", command));
        return sendRequest(request);
    }

    public JSONObject getVariables() {
        JSONObject request = new JSONObject();
        request.put("message_type", "get_variables");
        request.put("message_id", generateMessageId());
        request.put("data", new JSONObject());
        JSONObject jo = sendRequest(request);
        localVariables = jo.getJSONArray("data");
        return jo;
    }

    public JSONObject getVariable(String variableName) {
        JSONObject request = new JSONObject();
        request.put("message_type", "get_variable");
        request.put("message_id", generateMessageId());
        request.put("data", new JSONObject().put("variable_name", variableName));
        return sendRequest(request);
    }

    public JSONObject setVariable(String variableName, JSONObject variableValue) {
        JSONObject request = new JSONObject();
        request.put("message_type", "set_variables");
        request.put("message_id", generateMessageId());
        request.put("data", new JSONObject().put("variable_name", variableName).put("value", variableValue));
        return sendRequest(request);
    }

    private String generateMessageId() {
        // 简单生成一个唯一的消息 ID，实际应用中可以使用更复杂的方式
        return String.valueOf(System.currentTimeMillis());
    }

    public void close() {
        try {
            if (reader != null) reader.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
