package com.ncslab.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.HashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.ncslab.code.CodeModel;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeModelCFactory;
import com.ncslab.code.c.CodeStructC;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.communication.ServerResponseDto;
import com.ncslab.util.JsonUtils;
import com.ncslab.code.Solver;
import lombok.extern.slf4j.Slf4j;

import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;


/**
 * Servlet implementation class compile
 */
@WebServlet("/compile/*")
@Slf4j
public class compile extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public compile() {
        super();
    }

    // Enhanced compilation method with DTO support
    public static String compileWithDtoSupport(CodeModelC modelC, String jsonString) throws ModelException {
        System.out.println("Compiling with DTO-enhanced approach");
        
        modelC.setSolver(Solver.ode4);
        modelC.generate();
        System.out.println();

        String errorMsgs = "";
        if (modelC.getErrorList().isEmpty()) {
            if (modelC.makeExeFile()) {
                modelC.saveToDatabase();
                errorMsgs += "Compilation successful with DTO support.";
            } else {
                errorMsgs += "make exe failed.";
            }
        } else {
            for (ErrorMessage error : modelC.getErrorList()) {
                errorMsgs += error.toString() + "\n";
            }
        }
        
        return errorMsgs;
    }
    
    // Legacy method for backward compatibility
    public static String compileWithJSON(CodeModelC modelC, JSONObject jsonIn ) throws ModelException {
        int code = 2000;
        //����C���Ե�������CodeModelC

        modelC.setSolver(Solver.ode4);

        modelC.generate();

        System.out.println();

        String errorMsgs = "";
        if (modelC.getErrorList().isEmpty()) {
            if (modelC.makeExeFile()) {
                modelC.saveToDatabase();
                errorMsgs += "make exe success.";
            }else{
                errorMsgs += "make exe failed.";
            }
        } else {
            for (ErrorMessage em : modelC.getErrorList()) {
                errorMsgs += em.getMessage();
            }
            code = 400;
        }
        return errorMsgs;
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Enhanced with DTO support for Phase 3 migration
        String pathInfo = request.getPathInfo();
        
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
        	System.out.println("Compile servlet: Processing request with ObjectMapper-enhanced approach");
        	
			String platform = "raspberry";
			if(pathInfo != null && !pathInfo.equals("/")){
				String[] pathParts = pathInfo.split("/");
				if(pathParts.length >= 2)
					platform = pathParts[1];
			}
			
			// Validate JSON structure first using ObjectMapper
			String validationError = JsonUtils.validateJsonStructure(jsonString);
			if (validationError != null) {
				throw new ModelException("JSON validation failed: " + validationError);
			}
			
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
			
			// Create model using DTO factory method
			CodeModelC modelC = CodeModelCFactory.createInstanceFromDto(platform, modelDto, ModelMode.Compilation);
			
			if (modelC == null) {
				throw new ModelException("Failed to create compilation model from DTO");
			}
			
			System.out.println("Model created successfully: " + modelC.getModelName() + 
			                   " for platform: " + platform + 
			                   " with " + modelC.getBlockList().size() + " blocks");
			
			errorMsgs = compileWithDtoSupport(modelC, jsonString);
        }
        catch(ModelException e) {
        	System.err.println(e.getMessage());
        	System.err.println("Code generatrion terminated unsuccessfully������");
        	//response.getWriter().write("{\"code\":\"400\",\"message\":\""+e.getMessage()+"\"}");
        	// Use ObjectMapper directly for error response
        	ServerResponseDto errorResponse = ServerResponseDto.createError(e.getMessage(), "compile");
        	errorResponse.setCode(400);
        	
        	try {
        		String jsonResponse = JsonUtils.getObjectMapper().writeValueAsString(errorResponse);
        		response.getWriter().write(jsonResponse);
        	} catch (JsonProcessingException jsonE) {
        		log.error("Failed to serialize error response: {}", jsonE.getMessage());
        		response.getWriter().write("{\"error\":\"Internal server error\"}");
        	}
        }

		catch (InvalidParameterException e){
			System.err.println(e.getMessage());
			System.err.println("Code generation terminated unsuccessfully������");
			//response.getWriter().write("{\"code\":\"400\",\"message\":\""+e.getMessage()+"\"}");
			errorMsgs += e.getMessage();
			code = 400;
		}
		finally {
			// Use ObjectMapper directly for final response
			ServerResponseDto finalResponse;
			if (code == 2000) {
				finalResponse = ServerResponseDto.createSuccess(null, errorMsgs, "compile");
			} else {
				finalResponse = ServerResponseDto.createError(errorMsgs, "compile");
			}
			finalResponse.setCode(code);
			finalResponse.setMessage(errorMsgs);
			
			try {
				String jsonResponse = JsonUtils.getObjectMapper().writeValueAsString(finalResponse);
				response.getWriter().write(jsonResponse);
			} catch (JsonProcessingException jsonE) {
				log.error("Failed to serialize final response: {}", jsonE.getMessage());
				response.getWriter().write("{\"error\":\"Internal server error\"}");
			}
		}

	}

}
