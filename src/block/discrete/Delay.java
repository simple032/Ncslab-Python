package block.discrete;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import block.math.Matrix;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class Delay extends Block {
	private String name="Delay";
	
	private boolean feedThrough=false;	

	private int delayLength = 0;
	private Matrix initialConditions;
	
	private Vector<Vector<State>> xStatesList=new Vector<Vector<State>>();
	
	public Delay(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		
		parseVector();		
		
		if(initialConditions.isScalar()) {
			
		}else {					
			
			for(int i=0;i<delayLength+1;i++) {
				Vector<State> xStateList = new Vector<State>();
				for(int j=0; j<initialConditions.row; j++) {
					State xState=new State(this,
							i*initialConditions.row+(j+1)
							,"x"+(j+1)+(i+1));
					xStateList.add(xState);
					stateList.add(xState);
				}			
				xStatesList.add(xStateList);	
			}
		}
		
		
		
		//һ�����룬һ�����
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,feedThrough));
	}
	
	private void parseVector() {
		
		delayLength = paramValues.getInt("DelayLength");
		
		String initCond=paramValues.getString("InitialCondition");	
		this.initialConditions = new Matrix(initCond);
			
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		
		String initCode="";
		
		code.addInitCode(initCode);
	}
	
	
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
			
		String outputCode=getOutputPortVariable(0)+"=0";
		
		code.addOutputCode(outputCode);
	}
	
	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);
		
		String derivativeCode="";
		
		derivativeCode+=");\n";
		
		code.addDerivativeCode(derivativeCode);
	}
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		//initialConditions.print();
		Vector<State> xStateList;
		for(int i=0; i<xStatesList.size()-1; i++) {
			xStateList = xStatesList.elementAt(i);
			//System.out.println("i="+i);
			for(int j=0; j<xStateList.size(); j++) {
				//System.out.println("j="+j);
				initCode+=xStateList.elementAt(j).getName()+
						"="+initialConditions.elements[i][j]+";\n";
			}			
		}			
		xStateList = xStatesList.lastElement();			
		for(int i=0; i<xStateList.size(); i++) {			
			initCode+=xStateList.elementAt(i).getName()+"="
					+this.getInputPortVariable(0)+"["+i+"];\n";	
		}							
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		//y(k)=x(k)		
		Vector<State> xStateList = xStatesList.elementAt(0);			
		for(int j=0; j<xStateList.size(); j++) {
			outputCode+=getOutputPortVariable(0)+"["+j+"]="
					+xStateList.elementAt(j).getName()+";\n";
		}												
		

		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
		code.addDerivativeCode(derivativeCode);
	}
	
	public void  generateUpdateCodeC(CodeStructC code) throws MatDimException {
		String updateCode="/*Code for Update of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
		if(this.getInputPortList().elementAt(0).getWidth()*delayLength
				!=initialConditions.length())
			throw new MatDimException("The dimension of input is " 
				+this.getInputPortList().elementAt(0).getWidth()+ "*" + delayLength
				+" while the dimension of the initial conditions is " +initialConditions.length()
				+" in \""+ getBlockName() +"\"\n");
		
		//x(k)=x(k-1)
		Vector<State> xStateList;	
		for(int i=0;i<xStatesList.size()-1;i++) {
			xStateList = xStatesList.elementAt(i);
			Vector<State> oldxStateList = xStatesList.elementAt(i+1);
			for(int j=0; j<xStateList.size(); j++) {
				updateCode+=xStateList.elementAt(j).getName()+"="
					+oldxStateList.elementAt(j).getName()+";\n";
			}			
		}
		xStateList = xStatesList.lastElement();			
		for(int i=0; i<xStateList.size(); i++) {			
			updateCode+=xStateList.elementAt(i).getName()+"="
					+this.getInputPortVariable(0)+"["+i+"];\n";	
		}						
		
		code.addUpdateCode(updateCode);	
	}
	
	public void updateDimension() {
		OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
				
		out.setWidth(in.getWidth());
		out.getOutputSignalC().setWidth(in.getWidth());
		
	}
}
