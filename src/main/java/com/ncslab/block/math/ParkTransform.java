package com.ncslab.block.math;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import Jama.Matrix;

public class ParkTransform extends Block {
	
	private InputPort portAbc=new InputPort(this,1);
	private InputPort portTheta=new InputPort(this,2);
	private OutputPort portDQ0=new OutputPort(this,1,true);
	
	public ParkTransform(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);
		
		inputPortList.add(portAbc);
		inputPortList.add(portTheta);
		outputPortList.add(portDQ0);
		portDQ0.setDimThrough(false);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Park Transform:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		//outputCode+="REAL Block"+this.getBlockId()+"_theta="+this.getInputPortVariable(portTheta)+";\n";
		
		int portTheta=1;
		int portAbc=0;
		
		outputCode+=portDQ0.getOutputSignalC().getName()+"(0,0)=(2.0/3.0)*(cos("+this.getInputPortVariable(portTheta)+")*"+this.getInputPortVariable(portAbc)+"(0,0)+cos("+this.getInputPortVariable(portTheta)+"-2.0*3.1415926/3)*"+this.getInputPortVariable(portAbc)+"(1,0)+cos("+this.getInputPortVariable(portTheta)+"+2.0*3.1415926/3)*"+this.getInputPortVariable(portAbc)+"(2,0));\n";
		outputCode+=portDQ0.getOutputSignalC().getName()+"(1,0)=(2.0/3.0)*(-sin("+this.getInputPortVariable(portTheta)+")*"+this.getInputPortVariable(portAbc)+"(0,0)-sin("+this.getInputPortVariable(portTheta)+"-2.0*3.1415926/3)*"+this.getInputPortVariable(portAbc)+"(1,0)-sin("+this.getInputPortVariable(portTheta)+"+2.0*3.1415926/3)*"+this.getInputPortVariable(portAbc)+"(2,0));\n";
		outputCode+=portDQ0.getOutputSignalC().getName()+"(2,0)=(2.0/3.0)*((1.0/2.0)*"+this.getInputPortVariable(portAbc)+"(0,0)+(1.0/2.0)*"+this.getInputPortVariable(portAbc)+"(1,0)+(1.0/2.0)*"+this.getInputPortVariable(portAbc)+"(2,0));\n";
		
		code.addOutputCode(outputCode);
	}
	
	
	public void updateDimension() throws MatDimException{
		portDQ0.setWidth(1);
		portDQ0.setHeight(3);
		portDQ0.getOutputSignalC().setHeight(3);
		portDQ0.getOutputSignalC().setWidth(1);
		portDQ0.getOutputSignalC().setDataType(com.ncslab.block.data.DataType.MATRIX);
	}
	
	public void checkDimension() throws MatDimException{
	}
	
	@Override
    public void calculateOutput(double t) {
		com.ncslab.block.data.Data abcData = inputPortList.get(0).getData();
        com.ncslab.block.data.Data thetaData = inputPortList.get(1).getData();
        
        com.ncslab.block.data.Data dq0Data=new com.ncslab.block.data.Data(3,1);
        
        double theta=thetaData.getInitValue();
        double[][] abcMatrix=abcData.getDoubleMatrix();
        double a=abcMatrix[0][0];
        double b=abcMatrix[1][0];
        double c=abcMatrix[2][0];
        
     // Calculate d component
        double d = (2.0/3.0) * (a * Math.cos(theta) + 
                               b * Math.cos(theta - 2.0*Math.PI/3.0) + 
                               c * Math.cos(theta + 2.0*Math.PI/3.0));
        
        // Calculate q component  
        double q = (2.0/3.0) * (-a * Math.sin(theta) - 
                               b * Math.sin(theta - 2.0*Math.PI/3.0) - 
                               c * Math.sin(theta + 2.0*Math.PI/3.0));
        
        // Calculate 0 component (zero sequence)
        double zero = (1.0/3.0) * (a + b + c);
        
        Matrix dq0Matrix=dq0Data.getMatrix();
        dq0Matrix.set(0, 0, d);
        dq0Matrix.set(1, 0, q);
        dq0Matrix.set(2, 0, zero);
        
        portDQ0.setData(dq0Data);
        
	}
}
