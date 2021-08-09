package block.math;

import org.json.JSONObject;
import java.util.Vector;

import block.Block;
import block.io.OutputPort;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;
import block.io.InputPort;

public class Sum extends Block {
	
	private String seq; 
	
	public Sum(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//Ò»¸öÊä³ö
		outputPortList.add(new OutputPort(this,1,true));
		
		paraseParamValues();
	}
	
	private void paraseParamValues() {
		seq=paramValues.getString("Inputs");
		
		for(int i=0;i<seq.length();i++) {
			inputPortList.add(new InputPort(this,i+1));
		}
	}
	
	public String generateOutputCodeM() {
		String code="Block"+this.getBlockId()+"_Output1=0";
		
		for(int i=0;i<seq.length();i++) {
			if(seq.charAt(i)=='+') {
				code+="+";
			}
			if(seq.charAt(i)=='-') {
				code+="-";
			}
			code+="Block"+getInputPortList().get(i).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()
					+"_Output"+getInputPortList().get(i).getLinkedLine().getLinkedOutputPort().getNumber();
		}
		
		code+=";\n";
		
		return code;
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Sum:("+getBlockId()+")"+getBlockName()+"*/\n";
		
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
