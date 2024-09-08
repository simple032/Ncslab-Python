package com.ncslab.servlet;

import com.ncslab.code.Solver;
//import com.ncslab.code.plc.*;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Servlet implementation class compile
 */
@WebServlet("/explain/*")
public class explain extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public explain() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		// TODO Auto-generated method stub
//
//
//		//response.getWriter().append("Served at: ").append(request.getContextPath());
//		String pathInfo = request.getPathInfo();
//
//		//��ȡPost��JSON����
//		InputStreamReader insr = new InputStreamReader(request.getInputStream(),"utf-8");
//        String result = "";
//        int respInt = insr.read();
//        while(respInt!=-1) {
//            result +=(char)respInt;
//            respInt = insr.read();
//        }
//        JSONObject jsonIn = new JSONObject(result);
//        int code = 2000;
//        String errorMsgs="";
//        try {
//
//        	//����C���Ե�������CodeModelC
//			CodeModelPLC modelPLC = CodeModelPLC.createFromJSON(jsonIn, ModelMode.Simulation);
//
//        	//CodeModelCLinuxPC modelC=CodeModelCLinuxPC.createFromJSON(jsonIn,ModelMode.Compilation);
//        	modelPLC.setSolver(Solver.ode4);
//
//        	modelPLC.generate();
//
//        	System.out.println();
//
//        	if(modelPLC.getErrorList().size()==0) {
//        		// modelPLC.saveToDatabase();
//        		errorMsgs += "make plc success.";
//        	}else {
//        		for(ErrorMessage em: modelPLC.getErrorList()) {
//        			errorMsgs += em.getMessage();
//        		}
//        		code = 400;
//        		throw new ModelException(errorMsgs);
//        	}
//
//        	response.getWriter().write("{\"code\":"+code+","
//	        	+"\"msg\":"+"\""+errorMsgs+"\""
//	        	+"}");
//        }
//        catch(ModelException e) {
//        	System.err.println(e.getMessage());
//        	System.err.println("Code generatrion terminated unsuccessfully������");
//        	//response.getWriter().write("{\"code\":\"400\",\"message\":\""+e.getMessage()+"\"}");
//        	JSONObject jb=new JSONObject();
//        	jb.put("code", 400);
//        	jb.put("message",e.getMessage());
//        	response.getWriter().write(jb.toString());
//        }


	}

}
