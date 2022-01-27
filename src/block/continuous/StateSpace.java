package block.continuous;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.JSONArray;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import block.Block;
import block.data.DataType;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import block.io.State;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;
public class StateSpace extends Block{
	//private double D=0;
	private boolean feedThrough=false;
    
	private block.io.Parameter A;
	private block.io.Parameter B;
	private block.io.Parameter C;
	private block.io.Parameter D;
	private block.io.Parameter X0;
	
	private State xState;
    
    InputPort input;
    OutputPort output;
    
    private Vector<State> xStateList=new Vector<State>();
    
	public StateSpace(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		
		//parseVector();
		
		A=new Parameter(this,1,"A",paramValues.getString("A"));
		B=new Parameter(this,2,"B",paramValues.getString("B"));
		C=new Parameter(this,3,"C",paramValues.getString("C"));
		D=new Parameter(this,4,"D",paramValues.getString("D"));	
		X0=new Parameter(this,5,"X0",paramValues.getString("X0"));
		
		if(D.isZero()) {
			feedThrough=false;
		}
		else {
			feedThrough=true;
		}
		
		parameterList.add(A);
		parameterList.add(B);
		parameterList.add(C);
		parameterList.add(D);
		parameterList.add(X0);
		
		/*
		for(int i=0;i<A.getHeight();i++) {
			State xState=new State(this,1,"x"+(i+1)+"_");
			xStateList.add(xState);
			stateList.add(xState);	
		}*/
		
		xState=new State(this,1,"x",A.getWidth(),1);
		xStateList.add(xState);
		stateList.add(xState);
			
		
		//һ�����룬һ�����
		input=new InputPort(this,1);
		inputPortList.add(input);
		output=new OutputPort(this,1,feedThrough);
		output.setHeight(C.getHeight());
		outputPortList.add(output);
	}
	
	/*
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
			
	}*/
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		
		initCode+=A.getInitCodeM();
		initCode+=B.getInitCodeM();
		initCode+=C.getInitCodeM();
		initCode+=D.getInitCodeM();
		initCode+=X0.getInitCodeM();
		
		/*
        int i=0;
		for(State xState:xStateList) {
			initCode+=xState.getName()+
					"="+X0.getName()+"("+(i+1)+",1)"+";\n";
			i++;
		}*/
		
		initCode+=xState.getName()+"="+X0.getName()+";\n";
		
		code.addInitCode(initCode);
	}
   public void generateOutputCodeM(CodeStructM code) {
	   
		super.generateOutputCodeM(code);
		String outputCode="";
		
		/*
		for(int i=0;i<C.getHeight();i++) {
			outputCode+=getOutputPortVariable(0)+"=0";
			for(int j=0;j<C.getWidth();j++) {
				State xState=xStateList.get(j);
				outputCode+="+"+xState.getName()+"*"+C.getName()+"("+(i+1)+","+(j+1)+")";
			}
			outputCode+=";\n";
		}*/
		if(this.getOutputPortList().get(0).getFeedThrough()) {
			outputCode+=this.getOutputPortVariable(0)+"="+C.getName()+"*"+xState.getName()+"+"+D.getName()+"*"+this.getInputPortVariable(0)+";\n";
		}
		else {
			outputCode+=this.getOutputPortVariable(0)+"="+C.getName()+"*"+xState.getName()+";\n";
		}
		
		code.addOutputCode(outputCode);

		/*
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
		code.addOutputCode(outputCode);	*/
		
	}
   public void generateDerivativeCodeM(CodeStructM code) {
	    super.generateDerivativeCodeM(code);
		String derivativeCode="";
		
		//derivativeCode+="#######################\n";
		/*
		for(int i=0;i<A.getHeight();i++) {
			derivativeCode+=xStateList.get(i).getDerivativeName()+"=0";
			for(int j=0;j<A.getWidth();j++) {
				State xState=xStateList.get(j);
				derivativeCode+="+"+A.getName()+"("+(i+1)+","+(j+1)+")"+"*"+xState.getName();
			}
			
			for(int j=0;j<B.getWidth();j++) {
				derivativeCode+="+"+A.getName()+"("+(i+1)+","+(j+1)+")"+"*"+this.getInputPortVariable(0)+"("+(j+1)+")";
			}
			derivativeCode+=";\n";
		}*/
		
		//derivativeCode+="#######################\n";
		
		derivativeCode+=xState.getDerivativeName()+"="+A.getName()+"*"+xState.getName()+"+"+B.getName()+"*"+this.getInputPortVariable(0)+";\n";
		
		code.addDerivativeCode(derivativeCode);
		
	   /*
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
		*/
   }
   
   public void updateDimension() throws MatDimException{
	    OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		
		if(this.feedThrough) {
			if(A.getWidth()!=A.getHeight()
					||A.getHeight()!=B.getHeight()
					||A.getWidth()!=C.getWidth()
					||A.getWidth()!=xState.getHeight()
					||D.getWidth()!=B.getWidth()
					||D.getHeight()!=C.getHeight()
					||B.getWidth()!=in.getHeight()
					||in.getWidth()!=1
					) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!");
				throw(e);
				
			}
		}
		else {
			if(A.getWidth()!=A.getHeight()
					||A.getHeight()!=B.getHeight()
					||A.getWidth()!=C.getWidth()
					||A.getWidth()!=xState.getHeight()
					||B.getWidth()!=in.getHeight()
					||in.getWidth()!=1
					) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!");
				throw(e);
				
			}
		}
		
		
		out.setHeight(C.getHeight());
		out.setWidth(1);
		out.getOutputSignalC().setHeight(C.getHeight());
		out.getOutputSignalC().setWidth(1);
		if(D.getHeight()>1) {
			out.getOutputSignalC().setDataType(DataType.MATRIX);
		}
		else {
			out.getOutputSignalC().setDataType(DataType.REAL);
		}
   }
}
