package block.continuous;

import org.json.JSONObject;

import block.Block;
import block.data.DataType;
import ncslablink.NCSLabModel;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.State;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;

public class Integrator extends Block {
	private State stateIntegral;
	private Parameter initialCondition;
	
	Parameter externalReset;//zhou_20240507 add externalReset
	Parameter conditionSource;
	public Integrator(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		//һ�����룬һ�����
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,false));
		
		stateIntegral=new State(this,1,"integral");
		stateList.add(stateIntegral);
		
		initialCondition=new Parameter(this,1,"InitialCondition",paramValues.getString("IntegratorInitialCondition"));
		parameterList.add(initialCondition);
		
		//zhou_20240514 add externalReset
		externalReset=new Parameter(this,parameterList.size()+1,"externalReset",paramValues.getString("IntegratorExternalReset"));
		conditionSource=new Parameter(this,parameterList.size()+1,"conditionSource",paramValues.getString("InitialConditionSource"));
		parameterList.add(externalReset);
		parameterList.add(conditionSource);
		if(!paramValues.getString("IntegratorExternalReset").equals("none")&&paramValues.getString("InitialConditionSource").equals("External")) {
			inputPortList.add(new InputPort(this,2));
			inputPortList.add(new InputPort(this,3));
		}else if(!paramValues.getString("IntegratorExternalReset").equals("none")&&paramValues.getString("InitialConditionSource").equals("Internal")
				|| paramValues.getString("IntegratorExternalReset").equals("none")&&paramValues.getString("InitialConditionSource").equals("External")) {
			inputPortList.add(new InputPort(this,2));
		}else {}
	}
	
	public void generateInitCodeM(CodeStructM code) {
		String initCode="";
		
		super.generateInitCodeM(code);
		
		initCode+=initialCondition.getName()+"="+paramValues.getDouble("InitialCondition")+";\n"; 
		initCode+=stateIntegral.getName()+"="+initialCondition.getName()+";\n"; 
		
		code.addInitCode(initCode);
	}
	
	public void generateDerivativeCodeM(CodeStructM code) {
		String derivativeCode="";
		
		super.generateDerivativeCodeM(code);
		
		derivativeCode+=stateIntegral.getDerivativeName()+"="
				+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()
				+";\n";
		
		code.addDerivativeCode(derivativeCode);
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		
		String outputCode="";
		
		super.generateOutputCodeM(code);
		
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+stateIntegral.getName()
					+";\n";
		
		code.addOutputCode(outputCode);
	}
	
	
	//define arrays to save data
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block Intergator:("+getBlockId()+")"+getBlockName()+"*/\n";
		 
		 if(!paramValues.getString("IntegratorExternalReset").equals("none")) {
			 arraysCode+="double "+"Block"+getBlockId()+"intergate_resetSignal_data=0;\n";
		 }
		 if(paramValues.getString("InitialConditionSource").equals("External")) {
			 arraysCode+="double "+"Block"+getBlockId()+"intergate_init_flag=0;\n";
		 }
		 code.addArraysCode(arraysCode); 
	 }
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Intergator:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		if(paramValues.getString("InitialConditionSource").equals("External")) {
			if(paramValues.getString("IntegratorExternalReset").equals("none")) {
				initCode+=initialCondition.getName()+"="+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
			}else {
				initCode+=initialCondition.getName()+"="+inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
			}
		}else {
			initCode+=initialCondition.getName()+"="+paramValues.getDouble("IntegratorInitialCondition")+";\n";
		}
		
		initCode+=stateIntegral.getName()+"="+initialCondition.getName()+";\n"; 
		
		code.addInitCode(initCode);
	}
	
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Intergator:("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+="if(mp->majorStep>0) {\n";
		
		

		if(paramValues.getString("IntegratorExternalReset").equals("none")) {
				outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+stateIntegral.getName()
				+";\n";
		}else {
			if(paramValues.getString("IntegratorExternalReset").equals("risingEdge")) {
				outputCode+="if("+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+">=1&&Block"+getBlockId()+"intergate_resetSignal_data<=0){\n"
							+stateIntegral.getName()+"="+initialCondition.getName()+";\n"
							+"}else{\n"
							+outputPortList.get(0).getOutputSignalC().getName()+"="+stateIntegral.getName()+";\n"
							+"}\n";
			}else {
				outputCode+="if("+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"<=0&&Block"+getBlockId()+"intergate_resetSignal_data>=1){\n"
						+stateIntegral.getName()+"="+initialCondition.getName()+";\n"
						+"}else{\n"
						+outputPortList.get(0).getOutputSignalC().getName()+"="+stateIntegral.getName()+";\n"
						+"}\n";
			}
			outputCode+="Block"+getBlockId()+"intergate_resetSignal_data="+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
		}
		
			
//		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+stateIntegral.getName()
//					+";\n";
		outputCode+="}\n";
		code.addOutputCode(outputCode);
	}
	
	public void generateDerivativeCodeC(CodeStructC code) {
		
		String derivativeCode="/*Code for Derivative of block Intergator:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		derivativeCode+=stateIntegral.getDerivativeName()+"="
				+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()
				+";\n";
		
		if(paramValues.getString("InitialConditionSource").equals("External")) {
			derivativeCode+="if(Block"+getBlockId()+"intergate_init_flag==0){\n";
			if(paramValues.getString("IntegratorExternalReset").equals("none")) {
				derivativeCode+=initialCondition.getName()+"="+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
				derivativeCode+=stateIntegral.getName()+"="+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
			}else {
				derivativeCode+=initialCondition.getName()+"="+inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
				derivativeCode+=stateIntegral.getName()+"="+inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
			}
			derivativeCode+="Block"+getBlockId()+"intergate_init_flag=1;}\n";
		}
		
		code.addDerivativeCode(derivativeCode);
	}
}
