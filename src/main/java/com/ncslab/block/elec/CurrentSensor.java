package com.ncslab.block.elec;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

public class CurrentSensor extends Block{
	private com.ncslab.circuit2.block.element.CurrentSensor currentSensorE;
	public CurrentSensor(JSONObject blockJSON,NCSLabModel model,com.ncslab.circuit2.block.element.CurrentSensor currentSensorE) {
		super(blockJSON,model);
		this.currentSensorE=currentSensorE;
		outputPortList.add(new OutputPort(this,1,true));
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block currentSensor Sensor:("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=";
		outputCode+=currentSensorE.getCurrentString();
		outputCode+=";\n";
		code.addOutputCode(outputCode);
	}
	
	@Override
    public void calculateOutput(double t) {
        Data output = new Data();
        output.setInitValue(currentSensorE.getCurrentValue());
        outputPortList.get(0).getOutputSignalC().setData(output);
    }
}
