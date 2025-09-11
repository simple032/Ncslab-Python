package com.ncslab.servlet;

import com.ncslab.block.BlockType;
import com.ncslab.code.Solver;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeModelCFactory;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import com.ncslab.dto.communication.ServerResponseDto;
import com.ncslab.util.JsonUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;


/**
 * Servlet implementation class compile
 */
@Slf4j
@WebServlet("/ai/*")
public class ai extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ai() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
        System.out.println(request.getRequestURI());
        String pathInfo = request.getPathInfo();
		//��ȡPost��JSON����
        int code = 2000;
        String errorMsgs="";
        JSONArray properties = null;
        try {
			String platform = "properties";
			if(pathInfo != null && !pathInfo.equals("/")){
				String[] pathParts = pathInfo.split("/");
				if(pathParts.length >= 2)
					platform = pathParts[1];
			}
            // Modern approach to read request body
            String result;
            try (InputStream is = request.getInputStream()) {
                result = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
            System.out.println(result);
            JSONArray blockTypes = new JSONArray(result);
            properties = BlockType.getBlockProperties(blockTypes);
        }
		catch (NoSuchMethodException|InvocationTargetException|
               IllegalAccessException|InvalidParameterException e){
			System.err.println(e.getMessage());
			System.err.println("Code generation terminated unsuccessfully������");
			//response.getWriter().write("{\"code\":\"400\",\"message\":\""+e.getMessage()+"\"}");
			errorMsgs += e.getMessage();
			code = 400;
		}
        catch (Exception e) {
            log.error("Error:", e);
        }
		finally {
			// Enhanced response with DTO pattern
			ServerResponseDto responseDto = null;
			if (code == 2000) {
				responseDto = ServerResponseDto.createSuccess(properties, null, "ai");
			} else {
				responseDto = ServerResponseDto.createError(errorMsgs, "ai");
			}
			responseDto.setCode(code);

			// Use direct DTO serialization instead of legacy JSON conversion
			String jsonResponse = JsonUtils.serializeDto(responseDto);
			
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(jsonResponse);
            System.out.println("AI servlet response: " + jsonResponse);
		}

	}

}
