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
import lombok.extern.slf4j.Slf4j;

import com.ncslab.dto.communication.ServerResponseDto;
import com.ncslab.dto.communication.MfcalcResponseDto;
import com.ncslab.dto.communication.MfcalcServletRequestDto;
import com.ncslab.util.FileSystemApiClient;
import com.ncslab.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.ncslab.code.m.CodeOctaveM;
import com.ncslab.code.m.MfcalcClient;
import com.ncslab.code.m.MfcalcClientManager;
import com.ncslab.server.mfcalcServer.MfcalcServer;
import com.ncslab.server.mfcalcServer.MfcalcThread;

/**
 * Servlet implementation class octave
 */
@WebServlet("/mfcalc")
@Slf4j
public class mfcalc extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public mfcalc() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("=== MFCalc Servlet Request Started ===");
		log.debug("Request Method: {}", request.getMethod());
		log.debug("Request URI: {}", request.getRequestURI());
		log.debug("Content-Type: {}", request.getContentType());

		// Set response content type
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String result = "";
        // 使用try-with-resources自动关闭资源
		try (InputStream is = request.getInputStream()) {
			// 直接从InputStream读取所有字节并转换为字符串
			result = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			log.debug("Received request body length: {} bytes", result.length());
			log.debug("Request body: {}", result.length() > 500 ? result.substring(0, 500) + "..." : result);
		} catch (IOException e) {
			log.error("Error reading request input stream", e);
			ServerResponseDto errorResponse = ServerResponseDto.createError("Error reading request", "mfcalc");
			errorResponse.setCode(400);
			String errorJson = JsonUtils.serializeDto(errorResponse);
			response.getWriter().append(errorJson);
			response.getWriter().flush();
			log.debug("=== MFCalc Servlet Request Ended (Error Reading) ===");
			return;
		}

        // Parse JSON to DTO
        log.debug("Parsing request JSON to DTO...");
        MfcalcServletRequestDto requestDto = null;
        try {
            requestDto = JsonUtils.deserializeDto(result, MfcalcServletRequestDto.class);
            log.debug("Successfully parsed request DTO");
        } catch (Exception e) {
            log.error("Failed to parse request JSON", e);
            ServerResponseDto errorResponse = ServerResponseDto.createError("Invalid JSON format: " + e.getMessage(), "mfcalc");
            errorResponse.setCode(400);
            String errorJson = JsonUtils.serializeDto(errorResponse);
            response.getWriter().append(errorJson);
            response.getWriter().flush();
            log.debug("=== MFCalc Servlet Request Ended (Parse Error) ===");
            return;
        }

        // Validate request
        if (requestDto == null || !requestDto.isValid()) {
            String errorMsg = requestDto != null ? requestDto.getValidationError() : "Invalid JSON request format";
            log.error("Invalid mfcalc request: {}", errorMsg);
            ServerResponseDto errorResponse = ServerResponseDto.createError(errorMsg, "mfcalc");
            errorResponse.setCode(400);
            String errorJson = JsonUtils.serializeDto(errorResponse);
            response.getWriter().append(errorJson);
            response.getWriter().flush();
            log.debug("=== MFCalc Servlet Request Ended (Validation Error) ===");
            return;
        }

        log.debug("Request DTO validated - userId: {}, method: {}, data length: {}",
                requestDto.getUserId(),
                requestDto.getMethodOrDefault(),
                requestDto.getData() != null ? requestDto.getData().length() : 0);

        log.debug("Creating CodeOctaveM model...");
        CodeOctaveM model = new CodeOctaveM();
    	model.setMainCode(requestDto.getData());
		model.setUserId(requestDto.getUserId());
		String method = requestDto.getMethodOrDefault();

    	log.debug("Model main code length: {}", model.getMainCode() != null ? model.getMainCode().length() : 0);
    	log.debug("Method to execute: {}", method);

    	try {
    		log.info("Connecting to MFCalc server for user: {}", model.getUserId());
			String userId = String.valueOf(model.getUserId());

			ServerResponseDto responseDto;
			String message = "SUCCESS";
			boolean operationSuccess = true;

			// Use MfcalcClientManager for persistent connection per user
			MfcalcClient client = null;
			MfcalcResponseDto mfcalcResponse = null;

			try {
				// Get or create persistent client connection for this user
				client = MfcalcClientManager.getClientForUser(userId);
				if (client == null) {
					throw new IOException("Failed to get MFCalc client for user: " + userId);
				}

				switch (method) {

				case "runScript":
					log.info("Executing runScript via MfcalcClient...");

					// Execute the script on mfcalc.exe server
					mfcalcResponse = client.runScript(model.getMainCode());

					log.info("Script execution completed");

					if (mfcalcResponse != null) {
						// Always set the output log (contains detailed error info or normal output)
						model.setOutputResult(mfcalcResponse.getOutputLog());
						model.setFigureResult(mfcalcResponse.getFigures());

						// Check if there was an error status OR non-empty error message
						// Note: MFCalc server may return status:"success" but still have error messages
						String errorMsg = mfcalcResponse.getError();
						boolean hasError = "error".equals(mfcalcResponse.getStatus()) ||
								(errorMsg != null && !errorMsg.trim().isEmpty());

						if (hasError) {
							message = errorMsg != null ? errorMsg : "Script error";
							operationSuccess = false;
						}
					}
					break;

				case "getVariables":
					log.info("Executing getVariables via MfcalcClient...");

					// Execute getVariables on mfcalc.exe server
					mfcalcResponse = client.getVariables();

					log.info("getVariables completed");

					// Check if there was an error
					if (mfcalcResponse != null && "error".equals(mfcalcResponse.getStatus())) {
						message = mfcalcResponse.getError() != null ? mfcalcResponse.getError() : "Unknown error";
						operationSuccess = false;
					} else if (mfcalcResponse != null) {
						// Set variables result
						if (mfcalcResponse.getVariables() != null) {
							model.setOutputMat(JsonUtils.serializeDto(mfcalcResponse.getVariables()));
						} else {
							model.setOutputMat("[]");
						}
					}
					break;

				default:
					message = "Unknown method: " + method;
					operationSuccess = false;
					break;
				}

			} catch (Exception e) {
				log.error("Error executing {} via MfcalcClient: {}", method, e.getMessage(), e);
				message = "Connection error: " + e.getMessage();
				operationSuccess = false;
				// On connection error, remove the client from manager so it can reconnect next time
				MfcalcClientManager.getInstance().removeClientForUser(userId);
			}
			// Note: Do NOT close the client here - MfcalcClientManager maintains persistent connections

			// Build result data (include log even on error so client sees detailed message)
			log.info("Building response...");
			Map<String, Object> resultData = new HashMap<>();
			resultData.put("log", model.getOutputResult());
			if(model.getFigureResult() != null) {
				// FiguresData is already serializable by Jackson
				resultData.put("figures", model.getFigureResult());
				log.debug("Added figures to result");
			}
			if(model.getApp() != null) {
				resultData.put("app", model.getApp());
				log.debug("Added app to result");
			}
			if(model.getUiComponents() != null) {
				resultData.put("uiComponents", model.getUiComponents());
				log.debug("Added uiComponents to result");
			}
			resultData.put("BeginFigFileIndex", model.OutputFigBeginIndex);
			resultData.put("EndFigFileIndex", model.OutputFigEndIndex);
			resultData.put("figFileUrl", "/mfcalccode/figure");
			resultData.put("dataFileUrl", "/mfcalccode");
			resultData.put("mat", model.getOutputMat());

			if (operationSuccess) {
				// Create success response using DTO
				responseDto = ServerResponseDto.builder()
						.status("success")
						.message(message)
						.code(2000)
						.serverType("mfcalc")
						.result(resultData)
						.executionTime(System.currentTimeMillis())
						.build();

				log.info("MFCalc execution successful - building response complete");
			} else {
				log.error("Operation failed: {}", message);
				// Create error response but still include result data with log
				responseDto = ServerResponseDto.builder()
						.status("error")
						.message(message)
						.code(400)
						.serverType("mfcalc")
						.result(resultData)
						.executionTime(System.currentTimeMillis())
						.build();
			}

			// Use JsonUtils for serialization
			log.info("Serializing response to JSON...");
			String jsonResponse = JsonUtils.serializeDto(responseDto);
			log.debug("Response to frontend: {}", jsonResponse.length() > 500 ? jsonResponse.substring(0, 500) + "..." : jsonResponse);

			response.getWriter().append(jsonResponse);
			response.getWriter().flush();
			log.info("Response sent to client successfully");
			log.info("=== MFCalc Servlet Request Completed Successfully ===");

		} catch (JsonProcessingException e) {
			log.error("JSON processing error in mfcalc servlet", e);
			// Create error response using DTO
			ServerResponseDto errorResponse = ServerResponseDto.createError(
				"JSON processing error: " + e.getMessage(), "mfcalc");
			errorResponse.setCode(400);

			String errorJson = JsonUtils.serializeDto(errorResponse);
			response.getWriter().append(errorJson);
			response.getWriter().flush();
			log.info("=== MFCalc Servlet Request Ended (JSON Processing Error) ===");
		} catch (Exception e) {
			log.error("Unexpected error in mfcalc servlet", e);

			// Create generic error response
			ServerResponseDto errorResponse = ServerResponseDto.createError(
				"Internal server error: " + e.getMessage(), "mfcalc");
			errorResponse.setCode(500);

			try {
				String errorJson = JsonUtils.serializeDto(errorResponse);
				response.getWriter().append(errorJson);
				response.getWriter().flush();
			} catch (Exception e2) {
				log.error("Failed to send error response", e2);
			}
			log.info("=== MFCalc Servlet Request Ended (Unexpected Error) ===");
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
