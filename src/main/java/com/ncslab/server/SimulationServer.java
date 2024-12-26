package com.ncslab.server;

import com.ncslab.utils.Property;

import java.net.*;
import java.util.*;

public class SimulationServer extends Thread {

	public static SimulationServer instance=new SimulationServer();

	public static int ServerDefaultPort=2001;
	private Vector<SimulationThread> simulationThreadList=new Vector<SimulationThread>();

	public void removeSimulationThread(SimulationThread thread) {
		simulationThreadList.remove(thread);
	}

	public SimulationThread getVacantSimulationThread() {
		SimulationThread thread=null;
		synchronized(simulationThreadList) {
			for(SimulationThread simulationThread:simulationThreadList) {
				if(simulationThread.getIsBusy()==false) {
					thread=simulationThread;
					simulationThread.setIsBusy(true);
					break;
				}
			}
		}
		return thread;
	}

	public void run() {
		System.out.println("Hello");
		try {
            int server_port= Integer.parseInt(
                Optional.ofNullable( Property.instance.getProperty("SimulationServerPort") )
                    .orElse(String.valueOf(ServerDefaultPort))
            );
			ServerSocket serverSocket = new ServerSocket(server_port);

			Socket socket = new Socket();

            while(true){

            	socket = serverSocket.accept();

            	SimulationThread thread = new SimulationThread(socket,this);
            	simulationThreadList.add(thread);
            	thread.start();

            	InetAddress address=socket.getInetAddress();
                System.out.println("Host IP: "+address.getHostAddress());
            }
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
