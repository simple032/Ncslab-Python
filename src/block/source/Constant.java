package block.source;

import block.Block;
import lombok.Getter;
import org.json.JSONObject;

import block.data.DataType;
import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import code.plc.CodeStructPLC;
import ncslablink.NCSLabModel;

import java.util.Vector;

public class Constant extends Block {
	
	block.io.Parameter value;

	@Getter
	public static final Vector<String> parameterNames = new Vector<>();


	static {
		parameterNames.add("Value");
	}

	public Constant(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//һ�����
		value=new Parameter(this,1,getBlockName(),paramValues.getString("Value"));
		parameterList.add(value);
		outputPortList.get(0).setHeight(value.getHeight());
		outputPortList.get(0).setWidth(value.getWidth());
	}
	
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		
		String initCode="";
		initCode+=value.getInitCodeM();
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		switch(value.getDataType()) {
		case REAL:
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+value.getName()+";\n";
			break;
		case MATRIX:
			for(int i=1;i<value.getHeight()+1;i++) {
				for(int j=1;j<value.getWidth()+1;j++) {
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+value.getName()+"("+i+","+j+");\n";
				}
			}
			break;
		}		
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
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+value.getName()+";\n";
			/*
			for(int i=0;i<value.getHeight();i++) {
				for(int j=0;j<value.getWidth();j++) {
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+value.getName()+"["+i+"]["+j+"];\n";
				}
			}*/
			break;
		}
		
		code.addOutputCode(outputCode);
	}

	public void generateInitCodePLC(CodeStructPLC code){
		super.generateInitCodePLC(code);
		
		String initCode="(*Code for initialization of block Contant:("+getBlockId()+")"+getBlockName()+"*)\n";
		//initCode+=value.getName()+"="+paramValues.getDouble("Value")+";\n";
		
		initCode+=value.getInitCodePLC();
		
		code.addInitCode(initCode);
	}

	public void generateUpdateCodePLC(CodeStructC code){

	}

	public void generateOutputCodePLC(CodeStructC code){
		String outputCode="(*Code for output of block Constant:("+getBlockId()+")"+getBlockName()+"*)\n";
		
		switch(value.getDataType()) {
		case REAL:
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+":="+value.getName()+";\n";
			break;
		case MATRIX:
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+":="+value.getName()+";\n";
			/*
			for(int i=0;i<value.getHeight();i++) {
				for(int j=0;j<value.getWidth();j++) {
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+value.getName()+"["+i+"]["+j+"];\n";
				}
			}*/
			break;
		}
		
		code.addOutputCode(outputCode);
	}
}
