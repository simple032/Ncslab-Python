package block.source;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import code.c.CodeStructC;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class Clock extends block.Block{
	public Clock(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		outputPortList.add(new OutputPort(this,1,false));
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Clock::("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=model.time;\n";
		code.addOutputCode(outputCode);
	}
	  public void updateDimension() throws MatDimException{
		}
	public void checkDimension() throws MatDimException{
	}
}
