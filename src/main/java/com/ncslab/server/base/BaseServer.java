package com.ncslab.server.base;

import com.utils.Property;

import java.net.*;
import java.util.*;

/**
 * Base server class that provides common functionality for all server implementations.
 * This class handles the basic server socket operations and thread management.
 * 
 * @param <T> The type of thread that this server manages
 */
public abstract class BaseServer<T extends BaseServerThread<T, ?>> extends Thread {

    public static int ServerDefaultPort = 2001;
    
    protected Vector<T> threadList = new Vector<T>();
    protected String serverName = "BaseServer";
    
    /**
     * Constructor that allows setting the server name.
     * 
     * @param serverName The name of the server for logging purposes
     */
    public BaseServer(String serverName) {
        this.serverName = serverName;
    }
    
    /**
     * Removes a thread from the server's thread list.
     * 
     * @param thread The thread to remove
     */
    public void removeThread(T thread) {
        threadList.remove(thread);
    }
    
    /**
     * Gets a vacant thread from the thread list.
     * If no vacant thread is available, returns null.
     * 
     * @return A vacant thread or null if none is available
     */
    public T getVacantThread() {
        T thread = null;
        synchronized(threadList) {
            for(T serverThread : threadList) {
                if(!serverThread.getIsBusy()) {
                    thread = serverThread;
                    serverThread.setIsBusy(true);
                    break;
                }
            }
        }
        return thread;
    }
    
    /**
     * Gets the server port from properties or returns the default port.
     * 
     * @param propertyName The name of the property to retrieve the port from
     * @param defaultPort The default port to use if property is not found
     * @return The server port
     */
    protected int getServerPort(String propertyName, int defaultPort) {
        return Integer.parseInt(
            Optional.ofNullable(Property.instance.getProperty(propertyName))
                .orElse(String.valueOf(defaultPort))
        );
    }
    
    /**
     * Creates a new server thread for handling client connections.
     * This method must be implemented by subclasses to create their specific thread type.
     * 
     * @param socket The client socket
     * @return A new server thread for handling the connection
     */
    protected abstract T createServerThread(Socket socket);
    
    /**
     * Gets the port number for this server.
     * This method can be overridden by subclasses to use custom ports.
     * 
     * @return The port number
     */
    protected int getPort() {
        return ServerDefaultPort;
    }
    
    /**
     * Gets the log text when a server starts.
     * 
     * @return The log text
     */
    protected String getStartLogText() {
        return "Hello";
    }
    
    /**
     * Gets the log text when a client connects.
     * 
     * @param address The client address
     * @return The log text
     */
    protected String getClientConnectedLogText(InetAddress address) {
        return "Host IP: " + address.getHostAddress();
    }
    
    /**
     * The main server run method that accepts client connections and manages threads.
     */
    @Override
    public void run() {
        System.out.println(getStartLogText());
        
        try {
            int port = getPort();
            ServerSocket serverSocket = new ServerSocket(port);
            
            Socket socket = new Socket();
            
            while(true) {
                socket = serverSocket.accept();
                
                T thread = createServerThread(socket);
                threadList.add(thread);
                thread.start();
                
                InetAddress address = socket.getInetAddress();
                System.out.println(getClientConnectedLogText(address));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
