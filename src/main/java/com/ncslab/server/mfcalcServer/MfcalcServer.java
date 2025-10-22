package com.ncslab.server.mfcalcServer;

import java.net.*;
import java.util.*;
import com.utils.Property;

public class MfcalcServer extends Thread {

public static MfcalcServer instance=new MfcalcServer();

	public static int ServerDefaultPort= 2003;
	private Vector<MfcalcThread> mfcalcThreadList=new Vector<MfcalcThread>();

	/** Flag to signal server shutdown */
	private volatile boolean running = true;

	public void removeMfcalcThread(MfcalcThread thread) {
		mfcalcThreadList.remove(thread);
	}

	public MfcalcThread getVacantMfcalcThread() {
		MfcalcThread thread=null;
		synchronized(mfcalcThreadList) {
			for(MfcalcThread mfcalcThread:mfcalcThreadList) {
				if(!mfcalcThread.getIsBusy()) {
					thread=mfcalcThread;
					mfcalcThread.setIsBusy(true);
					break;
				}
			}
		}
		return thread;
	}


	public void run() {
		System.out.println("HelloMfcalc!");
		try {
			// ���������socket
            int server_port= Integer.parseInt(
            Optional.ofNullable( Property.instance.getProperty("MfcalcServerPort") )
                .orElse(String.valueOf(ServerDefaultPort))
            );
			// ServerSocket serverSocket = new ServerSocket(server_port);

			// �����ͻ���socket
			// Socket socket = new Socket();
			Socket socket = null;

			//ѭ�������ȴ��ͻ��˵�����
//            while(true){
            	// �����ͻ���
//            	socket = serverSocket.accept();
				System.out.println("MfcalcServer is running on port: " + server_port);
            	MfcalcThread thread = new MfcalcThread(socket,this);
            	mfcalcThreadList.add(thread);
				System.out.println("MfcalcThread start");
            	thread.start();

//            	InetAddress address=socket.getInetAddress();
//                System.out.println("��ǰMfcalc�ͻ��˵�IP��"+address.getHostAddress());
//                System.out.println("HelloMfcalcServer");
//            }
			synchronized(thread) {
				while (running) {
					thread.wait(1000); // Wait with timeout to check running flag periodically
				}
			}
		} catch (InterruptedException e) {
			// Thread interrupted, exit gracefully
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * Gracefully shuts down the MfcalcServer.
	 * Stops all active threads and releases resources.
	 * This method replaces the deprecated Thread.stop().
	 */
	public void shutdown() {
		running = false;

		// Close all active threads
		synchronized(mfcalcThreadList) {
			for (MfcalcThread thread : mfcalcThreadList) {
				try {
					thread.shutdown();
				} catch (Exception e) {
					System.err.println("Error shutting down MfcalcThread: " + e.getMessage());
				}
			}
			mfcalcThreadList.clear();
		}

		// Interrupt the server thread to wake it up from wait()
		this.interrupt();
	}
}
