package com.ncslab.server.octaveserver;

import com.utils.Property;

import java.net.*;
import java.util.*;


public class OctaveServer extends Thread {

public static OctaveServer instance=new OctaveServer();

	public static int ServerDefaultPort=2002;
	private Vector<OctaveThread> octaveThreadList=new Vector<OctaveThread>();

	public void removeOctaveThread(OctaveThread thread) {
		octaveThreadList.remove(thread);
	}

	public OctaveThread getVacantOctaveThread() {
		OctaveThread thread=null;
		synchronized(octaveThreadList) {
			for(OctaveThread octaveThread:octaveThreadList) {
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
		System.out.println("HelloOctave!");
		try {
			// ���������socket
            int server_port= Integer.parseInt(
                Optional.ofNullable( Property.instance.getProperty("OctaveServerPort") )
                    .orElse(String.valueOf(ServerDefaultPort))
            );
			ServerSocket serverSocket = new ServerSocket(server_port);

			// �����ͻ���socket
			Socket socket = new Socket();

			//ѭ�������ȴ��ͻ��˵�����
            while(true){
            	// �����ͻ���
            	socket = serverSocket.accept();

            	OctaveThread thread = new OctaveThread(socket,this);
            	octaveThreadList.add(thread);
            	thread.start();

            	InetAddress address=socket.getInetAddress();
                System.out.println("��ǰOctave�ͻ��˵�IP��"+address.getHostAddress());
                System.out.println("HelloOctaveServer");
            }
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
