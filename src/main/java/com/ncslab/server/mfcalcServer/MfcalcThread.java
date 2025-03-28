package com.ncslab.server.mfcalcServer;

import java.net.*;

import com.utils.Property;
import org.json.JSONObject;

import com.ncslab.code.m.CodeOctaveM;

import java.io.*;

public class MfcalcThread extends Thread {
	private Socket socket;
	private MfcalcServer server;

	private DataInputStream in;
	private DataOutputStream out;

	private Object waitObject=new Object();
	private Object finishObject=new Object();

	private boolean isBusy=true;

	private String codePathBase= Property.instance.getProperty("MfcalcCodePath");

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
            	this.model.OutputResult=inStr;
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
        	this.model.OutputResult=inStr;
        }
    }
    protected int readWait() {
        return 100;
    }


	public void run() {
		try {
			//socket.setKeepAlive(true);
			in=new DataInputStream(socket.getInputStream());
			out=new DataOutputStream(socket.getOutputStream());

			while(true) {

				System.out.println("Executing mfcalc...");
				isBusy=false;
				synchronized(waitObject) {
					System.out.println("waiting...");
					waitObject.wait();
				}

				isBusy=true;

				String mainCode=model.getMainCode();
				byte[] data =mainCode.getBytes();
				out.writeInt(data.length);
				out.write(data);

				System.out.println("Executing...");

				byte tmpByte;
				int size=0,len=0;
				String inStr="";

//				readStreamWithRecursion(inStr,in);


				tmpByte=in.readByte();
				inStr=inStr.concat(Character.toString((char)(tmpByte)));
				while (in.available() != 0) {
					tmpByte=in.readByte();
					inStr=inStr.concat(Character.toString((char)(tmpByte)));

		        }


//				tmpByte=in.readByte();
//				inStr=inStr.concat(Character.toString((char)(tmpByte)));
//				len=in.available();
//				System.out.println("len:"+len);
//				if(len>0) {
//					for(size=0;;size++) {
//						tmpByte=in.readByte();
//						//System.out.println(tmpByte);
//						//if (tmpByte==-1) break;
//						if (size > len-2) break;
//						//System.out.print(Character.toString((char)(tmpByte)));
//						inStr=inStr.concat(Character.toString((char)(tmpByte)));
//					}
//				}

				System.out.println(inStr);

				this.model.OutputResult = inStr.split("ZhouXWSplitBetweenResultAndFigNum")[0];

				this.model.OutputFigBeginIndex = Integer.valueOf(inStr.split("ZhouXWSplitBetweenResultAndFigNum")[1].split("ZhouXWSplitBetweenFigBeginAndFigEnd")[0]).intValue();

				this.model.OutputFigEndIndex = Integer.valueOf(inStr.split("ZhouXWSplitBetweenResultAndFigNum")[1].split("ZhouXWSplitBetweenFigBeginAndFigEnd")[1]).intValue();

//				System.out.println(in.length());
//				System.out.println(Character.toString((char)(in.readByte())));
//				System.out.println(in.readByte());
//				System.out.println(in.readByte());

				Process proc;
				String matline = null;
				String matline2 = "";


                String pythonPath = System.getenv("M2PLAB_ROOT") ;

                String osName = System.getProperty("os.name").toLowerCase();

                if (osName.contains("win")) {
                    pythonPath += "/server/python/python.exe";
//                    System.out.println("This is a Windows operating system.");
                } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
//                    System.out.println("This is a Unix or Linux operating system.");
                    pythonPath = "python";
                } else if (osName.contains("mac")) {
//                    System.out.println("This is a macOS operating system.");
                } else {
                    System.out.println("Unknown operating system: " + osName);
                }

		        try {
		        	//��ݮ���ϲ���ʹ���������
		            proc = Runtime.getRuntime().exec(pythonPath + " " + codePathBase +"/matload.py");// ִ��py�ļ�
		            //���ص���ʹ���������
//		        	proc = Runtime.getRuntime().exec("python D:\\Project\\react_antd\\faker\\NetConTop\\ncslablink\\src\\octaveserver\\matload.py");
		            //���������������ȡ���
		            System.out.println("proc:"+proc);

		            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		            while ((matline = in.readLine()) != null) {
		                System.out.println(matline);
		                matline2 = matline2 + matline;
		            }
		            this.model.OutputMat = matline2;
		            in.close();
		            proc.waitFor();
		        } catch (IOException e) {
		            e.printStackTrace();
		        } catch (InterruptedException e) {
		            e.printStackTrace();
		        }
				System.out.println("Done...");

				synchronized(finishObject) {
					finishObject.notify();
				}
			}

		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(InterruptedException e) {
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
