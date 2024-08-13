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
	private final double pumpK=1;
	private final double pumpT=2;

	private final double waterLevelK=0.1;
	private final double waterLevelT=50;

	State pumpState;
	State levelState;

	public WaterLevel(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//一个输入，两个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"Pump_Speed",1,false));
		outputPortList.add(new OutputPort(this,"Water_Level",2,false));

		pumpState=new State(this,1,"pumpState");
		stateList.add(pumpState);
		levelState=new State(this,2,"levelState");
		stateList.add(levelState);
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
		initCode+=pumpState.getName()+"="+0+";\n"
				+levelState.getName()+"="+0+";\n";

		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block WaterLevel:("+getBlockId()+")"+getBlockName()+"*/\n";

		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+pumpState.getName()+";\n";
		outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"="+levelState.getName()+";\n";

		code.addOutputCode(outputCode);
	}

	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of WaterLevel:("+getBlockId()+")"+getBlockName()+"*/\n";

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
}
