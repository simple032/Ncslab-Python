package block.continuous;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;

public class TransferFcn extends Block {
	private double D=0;
	private boolean feedThrough=false;
	
	private double[] num;
	private double[] den;
	
	private Vector<State> xStateList=new Vector<State>();
	
	public TransferFcn(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		
		parseVector();
		
		for(int i=0;i<num.length;i++) {
			State xState=new State(this,i+1,"x"+(i+1));
			xStateList.add(xState);
			stateList.add(xState);		
		}
		
		//一个输入，一个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,feedThrough));
	}
	
	private void parseVector() {
		String numStr=paramValues.getString("Numerator");
		String denStr=paramValues.getString("Denominator");
		
		//System.out.println(numStr+denStr);
		
		String regEx = "[' ']+"; // 一个或多个空格  
		Pattern p = Pattern.compile(regEx);  
		Matcher m = p.matcher(numStr);
		JSONArray numArray=new JSONArray(m.replaceAll(",").trim());
		
		m=p.matcher(denStr);
		JSONArray denArray=new JSONArray(m.replaceAll(",").trim());
		
		num=new double[numArray.length()];
		for(int i=0;i<numArray.length();i++) {
			num[i]=numArray.getDouble(i);
		}
		
		den=new double[denArray.length()];
		for(int i=0;i<denArray.length();i++) {
			den[i]=denArray.getDouble(i);
		}
		
		//归一化
		double unit=den[0];
		for(int i=0;i<den.length;i++) {
			den[i]=den[i]/unit;
		}
		
		for(int i=0;i<num.length;i++) {
			num[i]=num[i]/unit;
		}
		
		//System.out.println(""+num+den);
		
		if(num.length==den.length) {
			feedThrough=true;
			D=num[0]/den[0];
			
			for(int i=0;i<num.length;i++) {
				num[i]=num[i]-D*den[i];
			}
			
			double[] numShort=new double[num.length-1];
			for(int i=0;i<num.length-1;i++) {
				numShort[i]=num[i+1];
			}
			
			num=numShort;
		}
		
		double[] denShort=new double[den.length-1];
		for(int i=0;i<den.length-1;i++) {
			denShort[i]=den[i+1];
		}
		den=denShort;
		
		double[] numShort=new double[den.length];
		for(int i=0;i<den.length;i++) {
			if(i<den.length-num.length) {
				numShort[i]=0;
			}
			else {
				numShort[i]=num[i-(den.length-num.length)];
			}
		}
		num=numShort;
		
	}
	
	public String generateInitCodeM() {
		String code=super.generateInitCodeM();
		for(State xState:xStateList) {
			code+=xState.getName()+
					"=0;\n";
		}
		return code;
	}
	
	public String generateOutputCodeM() {
		String code=getOutputPortVariable(0)+"=0";
		
		int i=num.length-1;
		for(State xState:xStateList) {
			code+="+"+xState.getName()+"*"+num[i];
			i--;
		}
		
		if(feedThrough) {
			code+="+"+D+"*"+getInputPortVariable(0);
		}
		
		code+=";\n";
		
		return code;
	}
	
	public String generateUpdateCodeM() {
		String code=super.generateUpdateCodeM();
		
		for(int i=0;i<xStateList.size()-1;i++) {
			code+=xStateList.get(i).getName()+"="
					+xStateList.get(i).getName()+"+"
					+xStateList.get(i+1).getName()+"*"+getModel().getConfig().getFixedStep()
					+";\n";
		}
		
		code+=xStateList.get(xStateList.size()-1).getName()+"="+xStateList.get(xStateList.size()-1).getName()+"+("+getInputPortVariable(0);
		int i=den.length-1;
		for(State xState:xStateList) {
			code+="-"+xState.getName()+"*"+den[i];
			i--;
		}
		code+=")*"+getModel().getConfig().getFixedStep()+";\n";
		
		return code;
	}
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Transfer Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		for(State xState:xStateList) {
			initCode+=xState.getName()+
					"=0;\n";
		}
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Transfer Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+=getOutputPortVariable(0)+"=0";
		int i=num.length-1;
		for(State xState:xStateList) {
			outputCode+="+"+xState.getName()+"*"+num[i];
			i--;
		}
		
		if(feedThrough) {
			outputCode+="+"+D+"*"+getInputPortVariable(0);
		}
		
		outputCode+=";\n";
		
		code.addOutputCode(outputCode);
	}
	
	public void generateUpdateCodeC(CodeStructC code) {
		super.generateUpdateCodeC(code);
		
		String updateCode="/*Code for update of block Transfer Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		for(int i=0;i<xStateList.size()-1;i++) {
			updateCode+=xStateList.get(i).getName()+"="
					+xStateList.get(i).getName()+"+"
					+xStateList.get(i+1).getName()+"*"+getModel().getConfig().getFixedStep()
					+";\n";
		}
		
		updateCode+=xStateList.get(xStateList.size()-1).getName()+"+=("+getInputPortVariable(0);
		int i=den.length-1;
		for(State xState:xStateList) {
			updateCode+="-"+xState.getName()+"*"+den[i];
			i--;
		}
		updateCode+=")*"+getModel().getConfig().getFixedStep()+";\n";
		
		code.addUpdateCode(updateCode); 
	}
}
