package com.ncslab.block.discrete;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ncslab.block.io.State;
import com.ncslab.block.math.Matrix;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class DiscreteStateSpace extends DiscreteBlock {
	private String name="Discrete State Space";
	
	private boolean feedThrough=false;	

	private Matrix A;
	private Matrix B;
	private Matrix C;
	private Matrix D;
	private JSONArray initialConditions;
	
	
	private Vector<State> xStateList=new Vector<State>();
	
	public DiscreteStateSpace(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		
		parseVector();
		
		for(int i=0;i<initialConditions.length();i++) {
			State xState=new State(this,i+1,"x"+(i+1));
			xStateList.add(xState);
			stateList.add(xState);		
		}
		
		//һ�����룬һ�����
		//inputPortList.add(new InputPort(this,1,this.B.column));
		//outputPortList.add(new OutputPort(this,1,feedThrough, this.C.row));
	}
	
	private void parseVector() {
		String aStr=paramValues.getString("A");
		String bStr=paramValues.getString("B");
		String cStr=paramValues.getString("C");
		String dStr=paramValues.getString("D");
		String initCond=paramValues.getString("InitialCondition");
		
		this.A = new Matrix(aStr);
		this.B = new Matrix(bStr);
		this.C = new Matrix(cStr);
		this.D = new Matrix(dStr);
		
		String regEx = "[' ']+"; // һ�������ո�  
		Pattern p = Pattern.compile(regEx);  
		Matcher m = p.matcher(initCond);
		this.initialConditions =new JSONArray(m.replaceAll(",").trim());		
		
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		
		String initCode="";
		for(State xState:xStateList) {
			initCode+=xState.getName()+
					"=0;\n";
		}
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
		
		
		for(int i=0; i<xStateList.size(); i++) {
			initCode+=xStateList.elementAt(i).getName()+
					"="+initialConditions.getDouble(i)
					+";\n";
		}	
		
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		//y(k)=Cx(k)+Du(k)
		outputCode+=getOutputPortVariable(0)+"=0";
		for(int i=0; i<xStateList.size(); i++) {
			outputCode+="+"+xStateList.elementAt(i).getName()+"*"+this.C.elements[0][i];
		}
		
		if(feedThrough) {
			outputCode+="+"+D+"*"+getInputPortVariable(0);
		}
		
		outputCode+=";\n";
		
		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
		code.addDerivativeCode(derivativeCode);
	}
	
	public void  generateUpdateCodeC(CodeStructC code) {
		String updateCode="/*Code for Update of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
		updateCode+="real_T Block"+getBlockId()+"_State_temp["+xStateList.size()+"];\n";		
		for(int i=0; i<xStateList.size(); i++) {
			updateCode+=
					"Block"+getBlockId()+"_State_temp["+i+"]"
					+"="+xStateList.elementAt(i).getName()+";\n";
		}
		//x(k+1)=Ax(k)+Bu(k)
//		int len=num.length-1;
		for(int i=0; i<this.A.row; i++) {
			updateCode+=					
					xStateList.elementAt(i).getName()+"=0";
			for(int j=0; j<this.A.column; j++) {
				updateCode+=
						"+"+"Block"+getBlockId()+"_State_temp["+j+"]"+"*"+this.A.elements[i][j];
			}
			for(int j=0; j<this.B.column; j++) {
				updateCode+="+"+getInputPortVariable(j) +"*"+ this.B.elements[i][j];
			}			
			updateCode+=";\n";
			
		}
		code.addUpdateCode(updateCode);	
	}

}
