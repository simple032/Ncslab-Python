package com.ncslab.server;

import java.net.*;
import org.json.JSONObject;
import org.json.JSONArray;

import com.ncslab.code.m.CodeModelM;
import com.ncslab.server.base.BaseServerThread;

import java.io.*;

public class SimulationThread extends BaseServerThread<SimulationThread, SimulationServer> {
    
    private DataInputStream in;
    private DataOutputStream out;
    
    private Object waitObject = new Object();
    private Object finishObject = new Object();
    
    private CodeModelM model = null;
    
    public SimulationThread(Socket socket, SimulationServer server) {
        super(socket, server);
    }
    
    public void startSimulation(CodeModelM model) {
        this.model = model;
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
    
    @Override
    public void run() {
        try {
            // socket.setKeepAlive(true);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            
            while(true) {
                setIsBusy(false);
                synchronized(waitObject) {
                    waitObject.wait();
                }
                
                setIsBusy(true);
                
                String codePath = model.getCodePath();
                byte data[] = codePath.getBytes();
                out.writeInt(data.length);
                out.write(data);
                
                String modelSeq = "" + model.getModelSeq();
                data = modelSeq.getBytes();
                out.writeInt(data.length);
                out.write(data);
                
                System.out.println("Executing...");
                
                in.readByte();
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
        }
        finally {
            server.removeSimulationThread(this);
        }
    }
}
