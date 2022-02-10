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
		
		
		if(this.getOutputPortList().get(0).getFeedThrough()) {
			outputCode+=this.getOutputPortVariable(0)+"="+C.getName()+"*"+xState.getName()+"+"+D.getName()+"*"+this.getInputPortVariable(0)+";\n";
		}
		else {
			outputCode+=this.getOutputPortVariable(0)+"="+C.getName()+"*"+xState.getName()+";\n";
		}
		
		code.addOutputCode(outputCode);
		
	}
   public void generateDerivativeCodeM(CodeStructM code) {
	    super.generateDerivativeCodeM(code);
		String derivativeCode="";
		
		derivativeCode+=xState.getDerivativeName()+"="+A.getName()+"*"+xState.getName()+"+"+B.getName()+"*"+this.getInputPortVariable(0)+";\n";
		
		code.addDerivativeCode(derivativeCode);
   }
   
   public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block State Space:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		//initCode+="/*******************************/\n";
		initCode+=A.getInitCodeC();
		initCode+=B.getInitCodeC();
		initCode+=C.getInitCodeC();
		initCode+=D.getInitCodeC();
		initCode+=X0.getInitCodeC();
		
		/*
		for(int i=0;i<xState.getHeight();i++) {
			initCode+=xState.getName()+
					"="+X0.getName()+"("+i+",0)"+";\n";
		}
		*/
		
		initCode+=xState.getName()+"="+X0.getName()+";\n";
		//initCode+="/*******************************/\n";
		
		code.addInitCode(initCode);
   }
   
   public void generateOutputCodeC(CodeStructC code) {
	   super.generateOutputCodeC(code);
	   
	   String outputCode="/*Code for output of block State Space:("+getBlockId()+")"+getBlockName()+"*/\n";
	   
	   outputCode+="/*******************************/\n";
	   if(this.feedThrough) {
		   outputCode+=this.getOutputPortVariable(0)+"="+C.getName()+"*"+xState.getName()+"+"+D.getName()+"*"+this.getInputPortVariable(0)+";\n";
	   }
	   else {
		   outputCode+=this.getOutputPortVariable(0)+"="+C.getName()+"*"+xState.getName()+";\n";
	   }
	   outputCode+="/*******************************/\n";
	   
	   code.addOutputCode(outputCode);
   }
   
   public void generateDerivativeCodeC(CodeStructC code) {
	   super.generateDerivativeCodeC(code);
	   
	   String derivativeCode="/*Code for Derivative of block State Space:("+getBlockId()+")"+getBlockName()+"*/\n";
	   
	   derivativeCode+=xState.getDerivativeName()+"="+A.getName()+"*"+xState.getName()+"+"+B.getName()+"*"+this.getInputPortVariable(0)+";\n";
	   
	   code.addDerivativeCode(derivativeCode);
   }
   
   public void updateDimension() throws MatDimException{
	    OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		
		if(this.feedThrough) {
			if(A.getWidth()!=A.getHeight() //A是否是方阵
					||A.getHeight()!=B.getHeight() //A和B是否匹配
					||A.getWidth()!=C.getWidth() //A和CB是否匹配
					||A.getWidth()!=xState.getHeight() //A和状态是否匹配
					||D.getWidth()!=B.getWidth() //B和D是否匹配
					||D.getHeight()!=C.getHeight() //D和C是否匹配
					||B.getWidth()!=in.getHeight() //输入和B是否匹配
					||in.getWidth()!=1 //输入必须是列向量
					) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!");
				throw(e);
				
			}
		}
		else {
			//如果没有D，检查的时候就不用考虑D向量
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
		
		//根据参数，设置输出的宽度
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
