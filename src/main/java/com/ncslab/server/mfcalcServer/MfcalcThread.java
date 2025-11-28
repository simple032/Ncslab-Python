package com.ncslab.server.mfcalcServer;

import java.net.*;

import com.ncslab.code.m.MfcalcClient;
import com.ncslab.code.m.MfcalcClientManager;
import com.ncslab.dto.communication.MfcalcResponseDto;
import com.ncslab.server.base.BaseServerThread;
import com.utils.Property;
import org.json.JSONObject;

import com.ncslab.code.m.CodeOctaveM;

import java.io.*;
import java.util.Optional;

/**
 * MfcalcThread handles communication with a connected MFCalc computation client.
 * It receives computation tasks and dispatches them to the client for execution.
 */
public class MfcalcThread extends BaseServerThread<MfcalcThread, MfcalcServer> {

	private DataInputStream in;
	private DataOutputStream out;

	private Object waitObject = new Object();
	private Object finishObject = new Object();

	private String codePathBase = Property.instance.getProperty("MfcalcCodePath")
			.replace("${M2PLAB_ROOT}", Optional.ofNullable(System.getenv("M2PLAB_ROOT")).orElse(""));

	private CodeOctaveM model = null;

	/** Flag to signal thread shutdown */
	private volatile boolean running = true;

	public MfcalcThread(Socket socket, MfcalcServer server) {
		super(socket, server);
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
			// Initialize socket streams for communication with mfcalc client
			socket.setKeepAlive(true);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			System.out.println("MfcalcThread started, waiting for tasks...");

			while (running) {
				System.out.println("Executing mfcalc...");
				isBusy = false;
				synchronized (waitObject) {
					System.out.println("waiting for task...");
					waitObject.wait();
				}

				if (!running) {
					break;
				}

				isBusy = true;

				try {
					// Send the computation task to the mfcalc client
					String mainCode = model.getMainCode();
					byte[] data = mainCode.getBytes("UTF-8");
					out.writeInt(data.length);
					out.write(data);
					out.flush();

					System.out.println("Task sent to mfcalc client, waiting for result...");

					// Read response from mfcalc client
					String inStr = "";
					byte tmpByte = in.readByte();
					inStr = inStr.concat(Character.toString((char) (tmpByte)));
					while (in.available() != 0) {
						tmpByte = in.readByte();
						inStr = inStr.concat(Character.toString((char) (tmpByte)));
					}

					System.out.println("Received result from mfcalc client: " + inStr);

					// Parse the response - check if it contains the split markers
					if (inStr.contains("ZhouXWSplitBetweenResultAndFigNum")) {
						String[] parts = inStr.split("ZhouXWSplitBetweenResultAndFigNum");
						model.setOutputResult(parts[0]);

						if (parts.length > 1 && parts[1].contains("ZhouXWSplitBetweenFigBeginAndFigEnd")) {
							String[] figParts = parts[1].split("ZhouXWSplitBetweenFigBeginAndFigEnd");
							model.OutputFigBeginIndex = Integer.parseInt(figParts[0].trim());
							if (figParts.length > 1) {
								model.OutputFigEndIndex = Integer.parseInt(figParts[1].trim());
							}
						}
					} else {
						// Simple response without markers
						model.setOutputResult(inStr);
					}

					System.out.println("Done processing mfcalc task.");

				} catch (IOException e) {
					System.err.println("Error communicating with mfcalc client: " + e.getMessage());
					e.printStackTrace();
					// Set error result
					if (model != null) {
						model.setOutputResult("Error: " + e.getMessage());
					}
				}

				synchronized (finishObject) {
					finishObject.notify();
				}
			}
		} catch (InterruptedException e) {
			// Thread interrupted, exit gracefully
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			System.err.println("MfcalcThread error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			closeStreams();
			server.removeMfcalcThread(this);
		}
	}

	/**
	 * Closes the input and output streams.
	 */
	private void closeStreams() {
		try {
			if (in != null) in.close();
		} catch (IOException e) {
			System.err.println("Error closing input stream: " + e.getMessage());
		}
		try {
			if (out != null) out.close();
		} catch (IOException e) {
			System.err.println("Error closing output stream: " + e.getMessage());
		}
	}

	/**
	 * Gracefully shuts down this MfcalcThread.
	 * Closes the socket connection and interrupts the thread.
	 */
	@Override
	public void shutdown() {
		running = false;

		// Wake up the thread if it's waiting
		synchronized (waitObject) {
			waitObject.notify();
		}

		// Close streams
		closeStreams();

		// Call parent shutdown to close socket and interrupt thread
		super.shutdown();
	}

}
