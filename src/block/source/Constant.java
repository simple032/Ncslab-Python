package block.source;

import org.json.JSONObject;

import block.data.DataType;
import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class Constant extends block.Block{
	
	block.io.Parameter value;
	public Constant(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//һ�����
		outputPortList.add(new OutputPort(this,1,false));
		
		//value=new Parameter(this,1,"value");
		value=new Parameter(this,1,"value",paramValues.getString("Value"));
		parameterList.add(value);
		
		outputPortList.get(0).setHeight(value.getHeight());
		outputPortList.get(0).setWidth(value.getWidth());
	}
	
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		
		String initCode="";
		
		//initCode+=value.getName()+"="+paramValues.getDouble("Value")+";\n";
		initCode+=value.getInitCodeM();
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
				
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+value.getName()+";\n";
		
		code.addOutputCode(outputCode);
	}
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Contant:("+getBlockId()+")"+getBlockName()+"*/\n";
		//initCode+=value.getName()+"="+paramValues.getDouble("Value")+";\n";
		
		initCode+=value.getInitCodeC();
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Constant:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		switch(value.getDataType()) {
		case REAL:
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+value.getName()+";\n";
			break;
		case MATRIX:
			for(int i=0;i<value.getHeight();i++) {
				for(int j=0;j<value.getWidth();j++) {
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"["+i+"]["+j+"]="+value.getName()+"["+i+"]["+j+"];\n";
				}
			}
			break;
		}
		
		code.addOutputCode(outputCode);
	}
}
