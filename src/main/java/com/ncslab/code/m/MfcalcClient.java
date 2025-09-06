package com.ncslab.code.m;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Optional;

import com.utils.Property;
import com.ncslab.dto.communication.MfcalcResponseDto;
import com.ncslab.dto.communication.MfcalcRequestDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

@Slf4j
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
    private static List<Map<String, Object>> localVariables;

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
    private MfcalcResponseDto sendRequest(MfcalcRequestDto request) {
        try {
            String requestJson = request.toJsonString();
            log.debug("Sending request to MFCalc server: {}", requestJson);
            outputStream.write((requestJson + "\n").getBytes());
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
            String responseStr = response.toString();
            // Print the raw response from server
            System.out.println("Raw response from MFCalc server: " + responseStr);
            log.debug("Received response from MFCalc server: {}", responseStr);
            return MfcalcResponseDto.fromJsonString(responseStr);
        } catch (IOException e) {
            log.error("Error sending request to MFCalc server: {}", e.getMessage(), e);
            // Log to file for debugging
            try (java.io.FileWriter fw = new java.io.FileWriter("mfcalc.log", true)) {
                fw.write(e.toString() + "\n");
                for (StackTraceElement ste : e.getStackTrace()) {
                    fw.write("\tat " + ste + "\n");
                }
            } catch (IOException logEx) {
                log.error("Failed to write to mfcalc.log: {}", logEx.getMessage());
            }
            return MfcalcResponseDto.builder()
                    .status("error")
                    .error("Connection error: " + e.getMessage())
                    .build();
        }
    }

    public MfcalcResponseDto runScript(String script) {
        MfcalcRequestDto request = MfcalcRequestDto.createRunScript(script);
        return sendRequest(request);
    }

    public MfcalcResponseDto debugScript(String script, int[] breakpoints) {
        MfcalcRequestDto request = MfcalcRequestDto.createDebugScript(script, breakpoints);
        return sendRequest(request);
    }

    public MfcalcResponseDto runCommand(String command) {
        MfcalcRequestDto request = MfcalcRequestDto.createRunCommand(command);
        return sendRequest(request);
    }

    public MfcalcResponseDto getVariables() {
        MfcalcRequestDto request = MfcalcRequestDto.createGetVariables();
        MfcalcResponseDto response = sendRequest(request);
        if (response != null && response.getVariables() != null) {
            localVariables = response.getVariables();
        }
        return response;
    }

    public MfcalcResponseDto getVariable(String variableName) {
        MfcalcRequestDto request = MfcalcRequestDto.createGetVariable(variableName);
        return sendRequest(request);
    }

    public MfcalcResponseDto setVariable(String variableName, Object variableValue) {
        MfcalcRequestDto request = MfcalcRequestDto.createSetVariable(variableName, variableValue);
        return sendRequest(request);
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
