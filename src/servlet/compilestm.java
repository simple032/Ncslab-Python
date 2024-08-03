package servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import code.Solver;
import code.c.linux.raspberry.CodeModelCLinuxRaspberry;
import code.c.stm.CodeModelCStm32;
import code.c.linux.pc.CodeModelCLinuxPC;
import ncslablink.ErrorMessage;
import ncslablink.ModelException;
import ncslablink.ModelMode;

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
