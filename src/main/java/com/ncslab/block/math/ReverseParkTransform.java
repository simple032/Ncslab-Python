package com.ncslab.block.math;

import org.json.JSONObject;
import Jama.Matrix;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

public class ReverseParkTransform extends Block {
	
	private InputPort portDQ0=new InputPort(this,1);
	private InputPort portTheta=new InputPort(this,2);
	private OutputPort portAbc=new OutputPort(this,1,true);
	
	public ReverseParkTransform(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);
		
		inputPortList.add(portDQ0);
		inputPortList.add(portTheta);
		outputPortList.add(portAbc);
		portAbc.setDimThrough(false);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Reverse Park Transform:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		//outputCode+="REAL Block"+this.getBlockId()+"_theta="+this.getInputPortVariable(portTheta)+";\n";
		int portTheta=1;
		int portDQ0=0;
		outputCode+=portAbc.getOutputSignalC().getName()+"(0,0)=(cos("+this.getInputPortVariable(portTheta)+")*"+this.getInputPortVariable(portDQ0)+"(0,0)-sin("+this.getInputPortVariable(portTheta)+")*"+this.getInputPortVariable(portDQ0)+"(1,0)+sqrt(1.0/2.0)*"+this.getInputPortVariable(portDQ0)+"(2,0));\n";
		outputCode+=portAbc.getOutputSignalC().getName()+"(1,0)=(cos("+this.getInputPortVariable(portTheta)+"-2.0*3.1415926/3)*"+this.getInputPortVariable(portDQ0)+"(0,0)-sin("+this.getInputPortVariable(portTheta)+"-2.0*3.1415926/3)*"+this.getInputPortVariable(portDQ0)+"(1,0)+sqrt(1.0/2.0)*"+this.getInputPortVariable(portDQ0)+"(2,0));\n";
		outputCode+=portAbc.getOutputSignalC().getName()+"(2,0)=(cos("+this.getInputPortVariable(portTheta)+"+2.0*3.1415926/3)*"+this.getInputPortVariable(portDQ0)+"(0,0)-sin("+this.getInputPortVariable(portTheta)+"+2.0*3.1415926/3)*"+this.getInputPortVariable(portDQ0)+"(1,0)+sqrt(1.0/2.0)*"+this.getInputPortVariable(portDQ0)+"(2,0));\n";
		
		code.addOutputCode(outputCode);
	}
	
	public void updateDimension() throws MatDimException{
		portAbc.setWidth(1);
		portAbc.setHeight(3);
		portAbc.getOutputSignalC().setHeight(3);
		portAbc.getOutputSignalC().setWidth(1);
		portAbc.getOutputSignalC().setDataType(com.ncslab.block.data.DataType.MATRIX);
	}
	
	public void checkDimension() throws MatDimException{
	}
	
	@Override
    public void calculateOutput(double t) {
		com.ncslab.block.data.Data dq0Data = inputPortList.get(0).getData();
        com.ncslab.block.data.Data thetaData = inputPortList.get(1).getData();
        
        com.ncslab.block.data.Data abcData=new com.ncslab.block.data.Data(3,1);
        
        double theta=thetaData.getInitValue();
        double[][] dq0Matrix=dq0Data.getDoubleMatrix();
        double d=dq0Matrix[0][0];
        double q=dq0Matrix[1][0];
        double zero=dq0Matrix[2][0];
        
        // Calculate a component
        double a = d * Math.cos(theta) - q * Math.sin(theta) + zero;
        
        // Calculate b component
        double angle_b = theta - 2.0 * Math.PI / 3.0;
        double b = d * Math.cos(angle_b) - q * Math.sin(angle_b) + zero;
        
        // Calculate c component  
        double angle_c = theta + 2.0 * Math.PI / 3.0;
        double c = d * Math.cos(angle_c) - q * Math.sin(angle_c) + zero;
        
        Matrix abcMatrix=abcData.getMatrix();
        abcMatrix.set(0, 0, a);
        abcMatrix.set(1, 0, b);
        abcMatrix.set(2, 0, c);
        
        portAbc.setData(abcData);
        
	}
}