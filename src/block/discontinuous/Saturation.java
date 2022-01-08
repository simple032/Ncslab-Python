package block.discontinuous;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.Parameter;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class Saturation extends Block{
	block.io.Parameter lowerLimit;
	block.io.Parameter upperLimit;
	public Saturation(JSONObject blockIn,NCSLabModel model) {
	
		super(blockIn,model);
		//һ�����룬һ�����
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		lowerLimit=new Parameter(this,1,"lowerLimit",paramValues.getString("LowerLimit"));
		upperLimit=new Parameter(this,1,"upperLimit",paramValues.getString("UpperLimit"));
		parameterList.add(lowerLimit);
		parameterList.add(upperLimit);
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=lowerLimit.getName()+"="+paramValues.getDouble("LowerLimit")+";\n"; 
		initCode+=upperLimit.getName()+"="+paramValues.getDouble("UpperLimit")+";\n"; 
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		outputCode+="if "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+">"+upperLimit.getName()+"\n";	
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+upperLimit.getName()+";\n";
		outputCode+="elseif "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"<"+lowerLimit.getName()+"\n";	
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+lowerLimit.getName()+";\n";
		outputCode+="else\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
		outputCode+="end\n";
		code.addOutputCode(outputCode);
	}
	
}
