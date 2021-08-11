package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import java.io.*;

import code.c.CodeModelC;
import code.m.CodeModelM;

/**
 * Servlet implementation class simulate
 */
@WebServlet("/simulate")
public class simulate extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public simulate() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		
		//∂¡»°PostµƒJSON≈‰÷√
		InputStreamReader insr = new InputStreamReader(request.getInputStream(),"utf-8");
        String result = "";
        int respInt = insr.read();
        while(respInt!=-1) {
            result +=(char)respInt;
            respInt = insr.read();
        }  
        JSONObject jsonIn = new JSONObject(result);
		
        CodeModelM model=CodeModelM.createFromJSON(jsonIn);
        model.generate();
        model.showErrorMessages();
        
        System.out.println();
        
        if(model.getErrorList().size()==0) {
        	System.out.println(model.getCode());
        }
        
        
        System.out.println();
        
        CodeModelC modelC=CodeModelC.createFromJSON(jsonIn);
        modelC.generate();
        
        System.out.println();
        
        if(modelC.getErrorList().size()==0) {
        	modelC.makeExeFile();
        }
        
	}

}
