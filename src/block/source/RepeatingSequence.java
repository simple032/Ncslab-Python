package block.source;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import block.io.OutputPort;
import block.io.Parameter;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class RepeatingSequence extends block.Block {
	 private double[] rep_t;
	 private double[] rep_y;
	public RepeatingSequence(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON, model);
		//构建一个输出
		outputPortList.add(new OutputPort(this,1,false));
		parseNum();
		
	}
	private void parseNum( ) {
		String time_value = paramValues.getString("rep_seq_t");
		String output_value = paramValues.getString("rep_seq_y");
		String regEx = "[' ']+"; // 一个或多个空格  
		Pattern p = Pattern.compile(regEx);  
		Matcher m = p.matcher(time_value);
		JSONArray t_Array=new JSONArray(m.replaceAll(",").trim());
		
		m=p.matcher(output_value);
		JSONArray y_Array=new JSONArray(m.replaceAll(",").trim());
		rep_t=new double[t_Array.length()];
		for(int i=0;i<t_Array.length();i++) {
			rep_t[i]=t_Array.getDouble(i);
		}
		
		rep_y=new double[y_Array.length()];
		for(int i=0;i<y_Array.length();i++) {
			rep_y[i]=y_Array.getDouble(i);
		}
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+rep_y[0]+"+("+rep_y[1]+"-"+rep_y[0]+")/("+rep_t[1]+"-"+rep_t[0]+")*mod(t+offset,"+rep_t[1]+"-"+rep_t[0]+");\n";
		//outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=req_res(t+offset,"+rep_t[0]+","+rep_t[1]+","+rep_y[0]+","+rep_y[1]+");\n";
		code.addOutputCode(outputCode);
	}
}
