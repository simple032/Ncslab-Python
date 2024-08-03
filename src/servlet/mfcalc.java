package servlet;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import code.m.CodeModelM;
import code.m.CodeOctaveM;
import mfcalcServer.MfcalcServer;
import mfcalcServer.MfcalcThread;
import ncslablink.ModelMode;
import octaveserver.OctaveServer;
import octaveserver.OctaveThread;

/**
 * Servlet implementation class octave
 */
@WebServlet("/mfcalc")
public class mfcalc extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public mfcalc() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		
		System.out.println("mfcalc");
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
    	model.mainCode = jsonIn.getString("data");

    	System.out.println(model.mainCode);
//    	System.out.println(jsonIn);
//    	System.out.println(jsonIn.getString("data"));
    	
    	try {
			JSONObject jb=new JSONObject();
			jb.put("code", 2000);
			
			MfcalcThread thread=MfcalcServer.instance.getVacantOctaveThread();
			System.out.println(thread);
			
			if(thread!=null) {
				thread.startOctave(model);
				jb.put("message", "SUCCESS");
				JSONObject data=new JSONObject();
//				data.put("log", "/home/pi/Prj/octave/mylog.txt");
				data.put("log", model.OutputResult);
				data.put("BeginFigFileIndex", model.OutputFigBeginIndex);//"/home/pi/Prj/octave/"+
				data.put("EndFigFileIndex", model.OutputFigEndIndex);//"/home/pi/Prj/octave/"+
				data.put("figFileUrl", "/mfcalccode/figure");//"/home/pi/NetConTop/NCSLabLink/octavecode/"
				data.put("dataFileUrl", "/mfcalccode");
				data.put("mat", model.OutputMat);
//    		data.put("figFileUrl", "/MCode/"+model.getUserId()+"/"+model.getModelId()+"/scope");
				jb.put("data", data);
				System.out.println(jb);
			}
			else {
				System.out.println("No server available...");
				jb.put("message", "No server available...");
			}
			
			response.getWriter().append(jb.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
