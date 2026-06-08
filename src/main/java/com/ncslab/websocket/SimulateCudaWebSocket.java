package com.ncslab.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * CUDA-oriented simulation WebSocket: same JSON message contract as {@link SimulateWebSocket}.
 * Prefer {@code preferCudaSimulation: true} on the standard {@code /websocketsimulate} URL when nginx
 * cannot expose a dedicated path. This endpoint exists for direct Tomcat access or future reverse-proxy rules.
 */
@ServerEndpoint("/websocketsimulatecuda")
public class SimulateCudaWebSocket {

    private static final String LOG_TAG = "[SimulateCudaWebSocket]";

    @OnOpen
    public void onOpen(Session session) {
        SimulateWebSocketSupport.onOpen(session);
    }

    @OnClose
    public void onClose(Session session) {
        SimulateWebSocketSupport.onClose(session, LOG_TAG);
    }

    @OnMessage
    public void onMessage(Session session, String msgString) {
        SimulateWebSocketSupport.onMessage(session, msgString, LOG_TAG, true);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        SimulateWebSocketSupport.onError(session, throwable, LOG_TAG);
    }
}
