package block.elec;

import org.json.JSONObject;

import block.Block;
import block.io.OutputPort;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;

public class VoltageSensor extends Block {
	private circuit2.block.element.VoltageSensor voltageSensorE;
	public VoltageSensor(JSONObject blockJSON,NCSLabModel model,circuit2.block.element.VoltageSensor voltageSensorE) {
		super(blockJSON,model);
		this.voltageSensorE=voltageSensorE;
		outputPortList.add(new OutputPort(this,1,true));
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Voltage Sensor:("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=";
		outputCode+=voltageSensorE.getVoltageString();
		outputCode+=";\n";
		code.addOutputCode(outputCode);
	}
}
