package com.ncslab.websocket;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.websocket.Session;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.dto.communication.WebSocketMessageDto;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.simulation.SimulationBackend;
import com.ncslab.simulation.SimulationService;
import com.ncslab.simulation.SimulationService.PreparedSimulation;
import com.ncslab.simulation.SimulationService.ProgressSink;
import com.ncslab.util.JsonUtils;
import com.ncslab.util.UserContext;

/**
 * Shared handlers for {@link SimulateWebSocket} and {@link SimulateCudaWebSocket}.
 */
public final class SimulateWebSocketSupport {

    private static final Logger logger = Logger.getLogger(SimulateWebSocket.class.getName());

    private SimulateWebSocketSupport() {
    }

    public static void onOpen(Session session) {
        session.setMaxTextMessageBufferSize(WebSocketSecurity.MAX_MESSAGE_SIZE);
        session.setMaxBinaryMessageBufferSize(WebSocketSecurity.MAX_MESSAGE_SIZE);
        System.out.println("[SimulateWebSocketSupport] Opened session " + session.getId()
                + " with max message size " + WebSocketSecurity.MAX_MESSAGE_SIZE + " bytes");
    }

    public static void onClose(Session session, String logTag) {
        Thread simThread = (Thread) session.getUserProperties().get("simulationThread");
        if (simThread != null && simThread.isAlive()) {
            System.out.println(logTag + " Session closed, interrupting simulation thread...");
            simThread.interrupt();
        }

        Object modelObj = session.getUserProperties().get("modelC");
        if (modelObj instanceof CodeModelC) {
            System.out.println(logTag + " Session closed, stopping simulation...");
            ((CodeModelC) modelObj).stop();
        }

        WebSocketSecurity.cleanupSession(session);
    }

    public static void onError(Session session, Throwable throwable, String logTag) {
        String sessionId = session != null ? session.getId() : "null";
        String message = throwable != null ? throwable.getMessage() : "unknown";
        logger.severe(logTag + " WebSocket error on session " + sessionId + ": " + message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    static void sendMessage(Session session, String msgString) throws IOException {
        WebSocketMessageDto message = WebSocketMessageDto.createStatusMessage(msgString, null);
        if (session != null && session.isOpen()) {
            String messageJson = JsonUtils.serializeWebSocketMessage(message);
            session.getBasicRemote().sendText(messageJson);
        }
    }

    static void sendResultMessage(Session session, CodeModelC modelC) throws IOException {
        String resultsPath = "/CCode/" + modelC.getUserId() + "/" + modelC.getModelId() + "/results.json";
        WebSocketMessageDto message = WebSocketMessageDto.createResultMessage(
                resultsPath, modelC.getUserId(), modelC.getModelId());
        if (session != null && session.isOpen()) {
            String messageJson = JsonUtils.serializeWebSocketMessage(message);
            session.getBasicRemote().sendText(messageJson);
        }
    }

    static void sendErrorMessage(Session session, String msgString) throws IOException {
        WebSocketMessageDto message = WebSocketMessageDto.createErrorMessage(msgString);
        if (session != null) {
            String messageJson = JsonUtils.serializeWebSocketMessage(message);
            session.getBasicRemote().sendText(messageJson);
        }
    }

    static void sendSimulatingMessage(Session session, double endTime) throws IOException {
        WebSocketMessageDto message = WebSocketMessageDto.createSimulationProgress(0.0, endTime);
        if (session != null) {
            String messageJson = JsonUtils.serializeWebSocketMessage(message);
            session.getBasicRemote().sendText(messageJson);
        }
    }

    public static void onMessage(Session session, String msgString, String logTag, boolean endpointRequestsCuda) {
        System.out.println(logTag + " Received WebSocket message length="
                + (msgString != null ? msgString.length() : 0));
        if (msgString != null && msgString.length() > WebSocketSecurity.MAX_MESSAGE_SIZE) {
            try {
                sendErrorMessage(session, "Message size exceeds limit: " + msgString.length()
                        + " > " + WebSocketSecurity.MAX_MESSAGE_SIZE);
            } catch (IOException e) {
                logger.severe("Failed to send message-size error: " + e.getMessage());
            }
            return;
        }
        WebSocketMessageDto wsMessage = null;
        CodeModelC modelC = null;
        try {
            wsMessage = JsonUtils.getObjectMapper().readValue(msgString, WebSocketMessageDto.class);
            String com = wsMessage.getCom();
            System.out.println(logTag + " Parsed command=" + com);
            if (com.equals("start")) {
                boolean requestCuda = endpointRequestsCuda
                        || Boolean.TRUE.equals(wsMessage.getPreferCudaSimulation());
                try {
                    sendMessage(session, "start");

                    PreparedSimulation prepared = new SimulationService().prepareSimulation(
                            wsMessage.getMdlData(),
                            requestCuda,
                            new ProgressSink() {
                                @Override
                                public void status(String status) throws IOException {
                                    sendMessage(session, status);
                                }

                                @Override
                                public void simulating(double endTime) throws IOException {
                                    sendSimulatingMessage(session, endTime);
                                }
                            },
                            logTag);
                    modelC = prepared.getModel();
                    SimulationBackend backend = prepared.getBackend();

                    if (session != null) {
                        session.getUserProperties().put("modelC", modelC);
                        final CodeModelC finalModelC = modelC;
                        final SimulationBackend finalBackend = backend;
                        Thread simThread = new Thread(() -> {
                            finalBackend.beforeRun();
                            try {
                                if (session.isOpen()) {
                                    SimulateWebSocketSupport.sendMessage(session, finalBackend.statusMessage());
                                }
                                logger.info(String.format(
                                        "[SimulationBackend] worker=%s model=%s session=%s",
                                        finalBackend.displayName(),
                                        finalModelC.getModelName(),
                                        session.getId()));
                                finalModelC.simulate(session);
                                if (session.isOpen()) {
                                    sendMessage(session, "simulated");
                                    sendResultMessage(session, finalModelC);
                                }
                            } catch (Exception e) {
                                System.err.println(logTag + " Simulation thread error: " + e.getMessage());
                                e.printStackTrace();
                            } finally {
                                finalBackend.afterRun();
                                try {
                                    if (session != null && session.isOpen()) {
                                        session.close();
                                    }
                                } catch (IOException e) {
                                    // Ignore
                                }
                                try {
                                    session.getUserProperties().remove("simulationThread");
                                } catch (IllegalStateException e) {
                                    // Session already closed, ignore
                                }
                            }
                        }, finalBackend.threadPrefix() + session.getId());
                        simThread.setDaemon(true);
                        session.getUserProperties().put("simulationThread", simThread);
                        simThread.start();
                    }

                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    System.err.println("Code generatrion terminated unsuccessfully");
                    throw e;
                } catch (ModelException e) {
                    throw e;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        } catch (SecurityException e) {
            logger.warning("Security violation in WebSocket message: " + e.getMessage());
            try {
                sendErrorMessage(session, "Security validation failed: " + e.getMessage());
            } catch (IOException ioException) {
                logger.severe("Failed to send security error message: " + ioException.getMessage());
            }
        } catch (ModelException e) {
            logger.warning("Model exception: " + e.getMessage());
            try {
                sendErrorMessage(session, e.getMessage());
            } catch (IOException ee) {
                logger.severe("Failed to send model error message: " + ee.getMessage());
            }
        } catch (IOException e) {
            logger.severe("IO exception during simulation: " + e.getMessage());
            try {
                sendErrorMessage(session, "Internal server error during simulation");
            } catch (IOException ioException) {
                logger.severe("Failed to send IO error message: " + ioException.getMessage());
            }
        } catch (Exception e) {
            logger.severe("Unexpected exception: " + e.getMessage());
            if (session != null) {
                try {
                    sendErrorMessage(session, "Internal server error");
                } catch (IOException | RuntimeException ee) {
                    logger.severe("Failed to send error message: " + ee.getMessage());
                }
            }
        } finally {
            UserContext.clear();

            if (modelC != null) {
                try {
                    modelC.postBuild();
                } catch (Exception e) {
                    logger.warning("Error during model cleanup: " + e.getMessage());
                }
            }
        }
    }
}
