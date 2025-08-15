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
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.server.octaveserver.OctaveServer;
import com.ncslab.server.octaveserver.OctaveThread;

/**
 * Servlet implementation class octave
 */
@Slf4j
@WebServlet("/octave")
public class octave extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public octave() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());

		System.out.println("octave");
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
			OctaveThread thread=OctaveServer.instance.getVacantOctaveThread();
			System.out.println(thread);

			ServerResponseJson responseDto;
			
			if(thread!=null) {
				thread.startOctave(model);
				
				// Create result data using Map instead of JSONObject
				Map<String, Object> resultData = new HashMap<>();
				resultData.put("log", model.getOutputResult());
				resultData.put("BeginFigFileIndex", model.OutputFigBeginIndex);
				resultData.put("EndFigFileIndex", model.OutputFigEndIndex);
				resultData.put("figFileUrl", "/octavecode/figure");
				resultData.put("dataFileUrl", "/octavecode");
				resultData.put("mat", model.getOutputMat());
				
				// Create success response using DTO
				responseDto = ServerResponseJson.builder()
						.status("success")
						.message("SUCCESS")
						.code(2000)
						.serverType("octave")
						.result(resultData)
						.executionTime(System.currentTimeMillis())
						.build();
				
				System.out.println("Octave execution successful");
			}
			else {
				System.out.println("No server available...");
				responseDto = ServerResponseJson.createError("No server available...", "octave");
				responseDto.setCode(503); // Service Unavailable
			}

			// Use JsonUtils for serialization
			String jsonResponse = JsonUtils.serializeDto(responseDto);
			response.getWriter().append(jsonResponse);
			
		} catch (JSONException e) {
			log.error("JSON processing error in octave servlet: {}", e.getMessage());
			// Create error response using DTO
			ServerResponseJson errorResponse = ServerResponseJson.createError(
				"JSON processing error: " + e.getMessage(), "octave");
			errorResponse.setCode(400);
			
			String errorJson = JsonUtils.serializeDto(errorResponse);
			response.getWriter().append(errorJson);
		} catch (Exception e) {
			log.error("Unexpected error in octave servlet: {}", e.getMessage());
			// Create generic error response
			ServerResponseJson errorResponse = ServerResponseJson.createError(
				"Internal server error", "octave");
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
