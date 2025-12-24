package com.ncslab.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.*;
import java.util.Objects;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.linux.pc.simulation.CodeModelCLinuxPCSimulation;
import com.ncslab.code.c.windows.simulation.CodeModelCWindowsSimulation;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.communication.AndroidResponseDto;
import com.ncslab.util.JsonUtils;
import com.ncslab.util.ScopePlotGenerator;
import com.utils.Property;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
/**
 * Servlet implementation class simulate
 */
@WebServlet("/api/v1/ncslab/api/task/sim")
@Slf4j
public class android_simulate extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public android_simulate() {
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
        	
			System.out.println("Received JSON: " + jsonString);

        	// Direct DTO parsing with ObjectMapper - no intermediate JSONObject
        	ModelDto modelDto;
        	try {
        		modelDto = JsonUtils.getObjectMapper().readValue(jsonString, ModelDto.class);
        	} catch (JsonProcessingException e) {
        		log.error("Failed to parse JSON to ModelDto: {}", e.getMessage());
        		throw new ModelException("Failed to parse JSON to ModelDto DTO: " + e.getMessage());
        	}
        	
        	// Validate DTO structure
        	if (!modelDto.isValid()) {
        		throw new ModelException("Invalid ModelDto DTO structure");
        	}
        	
        	System.out.println("Using DTO-based model creation for: " + modelDto.getModelName());

			// host 应为运行Link的操作系统来决定，而不应该由用户来决定
			String osName = System.getProperty("os.name").toLowerCase();
			String host;
			if (osName.contains("win")) {
				host = "Windows";
			} else if (osName.contains("mac")) {
				host = "Mac";
			} else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
				host = "Linux";
			} else {
				host = "Unknown";
			}
			System.out.println("Running on " + host + " with DTO-enhanced servlet");
        	
        	CodeModelC modelC = null;
        	// Create model using DTO factory methods
			if(Objects.equals(host, "Windows")){
				modelC = CodeModelCWindowsSimulation.createFromDto(modelDto, ModelMode.Simulation);
			} else {
				modelC = CodeModelCLinuxPCSimulation.createFromDto(modelDto, ModelMode.Simulation);
			}
        	if (modelC == null) {
        		throw new ModelException("Failed to create simulation model from DTO");
        	}

			modelC.setUserId(0); // 固定0
			modelC.setModelId(0); // 固定0
        	
        	System.out.println("Model created successfully: " + modelC.getModelName() + 
        	                   " with " + modelC.getBlockList().size() + " blocks");
			
			modelC.preBuild();
        	
			modelC.generate();
        	        	
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
        	
        	modelC.simulate(null);

			modelC.postBuild();

        	// Create Android-specific response with proper URLs
        	// String modelName = modelC.getModelName()
			String modelName = "0";
        	// 固定0
			String userId = "0";

			

			String codePathBase = Property.instance.getProperty("CCodePath").replace(
				"${M2PLAB_ROOT}", System.getenv("M2PLAB_ROOT")
			);
			String outputDir = codePathBase + "/" + userId + "/" + modelName;
			String resultsJsonPath = outputDir + "/results.json";

			System.out.println("Generating scope plots from: " + resultsJsonPath);
			List<String> generatedPlots = ScopePlotGenerator.generateScopePlotsFromFile(resultsJsonPath, outputDir);
			System.out.println("Generated " + generatedPlots.size() + " scope plot(s)");		

			if(generatedPlots.size() == 0) {
				throw new ModelException("No scope plots were generated");
			}
			String generatedPlotsPath = generatedPlots.get(0);
		
			File file = new File(generatedPlotsPath);

			String figFileUrl = "/CCode/" + userId + "/" + modelName + "/" + file.getName();
        	String dataFileUrl = "/CCode/" + userId + "/" + modelName + "/results.json";

        	AndroidResponseDto responseDto = AndroidResponseDto.success(modelName, figFileUrl, dataFileUrl);

			System.out.println("Sending response: " + responseDto);

        	try {
        		String jsonResponse = JsonUtils.getObjectMapper().writeValueAsString(responseDto);
        		response.getWriter().write(jsonResponse);
        	} catch (JsonProcessingException e) {
        		log.error("Failed to serialize Android response: {}", e.getMessage());
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
        	System.err.println("Code generation terminated unsuccessfully");

        	// Create Android-specific error response
        	// Try to get model name if available
        	String modelName = null;
        	try {
        		ModelDto modelDto = JsonUtils.getObjectMapper().readValue(jsonString, ModelDto.class);
        		modelName = modelDto.getModelName();
        	} catch (Exception parseEx) {
        		log.warn("Could not extract model name from failed request");
        	}

        	AndroidResponseDto errorResponse = AndroidResponseDto.error(modelName, e.getMessage());

        	try {
        		String jsonResponse = JsonUtils.getObjectMapper().writeValueAsString(errorResponse);
        		response.getWriter().write(jsonResponse);
        	} catch (JsonProcessingException jsonE) {
        		log.error("Failed to serialize Android error response: {}", jsonE.getMessage());
        		response.getWriter().write("{\"error\":\"Internal server error\"}");
        	}
        }
		catch (Exception e){
			e.printStackTrace();
			throw e;
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
