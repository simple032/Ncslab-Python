package block.discontinuous;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.Parameter;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class DeadZone extends Block{
	block.io.Parameter lowervalue;
	block.io.Parameter uppervalue;
  public DeadZone(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		lowervalue=new Parameter(this,1,"lowervalue",paramValues.getString("LowerValue"));
		uppervalue=new Parameter(this,1,"uppervalue",paramValues.getString("UpperValue"));
		parameterList.add(lowervalue);
		parameterList.add(uppervalue);
  }
  public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=lowervalue.getName()+"="+paramValues.getDouble("LowerValue")+";\n"; 
		initCode+=uppervalue.getName()+"="+paramValues.getDouble("UpperValue")+";\n"; 
		code.addInitCode(initCode);
	}
  public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		outputCode+="if "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+">"+uppervalue.getName()+"\n";	
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"-"+uppervalue.getName()+";\n";
		outputCode+="elseif "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"<"+lowervalue.getName()+"\n";	
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"-"+lowervalue.getName()+";\n";
		outputCode+="else\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0;\n";
		outputCode+="end\n";
		code.addOutputCode(outputCode);
	}
}
