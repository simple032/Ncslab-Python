package com.ncslab.server.base;

import lombok.Getter;
import lombok.Setter;

import java.net.Socket;

/**
 * Base server thread class that provides common functionality for all server thread implementations.
 *
 * @param <T> The type of the thread itself (for self-reference)
 * @param <S> The type of the server that manages this thread
 */
public abstract class BaseServerThread<T extends BaseServerThread<T, S>, S extends BaseServer<T>> extends Thread {

    /**
     * -- GETTER --
     *  Gets the socket associated with this thread.
     *
     * @return The client socket
     */
    @Getter
    protected Socket socket;
    /**
     * -- GETTER --
     *  Gets the server that manages this thread.
     *
     * @return The server
     */
    @Getter
    protected S server;
    /**
     * -- SETTER --
     *  Sets the busy status of this thread.
     *
     * @param busy true if the thread is busy, false otherwise
     */
    protected boolean isBusy = false;

    /**
     * Constructor for BaseServerThread.
     *
     * @param socket The client socket
     * @param server The server that manages this thread
     */
    public BaseServerThread(Socket socket, S server) {
        this.socket = socket;
        this.server = server;
    }

    /**
     * Gets the busy status of this thread.
     *
     * @return true if the thread is busy, false otherwise
     */
    public boolean getIsBusy() {
        return isBusy;
    }

    /**
     * Sets the busy status of this thread.
     *
     * @param isBusy true if the thread is busy, false otherwise
     */
    public void setIsBusy(boolean isBusy) { this.isBusy = isBusy; }

    /**
     * Gracefully shuts down this thread.
     * Closes the socket connection and interrupts the thread if it's running.
     * Subclasses can override this to add additional cleanup logic.
     */
    public void shutdown() {
        // Close the socket if it's open
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (Exception e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }

        // Interrupt the thread to wake it up from any blocking operations
        this.interrupt();
    }
}
