package block.continuous;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.JSONArray;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;
public class StateSpace extends Block{
	//private double D=0;
	private boolean feedThrough=false;
    private double[]  para_A;
    private double[]  para_B;
    private double[]  para_C;
    private double para_D;
    private double[]  init_X;
    
    private Vector<State> xStateList=new Vector<State>();
    
	public StateSpace(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		parseVector();
		for(int i=0;i<Math.sqrt(para_A.length);i++) {
			State xState=new State(this,i+1,"x"+(i+1));
			xStateList.add(xState);
			stateList.add(xState);		
		}
		
		//一个输入，一个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,feedThrough));
	}
	private void parseVector() {
		String aStr=paramValues.getString("A");
		String bStr=paramValues.getString("B");
		String cStr=paramValues.getString("C");
		//String dStr=paramValues.getString("D");
		String xStr=paramValues.getString("X0");
		para_D=paramValues.getDouble("D");
		String regEx = "[' ']+|;";
		Pattern p = Pattern.compile(regEx);  
		Matcher m = p.matcher(aStr);
		JSONArray aArray=new JSONArray(m.replaceAll(",").trim());
		m=p.matcher(bStr);
		JSONArray bArray=new JSONArray(m.replaceAll(",").trim());
		m=p.matcher(cStr);
		JSONArray cArray=new JSONArray(m.replaceAll(",").trim());
		//m=p.matcher(dStr);
		//JSONArray dArray=new JSONArray(m.replaceAll(",").trim());
		m=p.matcher(xStr);
		JSONArray xArray=new JSONArray(m.replaceAll(",").trim());
		para_A=new double[aArray.length()];
		for(int i=0;i<aArray.length();i++) {
			para_A[i]=aArray.getDouble(i);
		}
		para_B=new double[bArray.length()];
		for(int i=0;i<bArray.length();i++) {
			para_B[i]=bArray.getDouble(i);
		}
		para_C=new double[cArray.length()];
		for(int i=0;i<cArray.length();i++) {
			para_C[i]=cArray.getDouble(i);
		}
		//para_D=new double[dArray.length()];
		//for(int i=0;i<dArray.length();i++) {
			//para_D[i]=dArray.getDouble(i);
		//}
		init_X=new double[xArray.length()];
		for(int i=0;i<xArray.length();i++) {
			init_X[i]=xArray.getDouble(i);
		}
		if(para_D==0) {
			feedThrough=false;
		}
		else {
			feedThrough=true;
		}
			
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
        int i=0;
		for(State xState:xStateList) {
			initCode+=xState.getName()+
					"="+init_X[i]+";\n";
			i++;
		}
		code.addInitCode(initCode);
	}
   public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		int i=0;
		String outputCode=getOutputPortVariable(0)+"=0";
		for(State xState:xStateList) {
			outputCode+="+"+xState.getName()+"*"+para_C[i];
			i++;
		}
		
		if(feedThrough) {
			outputCode+="+"+para_D+"*"+getInputPortVariable(0);
		}
		
		outputCode+=";\n";
		code.addOutputCode(outputCode);	
		
	}
   public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);
		String derivativeCode="";
		for(int i=0;i<xStateList.size();i++) {
			derivativeCode+=xStateList.get(i).getDerivativeName()+"="+para_B[i]+"*"+getInputPortVariable(0);
			for(int j=i*xStateList.size();j<(i+1)*xStateList.size();j++) {
			derivativeCode+="+"+xStateList.get(j%xStateList.size()).getName()+"*("+para_A[j]+")";
			}
			derivativeCode+=";\n";
		}
		code.addDerivativeCode(derivativeCode);
   }
}
