package com.ncslab.server.pythonServer;

import com.ncslab.server.base.BaseServer;
import com.utils.Property;

import java.net.*;
import java.util.*;

public class PythonServer extends BaseServer<PythonThread> {

    public static PythonServer instance = new PythonServer();
    public static int ServerDefaultPort = 2004;
    
    /**
     * Default constructor.
     */
    private PythonServer() {
        super("PythonServer");
    }
    
    /**
     * Removes a python thread from the server's thread list.
     * 
     * @param thread The thread to remove
     */
    public void removeOctaveThread(PythonThread thread) {
        removeThread(thread);
    }
    
    /**
     * Gets a vacant python thread from the thread list.
     * 
     * @return A vacant thread or null if none is available
     */
    public PythonThread getVacantOctaveThread() {
        return getVacantThread();
    }
    
    @Override
    protected PythonThread createServerThread(Socket socket) {
        return new PythonThread(socket, this);
    }
    
    @Override
    protected int getPort() {
        return getServerPort("PythonServerPort", ServerDefaultPort);
    }
    
    @Override
    protected String getStartLogText() {
        return "HelloPython!";
    }
    
    @Override
    protected String getClientConnectedLogText(InetAddress address) {
        return "当Python客户端的IP是" + address.getHostAddress() + "\nHelloPythonServer";
    }
}
