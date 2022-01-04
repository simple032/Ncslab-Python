package block.math;

import org.json.JSONObject;
import java.util.Vector;

import block.Block;
import block.io.OutputPort;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;
import block.io.InputPort;

public class Add extends Block{
	
	private String seq;
	
	public Add(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);
		
		//构建一个输入
		outputPortList.add(new OutputPort(this,1,true));
		
		paraseParamValues();
	}
	
	//根据输入构建输入个数
	public void paraseParamValues( ) {
		seq = paramValues.getString("Inputs");
		
		for(int i=0;i<seq.length();i++) {
			inputPortList.add(new InputPort(this,i+1));
		}
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="Block" + this.getBlockId() + "_Output1=0";
		
		for(int i=0; i<seq.length(); i++) {
			if(seq.charAt(i)=='+') {
				outputCode+= "+";
			}
			if(seq.charAt(i)=='-') {
				outputCode+= "-";
			}
			outputCode+="Block"+ getInputPortList().get(i).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()
					+"_Output"+getInputPortList().get(i).getLinkedLine().getLinkedOutputPort().getNumber();
			}
           outputCode+=";\n";
		
		  code.addOutputCode(outputCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Add:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0";
		
		for(int i=0;i<seq.length();i++) {
			if(seq.charAt(i)=='+') {
				outputCode+="+";
			}
			if(seq.charAt(i)=='-') {
				outputCode+="-";
			}
			outputCode+=inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		}
		
		outputCode+=";\n";
		
		code.addOutputCode(outputCode);
	}

}
