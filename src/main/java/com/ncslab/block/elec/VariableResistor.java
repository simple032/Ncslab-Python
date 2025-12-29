package block.elec;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;

public class VariableResistor extends Block {
	private circuit2.block.element.VariableResistor variableResistorE;
	public VariableResistor(JSONObject blockJSON,NCSLabModel model,circuit2.block.element.VariableResistor variableResistorE) {
		super(blockJSON,model);
		this.variableResistorE=variableResistorE;
		inputPortList.add(new InputPort(this,1));
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Variable Resisitor:("+getBlockId()+")"+getBlockName()+"*/\n";
		//outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=";
		//outputCode+=voltageSensorE.getVoltageString();
		//outputCode+=";\n";
		code.addOutputCode(outputCode);
	}
}
