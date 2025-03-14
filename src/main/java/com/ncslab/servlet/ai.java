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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

			JSONObject jb=new JSONObject();
			jb.put("code", code);
			jb.put("properties", properties);
			jb.put("message", errorMsgs);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(jb);
            System.out.println(jb);
		}

	}

}
