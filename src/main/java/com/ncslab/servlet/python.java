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

import com.ncslab.dto.ServerResponseJson;
import com.ncslab.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.code.m.CodeModelM;
import com.ncslab.code.m.CodeOctaveM;
import com.ncslab.server.mfcalcServer.MfcalcServer;
import com.ncslab.server.mfcalcServer.MfcalcThread;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.server.octaveserver.OctaveServer;
import com.ncslab.server.octaveserver.OctaveThread;
import com.ncslab.server.pythonServer.PythonServer;
import com.ncslab.server.pythonServer.PythonThread;

/**
 * Servlet implementation class octave
 */
@Slf4j
@WebServlet("/python")
public class python extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public python() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());

		System.out.println("python");
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

    	System.out.println(model.getMainCode());
//    	System.out.println(jsonIn);
//    	System.out.println(jsonIn.getString("data"));

    	try {
			PythonThread thread=PythonServer.instance.getVacantOctaveThread();
			System.out.println(thread);

			ServerResponseJson responseDto;
			
			if(thread!=null) {
				thread.startOctave(model);
				
				// Create result data using Map instead of JSONObject
				Map<String, Object> resultData = new HashMap<>();
				resultData.put("log", model.getOutputResult());
				resultData.put("BeginFigFileIndex", model.OutputFigBeginIndex);
				resultData.put("EndFigFileIndex", model.OutputFigEndIndex);
				resultData.put("figFileUrl", "/pythoncode/figure");
				resultData.put("dataFileUrl", "/pythoncode");
				resultData.put("mat", model.getOutputMat());
				
				// Create success response using DTO
				responseDto = ServerResponseJson.builder()
						.status("success")
						.message("SUCCESS")
						.code(2000)
						.serverType("python")
						.result(resultData)
						.executionTime(System.currentTimeMillis())
						.build();
				
				System.out.println("Python execution successful");
			}
			else {
				System.out.println("No server available...");
				responseDto = ServerResponseJson.createError("No server available...", "python");
				responseDto.setCode(503); // Service Unavailable
			}

			// Use JsonUtils for serialization
			String jsonResponse = JsonUtils.serializeDto(responseDto);
			response.getWriter().append(jsonResponse);
			
		} catch (JSONException e) {
			log.error("JSON processing error in python servlet: {}", e.getMessage());
			// Create error response using DTO
			ServerResponseJson errorResponse = ServerResponseJson.createError(
				"JSON processing error: " + e.getMessage(), "python");
			errorResponse.setCode(400);
			
			String errorJson = JsonUtils.serializeDto(errorResponse);
			response.getWriter().append(errorJson);
		} catch (Exception e) {
			log.error("Unexpected error in python servlet: {}", e.getMessage());
			// Create generic error response
			ServerResponseJson errorResponse = ServerResponseJson.createError(
				"Internal server error", "python");
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
