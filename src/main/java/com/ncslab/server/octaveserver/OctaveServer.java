package com.ncslab.server.octaveserver;

import com.ncslab.server.base.BaseServer;
import com.utils.Property;

import java.net.*;
import java.util.*;

public class OctaveServer extends BaseServer<OctaveThread> {

    public static OctaveServer instance = new OctaveServer();
    public static int ServerDefaultPort = 2002;
    
    /**
     * Default constructor.
     */
    private OctaveServer() {
        super("OctaveServer");
    }
    
    /**
     * Removes an octave thread from the server's thread list.
     * 
     * @param thread The thread to remove
     */
    public void removeOctaveThread(OctaveThread thread) {
        removeThread(thread);
    }
    
    /**
     * Gets a vacant octave thread from the thread list.
     * 
     * @return A vacant thread or null if none is available
     */
    public OctaveThread getVacantOctaveThread() {
        return getVacantThread();
    }
    
    @Override
    protected OctaveThread createServerThread(Socket socket) {
        return new OctaveThread(socket, this);
    }
    
    @Override
    protected int getPort() {
        return getServerPort("OctaveServerPort", ServerDefaultPort);
    }
    
    @Override
    protected String getStartLogText() {
        return "HelloOctave!";
    }
    
    @Override
    protected String getClientConnectedLogText(InetAddress address) {
        return "当前Octave客户端的IP是" + address.getHostAddress() + "\nHelloOctaveServer";
    }
}
