package block.source;

import org.json.JSONObject;

import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;
public class Ramp extends block.Block {
	    block.io.Parameter slope;
	    block.io.Parameter start;
	    block.io.Parameter initial_output;
	public Ramp(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON, model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,false));
		slope=new Parameter(this,1,"slope",paramValues.getString("slope"));
		start=new Parameter(this,1,"start",paramValues.getString("start"));
		initial_output=new Parameter(this,1,"initial_output",paramValues.getString("X0"));
		parameterList.add(slope);
		parameterList.add(start);
		parameterList.add(initial_output);
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=slope.getName()+"="+paramValues.getDouble("slope")+";\n";
		initCode+=start.getName()+"="+paramValues.getDouble("start")+";\n";
		initCode+=initial_output.getName()+"="+paramValues.getDouble("X0")+";\n";
		initCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0;\n";
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		outputCode+="if sign(t-"+start.getName()+"+offset)>=0\n";
		//outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+initial_output.getName()+"+"+slope.getName()+"*(t-"+start.getName()+");\n";
        outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+initial_output.getName()+"+"+slope.getName()+"*(t-"+start.getName()+"+offset);\n";
        //����
        outputCode+="else\n";
        //outputCode+="h=0.01;\n";
       // outputCode+="else\n";
        outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+initial_output.getName()+";\n";
        outputCode+="end\n";
		code.addOutputCode(outputCode);
	}
}
