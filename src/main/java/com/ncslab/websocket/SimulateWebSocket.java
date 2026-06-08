package com.ncslab.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * Standard C-code generation simulation WebSocket (same protocol as {@link SimulateCudaWebSocket}).
 *
 * @see WebSocketSecurity
 * @see WebSocketMessageDto
 */
@ServerEndpoint("/websocketsimulate")
public class SimulateWebSocket {

    private static final String LOG_TAG = "[SimulateWebSocket]";

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
        SimulateWebSocketSupport.onMessage(session, msgString, LOG_TAG, false);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        SimulateWebSocketSupport.onError(session, throwable, LOG_TAG);
    }
}
