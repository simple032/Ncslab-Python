package com.ncslab.servlet;

import java.io.IOException;
import java.io.InputStreamReader;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.ncslab.dto.communication.ServerResponseDto;
import com.ncslab.dto.communication.MfcalcResponseDto;
import com.ncslab.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.ncslab.code.m.CodeModelM;
import com.ncslab.code.m.CodeOctaveM;
import com.ncslab.code.m.MfcalcClient;
import com.ncslab.code.m.MfcalcClientManager;
import com.ncslab.server.mfcalcServer.MfcalcServer;
import com.ncslab.server.mfcalcServer.MfcalcThread;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.server.octaveserver.OctaveServer;
import com.ncslab.server.octaveserver.OctaveThread;

/**
 * Servlet implementation class octave
 */
@Slf4j
@WebServlet("/mfcalc")
public class mfcalc extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public mfcalc() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());

		System.out.println("mfcalc");
		//��ȡPost��JSON����
		InputStreamReader insr = new InputStreamReader(request.getInputStream(),"utf-8");
        String result = "";
        int respInt = insr.read();
        while(respInt!=-1) {
            result +=(char)respInt;
            respInt = insr.read();
        }
        JSONObject jsonIn = new JSONObject(result);

        CodeOctaveM model = new CodeOctaveM();
//    	model.mainCode = jsonIn.getJSONObject("data").toString();//result;
    	model.setMainCode(jsonIn.getString("data"));		
		model.setUserId(jsonIn.getInt("userId"));
		String method = jsonIn.optString("method", "runScript");

    	System.out.println(model.getMainCode());
//    	System.out.println(jsonIn);
//    	System.out.println(jsonIn.getString("data"));

    	try {
			MfcalcClient client = MfcalcClientManager.getClientForUser(String.valueOf(model.getUserId()));

			ServerResponseDto responseDto;
			
			if(client != null){
				String message = "SUCCESS";
				boolean operationSuccess = true;
				
				switch (method) {
				case "runScript":
					MfcalcResponseDto scriptResponse = client.runScript(model.getMainCode()+"\n");
					System.out.println(scriptResponse);
					
					// Check if the response indicates an error
					if (scriptResponse == null || scriptResponse.isError()) {
						String errorMsg = scriptResponse != null ? scriptResponse.getErrorInfo() : "Null response from MFCalc server";
						log.error("MFCalc runScript error: {}", errorMsg);
						message = errorMsg;
						operationSuccess = false;
					} else {
						if (scriptResponse.getData() != null) {
							// Handle data object - could be JSONObject from legacy response
							Object data = scriptResponse.getData();
							if (data instanceof JSONObject) {
								JSONObject jo = (JSONObject) data;
								model.setOutputResult(jo.optString("log",""));
								model.setFigureResult(jo.optJSONObject("figures"));
							}
						}
						if (scriptResponse.getOutput() != null) {
							model.setOutputResult(scriptResponse.getOutput());
						} else if (scriptResponse.getOutputLog() != null) {
							// Fallback to outputLog if output is not available
							model.setOutputResult(scriptResponse.getOutputLog());
						}
					}
					// Note: Missing break; in original code - maintaining the same behavior
				case "getVariables":	
					MfcalcResponseDto variablesResponse = client.getVariables();
					System.out.println(variablesResponse);
					
					// Check if the response indicates an error
					if (variablesResponse == null || variablesResponse.isError()) {
						String errorMsg = variablesResponse != null ? variablesResponse.getErrorInfo() : "Null response from MFCalc server";
						log.error("MFCalc getVariables error: {}", errorMsg);
						// Only update the status if we haven't already set it to failed
						if (operationSuccess) {
							message = errorMsg;
							operationSuccess = false;
						}
					} else if (variablesResponse.getData() != null) {
						// Convert variables data to proper JSON format
						String jsonData = JsonUtils.toJson(variablesResponse.getData());
						model.setOutputMat(jsonData != null ? jsonData : "[]");
					}
					break;
				default:
					message = "Unknown method: " + method;
					operationSuccess = false;
					break;
				}

				if (operationSuccess) {
					// Create result data using Map instead of JSONObject
					Map<String, Object> resultData = new HashMap<>();
					resultData.put("log", model.getOutputResult());
					if(model.getFigureResult() != null) {
						// Convert JSONObject to Map for Jackson serialization
						resultData.put("figures", model.getFigureResult().toMap());
					}
					resultData.put("BeginFigFileIndex", model.OutputFigBeginIndex);
					resultData.put("EndFigFileIndex", model.OutputFigEndIndex);
					resultData.put("figFileUrl", "/mfcalccode/figure");
					resultData.put("dataFileUrl", "/mfcalccode");
					resultData.put("mat", model.getOutputMat());
					
					// Create success response using DTO
					responseDto = ServerResponseDto.builder()
							.status("success")
							.message(message)
							.code(2000)
							.serverType("mfcalc")
							.result(resultData)
							.executionTime(System.currentTimeMillis())
							.build();
					
					System.out.println("MFCalc execution successful");
				} else {
					// Create error response for unknown method
					responseDto = ServerResponseDto.createError(message, "mfcalc");
					responseDto.setCode(400); // Bad Request
				}
			}else{
				System.out.println("No mfcalc server available...");
				responseDto = ServerResponseDto.createError("No mfcalc server available...", "mfcalc");
				responseDto.setCode(503); // Service Unavailable
			}

			// Use JsonUtils for serialization
			String jsonResponse = JsonUtils.serializeDto(responseDto);
			response.getWriter().append(jsonResponse);
			
		} catch (JSONException e) {
			log.error("JSON processing error in mfcalc servlet: {}", e.getMessage());
			// Create error response using DTO
			ServerResponseDto errorResponse = ServerResponseDto.createError(
				"JSON processing error: " + e.getMessage(), "mfcalc");
			errorResponse.setCode(400);
			
			String errorJson = JsonUtils.serializeDto(errorResponse);
			response.getWriter().append(errorJson);
		} catch (Exception e) {
			log.error("Unexpected error in mfcalc servlet: {}", e.getMessage());
			// Create generic error response
			ServerResponseDto errorResponse = ServerResponseDto.createError(
				"Internal server error", "mfcalc");
			errorResponse.setCode(500);
			
			String errorJson = JsonUtils.serializeDto(errorResponse);
			response.getWriter().append(errorJson);
		}


	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}


}
