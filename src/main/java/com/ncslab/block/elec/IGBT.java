package com.ncslab.block.elec;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

public class IGBT extends Block {

	private com.ncslab.circuit2.block.multielement.IGBT IGBTE;
	public IGBT(JSONObject blockIn, NCSLabModel model,circuit2.block.multielement.IGBT igbtE) {
		super(blockIn, model);
		// TODO Auto-generated constructor stub
		inputPortList.add(new InputPort(this, 1));
		outputPortList.add(new OutputPort(this,1,true));
		IGBTE = igbtE;
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block IGBT:("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"(0,0)=";
		outputCode+=IGBTE.getVoltageString();
		outputCode+=";\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"(0,1)=";
		outputCode+=IGBTE.getCurrentString();
		outputCode+=";\n";
		code.addOutputCode(outputCode);
	}
	
	public void updateDimension() throws MatDimException{
		OutputPort out = this.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort();
		if(out.getHeight() != 1 || out.getWidth()!=1) {
			MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions doesn't match !\n \n");
			throw(e);
		}
		OutputPort output = this.getOutputPortList().get(0);
		output.setHeight(1);
		output.setWidth(2);
		output.getOutputSignalC().setHeight(1);
		output.getOutputSignalC().setWidth(2);
	}
}
