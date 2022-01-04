package servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import code.Solver;
import code.c.CodeModelC;
import code.m.CodeModelM;

import ncslablink.SFcnException;
import ncslablink.ModelMode;

/**
 * Servlet implementation class compile
 */
@WebServlet("/function")
public class function extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public function() {
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
        
        try {
        	//����C���Ե�������CodeModelC
        	SFunction sfcn= new SFunction(jsonIn);
        	//modelC.setSolver(Solver.ode4);
        	sfcn.generateFile();
        	
        	response.getWriter().write("{\"code\":2000,\"message\":\"SUCCESS\"}");
        }
        catch(SFcnException e) {
        	System.err.println(e.getMessage());
        	System.err.println("Code generatrion terminated unsuccessfully������");
        	response.getWriter().write("{\"code\":400,\"message\":\""+e.getMessage()+"\"}");
        }
		
	}

}


class SFunction{
	
	private String codePathBase=utils.Property.instance.getProperty("CCodePath");
	
	private String name = "";
	private String body = "";
	private int userId = 0;
	private int modelId = 0;
	private String modelName = "";
	
	public SFunction(JSONObject jo) {
		name = jo.getString("functionName");
		body = jo.getString("functionBody");
		userId = jo.getInt("userId");
		modelId = jo.getInt("modelId");
		modelName = jo.getString("modelName");
	}
	
	public void generateFile() throws SFcnException {
		String filePath=codePathBase+"/"+userId;
		
		System.out.println("Writing file "+name+".c ...");

		File file=new File(filePath+"/"+name+".c");
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(outputStream);
			osw.write(body);
			osw.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SFcnException("Write S-Function "+name+" failed");
		}
		//TODO:Process the s-function file
		File linkfile=new File(filePath+"/"+name+".ncslink");
		//Inputport
		String numip = "ssSetNumInputPorts.*?;";
		body.regionMatches(0, numip, 0, 0);
	}
}

