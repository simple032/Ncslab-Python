package block.elec;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;

public class VariableInductor extends Block {
	private circuit2.block.element.VariableInductor variableInductorE;
	public VariableInductor(JSONObject blockJSON,NCSLabModel model,circuit2.block.element.VariableInductor variableInductorE) {
		super(blockJSON,model);
		this.variableInductorE=variableInductorE;
		inputPortList.add(new InputPort(this,1));
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Variable Inductor:("+getBlockId()+")"+getBlockName()+"*/\n";
		//outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=";
		//outputCode+=voltageSensorE.getVoltageString();
		//outputCode+=";\n";
		code.addOutputCode(outputCode);
	}
}
