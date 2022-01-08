package block.source;

import org.json.JSONObject;

import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class Step extends block.Block{
    block.io.Parameter time0;
    block.io.Parameter after;
    block.io.Parameter before;
	
	public Step(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON, model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,false));
		time0=new Parameter(this,1,"time",paramValues.getString("Time"));
		after=new Parameter(this,1,"after",paramValues.getString("After"));
		before=new Parameter(this,1,"before",paramValues.getString("Before"));
		parameterList.add(time0);
		parameterList.add(after);
		parameterList.add(before);
	}
	
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=time0.getName()+"="+paramValues.getDouble("Time")+";\n";
		initCode+=after.getName()+"="+paramValues.getDouble("After")+";\n";
		initCode+=before.getName()+"="+paramValues.getDouble("Before")+";\n";
		initCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0;\n";
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		outputCode+="if sign(t-"+time0.getName()+"+offset)>=0\n";
        outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+after.getName()+";\n";
        //����
        outputCode+="else\n";
        //outputCode+="h=0.01;\n";
       // outputCode+="else\n";
        outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+before.getName()+";\n";
        outputCode+="end\n";
		code.addOutputCode(outputCode);
	}

}
