package servlet;

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

import code.Solver;
import code.c.CodeModelC;
import code.m.CodeModelM;

import ncslablink.ModelException;
import ncslablink.ModelMode;


import server.SimulationServer;
import server.SimulationThread;

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

        	/*
        	if(model.getErrorList().size()==0) {
        		System.out.println(model.getCode());
        	}*/
        }
        catch(ModelException e) {
        	System.err.println(e.getMessage());
        	System.err.println("Code generatrion terminated unsuccessfully������");
        	response.getWriter().write("{\"code\":400,\"message\":\""+e.getMessage()+"\"}");
        }
	}

}
