package com.ncslab.block.elec;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

public class VoltageSensor extends Block {
	private com.ncslab.circuit2.block.element.VoltageSensor voltageSensorE;
	public VoltageSensor(JSONObject blockJSON,NCSLabModel model,com.ncslab.circuit2.block.element.VoltageSensor voltageSensorE) {
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
	
	@Override
    public void calculateOutput(double t) {
        Data output = new Data();
        output.setInitValue(voltageSensorE.getVoltage());
        outputPortList.get(0).getOutputSignalC().setData(output);
    }
}
