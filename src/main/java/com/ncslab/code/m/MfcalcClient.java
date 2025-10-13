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
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private static final long RECONNECT_DELAY_MS = 1000; // 1 second delay between retries

    private Socket socket;
    private OutputStream outputStream;
    private BufferedReader reader;
    private java.io.InputStream inputStream;
    private final String serverHost;
    private final int serverPort;

    @Getter
    private String userId; // User ID for maintaining user-specific context on server

    // 单例实例
    private static MfcalcClient instance;
    private static boolean initialized = false;
    @Getter
    private static List<Map<String, Object>> localVariables;

    public MfcalcClient(Socket socket) throws IOException {
        this(socket, null);
    }

    public MfcalcClient(Socket socket, String userId) throws IOException {
        this.serverHost = Optional.ofNullable(System.getenv("MfcalcServerHost"))
            .orElse(SERVER_HOST);
        this.serverPort = SERVER_PORT;
        this.userId = userId;

        if (socket == null) {
            this.socket = new Socket(serverHost, serverPort);
        } else {
            this.socket = socket;
        }

        initializeStreams();
    }

    /**
     * Set the user ID for this client
     * @param userId User ID to associate with this connection
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Initialize or reinitialize the streams from the socket
     */
    private void initializeStreams() throws IOException {
        if (socket != null && !socket.isClosed()) {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        } else {
            throw new IOException("Socket is null or closed, cannot initialize streams");
        }
    }

    /**
     * Check if the connection is alive
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Attempt to reconnect to the MFCalc server
     * @return true if reconnection successful, false otherwise
     */
    private boolean reconnect() {
        log.info("Attempting to reconnect to MFCalc server at {}:{}", serverHost, serverPort);

        // Close existing resources
        closeQuietly();

        for (int attempt = 1; attempt <= MAX_RECONNECT_ATTEMPTS; attempt++) {
            try {
                log.debug("Reconnection attempt {} of {}", attempt, MAX_RECONNECT_ATTEMPTS);
                socket = new Socket(serverHost, serverPort);
                initializeStreams();
                log.info("Successfully reconnected to MFCalc server");
                return true;
            } catch (IOException e) {
                log.warn("Reconnection attempt {} failed: {}", attempt, e.getMessage());
                if (attempt < MAX_RECONNECT_ATTEMPTS) {
                    try {
                        Thread.sleep(RECONNECT_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Reconnection interrupted");
                        return false;
                    }
                }
            }
        }

        log.error("Failed to reconnect to MFCalc server after {} attempts", MAX_RECONNECT_ATTEMPTS);
        return false;
    }

    /**
     * Close resources without throwing exceptions
     */
    private void closeQuietly() {
        try {
            if (reader != null) reader.close();
        } catch (IOException ignored) {}
        try {
            if (outputStream != null) outputStream.close();
        } catch (IOException ignored) {}
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    // 获取单例实例的静态方法
    private MfcalcResponseDto sendRequest(MfcalcRequestDto request) {
        return sendRequestWithRetry(request, true);
    }

    /**
     * Send request with optional automatic retry on connection failure
     * @param request The request to send
     * @param allowRetry Whether to allow automatic reconnection and retry
     * @return Response from server
     */
    private MfcalcResponseDto sendRequestWithRetry(MfcalcRequestDto request, boolean allowRetry) {
        try {
            String requestJson = request.toJsonString();
            log.info("=== Sending request to MFCalc server ===");
            log.info("Request type: {}", request.getMessageType());
            log.info("User ID: {}", request.getUserId());
            log.debug("Full request JSON: {}", requestJson);

            outputStream.write((requestJson + "\n").getBytes("UTF-8"));
            outputStream.flush();
            log.info("Request sent successfully, waiting for response...");

            // Read response - MFCalc server sends length-prefixed response
            // Format: "<length>\n<json_data>"
            // IMPORTANT: Use ONLY InputStream, not BufferedReader, to avoid buffering issues
            log.info("Reading response from MFCalc server...");
            System.out.println("[MFCALC-CLIENT-INFO] Reading response from MFCalc server...");

            // Set reasonable timeout for reading
            int originalTimeout = socket.getSoTimeout();
            socket.setSoTimeout(30000); // 30 second timeout

            String responseStr = "";
            try {
                // Read the length prefix line using raw InputStream (not BufferedReader!)
                System.out.println("[MFCALC-CLIENT-DEBUG] Reading length prefix from InputStream...");
                System.out.flush();

                // Read bytes until we hit '\n' to get the length
                StringBuilder lengthBuilder = new StringBuilder();
                int b;
                while ((b = inputStream.read()) != -1) {
                    if (b == '\n') {
                        break;
                    }
                    lengthBuilder.append((char) b);
                }

                String lengthLine = lengthBuilder.toString();

                System.out.println("[MFCALC-CLIENT-DEBUG] Length prefix read: '" + lengthLine + "'");
                System.out.flush();

                if (lengthLine.isEmpty()) {
                    System.err.println("[MFCALC-CLIENT-ERROR] Failed to read length prefix from MFCalc server");
                    System.err.flush();
                    throw new IOException("No length prefix received from MFCalc server");
                }

                // Parse the length
                int expectedLength;
                try {
                    expectedLength = Integer.parseInt(lengthLine.trim());
                    System.out.println("[MFCALC-CLIENT-INFO] Expecting " + expectedLength + " bytes of JSON data");
                    System.out.flush();
                } catch (NumberFormatException e) {
                    System.err.println("[MFCALC-CLIENT-ERROR] Invalid length prefix: '" + lengthLine + "' - " + e.getMessage());
                    System.err.flush();
                    throw new IOException("Invalid length prefix: " + lengthLine, e);
                }

                // Read exactly expectedLength BYTES using InputStream only
                System.out.println("[MFCALC-CLIENT-DEBUG] Allocating buffer for " + expectedLength + " BYTES");
                System.out.flush();

                byte[] buffer = new byte[expectedLength];
                int totalRead = 0;
                int bytesRead;

                System.out.println("[MFCALC-CLIENT-DEBUG] Starting to read JSON data from InputStream...");
                System.out.flush();

                while (totalRead < expectedLength) {
                    bytesRead = inputStream.read(buffer, totalRead, expectedLength - totalRead);
                    if (bytesRead == -1) {
                        System.err.println("[MFCALC-CLIENT-ERROR] Unexpected end of stream after reading " + totalRead + " of " + expectedLength + " bytes");
                        System.err.flush();
                        break;
                    }
                    totalRead += bytesRead;
                    if (totalRead % 100 == 0 || totalRead == expectedLength) {
                        System.out.println("[MFCALC-CLIENT-DEBUG] Read " + bytesRead + " bytes, total: " + totalRead + "/" + expectedLength);
                        System.out.flush();
                    }
                }

                // Convert bytes to String using UTF-8 encoding
                responseStr = new String(buffer, 0, totalRead, "UTF-8");
                System.out.println("[MFCALC-CLIENT-INFO] Successfully read " + totalRead + " bytes");
                System.out.println("[MFCALC-CLIENT-DEBUG] Response preview: " + (responseStr.length() > 200 ? responseStr.substring(0, 200) + "..." : responseStr));
                System.out.flush();

            } catch (java.net.SocketTimeoutException e) {
                System.err.println("[MFCALC-CLIENT-ERROR] Timeout reading response from MFCalc server: " + e.getMessage());
                System.err.flush();
                throw e;
            } catch (Exception e) {
                System.err.println("[MFCALC-CLIENT-ERROR] Exception reading response: " + e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace(System.err);
                System.err.flush();
                throw e;
            } finally {
                // Restore original timeout
                try {
                    socket.setSoTimeout(originalTimeout);
                } catch (Exception e) {
                    // Ignore
                }
            }

            responseStr = responseStr.trim();

            if (responseStr.isEmpty()) {
                System.err.println("[MFCALC-CLIENT-ERROR] Received empty response from MFCalc server");
                return MfcalcResponseDto.builder()
                        .status("error")
                        .error("Empty response from MFCalc server")
                        .build();
            }

            // Print the raw response from server
            log.info("Response received - length: {} bytes", responseStr.length());
            System.out.println("Raw response from MFCalc server: " + responseStr);
            log.debug("Full response: {}", responseStr.length() > 500 ? responseStr.substring(0, 500) + "..." : responseStr);

            MfcalcResponseDto dto = MfcalcResponseDto.fromJsonString(responseStr);
            log.info("Response parsed successfully - status: {}", dto != null ? dto.getStatus() : "null");
            log.info("=== MFCalc server communication completed ===");
            return dto;
        } catch (IOException e) {
            // Check if this is a connection reset or similar connection error
            boolean isConnectionError = e.getMessage() != null &&
                    (e.getMessage().contains("Connection reset") ||
                     e.getMessage().contains("Broken pipe") ||
                     e.getMessage().contains("Connection refused") ||
                     e.getMessage().contains("Socket closed"));

            if (isConnectionError && allowRetry) {
                log.warn("Connection error detected: {}. Attempting to reconnect...", e.getMessage());

                // Try to reconnect
                if (reconnect()) {
                    log.info("Reconnection successful, retrying request");
                    // Retry the request once after successful reconnection
                    return sendRequestWithRetry(request, false); // Don't retry again to avoid infinite loop
                } else {
                    log.error("Reconnection failed, returning error response");
                }
            } else {
                log.error("Error sending request to MFCalc server: {}", e.getMessage(), e);
            }

            // Log to file for debugging
            try (java.io.FileWriter fw = new java.io.FileWriter("mfcalc.log", true)) {
                fw.write(java.time.LocalDateTime.now() + " - " + e.toString() + "\n");
                for (StackTraceElement ste : e.getStackTrace()) {
                    fw.write("\tat " + ste + "\n");
                }
                if (isConnectionError && !allowRetry) {
                    fw.write("Note: Reconnection was already attempted and failed\n");
                }
                fw.write("\n");
            } catch (IOException logEx) {
                log.error("Failed to write to mfcalc.log: {}", logEx.getMessage());
            }

            return MfcalcResponseDto.builder()
                    .status("error")
                    .error("Connection error: " + e.getMessage() +
                           (isConnectionError ? " (reconnection " + (allowRetry ? "attempted" : "not available") + ")" : ""))
                    .build();
        }
    }

    public MfcalcResponseDto runScript(String script) {
        MfcalcRequestDto request = MfcalcRequestDto.createRunScript(script, userId);
        return sendRequest(request);
    }

    public MfcalcResponseDto debugScript(String script, int[] breakpoints) {
        MfcalcRequestDto request = MfcalcRequestDto.createDebugScript(script, breakpoints, userId);
        return sendRequest(request);
    }

    public MfcalcResponseDto runCommand(String command) {
        MfcalcRequestDto request = MfcalcRequestDto.createRunCommand(command, userId);
        return sendRequest(request);
    }

    public MfcalcResponseDto getVariables() {
        MfcalcRequestDto request = MfcalcRequestDto.createGetVariables(userId);
        MfcalcResponseDto response = sendRequest(request);
        if (response != null && response.getVariables() != null) {
            localVariables = response.getVariables();
        }
        return response;
    }

    public MfcalcResponseDto getVariable(String variableName) {
        MfcalcRequestDto request = MfcalcRequestDto.createGetVariable(variableName, userId);
        return sendRequest(request);
    }

    public MfcalcResponseDto setVariable(String variableName, Object variableValue) {
        MfcalcRequestDto request = MfcalcRequestDto.createSetVariable(variableName, variableValue, userId);
        return sendRequest(request);
    }

    public void close() {
        log.info("Closing MFCalc client connection");
        closeQuietly();
    }
}
