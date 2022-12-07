package block.testrig;

import java.util.Vector;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;

public class Alp extends Block {
	String hardwareDefineName;
	private double num[] = {0.1308,0,0};
	private double den[]= {1,3.091,1.19,0.2};
	private Vector<State> xStateList=new Vector<State>();
	public Alp(JSONObject blockJSON,NCSLabModel model) {
        super(blockJSON,model);
		this.isHardware=true;
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"FanSpeed",1,false));
		outputPortList.add(new OutputPort(this,"Position",2,false));
		switch(model.getModelMode()) {
		case Simulation:
			for(int i=0;i<3;i++) {
				State xState=new State(this,i+1,"x"+(i+1));
				xStateList.add(xState);
				stateList.add(xState);		
			}
			break;
		case Compilation:
			break;
			}
	}
	public String getHardwareDefineCodeC() {
		String hardwareDefineCode="";
		hardwareDefineName="Block"+this.getBlockId()+"_Alp";
		hardwareDefineCode+="ALP "+hardwareDefineName+";\n";
		return hardwareDefineCode;
	}
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block ALP:("+getBlockId()+")"+getBlockName()+"*/\n";
		switch(model.getModelMode()) {
		case Simulation:
			for(State xState:xStateList) {
				initCode+=xState.getName()+
						"=0;\n";
			}
			break;
		case Compilation:
			hardwareDefineName="Block"+this.getBlockId()+"_Alp";
			initCode+="initAlp(&"+hardwareDefineName+");\n";
			break;
		}
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block ALP:("+getBlockId()+")"+getBlockName()+"*/\n";
		switch(model.getModelMode()) {
		case Simulation:
			outputCode+=getOutputPortVariable(0)+"=0";
			int i=num.length-1;
			for(State xState:xStateList) {
				outputCode+="+"+xState.getName()+"*"+num[i];
				i--;
			}
			outputCode+=";\n";
			break;
		case Compilation:
			hardwareDefineName="Block"+this.getBlockId()+"_Alp";
			outputCode+="if(mp->majorStep>0) {\n";
			outputCode+=hardwareDefineName+".alpPWM=2600*"+this.getInputPortVariable(0)+";\n";
			outputCode+=hardwareDefineName+".alpPWM="+hardwareDefineName+".alpPWM>2400.0?2400.0:"+hardwareDefineName+".alpPWM;\n";
			outputCode+=hardwareDefineName+".alpPWM="+hardwareDefineName+".alpPWM<0?0:"+hardwareDefineName+".alpPWM;\n";
			outputCode+=hardwareDefineName+".alpPWM="+hardwareDefineName+".alpPWM*1.0/2600.0;\n";
			outputCode+="outputAlp(&"+hardwareDefineName+");\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=2.0*"+hardwareDefineName+".fanspeed_output;\n";
			outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"=calalpoutput(635-19.0114*"+hardwareDefineName+".position);\n";
			outputCode+="}\n";
			break;
		}
		code.addOutputCode(outputCode);
	}
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of ALP" + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
		switch(model.getModelMode()) {
		case Simulation:
			for(int i=0;i<xStateList.size()-1;i++) {
				derivativeCode+=xStateList.get(i).getDerivativeName()+"="
						+xStateList.get(i+1).getName()
						+";\n";
			}
			derivativeCode+=xStateList.get(xStateList.size()-1).getDerivativeName()+"=("+getInputPortVariable(0);
			int i=den.length-1;
			for(State xState:xStateList) {
				derivativeCode+="-"+xState.getName()+"*"+den[i];
				i--;
			}
			derivativeCode+=");\n";
		     break;
		case Compilation:
			break;
		}
		
		code.addDerivativeCode(derivativeCode);
	}
}
