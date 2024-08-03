package block.math;

import org.json.JSONObject;

import block.Block;
import block.data.DataType;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import code.c.CodeStructC;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class abc2dq0 extends Block{
	String function;
	public abc2dq0(JSONObject blockJSON,NCSLabModel model) {
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
		String initCode="/*Code for initialization of block abc2dq0:("+getBlockId()+")"+getBlockName()+"*/\n";
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block abc2dq0:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+="if(model.majorStep==1){\n";
		
		String Ua = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		String Ub = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		String Uc = inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		String wt = inputPortList.get(3).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		
		String Ud = outputPortList.get(0).getOutputSignalC().getName();
		String Uq = outputPortList.get(1).getOutputSignalC().getName();
		String U0 = outputPortList.get(2).getOutputSignalC().getName();
		
		switch (function) {
			case "Aligned with phase A axis":
					outputCode+=Ud+"=2/3.0*(cos("+wt+")*"+Ua+"+cos("+wt+"-2*M_PI/3.0)*"+Ub+"+cos("+wt+"+2*M_PI/3.0)*"+Uc+");\n";
					outputCode+=Uq+"=2/3.0*(-sin("+wt+")*"+Ua+"-sin("+wt+"-2*M_PI/3.0)*"+Ub+"-sin("+wt+"+2*M_PI/3.0)*"+Uc+");\n";
					outputCode+=U0+"=2/3.0*(0.5*"+Ua+"+0.5*"+Ub+"+0.5*"+Uc+");\n";
				break;
			case "90 degrees behind phase A axis":
					outputCode+=Ud+"=2/3.0*(sin("+wt+")*"+Ua+"+sin("+wt+"-2*M_PI/3.0)*"+Ub+"+sin("+wt+"+2*M_PI/3.0)*"+Uc+");\n";
					outputCode+=Uq+"=2/3.0*(cos("+wt+")*"+Ua+"+cos("+wt+"-2*M_PI/3.0)*"+Ub+"+cos("+wt+"+2*M_PI/3.0)*"+Uc+");\n";
					outputCode+=U0+"=2/3.0*(0.5*"+Ua+"+0.5*"+Ub+"+0.5*"+Uc+");\n";
				break;
			default:
					outputCode+=Ud+"=2/3.0*(cos("+wt+")*"+Ua+"+cos("+wt+"-2*M_PI/3.0)*"+Ub+"+cos("+wt+"+2*M_PI/3.0)*"+Uc+");\n";
					outputCode+=Uq+"=2/3.0*(-sin("+wt+")*"+Ua+"-sin("+wt+"-2*M_PI/3.0)*"+Ub+"-sin("+wt+"+2*M_PI/3.0)*"+Uc+");\n";
					outputCode+=U0+"=2/3.0*(0.5*"+Ua+"+0.5*"+Ub+"+0.5*"+Uc+");\n";
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
