package com.ncslab.server;

import com.ncslab.server.base.BaseServer;
import com.utils.Property;

import java.net.*;
import java.util.*;

public class SimulationServer extends BaseServer<SimulationThread> {

    public static SimulationServer instance = new SimulationServer();
    public static int ServerDefaultPort = 2001;
    
    /**
     * Default constructor.
     */
    private SimulationServer() {
        super("SimulationServer");
    }
    
    /**
     * Removes a simulation thread from the server's thread list.
     * 
     * @param thread The thread to remove
     */
    public void removeSimulationThread(SimulationThread thread) {
        removeThread(thread);
    }
    
    /**
     * Gets a vacant simulation thread from the thread list.
     * 
     * @return A vacant thread or null if none is available
     */
    public SimulationThread getVacantSimulationThread() {
        return getVacantThread();
    }
    
    @Override
    protected SimulationThread createServerThread(Socket socket) {
        return new SimulationThread(socket, this);
    }
    
    @Override
    protected int getPort() {
        return getServerPort("SimulationServerPort", ServerDefaultPort);
    }
    
    @Override
    protected String getStartLogText() {
        return "Hello";
    }
    
    @Override
    protected String getClientConnectedLogText(InetAddress address) {
        return "Host IP: " + address.getHostAddress();
    }
}
