package block.elec;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;

public class VariableCapacitor extends Block {
	private circuit2.block.element.VariableCapacitor variableVariableE;
	public VariableCapacitor(JSONObject blockJSON,NCSLabModel model,circuit2.block.element.VariableCapacitor variableVariableE) {
		super(blockJSON,model);
		this.variableVariableE=variableVariableE;
		inputPortList.add(new InputPort(this,1));
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Variable Indcutor:("+getBlockId()+")"+getBlockName()+"*/\n";
		//outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=";
		//outputCode+=voltageSensorE.getVoltageString();
		//outputCode+=";\n";
		code.addOutputCode(outputCode);
	}
}
