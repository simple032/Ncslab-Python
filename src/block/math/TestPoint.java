package block.math;

import org.json.JSONObject;

import block.Block;
import block.data.DataType;
import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import block.io.InputPort;
import block.io.OutputSignal;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class TestPoint extends Block{
	public TestPoint(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		InputPort in;
		OutputPort out;
		
		out = new OutputPort(this,1,true);
		in = new InputPort(this,1);
		
		outputPortList.add(out);
		inputPortList.add(in);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block TestPoint:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		
		outputCode+=out.getOutputSignalC().getName()+"=";
		outputCode+=ops.getOutputSignalC().getName()+";\n";
		
		code.addOutputCode(outputCode);
	}
	
	public void updateDimension() throws MatDimException{
		OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			out.setWidth(signal.getWidth());
			out.setHeight(signal.getHeight());
			out.getOutputSignalC().setHeight(signal.getWidth());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());
	}
}

