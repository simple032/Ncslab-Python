package com.ncslab.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.ncslab.dto.communication.ServerResponseDto;
import com.ncslab.dto.communication.MfcalcResponseDto;
import com.ncslab.dto.communication.MfcalcServletRequestDto;
import com.ncslab.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

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
@WebServlet("/mfcalc")
public class mfcalc extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Helper method for logging
	private void logInfo(String message) {
		System.out.println("[MFCALC-INFO] " + new java.util.Date() + " - " + message);
	}

	private void logError(String message) {
		System.err.println("[MFCALC-ERROR] " + new java.util.Date() + " - " + message);
	}

	private void logError(String message, Throwable e) {
		System.err.println("[MFCALC-ERROR] " + new java.util.Date() + " - " + message);
		e.printStackTrace(System.err);
	}

	private void logDebug(String message) {
		System.out.println("[MFCALC-DEBUG] " + new java.util.Date() + " - " + message);
	}

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
		logInfo("=== MFCalc Servlet Request Started ===");
		logInfo("Request Method: " + request.getMethod());
		logInfo("Request URI: " + request.getRequestURI());
		logInfo("Content-Type: " + request.getContentType());

		// Set response content type
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String result = "";
        // 使用try-with-resources自动关闭资源
		try (InputStream is = request.getInputStream()) {
			// 直接从InputStream读取所有字节并转换为字符串
			result = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			logInfo("Received request body length: " + result.length() + " bytes");
			logDebug("Request body: " + (result.length() > 500 ? result.substring(0, 500) + "..." : result));
		} catch (IOException e) {
			logError("Error reading request input stream", e);
			ServerResponseDto errorResponse = ServerResponseDto.createError("Error reading request", "mfcalc");
			errorResponse.setCode(400);
			String errorJson = JsonUtils.serializeDto(errorResponse);
			response.getWriter().append(errorJson);
			response.getWriter().flush();
			logInfo("=== MFCalc Servlet Request Ended (Error Reading) ===");
			return;
		}
				
        // Parse JSON to DTO
        logInfo("Parsing request JSON to DTO...");
        MfcalcServletRequestDto requestDto = null;
        try {
            requestDto = JsonUtils.deserializeDto(result, MfcalcServletRequestDto.class);
            logInfo("Successfully parsed request DTO");
        } catch (Exception e) {
            logError("Failed to parse request JSON", e);
            ServerResponseDto errorResponse = ServerResponseDto.createError("Invalid JSON format: " + e.getMessage(), "mfcalc");
            errorResponse.setCode(400);
            String errorJson = JsonUtils.serializeDto(errorResponse);
            response.getWriter().append(errorJson);
            response.getWriter().flush();
            logInfo("=== MFCalc Servlet Request Ended (Parse Error) ===");
            return;
        }

        // Validate request
        if (requestDto == null || !requestDto.isValid()) {
            String errorMsg = requestDto != null ? requestDto.getValidationError() : "Invalid JSON request format";
            logError("Invalid mfcalc request: " + errorMsg);
            ServerResponseDto errorResponse = ServerResponseDto.createError(errorMsg, "mfcalc");
            errorResponse.setCode(400);
            String errorJson = JsonUtils.serializeDto(errorResponse);
            response.getWriter().append(errorJson);
            response.getWriter().flush();
            logInfo("=== MFCalc Servlet Request Ended (Validation Error) ===");
            return;
        }

        logInfo("Request DTO validated - userId: " + requestDto.getUserId() + ", method: " + requestDto.getMethodOrDefault() +
                ", data length: " + (requestDto.getData() != null ? requestDto.getData().length() : 0));

        logInfo("Creating CodeOctaveM model...");
        CodeOctaveM model = new CodeOctaveM();
    	model.setMainCode(requestDto.getData());
		model.setUserId(requestDto.getUserId());
		String method = requestDto.getMethodOrDefault();

    	logDebug("Model main code length: " + (model.getMainCode() != null ? model.getMainCode().length() : 0));
    	logInfo("Method to execute: " + method);

    	try {
    		logInfo("Getting MFCalc client for user: " + model.getUserId());
    		System.out.flush();
			MfcalcClient client = MfcalcClientManager.getClientForUser(String.valueOf(model.getUserId()));
			logInfo("MFCalc client retrieved");

			ServerResponseDto responseDto;

			if(client != null){
				logInfo("MFCalc client obtained successfully");
				System.out.flush();
				String message = "SUCCESS";
				boolean operationSuccess = true;

				switch (method) {
				case "runScript":
					logInfo("Executing runScript...");
					System.out.flush();
					logInfo("About to call client.runScript()");
					System.out.flush();

					MfcalcResponseDto scriptResponse = client.runScript(model.getMainCode());

					logInfo("client.runScript() returned");
					System.out.flush();
					logInfo("runScript response received - status: " + (scriptResponse != null ? scriptResponse.getStatus() : "null"));

					// Check if the response indicates an error
					if (scriptResponse.isError()) {
						String errorMsg = scriptResponse.getErrorInfo();
						logError("MFCalc runScript error: " + errorMsg);
						message = errorMsg;
						operationSuccess = false;
					} else {
						// Handle output in priority order: output > outputLog (log) > empty
						if (scriptResponse.getOutput() != null) {
							model.setOutputResult(scriptResponse.getOutput());
						} else if (scriptResponse.getOutputLog() != null) {
							// Fallback to outputLog (mapped from "log" field) if output is not available
							model.setOutputResult(scriptResponse.getOutputLog());
						}

						// Handle figures data if present
						if (scriptResponse.getFigures() != null) {
							model.setFigureResult(scriptResponse.getFigures());
						}

						// Handle app data if present
						if (scriptResponse.getApp() != null) {
							model.setApp(scriptResponse.getApp());
						}
					}
					break;
					// Note: Missing break; in original code - maintaining the same behavior
				case "getVariables":
					logInfo("Executing getVariables...");
					MfcalcResponseDto variablesResponse = client.getVariables();
					logInfo("getVariables response received - status: " + (variablesResponse != null ? variablesResponse.getStatus() : "null"));
					
					// Check if the response indicates an error
					if (variablesResponse == null || variablesResponse.isError()) {
						String errorMsg = variablesResponse != null ? variablesResponse.getErrorInfo() : "Null response from MFCalc server";
						logError("MFCalc getVariables error: " + errorMsg);
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
					logInfo("Building success response...");
					Map<String, Object> resultData = new HashMap<>();
					resultData.put("log", model.getOutputResult());
					if(model.getFigureResult() != null) {
						// FiguresData is already serializable by Jackson
						resultData.put("figures", model.getFigureResult());
						logDebug("Added figures to result");
					}
					if(model.getApp() != null) {
						resultData.put("app", model.getApp());
						logDebug("Added app to result");
					}
					if(model.getUiComponents() != null) {
						resultData.put("uiComponents", model.getUiComponents());
						logDebug("Added uiComponents to result");
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
					
					logInfo("MFCalc execution successful - building response complete");
				} else {
					logError("Operation failed: " + message);
					// Create error response for unknown method
					responseDto = ServerResponseDto.createError(message, "mfcalc");
					responseDto.setCode(400); // Bad Request
				}
			}else{
				logError("No mfcalc server available for user: " + model.getUserId());
				responseDto = ServerResponseDto.createError("No mfcalc server available...", "mfcalc");
				responseDto.setCode(503); // Service Unavailable
			}

			// Use JsonUtils for serialization
			logInfo("Serializing response to JSON...");
			String jsonResponse = JsonUtils.serializeDto(responseDto);
			logInfo("Response JSON length: " + (jsonResponse != null ? jsonResponse.length() : 0) + " bytes" + " bytes");
			logDebug("Response to frontend: " + (jsonResponse.length() > 500 ? jsonResponse.substring(0, 500) + "..." : jsonResponse));

			response.getWriter().append(jsonResponse);
			response.getWriter().flush();
			logInfo("Response sent to client successfully");
			logInfo("=== MFCalc Servlet Request Completed Successfully ===");
			
		} catch (JsonProcessingException e) {
			logError("JSON processing error in mfcalc servlet", e);
			// Create error response using DTO
			ServerResponseDto errorResponse = ServerResponseDto.createError(
				"JSON processing error: " + e.getMessage(), "mfcalc");
			errorResponse.setCode(400);

			String errorJson = JsonUtils.serializeDto(errorResponse);
			response.getWriter().append(errorJson);
			response.getWriter().flush();
			logInfo("=== MFCalc Servlet Request Ended (JSON Processing Error) ===");
		} catch (Exception e) {
			logError("Unexpected error in mfcalc servlet", e);
			e.printStackTrace(); // Print full stack trace for debugging

			// Create generic error response
			ServerResponseDto errorResponse = ServerResponseDto.createError(
				"Internal server error: " + e.getMessage(), "mfcalc");
			errorResponse.setCode(500);

			try {
				String errorJson = JsonUtils.serializeDto(errorResponse);
				response.getWriter().append(errorJson);
				response.getWriter().flush();
			} catch (Exception e2) {
				logError("Failed to send error response", e2);
			}
			logInfo("=== MFCalc Servlet Request Ended (Unexpected Error) ===");
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
