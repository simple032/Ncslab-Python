package com.ncslab.block.source;

import org.json.JSONObject;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;

public class Ramp extends Block {
	    Parameter slope;
	    Parameter start;
	    Parameter initial_output;
	public Ramp(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON, model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,false));
		slope=new Parameter(this,1,"slope",paramValues.getString("slope"));
		start=new Parameter(this,2,"start",paramValues.getString("start"));
		initial_output=new Parameter(this,3,"initial_output",paramValues.getString("X0"));
		parameterList.add(slope);
		parameterList.add(start);
		parameterList.add(initial_output);
		outputPortList.get(0).setHeight(slope.getHeight());
		outputPortList.get(0).setWidth(slope.getWidth());
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=slope.getInitCodeM();
		initCode+=start.getInitCodeM();
		initCode+=initial_output.getInitCodeM();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		switch(slope.getDataType()) {
		case REAL:
		outputCode+="if sign(t-"+start.getName()+"+offset)>=0\n";
        outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+initial_output.getName()+"+"+slope.getName()+"*(t-"+start.getName()+"+offset);\n";
        outputCode+="else\n";
        outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+initial_output.getName()+";\n";
        outputCode+="end\n";
        break;
		case MATRIX:
			for(int i=1;i<slope.getHeight()+1;i++) {
				for(int j=1;j<slope.getWidth()+1;j++) {	
					outputCode+="if sign(t-"+start.getName()+"("+i+","+j+")+offset)>=0\n";
			        outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+initial_output.getName()+"("+i+","+j+")+"+slope.getName()+"("+i+","+j+")*(t-"+start.getName()+"("+i+","+j+")+offset);\n";
			        outputCode+="else\n";
			        outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+initial_output.getName()+"("+i+","+j+");\n";
			        outputCode+="end\n";	
				}
			}
			break;
		}
		code.addOutputCode(outputCode);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Ramp:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=slope.getInitCodeC();
		initCode+=start.getInitCodeC();
		initCode+=initial_output.getInitCodeC();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Step:("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+="{real_T currentTime = model.time;\n";
		switch(slope.getDataType()) {
		case REAL:
			outputCode+="if(currentTime<"+start.getName()+"){\n";
			outputCode+=this.getOutputPortVariable(0)+"="+initial_output.getName()+";}\n";
			outputCode+="else{\n";
			outputCode+=this.getOutputPortVariable(0)+"="+initial_output.getName()+"+"+slope.getName()+"*(currentTime-"+start.getName()+");}\n";
			break;
		case MATRIX:	
			for(int i=0;i<slope.getHeight();i++) {
				for(int j=0;j<slope.getWidth();j++) {
			outputCode+="if(currentTime<"+start.getName()+"("+i+","+j+")){\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+initial_output.getName()+"("+i+","+j+");}\n";
			outputCode+="else{\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+initial_output.getName()+"("+i+","+j+")+"+slope.getName()+"("+i+","+j+")*(currentTime-"+start.getName()+"("+i+","+j+"));}\n";
		}
			}
			break;
			}
		outputCode+="}\n";
		code.addOutputCode(outputCode);
	}
	
	 public void updateDimension() throws MatDimException{
	    	if(slope.getWidth()!=initial_output.getWidth()
	    			||slope.getWidth()!=start.getWidth()
	    			||slope.getHeight()!=start.getHeight()
	    			||slope.getHeight()!=initial_output.getHeight()) {
	    		MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
				throw(e);
	    	}
	    }
}
