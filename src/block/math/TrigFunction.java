package block.math;

import org.json.JSONObject;

import block.Block;
import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import block.io.InputPort;
import ncslablink.NCSLabModel;

public class TrigFunction extends Block{
	public TrigFunction(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		//构建一个输出
		outputPortList.add(new OutputPort(this,1,true));
		//构建一个输入
		inputPortList.add(new InputPort(this,1));	
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode = "x=" + inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
		outputCode+="Block" + this.getBlockId()+"_Output1=sin(x);\n";
		code.addOutputCode(outputCode);
	}	

}
