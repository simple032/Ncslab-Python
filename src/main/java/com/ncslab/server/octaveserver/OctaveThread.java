package com.ncslab.server.octaveserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.*;
import org.json.JSONObject;

import com.ncslab.code.m.CodeOctaveM;
import com.ncslab.server.base.BaseServerThread;

import java.io.*;

public class OctaveThread extends BaseServerThread<OctaveThread, OctaveServer> {
    private DataInputStream in;
    private DataOutputStream out;

    private Object waitObject = new Object();
    private Object finishObject = new Object();

    private CodeOctaveM model = null;

    public OctaveThread(Socket socket, OctaveServer server) {
        super(socket, server);
    }

    public void startOctave(CodeOctaveM model) {
        this.model = model;

        System.out.println("startOctave...");
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
        JSONObject jb = new JSONObject();
        jb.put("code", 2000);
        jb.put("message", "SUCCESS");
        JSONObject data = new JSONObject();
        data.put("figureFileUrl", "scope");
        jb.put("data", data);
        return jb.toString();
    }

    public void readStreamWithRecursion(String inStr, DataInputStream inStream) throws Exception {
        long start = System.currentTimeMillis();
        int time = 3000; // 设置超时时间
        while (inStream.available() == 0) {
            if ((System.currentTimeMillis() - start) > time) { // 超时退出
                this.model.setOutputResult(inStr);
                throw new SocketTimeoutException("超时读取");
            }
        }
        byte tmpByte = inStream.readByte();
        inStr = inStr.concat(Character.toString((char)(tmpByte)));

        int wait = readWait();
        long startWait = System.currentTimeMillis();
        boolean checkExist = false;
        while (System.currentTimeMillis() - startWait <= wait) {
            int a = inStream.available();
            if (a > 0) {
                checkExist = true;
                // System.out.println("========发现剩余：" + a + "个字节，继续读");
                break;
            }
        }
        if (checkExist) {
            readStreamWithRecursion(inStr, inStream);
        } else {
            this.model.setOutputResult(inStr);
        }
    }

    protected int readWait() {
        return 100;
    }

    @Override
    public void run() {
        try {
            // socket.setKeepAlive(true);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            while(true) {
                System.out.println("Executing octave...");
                setIsBusy(false);
                synchronized(waitObject) {
                    System.out.println("waiting...");
                    waitObject.wait();
                }

                setIsBusy(true);

                String mainCode = model.getMainCode();
                byte data[] = mainCode.getBytes();
                out.writeInt(data.length);
                out.write(data);

                System.out.println("Executing...");

                byte tmpByte;
                int size = 0, len = 0;
                String inStr = "";

                // readStreamWithRecursion(inStr,in);

                tmpByte = in.readByte();
                inStr = inStr.concat(Character.toString((char)(tmpByte)));
                while (in.available() != 0) {
                    tmpByte = in.readByte();
                    inStr = inStr.concat(Character.toString((char)(tmpByte)));
                }

                System.out.println(inStr);

                this.model.setOutputResult(inStr.split("ZhouXWSplitBetweenResultAndFigNum")[0]);

                this.model.OutputFigBeginIndex = Integer.valueOf(inStr.split("ZhouXWSplitBetweenResultAndFigNum")[1].split("ZhouXWSplitBetweenFigBeginAndFigEnd")[0]).intValue();

                this.model.OutputFigEndIndex = Integer.valueOf(inStr.split("ZhouXWSplitBetweenResultAndFigNum")[1].split("ZhouXWSplitBetweenFigBeginAndFigEnd")[1]).intValue();

                Process proc;
                String matline = null;
                String matline2 = "";
                try {
                    // 树莓派上正式使用下面的命令
                    // Use ProcessBuilder instead of deprecated Runtime.exec()
                    ProcessBuilder processBuilder = new ProcessBuilder("python", "/home/pi/NetConTop/NCSLabLink/octavecode/matload.py");
                    proc = processBuilder.start();
                    // 本地调试使用下面的命令
                    // proc = Runtime.getRuntime().exec("python D:\\Project\\react_antd\\faker\\NetConTop\\ncslablink\\src\\octaveserver\\matload.py");
                    // 用输入输出流来获取结果
                    System.out.println("proc:" + proc);

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                    while ((matline = bufferedReader.readLine()) != null) {
                        System.out.println(matline);
                        matline2 = matline2 + matline;
                    }
                    System.out.println("matline2 in octaveThread:" + matline2);
                    model.setOutputMat(matline2);
                    bufferedReader.close();
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
            e.printStackTrace();
        }
        finally {
            server.removeOctaveThread(this);
        }
    }
}
