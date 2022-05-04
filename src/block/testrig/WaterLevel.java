package block.testrig;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class WaterLevel extends Block {
	private double pumpK=1;
	private double pumpT=2;
	
	private double waterLevelK=0.1;
	private double waterLevelT=50;
	
	State pumpState;
	State levelState;
	
	String hardwareDefineName;
	
	public WaterLevel(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		this.isHardware=true;
		
		//һ�����룬�������
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"Pump_Speed",1,false));
		outputPortList.add(new OutputPort(this,"Water_Level",2,false));
		
		switch(model.getModelMode()) {
		case Simulation:
			pumpState=new State(this,1,"pumpState");
			stateList.add(pumpState);
			levelState=new State(this,2,"levelState");
			stateList.add(levelState);
			break;
		case Compilation:
			break;
		}
		
	}
	
	public String getHardwareDefineCodeC() {
		String hardwareDefineCode="";
		hardwareDefineName="Block"+this.getBlockId()+"_WaterLevel";
		hardwareDefineCode+="WATER_LEVEL "+hardwareDefineName+";\n";
		
		return hardwareDefineCode;
	}
	
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=pumpState.getName()+"=0;\n";
		initCode+=levelState.getName()+"=0;\n";
		
		code.addInitCode(initCode);
	}
	
	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);
		
		String derivativeCode="";
		
		derivativeCode+=pumpState.getDerivativeName()+"=("
				+this.getInputPortVariable(0)
				+"*"+pumpK+"-"+pumpState.getName()+")"
				+"*"+(1/pumpT)
				+";\n";
		
		derivativeCode+=levelState.getDerivativeName()+"=("
				+pumpState.getName()+"*"+waterLevelK+"-"+levelState.getName()+")"
				+"*"+(1/waterLevelT)
				+";\n";
		
		code.addDerivativeCode(derivativeCode);
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode=getOutputPortVariable(0)+"="
				+pumpState.getName()
				+";\n";
		
		outputCode+=getOutputPortVariable(1)+"="
				+levelState.getName()
				+ ";\n";
		
		code.addOutputCode(outputCode);
	}
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block WaterLevel:("+getBlockId()+")"+getBlockName()+"*/\n";
		switch(model.getModelMode()) {
		case Simulation:
			initCode+=pumpState.getName()+"="+0+";\n"
					+levelState.getName()+"="+0+";\n"; 
			break;
		case Compilation:
			/*initCode+="pinMode(1, PWM_OUTPUT);\n" + 
					"    pwmSetMode (PWM_MODE_MS) ;	\n" + 
					"    pwmSetClock(3);\n" + 
					"    pwmSetRange(1000);\n";*/
			hardwareDefineName="Block"+this.getBlockId()+"_WaterLevel";
			initCode+="initWaterLevel(&"+hardwareDefineName+");\n";
			break;
		}
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block WaterLevel:("+getBlockId()+")"+getBlockName()+"*/\n";
		switch(model.getModelMode()) {
		case Simulation:
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+pumpState.getName()+";\n";
			outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"="+levelState.getName()+";\n";
			break;
		case Compilation:
			//outputCode+="pwmWrite(1,(1-"+this.getInputPortVariable(0)+")*1000);\n";
			hardwareDefineName="Block"+this.getBlockId()+"_WaterLevel";
			outputCode+="if(mp->majorStep>0) {\n";
			outputCode+=hardwareDefineName+".pumpPWM="+this.getInputPortVariable(0)+";\n";
			outputCode+="outputWaterLevel(&"+hardwareDefineName+");\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+hardwareDefineName+".speed_counter_in;\n";
			outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"="+hardwareDefineName+".level;\n";
			outputCode+="}\n";
			break;
		}
		
		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of WaterLevel:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		switch(model.getModelMode()) {
		case Simulation:
		derivativeCode+=pumpState.getDerivativeName()+"=("
				+this.getInputPortVariable(0)
				+"*"+pumpK+"-"+pumpState.getName()+")"
				+"*"+(1/pumpT)
				+";\n";
		
		derivativeCode+=levelState.getDerivativeName()+"=("
				+pumpState.getName()+"*"+waterLevelK+"-"+levelState.getName()+")"
				+"*"+(1/waterLevelT)
				+";\n";
		case Compilation:
			break;
		}
		code.addDerivativeCode(derivativeCode);
	}
}
