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
import com.ncslab.dto.communication.AndroidResponseDto;
import com.ncslab.util.JsonUtils;
import com.ncslab.code.Solver;
import lombok.extern.slf4j.Slf4j;

import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;


/**
 * Servlet implementation class compile
 */
@WebServlet("/api/v1/ncslab/api/task/cmp")
@Slf4j
public class android_compile extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public android_compile() {
        super();
    }

    // Enhanced compilation method with DTO support
    public static AndroidResponseDto compileWithDtoSupport(CodeModelC modelC, String userName, String modelName) throws ModelException {
        System.out.println("Compiling with DTO-enhanced approach");

        modelC.setSolver(Solver.ode4);
        modelC.generate();
        System.out.println();

        String errorMsgs = "";
        if (modelC.getErrorList().isEmpty()) {
            if (modelC.makeExeFile()) {
                modelC.saveToDatabase();

                // Construct binary file URL: files\{userName}\{modelName}.bin
                // String binaryFileUrl = "files\\" + userName + "\\" + modelName + ".bin";
				String binaryFileUrl = "/CCode/" + "0" + "/" + "0" + "/" +  "ncslab.exe";

                return AndroidResponseDto.compilationSuccess(modelName, binaryFileUrl);
            } else {
                errorMsgs = "make exe failed.";
                return AndroidResponseDto.compilationError(modelName, errorMsgs);
            }
        } else {
            for (ErrorMessage error : modelC.getErrorList()) {
                errorMsgs += error.toString() + "\n";
            }
            return AndroidResponseDto.compilationError(modelName, errorMsgs);
        }
    }
    
    // Legacy method for backward compatibility
    public static String compileWithJSON(CodeModelC modelC, JSONObject jsonIn ) throws ModelException {
        int code = 2000;
        //����C���Ե�������CodeModelC

        modelC.setSolver(Solver.ode4);

		modelC.preBuild();

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
        
        String modelName = null;
        String userName = null;
        AndroidResponseDto responseDto = null;
        
        try {
        	System.out.println("Compile servlet: Processing request with ObjectMapper-enhanced approach");
        	
			String platform = "android";
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
			
			modelName = modelDto.getModelName();
			userName = modelDto.getUserName();
			System.out.println("Using DTO-based model creation for: " + modelName + " (user: " + userName + ")");

			// Create model using DTO factory method
			CodeModelC modelC = CodeModelCFactory.createInstanceFromDto(platform, modelDto, ModelMode.Compilation);

			if (modelC == null) {
				throw new ModelException("Failed to create compilation model from DTO");
			}

			System.out.println("Model created successfully: " + modelC.getModelName() +
			                   " for platform: " + platform +
			                   " with " + modelC.getBlockList().size() + " blocks");

			responseDto = compileWithDtoSupport(modelC, userName, modelName);

			System.out.println("Sending response:" + responseDto);
        }
        catch(ModelException e) {
        	System.err.println(e.getMessage());
        	System.err.println("Code generation terminated unsuccessfully");

        	// Create Android-specific error response with code 4000
        	responseDto = AndroidResponseDto.compilationError(modelName, e.getMessage());
        }

		catch (InvalidParameterException e){
			System.err.println(e.getMessage());
			System.err.println("Code generation terminated unsuccessfully");

			// Create Android-specific error response with code 4000
			responseDto = AndroidResponseDto.compilationError(modelName, e.getMessage());
		}
		finally {
			// Write the response if we have one
			if (responseDto != null) {
				try {
					String jsonResponse = JsonUtils.getObjectMapper().writeValueAsString(responseDto);
					response.getWriter().write(jsonResponse);
				} catch (JsonProcessingException jsonE) {
					log.error("Failed to serialize Android response: {}", jsonE.getMessage());
					response.getWriter().write("{\"error\":\"Internal server error\"}");
				}
			}
		}

	}

}
