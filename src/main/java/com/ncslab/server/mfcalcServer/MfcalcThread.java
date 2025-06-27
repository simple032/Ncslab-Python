package com.ncslab.server.mfcalcServer;

import java.net.*;

import com.ncslab.code.m.MfcalcClient;
import com.ncslab.code.m.MfcalcClientManager;
import com.utils.Property;
import lombok.Setter;
import org.json.JSONObject;

import com.ncslab.code.m.CodeOctaveM;

import java.io.*;
import java.util.Optional;

public class MfcalcThread extends Thread {
	private Socket socket;
	private MfcalcServer server;

	private DataInputStream in;
	private DataOutputStream out;

	private Object waitObject=new Object();
	private Object finishObject=new Object();

    private boolean isBusy=true;

	private String codePathBase= Property.instance.getProperty("MfcalcCodePath")
        .replace("${M2PLAB_ROOT}", Optional.ofNullable(System.getenv("M2PLAB_ROOT")).orElse(""));

	private CodeOctaveM model=null;

	public MfcalcThread(Socket socket,MfcalcServer server){
		this.socket=socket;
		this.server=server;
	}

	public boolean getIsBusy() {
		return isBusy;
	}
    public void setIsBusy(boolean isBusy) {
    	this.isBusy=isBusy;
    }

    public void startOctave(CodeOctaveM model) {
		this.model=model;

		System.out.println("startMfcalc...");
		synchronized(waitObject) {
			waitObject.notify();
		}

		try {
			synchronized(finishObject) {
				finishObject.wait();
			}
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
	}


	public String getResult() {
		JSONObject jb=new JSONObject();
		jb.put("code", 2000);
		jb.put("message", "SUCCESS");
		JSONObject data=new JSONObject();
		data.put("figureFileUrl", "scope");
		jb.put("data", data);
		return jb.toString();
	}


	public void readStreamWithRecursion(String inStr,DataInputStream inStream) throws Exception {
		long start = System.currentTimeMillis();
        int time =3000;//���룬�俴ʵ�����
        while (inStream.available() == 0) {
            if ((System.currentTimeMillis() - start) >time) {//��ʱ�˳�
            	this.model.setOutputResult(inStr);
                throw new SocketTimeoutException("��ʱ��ȡ");
            }
        }
        byte tmpByte = inStream.readByte();
        inStr=inStr.concat(Character.toString((char)(tmpByte)));

        int wait = readWait();
        long startWait = System.currentTimeMillis();
        boolean checkExist = false;
        while (System.currentTimeMillis() - startWait <= wait) {
            int a = inStream.available();
            if (a > 0) {
                checkExist = true;
               // System.out.println("========����ʣ�ࣺ" + a + "���ֽ�����û��");
                break;
            }

        }
        if (checkExist) {
                readStreamWithRecursion(inStr,inStream);
        }else {
        	this.model.setOutputResult(inStr);
        }
    }
    protected int readWait() {
        return 100;
    }


	public void run() {

		try {
//			socket.setKeepAlive(true);
//			in=new DataInputStream(socket.getInputStream());
//			out=new DataOutputStream(socket.getOutputStream());
            MfcalcClient client = MfcalcClientManager.getClientForUser("18");
			while(client != null) {

				System.out.println("Executing mfcalc...");
				isBusy=false;
				synchronized(waitObject) {
					System.out.println("waiting...");
					waitObject.wait();
				}

				isBusy=true;

				String mainCode=model.getMainCode();

                JSONObject jo = client.runScript(mainCode+"\n");
                System.out.println(jo);
                model.setOutputResult(jo.optString("log",""));
                model.setFigureResult(jo.optJSONObject("figures"));
                JSONObject variables = client.getVariables();
                System.out.println(variables);
                this.model.OutputMat = variables.toString();

				System.out.println("Done...");

				synchronized(finishObject) {
					finishObject.notify();
				}
			}

		} catch(InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			server.removeMfcalcThread(this);
		}
	}

}
