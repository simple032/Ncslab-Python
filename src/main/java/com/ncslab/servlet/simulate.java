package com.ncslab.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.json.JSONObject;
import java.io.*;
import java.util.*;

import com.ncslab.code.Solver;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.linux.pc.simulation.CodeModelCLinuxPCSimulation;
import com.ncslab.code.m.CodeModelM;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;


import com.ncslab.server.SimulationServer;
import com.ncslab.server.SimulationThread;

/**
 * Servlet implementation class simulate
 */
@WebServlet("/simulate")
public class simulate extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public simulate() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		
		//��ȡPost��JSON����
		InputStreamReader insr = new InputStreamReader(request.getInputStream(),"utf-8");
        String result = "";
        int respInt = insr.read();
        while(respInt!=-1) {
            result +=(char)respInt;
            respInt = insr.read();
        }  
        JSONObject jsonIn = new JSONObject(result);
		
        int code = 2000;
        String errorMsgs="";
        
        try {

        	//����C���Ե�������CodeModelC
        	//CodeModelCLinuxRaspberry modelC=CodeModelCLinuxRaspberry.createFromJSON(jsonIn,ModelMode.Compilation);
        	CodeModelCLinuxPCSimulation modelC=CodeModelCLinuxPCSimulation.createFromJSON(jsonIn,ModelMode.Simulation);
        	//modelC.setSolver(Solver.ode4);

        	modelC.generate();
        	
        	System.out.println();
        	
        	if(modelC.getErrorList().size()>0) {
        		for(ErrorMessage em: modelC.getErrorList()) {
        			errorMsgs += em.getMessage();
        		}
        		code = 400;
        		throw new ModelException(errorMsgs);
        	}
        	
        	if(modelC.makeExeFile()==false) {
        		throw new ModelException("Can not make exe file!");
        	}
        	
        	// modelC.simulate();
        	
        	JSONObject jb=new JSONObject();
        	jb.put("code", code);
        	jb.put("ver", 1);
        	jb.put("resultsFile", "/CCode/"+modelC.getUserId()+"/"+modelC.getModelId()+"/results.json");
        	
        	response.getWriter().write(jb.toString());
        	/*
        	response.getWriter().write("{\"code\":"+code+","
    	        	+"\"msg\":"+"\""+errorMsgs+"\""
    	        	+"}");*/
        	
        	/*
        	if(modelC.getErrorList().size()==0) {
        		if(modelC.makeExeFile()) {
        			//modelC.saveToDatabase();
        			errorMsgs += "make exe success.";
        		}        		
        	}else {
        		for(ErrorMessage em: modelC.getErrorList()) {
        			errorMsgs += em.getMessage();
        		}
        		code = 400;
        		throw new ModelException(errorMsgs);
        	}
        	
        	response.getWriter().write("{\"code\":"+code+","
	        	+"\"msg\":"+"\""+errorMsgs+"\""
	        	+"}");*/
        }
        catch(ModelException e) {
        	System.err.println(e.getMessage());
        	System.err.println("Code generatrion terminated unsuccessfully������");
        	//response.getWriter().write("{\"code\":\"400\",\"message\":\""+e.getMessage()+"\"}");
        	JSONObject jb=new JSONObject();
        	jb.put("code", 400);
        	jb.put("message",e.getMessage());
        	response.getWriter().write(jb.toString());
        }
        
        /*
        try {
        	//����M���Ե�������CodeModelM
        	CodeModelM model=CodeModelM.createFromJSON(jsonIn,ModelMode.Simulation);
        	model.setSolver(Solver.ode23);
        	model.generate();
        	model.showErrorMessages();

        	System.out.println();
        	
        	SimulationThread thread=SimulationServer.instance.getVacantSimulationThread();
        	
        	JSONObject jb=new JSONObject();
    		jb.put("code", 2000);
        	
        	if(thread!=null) {
        		thread.startSimulation(model);
        		jb.put("message", "SUCCESS");
        		JSONObject data=new JSONObject();
        		data.put("figFileUrl", "/MCode/"+model.getUserId()+"/"+model.getModelId()+"/scope");
        		jb.put("data", data);
        	}
        	else {
        		System.out.println("No server available...");
        		jb.put("message", "No server available...");
        	}
        	
        	response.getWriter().append(jb.toString());

        	
        	//if(model.getErrorList().size()==0) {
        	//	System.out.println(model.getCode());
        	//}
        }
        catch(ModelException e) {
        	System.err.println(e.getMessage());
        	System.err.println("Code generatrion terminated unsuccessfully������");
        	response.getWriter().write("{\"code\":400,\"message\":\""+e.getMessage()+"\"}");
        }*/
	}

}
