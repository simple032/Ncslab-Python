package mfcalcServer;

import java.net.*;
import java.util.*;


public class MfcalcServer extends Thread {

public static MfcalcServer instance=new MfcalcServer();
	
	public static int Server_Port=2003;
	private Vector<MfcalcThread> octaveThreadList=new Vector<MfcalcThread>();
	
	public void removeOctaveThread(MfcalcThread thread) {
		octaveThreadList.remove(thread);
	}
	
	public MfcalcThread getVacantOctaveThread() {
		MfcalcThread thread=null;
		synchronized(octaveThreadList) {
			for(MfcalcThread octaveThread:octaveThreadList) {
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
		System.out.println("HelloMfcalc!");
		try {
			// ���������socket
			ServerSocket serverSocket = new ServerSocket(Server_Port);
			
			// �����ͻ���socket
			Socket socket = new Socket();	
			
			//ѭ�������ȴ��ͻ��˵�����
            while(true){
            	// �����ͻ���
            	socket = serverSocket.accept();
            	
            	MfcalcThread thread = new MfcalcThread(socket,this);
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
