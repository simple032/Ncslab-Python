package block.math;

import org.json.JSONObject;

import block.Block;
import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import block.io.InputPort;
import ncslablink.NCSLabModel;

public class Sign extends Block{
	public Sign (JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		//构建一个输出
		outputPortList.add(new OutputPort(this,1,true));
		//构建一个输入
		inputPortList.add(new InputPort(this,1));	
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		//String outputCode = "Block" + this.getBlockId()+"_Output1=0;\n";
		
		String outputCode="if "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+">0\n";
		outputCode+="Block" + this.getBlockId()+"_Output1=1.0;\n";
		outputCode+="elseif "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"==0\n";
		outputCode+="Block" + this.getBlockId()+"_Output1=0;\n";
		//outputCode+="else "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"<0\n";
		outputCode+="else\n";
		outputCode+="Block" + this.getBlockId()+"_Output1=-1.0;\n";
		outputCode+="end\n";
		code.addOutputCode(outputCode);
	}
}
