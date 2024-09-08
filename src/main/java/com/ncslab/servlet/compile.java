package com.ncslab.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ncslab.code.CodeModel;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeModelCFactory;
import com.ncslab.code.c.CodeStructC;
import org.json.JSONObject;

import com.ncslab.code.Solver;

import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;


/**
 * Servlet implementation class compile
 */
@WebServlet("/compile/*")
public class compile extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public compile() {
        super();
    }

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
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
        String pathInfo = request.getPathInfo();
		//��ȡPost��JSON����
		InputStreamReader insr = new InputStreamReader(request.getInputStream(),"utf-8");
        String result = "";
        int respInt = insr.read();
        while(respInt!=-1) {
            result +=(char)respInt;
            respInt = insr.read();
        }
        JSONObject jsonIn = new JSONObject(result);
        int code = 2000;
        String errorMsgs="";
        try {
			String platform = "raspberry";
			if(pathInfo != null && !pathInfo.equals("/")){
				String[] pathParts = pathInfo.split("/");
				if(pathParts.length >= 2)
					platform = pathParts[1];
			}
			CodeModelC modelC = CodeModelCFactory.createInstance(platform, jsonIn, ModelMode.Compilation);
			errorMsgs = compileWithJSON(modelC, jsonIn);
        }
        catch(ModelException e) {
        	System.err.println(e.getMessage());
        	System.err.println("Code generatrion terminated unsuccessfully������");
        	//response.getWriter().write("{\"code\":\"400\",\"message\":\""+e.getMessage()+"\"}");
        	JSONObject jb=new JSONObject();
        	jb.put("code", 400);
        	jb.put("message",e.getMessage());
        	response.getWriter().write(jb.toString());
        }

		catch (InvalidParameterException e){
			System.err.println(e.getMessage());
			System.err.println("Code generation terminated unsuccessfully������");
			//response.getWriter().write("{\"code\":\"400\",\"message\":\""+e.getMessage()+"\"}");
			errorMsgs += e.getMessage();
			code = 400;
		}
		finally {
			JSONObject jb=new JSONObject();
			jb.put("code", code);
			jb.put("msg", errorMsgs);
			jb.put("message", errorMsgs);
			response.getWriter().write(jb.toString());
		}

	}

}
