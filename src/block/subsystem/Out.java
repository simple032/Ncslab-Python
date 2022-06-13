package block.subsystem;
import java.util.Vector;

import org.json.JSONObject;

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
public class Out extends block.Block{
	public Out(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Out:("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+=this.getOutputPortVariable(0)+"="+this.getInputPortVariable(0)+";\n";
		code.addOutputCode(outputCode);
	}
	  public void updateDimension() throws MatDimException{
			OutputPort out  = outputPortList.get(0);
			InputPort in  = inputPortList.get(0);
			OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			out.setHeight(signal.getHeight());
			out.setWidth(signal.getWidth());
			out.getOutputSignalC().setHeight(signal.getHeight());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());
		}
	public void checkDimension() throws MatDimException{
	}
}
