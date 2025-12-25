package com.ncslab.server.mfcalcServer;

import com.ncslab.server.base.BaseServer;

import java.net.*;

/**
 * MfcalcServer manages connections from MFCalc computation clients.
 * It listens for incoming connections and assigns computation tasks to connected clients.
 */
public class MfcalcServer extends BaseServer<MfcalcThread> {

    public static MfcalcServer instance = new MfcalcServer();
    public static int ServerDefaultPort = 2003;

    /**
     * Private constructor for singleton pattern.
     */
    private MfcalcServer() {
        super("MfcalcServer");
    }

    /**
     * Removes a MfcalcThread from the server's thread list.
     *
     * @param thread The thread to remove
     */
    public void removeMfcalcThread(MfcalcThread thread) {
        removeThread(thread);
    }

    /**
     * Gets a vacant MfcalcThread from the thread list.
     *
     * @return A vacant thread or null if none is available
     */
    public MfcalcThread getVacantMfcalcThread() {
        return getVacantThread();
    }

    @Override
    protected MfcalcThread createServerThread(Socket socket) {
        return new MfcalcThread(socket, this);
    }

    @Override
    protected int getPort() {
        return getServerPort("MfcalcServerPort", ServerDefaultPort);
    }

    @Override
    protected String getStartLogText() {
        return "HelloMfcalc!";
    }

    @Override
    protected String getClientConnectedLogText(InetAddress address) {
        return "MfcalcClient connected from: " + address.getHostAddress() + "\nHelloMfcalcServer";
    }
}
