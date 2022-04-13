package block.continuous;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import block.io.State;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class PIDController extends block.Block{
	
	Parameter cparaP;
	Parameter cparaI;
	Parameter cparaD;
	Parameter cparaN;
	Parameter lowerSaturationLimit=null;
	Parameter upperSaturationLimit=null;
	State stateIntegral;
	State stateFilter;
	public PIDController(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		
		//一个输入，一个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		
		cparaP=new Parameter(this,parameterList.size()+1,"P",paramValues.getString("P"));
		parameterList.add(cparaP);
		cparaI=new Parameter(this,parameterList.size()+1,"I",paramValues.getString("I"));
		parameterList.add(cparaI);
		cparaD=new Parameter(this,parameterList.size()+1,"D",paramValues.getString("D"));
		parameterList.add(cparaD);
		cparaN=new Parameter(this,parameterList.size()+1,"N",paramValues.getString("N"));
		parameterList.add(cparaN);
		
		stateIntegral=new State(this,1,"integral");
		stateList.add(stateIntegral);
		stateFilter=new State(this,2,"filter");
		stateList.add(stateFilter);
		
		if(paramValues.getString("LimitOutput").equals("on")) {
			lowerSaturationLimit=new Parameter(this,parameterList.size()+1,"LowerSaturationLimit",paramValues.getString("LowerSaturationLimit"));
			parameterList.add(lowerSaturationLimit);
			upperSaturationLimit=new Parameter(this,parameterList.size()+1,"UpperSaturationLimit",paramValues.getString("UpperSaturationLimit"));
			parameterList.add(upperSaturationLimit);
		}
	}
	 //define arrays to save data
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block discrete_Delay:("+getBlockId()+")"+getBlockName()+"*/\n";
		 OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		 arraysCode+="double "+"Block"+getBlockId()+"save_data[5];\n";
		 code.addArraysCode(arraysCode);
	 }
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block PID Controller:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		initCode+=cparaP.getInitCodeC();
		initCode+=cparaI.getInitCodeC();
		initCode+=cparaD.getInitCodeC();
		initCode+=cparaN.getInitCodeC();

		if(paramValues.getString("LimitOutput").equals("on")) {
			initCode+=lowerSaturationLimit.getName()+"="+paramValues.getDouble("LowerSaturationLimit")+";\n";
			initCode+=upperSaturationLimit.getName()+"="+paramValues.getDouble("UpperSaturationLimit")+";\n";
		}
		initCode+=stateIntegral.getName()+"=0;\n";
		initCode+=stateFilter.getName()+"=0;\n";
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block PID Controller:("+getBlockId()+")"+getBlockName()+"*/\n";
		 OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		 outputCode+="if(mp->majorStep>0) {\n";
		outputCode+="Block"+getBlockId()+"save_data[0]="+cparaP.getName()+"*"+signal.getName()+";\n";
		outputCode+="Block"+getBlockId()+"save_data[1]="+cparaD.getName()+"*"+signal.getName()+";\n";
		outputCode+="Block"+getBlockId()+"save_data[2]="+cparaI.getName()+"*"+signal.getName()+";\n";
		outputCode+="}\n";
		outputCode+="Block"+getBlockId()+"save_data[3]=(Block"+getBlockId()+"save_data[1]-"+stateFilter.getName()+")*"+cparaN.getName()+";\n";
		outputCode+="Block"+getBlockId()+"save_data[4]="+"Block"+getBlockId()+"save_data[0]"
					+"+"+stateIntegral.getName()
					+"+Block"+getBlockId()+"save_data[3]"
					+";\n";
		if(paramValues.getString("LimitOutput").equals("on")) {
		outputCode+="if(Block"+getBlockId()+"save_data[4]>"+upperSaturationLimit.getName()+"){\n";
		outputCode+=this.getOutputPortVariable(0)+"="+upperSaturationLimit.getName()+";}\n";
		outputCode+="else if(Block"+getBlockId()+"save_data[4]<"+lowerSaturationLimit.getName()+"){\n";
		outputCode+=this.getOutputPortVariable(0)+"="+lowerSaturationLimit.getName()+";}\n";
		outputCode+="else{\n";
		outputCode+=this.getOutputPortVariable(0)+"="+"Block"+getBlockId()+"save_data[4];}\n";
		}else {
			outputCode+=this.getOutputPortVariable(0)+"="+"Block"+getBlockId()+"save_data[4];\n";
		}
		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of PID Controller:("+getBlockId()+")"+getBlockName()+"*/\n";
		derivativeCode+=stateIntegral.getDerivativeName()+"="+"Block"+getBlockId()+"save_data[2];\n";
		derivativeCode+=stateFilter.getDerivativeName()+"="+"Block"+getBlockId()+"save_data[3];\n";
		code.addDerivativeCode(derivativeCode);
	}
}
