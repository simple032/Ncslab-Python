package block.math;

import block.Block;
import block.data.DataType;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;
import org.json.JSONObject;

public class TestPoint extends Block{
	//Matrix gain;
	public TestPoint(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);
				
		//this.gain = new Matrix(paramValues.getString("Gain"));

		InputPort in;
		OutputPort out;
		
		out = new OutputPort(this,1,true);
		in = new InputPort(this,1);

		//一锟斤拷锟斤拷锟�		
		outputPortList.add(out);
		
		//一锟斤拷锟斤拷锟斤拷
		inputPortList.add(in);
		
//		gain=new Parameter(this,1,"value");
//		parameterList.add(gain);

		
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		String outputCode="";
		//outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"*"+paramValues.getString("Gain")+";\n";
		
		outputCode+=out.getOutputSignalC().getName()+"="
		+ops.getOutputSignalC().getName()+";\n";				
		
		
		code.addOutputCode(outputCode);
	}
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Gain:("+getBlockId()+")"+getBlockName()+"*/\n";
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Constant:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		
		outputCode+=out.getOutputSignalC().getName()+"=";
		outputCode+=ops.getOutputSignalC().getName()+";\n";
		
		code.addOutputCode(outputCode);
	}
	
	public void checkDimension() throws MatDimException{
	}
}
