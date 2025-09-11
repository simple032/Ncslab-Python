package com.ncslab.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.ncslab.code.Solver;
import com.ncslab.code.c.linux.raspberry.CodeModelCLinuxRaspberry;
import com.ncslab.code.c.stm.CodeModelCStm32;
import com.ncslab.code.c.linux.pc.CodeModelCLinuxPC;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;

/**
 * Servlet implementation class compile
 */
@WebServlet("/compilestm32")
public class compilestm extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public compilestm() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub


		//response.getWriter().append("Served at: ").append(request.getContextPath());


		// Modern approach to read request body
		String result;
		try (InputStream is = request.getInputStream()) {
			result = new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
        JSONObject jsonIn = new JSONObject(result);
        int code = 2000;
        String errorMsgs="";
        try {

        	//����C���Ե�������CodeModelC
        	CodeModelCStm32 modelC=CodeModelCStm32.createFromJSON(jsonIn,ModelMode.Compilation);
        	//CodeModelCLinuxPC modelC=CodeModelCLinuxPC.createFromJSON(jsonIn,ModelMode.Compilation);
        	System.out.println("jsonIn in compile is: "+jsonIn);

        	modelC.setSolver(Solver.ode4);

        	modelC.generate();

        	System.out.println();

        	if(modelC.getErrorList().size()==0) {
        		if(modelC.makeExeFile()) {
        			modelC.saveToDatabase();
        			errorMsgs += "make exe success.";
        		}
        	}else {
        		for(ErrorMessage em: modelC.getErrorList()) {
        			errorMsgs += em.getMessage();
        		}
        		code = 400;
        		throw new ModelException(errorMsgs);
        	}

        	response.getWriter().write("{\"code\":"+code+","
	        	+"\"msg\":"+"\""+errorMsgs+"\""
	        	+"}");
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

	}

}
