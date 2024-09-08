package com.ncslab.block.math;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

public class dq02abc extends Block{
	String function;
	public dq02abc(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		inputPortList.add(new InputPort(this,1));
		inputPortList.add(new InputPort(this,2));
		inputPortList.add(new InputPort(this,3));
		inputPortList.add(new InputPort(this,4));

		outputPortList.add(new OutputPort(this,1,true));
		outputPortList.add(new OutputPort(this,2,true));
		outputPortList.add(new OutputPort(this,3,true));

		function = paramValues.getString("rotatingFrame");
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block dq02abc:("+getBlockId()+")"+getBlockName()+"*/\n";
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block dq02abc:("+getBlockId()+")"+getBlockName()+"*/\n";

		outputCode+="if(model.majorStep==1){\n";

		String Ud = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		String Uq = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		String U0 = inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		String wt = inputPortList.get(3).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();

		String Ua = outputPortList.get(0).getOutputSignalC().getName();
		String Ub = outputPortList.get(1).getOutputSignalC().getName();
		String Uc = outputPortList.get(2).getOutputSignalC().getName();

		switch (function) {
			case "Aligned with phase A axis":
					outputCode+=Ua+"=(cos("+wt+")*"+Ud+"-sin("+wt+")*"+Uq+"+1*"+U0+");\n";
					outputCode+=Ub+"=(cos("+wt+"-2*M_PI/3.0)*"+Ud+"-sin("+wt+"-2*M_PI/3.0)*"+Uq+"+1*"+U0+");\n";
					outputCode+=Uc+"=(cos("+wt+"+2*M_PI/3.0)*"+Ud+"-sin("+wt+"+2*M_PI/3.0)*"+Uq+"+1*"+U0+");\n";
				break;
			case "90 degrees behind phase A axis":
					outputCode+=Ua+"=(sin("+wt+")*"+Ud+"+cos("+wt+")*"+Uq+"+1*"+U0+");\n";
					outputCode+=Ub+"=(sin("+wt+"-2*M_PI/3.0)*"+Ud+"+cos("+wt+"-2*M_PI/3.0)*"+Uq+"+1*"+U0+");\n";
					outputCode+=Uc+"=(sin("+wt+"+2*M_PI/3.0)*"+Ud+"+cos("+wt+"+2*M_PI/3.0)*"+Uq+"+1*"+U0+");\n";
				break;
			default:
					outputCode+=Ua+"=(cos("+wt+")*"+Ud+"-sin("+wt+")*"+Uq+"+1*"+U0+");\n";
					outputCode+=Ub+"=(cos("+wt+"-2*M_PI/3.0)*"+Ud+"-sin("+wt+"-2*M_PI/3.0)*"+Uq+"+1*"+U0+");\n";
					outputCode+=Uc+"=(cos("+wt+"+2*M_PI/3.0)*"+Ud+"-sin("+wt+"+2*M_PI/3.0)*"+Uq+"+1*"+U0+");\n";
				break;
		}

		outputCode+="}\n";
		code.addOutputCode(outputCode);
	}
     public void updateDimension() throws MatDimException{
	 }

	public void checkDimension() throws MatDimException{
	}
}
