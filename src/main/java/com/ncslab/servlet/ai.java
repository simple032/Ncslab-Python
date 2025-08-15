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
import com.ncslab.dto.ServerResponseJson;
import com.ncslab.util.JsonUtils;

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
            InputStreamReader insr = new InputStreamReader(request.getInputStream(),"utf-8");
            String result = "";
            int respInt = insr.read();
            while(respInt!=-1) {
                result +=(char)respInt;
                respInt = insr.read();
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
			ServerResponseJson responseDto = null;
			if (code == 2000) {
				responseDto = ServerResponseJson.createSuccess(properties, null, "ai");
			} else {
				responseDto = ServerResponseJson.createError(errorMsgs, "ai");
			}
			responseDto.setCode(code);

			// Convert to legacy format for backward compatibility
			JSONObject jb = responseDto.toLegacyJson();
			jb.put("properties", properties); // Ensure properties field is included
			if (!jb.has("message")) {
				jb.put("message", errorMsgs);
			}
			
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(jb);
            System.out.println("AI servlet response: " + jb);
		}

	}

}
