package com.ncslab.server.pythonServer;

import com.utils.Property;

import java.net.*;
import java.util.*;


public class PythonServer extends Thread {

public static PythonServer instance=new PythonServer();

	public static int ServerDefaultPort=2004;
	private Vector<PythonThread> octaveThreadList=new Vector<PythonThread>();

	public void removeOctaveThread(PythonThread thread) {
		octaveThreadList.remove(thread);
	}

	public PythonThread getVacantOctaveThread() {
		PythonThread thread=null;
		synchronized(octaveThreadList) {
			for(PythonThread octaveThread:octaveThreadList) {
				if(octaveThread.getIsBusy()==false) {
					thread=octaveThread;
					octaveThread.setIsBusy(true);
					break;
				}
			}
		}
		return thread;
	}


	public void run() {
		System.out.println("HelloPython!");
		try {
			// ���������socket
            int server_port= Integer.parseInt(
                Optional.ofNullable( Property.instance.getProperty("PythonServerPort") )
                    .orElse(String.valueOf(ServerDefaultPort))
            );
			ServerSocket serverSocket = new ServerSocket(server_port);

			// �����ͻ���socket
			Socket socket = new Socket();

			//ѭ�������ȴ��ͻ��˵�����
            while(true){
            	// �����ͻ���
            	socket = serverSocket.accept();

            	PythonThread thread = new PythonThread(socket,this);
            	octaveThreadList.add(thread);
            	thread.start();

            	InetAddress address=socket.getInetAddress();
                System.out.println("��Python�ͻ��˵�IP��"+address.getHostAddress());
                System.out.println("HelloPythonServer");
            }
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
