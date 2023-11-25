package server;

import java.net.*;
import java.util.*;

public class SimulationServer extends Thread {
	
	public static SimulationServer instance=new SimulationServer();
	
	public static int Server_Port=2001;
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
		
			ServerSocket serverSocket = new ServerSocket(Server_Port);
			
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
