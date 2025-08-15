package com.ncslab.servlet;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.*;
import com.ncslab.code.c.linux.pc.simulation.CodeModelCLinuxPCSimulation;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.dto.ModelJson;
import com.ncslab.dto.ServerResponseJson;
import com.ncslab.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
/**
 * Servlet implementation class simulate
 */
@WebServlet("/simulate")
@Slf4j
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
		// Enhanced with DTO support for Phase 3 migration
		
		// Read POST JSON data
		InputStreamReader insr = new InputStreamReader(request.getInputStream(),"utf-8");
        String jsonString = "";
        int respInt = insr.read();
        while(respInt!=-1) {
            jsonString +=(char)respInt;
            respInt = insr.read();
        }
        
        int code = 2000;
        String errorMsgs="";
        
        try {
        	System.out.println("Simulate servlet: Processing request with ObjectMapper-enhanced approach");
        	
        	// Validate JSON structure first using ObjectMapper
        	String validationError = JsonUtils.validateJsonStructure(jsonString);
        	if (validationError != null) {
        		throw new ModelException("JSON validation failed: " + validationError);
        	}
        	
        	// Direct DTO parsing with ObjectMapper - no intermediate JSONObject
        	ModelJson modelDto;
        	try {
        		modelDto = JsonUtils.getObjectMapper().readValue(jsonString, ModelJson.class);
        	} catch (JsonProcessingException e) {
        		log.error("Failed to parse JSON to ModelJson: {}", e.getMessage());
        		throw new ModelException("Failed to parse JSON to ModelJson DTO: " + e.getMessage());
        	}
        	
        	// Validate DTO structure
        	if (!modelDto.isValid()) {
        		throw new ModelException("Invalid ModelJson DTO structure");
        	}
        	
        	System.out.println("Using DTO-based model creation for: " + modelDto.getModelName());
        	
        	// Create model using DTO factory method
        	CodeModelCLinuxPCSimulation modelC = CodeModelCLinuxPCSimulation.createFromDto(modelDto, ModelMode.Simulation);
        	
        	if (modelC == null) {
        		throw new ModelException("Failed to create simulation model from DTO");
        	}
        	
        	System.out.println("Model created successfully: " + modelC.getModelName() + 
        	                   " with " + modelC.getBlockList().size() + " blocks");

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
        	
        	// Use ObjectMapper directly for response serialization
        	ServerResponseJson responseDto = ServerResponseJson.builder()
        			.status("success")
        			.code(code)
        			.result("/CCode/"+modelC.getUserId()+"/"+modelC.getModelId()+"/results.json")
        			.serverType("simulate")
        			.build();
        	
        	try {
        		String jsonResponse = JsonUtils.getObjectMapper().writeValueAsString(responseDto);
        		response.getWriter().write(jsonResponse);
        	} catch (JsonProcessingException e) {
        		log.error("Failed to serialize response: {}", e.getMessage());
        		response.getWriter().write("{\"error\":\"Failed to serialize response\"}");
        	}
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
        	// Use ObjectMapper directly for error response
        	ServerResponseJson errorResponse = ServerResponseJson.createError(e.getMessage(), "simulate");
        	errorResponse.setCode(400);
        	
        	try {
        		String jsonResponse = JsonUtils.getObjectMapper().writeValueAsString(errorResponse);
        		response.getWriter().write(jsonResponse);
        	} catch (JsonProcessingException jsonE) {
        		log.error("Failed to serialize error response: {}", jsonE.getMessage());
        		response.getWriter().write("{\"error\":\"Internal server error\"}");
        	}
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
