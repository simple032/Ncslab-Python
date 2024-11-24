package com.ncslab.block.route;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.MatDimException;

import java.util.Vector;

public class Demux extends Block {
	private int num;
	private boolean feedThrough = true;



    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        //输出个数不确定
        inputNames.add("in1");
    }

	public Demux(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);

		this.feedThrough=true;

		this.num = paramValues.getInt("Outputs");

		//一锟斤拷锟斤拷锟诫，一锟斤拷锟斤拷锟�
		for(int i=0; i<num; i++) {
			outputPortList.add(new OutputPort(this,i+1,feedThrough));
		}
		inputPortList.add(new InputPort(this,1));
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		for(int i=0; i<num; i++) {
			initCode+=getOutputPortVariable(0)+"=0;\n";
		}

		code.addInitCode(initCode);
	}



	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);

		//String outputCode=getOutputPortVariable(0)+"=0";


		//outputCode+=";\n";
		String outputCode="";
		for(int i=0; i<num; i++) {
			outputCode+=getOutputPortVariable(i)+"="+getInputPortVariable(0)+"("+(i+1)+");\n";
		}

		code.addOutputCode(outputCode);
	}

	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);

		String derivativeCode="";


		//derivativeCode+=");\n";

		code.addDerivativeCode(derivativeCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		String initCode="/*Code for initialization of block Transfer Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";


		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Transfer Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";

		if(this.getInputPortList().get(0).getHeight()==1) {
			for(int i=0; i<num; i++) {
				outputCode+=getOutputPortVariable(i)+"="
					+getInputPortVariable(0)+"(0,"+i+")"
					+";\n";
			}
		}
		else {
			for(int i=0; i<num; i++) {
				outputCode+=getOutputPortVariable(i)+"="
					+getInputPortVariable(0)+"("+i+",0)"
					+";\n";
			}
		}

		code.addOutputCode(outputCode);
	}

	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of Transfer Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";


		code.addDerivativeCode(derivativeCode);
	}

	public void  generateUpdateCodeC(CodeStructC code) {
		String updateCode="/*Code for Derivative of Transfer Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";

		code.addUpdateCode(updateCode);
	}

	public void updateDimension() throws MatDimException{
		//super.updateDimension();
		/*
		System.out.println("Update dimension:Demux");
		System.out.println("Input width: "+this.getInputPortList().get(0).getWidth());
		System.out.println("Input height: "+this.getInputPortList().get(0).getHeight());
		System.out.println("Is a vector: "+this.getInputPortList().get(0).isVector());*/


	}

	public void checkDimension() throws MatDimException{
		if(this.getInputPortList().get(0).isVector()==false||this.getInputPortList().get(0).isReal()==true) {
			MatDimException e=new MatDimException("Block "+this.blockName+" input dimension error!\n Only a vector is applicable for demux\n");
			throw(e);
		}

		if(this.getInputPortList().get(0).getVectorSize()!=num) {
			MatDimException e=new MatDimException("Block "+this.blockName+" output dimension error!\n The input signal width is "+this.getInputPortList().get(0).getVectorSize()+", but the number of output is "+num+"\n");
			throw(e);
		}
	}
}
