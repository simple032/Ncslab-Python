package block.discontinuous;
import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.Parameter;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;
public class Relay extends Block{
	block.io.Parameter onSwitchValue;
	block.io.Parameter offSwitchValue;
	block.io.Parameter onOutputValue;
	block.io.Parameter offOutputValue;
	public Relay(JSONObject blockIn,NCSLabModel model) {
		
		super(blockIn,model);
		//一个输入，一个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		onSwitchValue=new Parameter(this,1,"onSwitchValue");
		offSwitchValue=new Parameter(this,1,"offSwitchValue");
		onOutputValue=new Parameter(this,1,"onOutputValue");
		offOutputValue=new Parameter(this,1,"offOutputValue");
		parameterList.add(onSwitchValue);
		parameterList.add(offSwitchValue);
		parameterList.add(onOutputValue);
		parameterList.add(offOutputValue);
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=onSwitchValue.getName()+"="+paramValues.getDouble("OnSwitchValue")+";\n"; 
		initCode+=offSwitchValue.getName()+"="+paramValues.getDouble("OffSwitchValue")+";\n"; 
		initCode+=onOutputValue.getName()+"="+paramValues.getDouble("OnOutputValue")+";\n"; 
		initCode+=offOutputValue.getName()+"="+paramValues.getDouble("OffOutputValue")+";\n"; 
		initCode+="relay_outdata=0;\n";
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		outputCode+="if "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+">"+onSwitchValue.getName()+"\n";	
		outputCode+="relay_outdata="+onOutputValue.getName()+";\n";
		outputCode+="else\n";
		outputCode+="if "+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"<"+offSwitchValue.getName()+"\n";
		outputCode+="relay_outdata="+offOutputValue.getName()+";\n";
		outputCode+="end\n";
		outputCode+="end\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=relay_outdata;\n";
		code.addOutputCode(outputCode);
	}
}
