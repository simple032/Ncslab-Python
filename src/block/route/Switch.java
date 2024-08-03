package block.route;

import org.json.JSONObject;

import block.data.DataType;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import code.c.CodeStructC;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class Switch extends block.Block{
	block.io.Parameter threshold;
	public Switch(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		inputPortList.add(new InputPort(this,1));
		inputPortList.add(new InputPort(this,2));
		inputPortList.add(new InputPort(this,3));
		outputPortList.add(new OutputPort(this,1,true));
	   this.threshold=new Parameter(this,1,"threshold",paramValues.getString("Threshold"));
		parameterList.add(threshold);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Switch:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=threshold.getInitCodeC();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Switch::("+getBlockId()+")"+getBlockName()+"*/\n";
		OutputPort out  = outputPortList.get(0);
		InputPort in1  = inputPortList.get(0);
		InputPort in2  =  inputPortList.get(1);
		InputPort in3  =  inputPortList.get(2);
		OutputSignal signal1=in1.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		OutputSignal signal2=in2.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		OutputSignal signal3=in3.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		outputCode+="if("+signal2.getName()+">"+threshold.getName()+"){\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+signal1.getName()+";}\n";
		outputCode+="else{\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+signal3.getName()+";}\n";
		code.addOutputCode(outputCode);
	}
	  public void updateDimension() throws MatDimException{
			OutputPort out  = outputPortList.get(0);
			InputPort in1  = inputPortList.get(0);
			InputPort in3  =  inputPortList.get(2);
			OutputSignal signal1=in1.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputSignal signal3=in3.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if(signal1.getHeight()!=signal3.getHeight()||signal1.getWidth()!=signal3.getWidth()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input1 and input3 dimension doesn't match!\n \n");
				throw(e);
				}
			out.setHeight(signal1.getHeight());
			out.setWidth(signal1.getWidth());
			out.getOutputSignalC().setHeight(signal1.getHeight());
			out.getOutputSignalC().setWidth(signal1.getWidth());
			out.getOutputSignalC().setDataType(signal1.getDataType());
			}
	   public void checkDimension() throws MatDimException{
	  }
}
