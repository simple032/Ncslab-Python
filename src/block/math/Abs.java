package block.math;

import org.json.JSONObject;

import block.Block;
import block.data.DataType;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import code.c.CodeStructC;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class Abs extends Block{
	public Abs(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Abs:("+getBlockId()+")"+getBlockName()+"*/\n";
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Abs:("+getBlockId()+")"+getBlockName()+"*/\n";
		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=fabs("+signal.getName()+");\n";
		code.addOutputCode(outputCode);
	}
     public void updateDimension() throws MatDimException{
	 }
	 
	public void checkDimension() throws MatDimException{
	}   
}
